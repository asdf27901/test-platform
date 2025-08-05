package com.lmj.platformserver.aop;

import cn.hutool.core.util.ArrayUtil;
import com.lmj.platformserver.assertion.AssertionResult;
import com.lmj.platformserver.constant.ApiTestcaseRequestType;
import com.lmj.platformserver.constant.MessageTypeConstant;
import com.lmj.platformserver.constant.RequestResultConstant;
import com.lmj.platformserver.context.ApiRequestLogsContextHolder;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.exception.*;
import com.lmj.platformserver.mapper.ApiRequestLogsMapper;
import com.lmj.platformserver.mapper.UserMapper;
import com.lmj.platformserver.pojo.RequestSteps;
import com.lmj.platformserver.service.MessageService;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import io.lettuce.core.RedisException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class RequestLogAspect {

    @Autowired
    private ApiRequestLogsMapper apiRequestLogsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private MessageService messageService;

    private static final String LOCK_PREFIX = "lock:chain_request:";
    private static final String REDIS_COUNT_KEY = "api_chain_request_count:";
    private static final Long EXPIRE_TIME = 3600L;

    private static final String INCR_AND_EXPIRE_NO_RETURN_SCRIPT =
            "local key = KEYS[1]\n" +
                    "local ttl = ARGV[1]\n" +
                    "redis.call('INCR', key)\n" +
                    "redis.call('EXPIRE', key, ttl)";

    private static final String DECR_OR_DELETE_SCRIPT =
            "if redis.call('EXISTS', KEYS[1]) == 1 then\n" +
                    "  local new_value = redis.call('DECR', KEYS[1])\n" +
                    "  if new_value <= 0 then\n" +
                    "    redis.call('DEL', KEYS[1])\n" +
                    "  end\n" +
                    "end";

    @Pointcut("@annotation(com.lmj.platformserver.annotation.SingleRequestCatchLog)")
    public void singleRequestLogPointCut() {
        // 切入点
    }

    @Pointcut("@annotation(com.lmj.platformserver.annotation.ChainRequestCatchLog)")
    public void chainRequestLogPointCut() {

    }

    @Around("singleRequestLogPointCut()")
    public Object doSingleRequestAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ApiRequestLogs apiRequestLogs = new ApiRequestLogs();
        apiRequestLogs.setExecutionTime(LocalDateTime.now());
        ApiRequestLogsContextHolder.set(apiRequestLogs);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        Long testcaseId = findParamValue("testcaseId", args, parameterNames, Long.class);
        apiRequestLogs.setTestcaseId(testcaseId);
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error("出现异常：", e);
            if (e instanceof InterfaceErrorException || e instanceof InterfaceTestcaseErrorException) {
                apiRequestLogs.setInterfaceId(null);
                apiRequestLogs.setTestcaseId(null);
            }
            apiRequestLogs.setErrorMsg(e.getMessage());
            apiRequestLogs.setExecuteResult(RequestResultConstant.ERROR);
            throw e;
        } finally {
            int stepSize = apiRequestLogs.getSteps().size();

            if (stepSize > 0) {
                RequestSteps step = apiRequestLogs.getSteps().get(0);
                Byte dealt = dealWithStep(step);
                apiRequestLogs.setExecuteResult(dealt);
                step.setErrorMsg(apiRequestLogs.getErrorMsg());
            }

            apiRequestLogs.setRequestType(ApiTestcaseRequestType.SINGLE.getValue());
            Long userId = UserContextHolder.getUserId();
            User user = userMapper.selectById(userId);
            apiRequestLogs.setExecutorId(userId);
            apiRequestLogs.setExecutorName(user.getNickName());

            apiRequestLogsMapper.insert(apiRequestLogs);
            ApiRequestLogsContextHolder.remove();
        }
    }

    @Around("chainRequestLogPointCut()")
    public Object doChainRequestAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ApiRequestLogs apiRequestLogs = new ApiRequestLogs();
        apiRequestLogs.setExecutionTime(LocalDateTime.now());
        ApiRequestLogsContextHolder.set(apiRequestLogs);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        Long chainId = findParamValue("chainId", args, parameterNames, Long.class);
        Long userId = findParamValue("userId", args, parameterNames, Long.class);
        apiRequestLogs.setChainId(chainId);
        try {
            setChainRequestCount(chainId, EXPIRE_TIME);
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error("出现异常：", e);
            apiRequestLogs.setErrorMsg(e.getMessage());
            if (e instanceof ChainRequestErrorException) {
                apiRequestLogs.setChainId(null);
            } else if (
                    e instanceof RedisException
                            || e instanceof org.redisson.client.RedisException
                            || e instanceof RedisSystemException
            ) {
                apiRequestLogs.setErrorMsg("redis服务异常");
                throw new BaseException("系统服务异常");
            } else if (e instanceof GetLockTimeout) {
                throw e;
            }
            apiRequestLogs.setExecuteResult(RequestResultConstant.ERROR);
            return null;
        } finally {
            rollbackChainRequestCount(chainId);

            int stepSize = apiRequestLogs.getSteps().size();
            if (stepSize > 0) {
                apiRequestLogs.getSteps().forEach(this::dealWithStep);
                long count = apiRequestLogs.getSteps().stream()
                        .filter(step -> step.getExecuteResult() != RequestResultConstant.SUCCESS).count();
                if (count > 0) {
                    apiRequestLogs.setExecuteResult(RequestResultConstant.FAIL);
                } else {
                    apiRequestLogs.setExecuteResult(RequestResultConstant.SUCCESS);
                }
            }

            apiRequestLogs.setRequestType(ApiTestcaseRequestType.CHAIN.getValue());
            User user = userMapper.selectById(userId);
            apiRequestLogs.setExecutorId(userId);
            apiRequestLogs.setExecutorName(user.getNickName());
            apiRequestLogsMapper.insert(apiRequestLogs);

            if (apiRequestLogs.getChainId() == null) {
                messageService.sendChainRequestExecuteNotify(
                        "您有一个请求出错了！",
                        userId,
                        MessageTypeConstant.ERROR,
                        "/interfaces-test/apiRequestLog"
                );
            } else {
                messageService.sendChainRequestExecuteNotify(
                        "链路请求ID：" + chainId + "已执行完成",
                        userId,
                        MessageTypeConstant.SUCCESS,
                        "/interfaces-test/apiRequestLog/apiRequestLogDetail?id=" + apiRequestLogs.getId()
                );
            }
            ApiRequestLogsContextHolder.remove();
        }
    }

    private <T> T findParamValue(String targetName, Object[] args, String[] parameterNames, Class<T> clazz) {
        if (!StringUtils.hasText(targetName) || ArrayUtil.isEmpty(args) || ArrayUtil.isEmpty(parameterNames)) {
            return null;
        }
        for (int i = 0; i < parameterNames.length; i++) {
            if (targetName.equals(parameterNames[i])) {
                if (args[i] != null && clazz.isAssignableFrom(args[i].getClass())) {
                    return clazz.cast(args[i]);
                }
            }
        }
        return null;
    }

    private Byte dealWithStep(RequestSteps step) {
        if (step.getRequestData() == null || step.getResponseData() == null) {
            step.setExecuteResult(RequestResultConstant.ERROR);
            return RequestResultConstant.ERROR;
        }

        ScriptExecutionResultVo preScriptData = step.getPreScriptData();
        if (preScriptData != null && preScriptData.getError() != null) {
            step.setExecuteResult(RequestResultConstant.FAIL);
            return RequestResultConstant.FAIL;
        }
        if (preScriptData != null && preScriptData.getResults() != null) {
            for (AssertionResult result : preScriptData.getResults()) {
                if (!result.getResult()) {
                    step.setExecuteResult(RequestResultConstant.FAIL);
                    return RequestResultConstant.FAIL;
                }
            }
        }

        ScriptExecutionResultVo postScriptData = step.getPostScriptData();
        if (postScriptData != null && postScriptData.getError() != null) {
            step.setExecuteResult(RequestResultConstant.FAIL);
            return RequestResultConstant.FAIL;
        }
        if (postScriptData != null && postScriptData.getResults() != null) {
            for (AssertionResult result : postScriptData.getResults()) {
                if (!result.getResult()) {
                    step.setExecuteResult(RequestResultConstant.FAIL);
                    return RequestResultConstant.FAIL;
                }
            }
        }
        Integer statusCode = (Integer) step.getResponseData().get("statusCode");
        if (statusCode >= 400) {
            step.setExecuteResult(RequestResultConstant.FAIL);
            return RequestResultConstant.FAIL;
        }
        step.setExecuteResult(RequestResultConstant.SUCCESS);
        return RequestResultConstant.SUCCESS;
    }

    @SneakyThrows
    private void setChainRequestCount(Long chainId, Long expireTime) {
        if (chainId == null) {
            return;
        }
        RLock lock = redissonClient.getLock(LOCK_PREFIX + chainId);
        if (!lock.tryLock(10, TimeUnit.SECONDS)) {
            // 在10秒内没有获取到锁，说明有其他操作（很可能是删除）正在进行
            throw new ChainRequestErrorException("系统繁忙，请稍后重试。");
        }
        try {
            DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>(INCR_AND_EXPIRE_NO_RETURN_SCRIPT, Void.class);
            redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(REDIS_COUNT_KEY + chainId), // KEYS[1]
                    expireTime                    // ARGV[1]
            );
        } finally {
            lock.unlock();
        }

    }

    private void rollbackChainRequestCount(Long chainId) {
        if (chainId == null) {
            return;
        }
        DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>(DECR_OR_DELETE_SCRIPT, Void.class);
        redisTemplate.execute(
                redisScript,
                Collections.singletonList(REDIS_COUNT_KEY + chainId)
        );
    }
}
