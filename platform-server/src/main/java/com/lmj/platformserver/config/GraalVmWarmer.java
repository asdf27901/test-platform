package com.lmj.platformserver.config;

import com.lmj.platformserver.assertion.PostAssertionTool;
import com.lmj.platformserver.service.JsScriptExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class GraalVmWarmer implements ApplicationRunner {

    @Autowired
    private JsScriptExecutionService scriptService;

    @Override
    public void run(ApplicationArguments args) {
        scriptService.executeJsScript("console.log('Engine warmed up!')", new PostAssertionTool(null, null));
    }
}
