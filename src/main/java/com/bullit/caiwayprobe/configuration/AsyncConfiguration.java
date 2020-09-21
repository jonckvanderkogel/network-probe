package com.bullit.caiwayprobe.configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class AsyncConfiguration {
    @Bean(name="pingExecutor")
    public Executor getPingExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("pingExecutor-%d")
                .setDaemon(false)
                .build();
        ExecutorService executorService = Executors.newFixedThreadPool(10, threadFactory);

        return executorService;
    }
}
