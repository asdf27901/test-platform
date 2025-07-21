package com.lmj.platformserver.aop;

import cn.hutool.core.util.ArrayUtil;
import com.lmj.platformserver.annotation.RequestCatchLog;
import com.lmj.platformserver.assertion.AssertionResult;
import com.lmj.platformserver.constant.ApiTestcaseRequestType;
import com.lmj.platformserver.constant.RequestResultConstant;
import com.lmj.platformserver.context.ApiRequestLogsContextHolder;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.exception.InterfaceErrorException;
import com.lmj.platformserver.exception.InterfaceTestcaseErrorException;
import com.lmj.platformserver.mapper.ApiRequestLogsMapper;
import com.lmj.platformserver.mapper.UserMapper;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Aspect
@Component
public class RequestLogAspect {

    @Autowired
    private ApiRequestLogsMapper apiRequestLogsMapper;

    @Autowired
    private UserMapper userMapper;

    @Pointcut("@annotation(com.lmj.platformserver.annotation.RequestCatchLog)")
    public void requestLogPointCut() {
        // 切入点
    }

    @Around("requestLogPointCut() && @annotation(anno)")
    public Object doAround(ProceedingJoinPoint joinPoint, RequestCatchLog anno) throws Throwable {
        ApiRequestLogs apiRequestLogs = new ApiRequestLogs();
        apiRequestLogs.setExecutionTime(LocalDateTime.now());
        ApiRequestLogsContextHolder.set(apiRequestLogs);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        ApiTestcaseRequestType requestType = anno.value();

        if (requestType == ApiTestcaseRequestType.SINGLE) {
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            Long testcaseId = findParamValue("testcaseId", args, parameterNames, Long.class);
            apiRequestLogs.setTestcaseId(testcaseId);
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            apiRequestLogs.setErrorMsg(e.getMessage());
            if (e instanceof InterfaceErrorException || e instanceof InterfaceTestcaseErrorException) {
                apiRequestLogs.setInterfaceId(null);
                apiRequestLogs.setTestcaseId(null);
            }
            throw e;
        } finally {
            apiRequestLogs.setRequestType(requestType.getValue());
            apiRequestLogs.setExecuteResult(dealWithSingleRequestLogResult(apiRequestLogs));

            Long userId = UserContextHolder.getUserId();
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

    private Byte dealWithSingleRequestLogResult(ApiRequestLogs logs) {
        if (logs.getRequestData() == null || logs.getResponseData() == null) {
            return RequestResultConstant.ERROR;
        }
        List<ScriptExecutionResultVo> preScriptData = logs.getPreScriptData();
        if (preScriptData.size() != 0) {
            ScriptExecutionResultVo preExecutionResult = preScriptData.get(0);
            if (preExecutionResult.getError() != null) {
                return RequestResultConstant.FAIL;
            }
            if (preExecutionResult.getResults() != null) {
                for (AssertionResult result : preExecutionResult.getResults()) {
                    if (!result.getResult()) {
                        return RequestResultConstant.FAIL;
                    }
                }
            }
        }
        List<ScriptExecutionResultVo> postScriptData = logs.getPostScriptData();
        if (postScriptData.size() != 0) {
            ScriptExecutionResultVo postExecutionResult = postScriptData.get(0);
            if (postExecutionResult.getError() != null) {
                return RequestResultConstant.FAIL;
            }
            if (postExecutionResult.getResults() != null) {
                for (AssertionResult result : postExecutionResult.getResults()) {
                    if (!result.getResult()) {
                        return RequestResultConstant.FAIL;
                    }
                }
            }
        }
        if (!logs.getResponseData().get(0).get("statusCode").equals(200)) {
            return RequestResultConstant.FAIL;
        }
        return RequestResultConstant.SUCCESS;
    }
}
