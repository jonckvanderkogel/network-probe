package com.bullit.networkprobe.configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class AsyncConfiguration {
    @Bean(name= "connectionExecutor")
    public Executor getConnectionExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("connectionExecutor-%d")
                .setDaemon(false)
                .build();

        return Executors.newFixedThreadPool(10, threadFactory);
    }
}
