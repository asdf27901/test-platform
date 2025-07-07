package com.lmj.platformserver.controller;

import com.lmj.platformserver.entity.EnvironmentVariable;
import com.lmj.platformserver.groups.Update;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.EnvironmentVariableService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequestMapping("/config/environmentVariable")
public class EnvironmentVariableController {

    @Autowired
    private EnvironmentVariableService environmentVariableService;

    @PostMapping("/add")
    public Response<Long> addEnvironmentVariable(@RequestBody @Validated EnvironmentVariable environmentVariable) {
        log.info("添加环境变量: {}", environmentVariable);
        Long id = environmentVariableService.addEnvironmentVariable(environmentVariable);
        return Response.success(id);
    }

    @GetMapping("/get")
    public Response<List<EnvironmentVariable>> getEnvironmentVariable() {
        log.info("获取当前用户环境变量");
        List<EnvironmentVariable> environmentVariables = environmentVariableService.getEnvironmentVariable();
        return Response.success(environmentVariables);
    }

    @PostMapping("/delete")
    public Response<?> deleteEnvironmentVariable(@NotNull(message = "ID不能为空") Long id) {
        log.info("删除环境变量: {}", id);
        environmentVariableService.deleteEnvironmentVariable(id);
        return Response.success();
    }

    @PostMapping("/update")
    public Response<?> updateEnvironmentVariable(@RequestBody @Validated(Update.class) EnvironmentVariable environmentVariable) {
        log.info("更新环境变量: {}", environmentVariable);
        environmentVariableService.updateEnvironmentVariable(environmentVariable);
        return Response.success();
    }
}
