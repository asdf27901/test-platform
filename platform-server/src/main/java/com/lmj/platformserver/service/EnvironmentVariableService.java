package com.lmj.platformserver.service;

import com.lmj.platformserver.entity.EnvironmentVariable;

import java.util.List;

public interface EnvironmentVariableService {

    Long addEnvironmentVariable(EnvironmentVariable environmentVariable);

    List<EnvironmentVariable> getEnvironmentVariable();

    void deleteEnvironmentVariable(Long id);

    void updateEnvironmentVariable(EnvironmentVariable environmentVariable);
}
