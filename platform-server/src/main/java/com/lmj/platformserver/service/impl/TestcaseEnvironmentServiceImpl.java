package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.TestcaseEnvironment;
import com.lmj.platformserver.mapper.TestcaseEnvironmentMapper;
import com.lmj.platformserver.service.TestcaseEnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestcaseEnvironmentServiceImpl implements TestcaseEnvironmentService {

    @Autowired
    private TestcaseEnvironmentMapper testcaseEnvironmentMapper;

    @Override
    public void saveOrUpdateTestcaseEnvironment(List<TestcaseEnvironment> testcaseEnvironmentList) {
        Long userId = UserContextHolder.getUserId();
        Set<Long> testcaseIds = testcaseEnvironmentList.stream().map(TestcaseEnvironment::getTestcaseId).collect(Collectors.toSet());

        testcaseEnvironmentMapper.delete(
                new LambdaQueryWrapper<TestcaseEnvironment>()
                        .in(TestcaseEnvironment::getTestcaseId, testcaseIds)
                        .eq(TestcaseEnvironment::getUserId, userId)
        );

        testcaseEnvironmentMapper.insert(testcaseEnvironmentList);
    }
}
