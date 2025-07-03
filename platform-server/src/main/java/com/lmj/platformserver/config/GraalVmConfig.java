package com.lmj.platformserver.config;

import org.graalvm.polyglot.Engine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraalVmConfig {

    @Bean(destroyMethod = "close")
    public Engine graalVmEngine() {
        return Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }
}
