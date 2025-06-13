package com.lmj.platformserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync // 开启异步
public class PlatformServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformServerApplication.class, args);
    }

}
