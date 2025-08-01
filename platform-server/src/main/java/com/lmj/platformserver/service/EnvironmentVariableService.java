package com.lmj.platformserver.service;

import com.lmj.platformserver.entity.EnvironmentVariable;
import com.lmj.platformserver.entity.InterfaceTestcase;

import java.util.List;
import java.util.Map;

public interface EnvironmentVariableService {

    Long addEnvironmentVariable(EnvironmentVariable environmentVariable);

    List<EnvironmentVariable> getEnvironmentVariable();

    void deleteEnvironmentVariable(Long id);

    void updateEnvironmentVariable(EnvironmentVariable environmentVariable);

    void updateEnvironmentVariableAsync(EnvironmentVariable environmentVariable, Long userId);

    Map<Long, EnvironmentVariable> getTestcaseBindingEnvironment(List<InterfaceTestcase> interfaceTestcaseList, Long userId);
}
