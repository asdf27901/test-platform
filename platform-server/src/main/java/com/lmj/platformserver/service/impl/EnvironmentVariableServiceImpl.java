package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.EnvironmentVariable;
import com.lmj.platformserver.exception.EnvironmentVariableErrorException;
import com.lmj.platformserver.mapper.EnvironmentVariableMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.EnvironmentVariableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EnvironmentVariableServiceImpl implements EnvironmentVariableService {

    @Autowired
    private EnvironmentVariableMapper environmentVariableMapper;

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
        Long userId = UserContextHolder.getUserId();
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
