package com.lmj.platformserver.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.lmj.platformserver.assertion.PostAssertionTool;
import com.lmj.platformserver.assertion.PreAssertionTool;
import com.lmj.platformserver.constant.RequestResultConstant;
import com.lmj.platformserver.context.ApiRequestLogsContextHolder;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.*;
import com.lmj.platformserver.pojo.RequestSteps;
import com.lmj.platformserver.utils.HttpUtil;
import com.lmj.platformserver.utils.VariableResolver;
import com.lmj.platformserver.vo.RequestResultVo;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class RunInterfaceTestcaseService {

    @Autowired
    private JsScriptExecutionService jsScriptExecutionService;
    @Autowired
    private EnvironmentVariableService environmentVariableService;
    @Autowired
    private HttpUtil httpUtil;

    public RequestResultVo runSingleRequest(
            String path,
            EnvironmentVariable environmentVariable,
            Map<String, Object> requestData
    ) {
        ApiRequestLogs apiRequestLogs = ApiRequestLogsContextHolder.get();
        if (apiRequestLogs == null) {
            apiRequestLogs = new ApiRequestLogs();
        }
        return (RequestResultVo) runRequest(path, environmentVariable, requestData, apiRequestLogs, false);
    }

    @SuppressWarnings("unchecked")
    private Object runRequest(
            String path,
            EnvironmentVariable environmentVariable,
            Map<String, Object> requestData,
            ApiRequestLogs apiRequestLogs,
            boolean isChain
    ) {
        RequestSteps step = new RequestSteps();
        apiRequestLogs.getSteps().add(step);
        step.setInterfaceId((Long) requestData.get("interfaceId"));
        step.setTestcaseId((Long) requestData.get("id"));

        Map<String, Object> requestDataMap = httpUtil.processRequestDataForHttp(requestData);
        ScriptExecutionResultVo preScriptResult = executeScript(requestDataMap, null, environmentVariable, (String) requestData.get("preRequestScript"), true);

        step.setPreScriptData(preScriptResult);
        if (environmentVariable != null) {
            requestDataMap = (Map<String, Object>) VariableResolver.resolve(environmentVariable.getVariables(), requestDataMap);
        }
        HttpRequest httpRequest = httpUtil.createHttpRequest(requestDataMap, path);
        step.setRequestData(getRequestData(path, httpRequest, requestData));

        Map<String, Object> responseDataMap = sendRequest(httpRequest);
        step.setResponseData(responseDataMap);

        ScriptExecutionResultVo postScriptResult = executeScript(null, responseDataMap, environmentVariable, (String) requestData.get("postRequestScript"), false);
        step.setPostScriptData(postScriptResult);

        if (!isChain) {
            apiRequestLogs.setInterfaceId((Long) requestData.get("interfaceId"));
            RequestResultVo resultVo = new RequestResultVo();
            resultVo.setPreExecutionResult(preScriptResult);
            resultVo.setResponse(responseDataMap);
            resultVo.setPostExecutionResult(postScriptResult);

            if (environmentVariable != null) {
                environmentVariableService.updateEnvironmentVariableAsync(environmentVariable, UserContextHolder.getUserId());
            }
            return resultVo;
        }
        return null;
    }

    public void runChainRequest(
            List<InterfaceTestcase> interfaceTestcaseList,
            Map<Long, String> interfaceIdPath,
            EnvironmentVariable environmentVariable,
            Long userId
    ) {
        ApiRequestLogs apiRequestLogs = ApiRequestLogsContextHolder.get();
        if (apiRequestLogs == null) {
            apiRequestLogs = new ApiRequestLogs();
        }
        runChainRequest(interfaceTestcaseList, interfaceIdPath, environmentVariable, apiRequestLogs, userId);
    }

    private void runChainRequest(
            List<InterfaceTestcase> interfaceTestcaseList,
            Map<Long, String> interfaceIdPath,
            EnvironmentVariable environmentVariable,
            ApiRequestLogs apiRequestLogs,
            Long userId
    ) {
        Map<Long, EnvironmentVariable> bindingEnvironment = (environmentVariable == null)
                ? environmentVariableService.getTestcaseBindingEnvironment(interfaceTestcaseList, userId)
                : null;
        for (InterfaceTestcase testcase : interfaceTestcaseList) {
            try {
                EnvironmentVariable currentEnv;
                if (environmentVariable != null) {
                    // 最高优先级：使用方法传入的全局环境
                    currentEnv = environmentVariable;
                } else if (bindingEnvironment != null) {
                    // 次高优先级：使用用例自身绑定的环境
                    currentEnv = bindingEnvironment.get(testcase.getId()); // get() 返回 null 也没关系
                } else {
                    // 最低优先级：没有环境
                    currentEnv = null;
                }
                runRequest(
                        interfaceIdPath.get(testcase.getInterfaceId()),
                        currentEnv,
                        BeanUtil.beanToMap(testcase),
                        apiRequestLogs,
                        true
                );
            } catch (Exception e) {
                int size = apiRequestLogs.getSteps().size();
                if (size > 0) {
                    apiRequestLogs.getSteps().get(size - 1).setErrorMsg(e.getMessage());
                    apiRequestLogs.getSteps().get(size - 1).setExecuteResult(RequestResultConstant.ERROR);
                }
            }
        }
    }

    private ScriptExecutionResultVo executeScript(
            Map<String, Object> requestData,
            Map<String, Object> response,
            EnvironmentVariable environmentVariable,
            String script,
            boolean isPreScript
    ) {
        if (script == null || script.equals("")) {
            return null;
        }

        if (isPreScript) {
            return jsScriptExecutionService.executeJsScript(
                    script,
                    new PreAssertionTool(requestData, environmentVariable == null ? null : environmentVariable.getVariables())
            );
        }

        return jsScriptExecutionService.executeJsScript(
                script,
                new PostAssertionTool(response, environmentVariable == null ? null : environmentVariable.getVariables())
        );
    }

    private Map<String, Object> sendRequest(HttpRequest httpRequest) {
        long start = System.currentTimeMillis();
        @Cleanup HttpResponse httpResponse = httpUtil.sendCustomizeHttpRequest(httpRequest);
        long end = System.currentTimeMillis();
        return httpUtil.getHttpResponseDataMap(httpResponse, end - start);
    }

    private Map<String, Object> getRequestData(String path, HttpRequest httpRequest, Map<String, Object> requestData) {
        Map<String, Object> requestDataMap = httpUtil.getHttpRequestDataMap(httpRequest);
        if (requestDataMap.get("host") == null) {
            requestDataMap.put("host", requestData.get("host"));
            requestDataMap.put("path", StringUtils.hasText((String) requestDataMap.get("queryParam")) ? requestData.get("host") + path + requestDataMap.get("queryParam") : requestData.get("host") + path);
        }
        requestDataMap.put("preScript", requestData.get("preRequestScript"));
        requestDataMap.put("postScript", requestData.get("postRequestScript"));

        return requestDataMap;
    }
}
