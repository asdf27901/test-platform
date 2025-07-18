package com.lmj.platformserver.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.assertion.PostAssertionTool;
import com.lmj.platformserver.assertion.PreAssertionTool;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.EnvironmentVariable;
import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.entity.TestcaseEnvironment;
import com.lmj.platformserver.exception.EnvironmentVariableErrorException;
import com.lmj.platformserver.exception.InterfaceErrorException;
import com.lmj.platformserver.exception.InterfaceTestcaseErrorException;
import com.lmj.platformserver.mapper.EnvironmentVariableMapper;
import com.lmj.platformserver.mapper.InterfaceMapper;
import com.lmj.platformserver.mapper.InterfaceTestcaseMapper;
import com.lmj.platformserver.mapper.TestcaseEnvironmentMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.InterfaceTestcaseService;
import com.lmj.platformserver.service.JsScriptExecutionService;
import com.lmj.platformserver.service.TestcaseEnvironmentService;
import com.lmj.platformserver.utils.HttpUtil;
import com.lmj.platformserver.utils.VariableResolver;
import com.lmj.platformserver.vo.InterfaceTestcasePageVo;
import com.lmj.platformserver.vo.InterfaceTestcaseVo;
import com.lmj.platformserver.vo.RequestResultVo;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterfaceTestcaseServiceImpl implements InterfaceTestcaseService {

    @Autowired
    private InterfaceTestcaseMapper interfaceTestcaseMapper;

    @Autowired
    private InterfaceMapper interfaceMapper;

    @Autowired
    private HttpUtil httpUtil;

    @Autowired
    private JsScriptExecutionService jsScriptExecutionService;

    @Autowired
    private EnvironmentVariableMapper environmentVariableMapper;

    @Autowired
    private TestcaseEnvironmentMapper testcaseEnvironmentMapper;

    @Autowired
    private TestcaseEnvironmentService testcaseEnvironmentService;

    @Override
    @Transactional
    public void save(List<InterfaceTestcase> interfaceTestcases, Long envId) {
        Set<Long> interfaceIds = interfaceTestcases.stream().map(InterfaceTestcase::getInterfaceId).collect(Collectors.toSet());
        Long count = interfaceMapper.selectCount(
                new LambdaQueryWrapper<Interface>()
                        .in(Interface::getId, interfaceIds)
        );
        if (count != interfaceIds.size()) {
            throw new InterfaceErrorException(ResultCodeEnum.INTERFACE_ID_NOT_FOUND);
        }

        interfaceTestcaseMapper.insertOrUpdate(interfaceTestcases);

        if (envId != null) {
            Long userId = UserContextHolder.getUserId();
            EnvironmentVariable variable = environmentVariableMapper.selectOne(
                    new LambdaQueryWrapper<EnvironmentVariable>()
                            .eq(EnvironmentVariable::getId, envId)
                            .eq(EnvironmentVariable::getCreateUser, userId)
            );
            if (variable == null) {
                throw new EnvironmentVariableErrorException(ResultCodeEnum.ENVIRONMENT_VARIABLE_ID_NOT_FOUND);
            }

            List<TestcaseEnvironment> testcaseEnvironmentList = interfaceTestcases.stream().map(i -> {
                TestcaseEnvironment testcaseEnvironment = new TestcaseEnvironment();
                testcaseEnvironment.setTestcaseId(i.getId());
                testcaseEnvironment.setUserId(userId);
                testcaseEnvironment.setEnvironmentId(envId);
                return testcaseEnvironment;
            }).toList();

            testcaseEnvironmentService.saveOrUpdateTestcaseEnvironment(testcaseEnvironmentList);
        }
    }

    @Override
    public IPage<InterfaceTestcasePageVo> getInterfaceTestcaseList(InterfaceTestcaseListQueryDTO interfaceTestcaseListQueryDTO) {
        Page<InterfaceTestcasePageVo> page = new Page<>(interfaceTestcaseListQueryDTO.getCurrent(), interfaceTestcaseListQueryDTO.getSize());
        return interfaceTestcaseMapper.getInterfaceTestcaseList(page, interfaceTestcaseListQueryDTO);
    }

    @Override
    public void deleteInterfaceTestcaseBatch(List<Long> ids) {
        interfaceTestcaseMapper.deleteByIds(ids);
    }

    @Override
    public InterfaceTestcaseVo getInterfaceTestcaseDetail(Long id) {
        InterfaceTestcase interfaceTestcase = interfaceTestcaseMapper.selectById(id);
        if (interfaceTestcase == null) {
            throw new InterfaceTestcaseErrorException(ResultCodeEnum.INTERFACE_TESTCASE_ID_NOT_FOUND);
        }
        // 获取当前用户，当前用例下绑定的环境变量
        Long userId = UserContextHolder.getUserId();
        TestcaseEnvironment testcaseEnvironment = testcaseEnvironmentMapper.selectOne(
                new LambdaQueryWrapper<TestcaseEnvironment>()
                        .eq(TestcaseEnvironment::getTestcaseId, id)
                        .eq(TestcaseEnvironment::getUserId, userId)
        );
        // 获取当前用例的请求路径
        Interface i = interfaceMapper.selectById(interfaceTestcase.getInterfaceId());

        InterfaceTestcaseVo interfaceTestcaseVo = new InterfaceTestcaseVo();
        interfaceTestcaseVo.setInterfaceTestcase(interfaceTestcase);
        interfaceTestcaseVo.setPath(i.getPath());
        interfaceTestcaseVo.setEnvId(testcaseEnvironment == null ? null : testcaseEnvironment.getEnvironmentId());

        return interfaceTestcaseVo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestResultVo sendInterfaceTestcaseRequest(Map<String, Object> requestData, Long envId, Long testcaseId) {
        Long interfaceId = (Long) requestData.get("interfaceId");
        Interface i = interfaceMapper.selectById(interfaceId);
        if (i == null) {
            throw new InterfaceErrorException(ResultCodeEnum.INTERFACE_ID_NOT_FOUND);
        }

        // 获取环境变量
        EnvironmentVariable environmentVariable = null;
        if (envId != null) {
            environmentVariable = environmentVariableMapper.selectOne(
                    new LambdaQueryWrapper<EnvironmentVariable>()
                            .eq(EnvironmentVariable::getCreateUser, UserContextHolder.getUserId())
                            .eq(EnvironmentVariable::getId, envId)
            );
        }

        String path = i.getPath();
        RequestResultVo requestResultVo = new RequestResultVo();
        Map<String, Object> map = httpUtil.processRequestDataForHttp(requestData);

        String preRequestScript = (String) requestData.get("preRequestScript");
        if (preRequestScript != null && !preRequestScript.equals("")) {
            ScriptExecutionResultVo executionResultVo = jsScriptExecutionService.executeJsScript(
                    preRequestScript,
                    new PreAssertionTool(map, environmentVariable == null ? null : environmentVariable.getVariables())
            );
            requestResultVo.setPreExecutionResult(executionResultVo);
        }

        if (environmentVariable != null) {
            map = (Map<String, Object>) VariableResolver.resolve(environmentVariable.getVariables(), map);
        }

        HttpRequest httpRequest = httpUtil.createHttpRequest(map, path);

        Map<String, Object> httpRequestDataMap = httpUtil.getHttpRequestDataMap(httpRequest);

        long start = System.currentTimeMillis();
        @Cleanup HttpResponse httpResponse = httpUtil.sendCustomizeHttpRequest(httpRequest);
        long end = System.currentTimeMillis();

        Map<String, Object> response = httpUtil.getHttpResponseDataMap(httpResponse, end - start);

        requestResultVo.setResponse(response);

        String postRequestScript = (String) requestData.get("postRequestScript");
        if (postRequestScript != null && !postRequestScript.equals("")) {
            ScriptExecutionResultVo executionResultVo = jsScriptExecutionService.executeJsScript(
                    postRequestScript,
                    new PostAssertionTool(response, environmentVariable == null ? null : environmentVariable.getVariables())
            );
            requestResultVo.setPostExecutionResult(executionResultVo);
        }

        // 更新环境变量
        environmentVariableMapper.updateById(environmentVariable);

        requestResultVo.setRequest(httpRequestDataMap);
        requestResultVo.setInterfaceId(interfaceId);
        requestResultVo.setTestcaseId(testcaseId);
        return requestResultVo;
    }
}
