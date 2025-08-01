package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.BaseEntity;
import com.lmj.platformserver.entity.EnvironmentVariable;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.entity.TestcaseEnvironment;
import com.lmj.platformserver.exception.EnvironmentVariableErrorException;
import com.lmj.platformserver.mapper.EnvironmentVariableMapper;
import com.lmj.platformserver.mapper.TestcaseEnvironmentMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.EnvironmentVariableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnvironmentVariableServiceImpl implements EnvironmentVariableService {

    @Autowired
    private EnvironmentVariableMapper environmentVariableMapper;

    @Autowired
    private TestcaseEnvironmentMapper testcaseEnvironmentMapper;

    @Override
    public Long addEnvironmentVariable(EnvironmentVariable environmentVariable) {
        existDuplicateVariable(environmentVariable.getVariables());
        environmentVariableMapper.insert(environmentVariable);
        return environmentVariable.getId();
    }

    @Override
    public List<EnvironmentVariable> getEnvironmentVariable() {
        Long userId = UserContextHolder.getUserId();
        return environmentVariableMapper.selectList(
                new LambdaQueryWrapper<EnvironmentVariable>()
                        .eq(EnvironmentVariable::getCreateUser, userId)
                        .orderByDesc(EnvironmentVariable::getUpdatedTime)
        );
    }

    @Override
    public void deleteEnvironmentVariable(Long id) {
        Long userId = UserContextHolder.getUserId();
        environmentVariableMapper.delete(
                new LambdaQueryWrapper<EnvironmentVariable>()
                        .eq(EnvironmentVariable::getCreateUser, userId)
                        .eq(EnvironmentVariable::getId, id)
        );
    }

    @Override
    public void updateEnvironmentVariable(EnvironmentVariable environmentVariable) {
        doUpdate(environmentVariable, UserContextHolder.getUserId());
    }

    @Async
    public void updateEnvironmentVariableAsync(EnvironmentVariable environmentVariable, Long userId) {
        try {
            doUpdate(environmentVariable, userId);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public Map<Long, EnvironmentVariable> getTestcaseBindingEnvironment(List<InterfaceTestcase> interfaceTestcaseList, Long userId) {

        if (interfaceTestcaseList == null || interfaceTestcaseList.isEmpty()) {
            return null;
        }

        Set<Long> testcaseIdList = interfaceTestcaseList.stream().map(BaseEntity::getId).collect(Collectors.toSet());
        List<TestcaseEnvironment> testcaseEnvironmentList = testcaseEnvironmentMapper.selectList(
                new LambdaQueryWrapper<TestcaseEnvironment>()
                        .select(TestcaseEnvironment::getTestcaseId, TestcaseEnvironment::getEnvironmentId)
                        .eq(TestcaseEnvironment::getUserId, userId)
                        .in(TestcaseEnvironment::getTestcaseId, testcaseIdList)
        );
        if (testcaseEnvironmentList.isEmpty()) {
            return null;
        }

        Set<Long> envIdSet = testcaseEnvironmentList.stream().map(TestcaseEnvironment::getEnvironmentId).collect(Collectors.toSet());
        List<EnvironmentVariable> environmentVariables = environmentVariableMapper.selectList(
                new LambdaQueryWrapper<EnvironmentVariable>()
                        .in(EnvironmentVariable::getId, envIdSet)
        );
        if (environmentVariables == null || environmentVariables.isEmpty()) {
            return null;
        }

        Map<Long, EnvironmentVariable> map = new HashMap<>();
        Map<Long, EnvironmentVariable> envIdToEnvObjectMap = environmentVariables.stream()
                .collect(Collectors.toMap(EnvironmentVariable::getId, env -> env));
        for (TestcaseEnvironment association : testcaseEnvironmentList) {
            EnvironmentVariable environmentVariable = envIdToEnvObjectMap.get(association.getEnvironmentId());
            if (environmentVariable != null) {
                map.put(association.getTestcaseId(), environmentVariable);
            }
        }
        return map;
    }

    private void doUpdate(EnvironmentVariable environmentVariable, Long userId) {
        EnvironmentVariable variable = environmentVariableMapper.selectOne(
                new LambdaQueryWrapper<EnvironmentVariable>()
                        .eq(EnvironmentVariable::getId, environmentVariable.getId())
                        .eq(EnvironmentVariable::getCreateUser, userId)
        );
        if (variable == null) {
            throw new EnvironmentVariableErrorException(ResultCodeEnum.ENVIRONMENT_VARIABLE_ID_NOT_FOUND);
        }
        existDuplicateVariable(environmentVariable.getVariables());
        environmentVariableMapper.updateById(environmentVariable);
    }

    private void existDuplicateVariable(List<Map<String, Object>> variables) {

        if (!CollectionUtils.isEmpty(variables)) {
            Map<String, Integer> keyCounter = new HashMap<>();
            variables.forEach(v -> {
                String key = (String) v.get("key");
                Integer count = keyCounter.getOrDefault(key, 0) + 1;
                keyCounter.put(key, count);
            });

            for (Map.Entry<String, Integer> entry : keyCounter.entrySet()) {
                if (entry.getValue() > 1) {
                    throw new EnvironmentVariableErrorException(ResultCodeEnum.ENVIRONMENT_VARIABLE_DUPLICATE);
                }
            }
        }
    }
}
