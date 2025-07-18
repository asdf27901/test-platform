package com.lmj.platformserver.service;

import com.lmj.platformserver.entity.TestcaseEnvironment;

import java.util.List;

public interface TestcaseEnvironmentService {

    void saveOrUpdateTestcaseEnvironment(List<TestcaseEnvironment> testcaseEnvironmentList);
}
