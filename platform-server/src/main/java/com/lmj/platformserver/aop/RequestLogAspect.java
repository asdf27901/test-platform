package com.lmj.platformserver.aop;

import cn.hutool.core.util.ArrayUtil;
import com.lmj.platformserver.assertion.AssertionResult;
import com.lmj.platformserver.constant.ApiTestcaseRequestType;
import com.lmj.platformserver.constant.RequestResultConstant;
import com.lmj.platformserver.context.ApiRequestLogsContextHolder;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.exception.ChainRequestErrorException;
import com.lmj.platformserver.exception.InterfaceErrorException;
import com.lmj.platformserver.exception.InterfaceTestcaseErrorException;
import com.lmj.platformserver.mapper.ApiRequestLogsMapper;
import com.lmj.platformserver.mapper.UserMapper;
import com.lmj.platformserver.pojo.RequestSteps;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Aspect
@Component
public class RequestLogAspect {

    @Autowired
    private ApiRequestLogsMapper apiRequestLogsMapper;

    @Autowired
    private UserMapper userMapper;

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
    public Object doChainRequestAround(ProceedingJoinPoint joinPoint) {
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
            return joinPoint.proceed();
        } catch (Throwable e) {
            if (e instanceof ChainRequestErrorException) {
                apiRequestLogs.setChainId(null);
            }
            apiRequestLogs.setErrorMsg(e.getMessage());
            apiRequestLogs.setExecuteResult(RequestResultConstant.ERROR);
            return null;
        } finally {
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
}
