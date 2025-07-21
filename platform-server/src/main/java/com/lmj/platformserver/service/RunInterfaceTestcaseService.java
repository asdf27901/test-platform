package com.lmj.platformserver.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.lmj.platformserver.assertion.PostAssertionTool;
import com.lmj.platformserver.assertion.PreAssertionTool;
import com.lmj.platformserver.context.ApiRequestLogsContextHolder;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.entity.EnvironmentVariable;
import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.mapper.EnvironmentVariableMapper;
import com.lmj.platformserver.utils.HttpUtil;
import com.lmj.platformserver.utils.VariableResolver;
import com.lmj.platformserver.vo.RequestResultVo;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RunInterfaceTestcaseService {

    @Autowired
    private JsScriptExecutionService jsScriptExecutionService;
    @Autowired
    private EnvironmentVariableMapper environmentVariableMapper;
    @Autowired
    private HttpUtil httpUtil;

    public RequestResultVo runSingleRequest(
            Interface api,
            EnvironmentVariable environmentVariable,
            Map<String, Object> requestData
    ) {
        ApiRequestLogs apiRequestLogs = ApiRequestLogsContextHolder.get();
        if (apiRequestLogs == null) {
            apiRequestLogs = new ApiRequestLogs();
        }
        return runSingleRequest(api, environmentVariable, requestData, apiRequestLogs);
    }

    @SuppressWarnings("unchecked")
    private RequestResultVo runSingleRequest(
            Interface api,
            EnvironmentVariable environmentVariable,
            Map<String, Object> requestData,
            ApiRequestLogs apiRequestLogs
    ) {
        RequestResultVo resultVo = new RequestResultVo();
        apiRequestLogs.setInterfaceId(api.getId());

        Map<String, Object> requestDataMapper = httpUtil.processRequestDataForHttp(requestData);
        String preRequestScript = (String) requestData.get("preRequestScript");
        ScriptExecutionResultVo preScriptResult = executeScript(requestDataMapper, null, environmentVariable, preRequestScript, true);
        resultVo.setPreExecutionResult(preScriptResult);
        if (preScriptResult != null) {
            apiRequestLogs.setPreScriptData(new ArrayList<>(List.of(preScriptResult)));
        } else {
            apiRequestLogs.setPreScriptData(new ArrayList<>());
        }
        if (environmentVariable != null) {
            requestDataMapper = (Map<String, Object>) VariableResolver.resolve(environmentVariable.getVariables(), requestDataMapper);
        }
        apiRequestLogs.setRequestData(new ArrayList<>(List.of(requestDataMapper)));
        HttpRequest httpRequest = httpUtil.createHttpRequest(requestDataMapper, api.getPath());

        long start = System.currentTimeMillis();
        @Cleanup HttpResponse httpResponse = sendRequest(httpRequest);
        long end = System.currentTimeMillis();
        Map<String, Object> responseDataMap = httpUtil.getHttpResponseDataMap(httpResponse, end - start);
        apiRequestLogs.setResponseData(new ArrayList<>(List.of(responseDataMap)));
        resultVo.setResponse(responseDataMap);

        String postRequestScript = (String) requestData.get("postRequestScript");
        ScriptExecutionResultVo postScriptResult = executeScript(null, responseDataMap, environmentVariable, postRequestScript, false);
        resultVo.setPostExecutionResult(postScriptResult);
        if (postScriptResult != null) {
            apiRequestLogs.setPostScriptData(new ArrayList<>(List.of(postScriptResult)));
        } else {
            apiRequestLogs.setPostScriptData(new ArrayList<>());
        }


        updateEnvironment(environmentVariable);
        return resultVo;
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

    private HttpResponse sendRequest(HttpRequest httpRequest) {
        return httpUtil.sendCustomizeHttpRequest(httpRequest);
    }

    private void updateEnvironment(EnvironmentVariable environmentVariable) {
        // 更新环境变量
        if (environmentVariable != null) {
            environmentVariableMapper.updateById(environmentVariable);
        }
    }
}
