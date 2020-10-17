package com.bullit.networkprobe.configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Validated
@Setter
@Configuration
@ConfigurationProperties(prefix = "connection")
public class ConnectionServerConfiguration {
    private static final String URL = "^https:\\/\\/(?:[\\w-]+\\.)+([a-z]|[A-Z]|[0-9]){2,6}$";

    @Pattern(regexp = URL)
    private String serverOne;
    @Pattern(regexp = URL)
    private String serverTwo;
    private Integer timeOutMillis;

    @Bean(name="connectionServerOne")
    public String getServerOne() {
        return this.serverOne;
    }

    @Bean(name="connectionServerTwo")
    public String getServerTwo() {
        return this.serverTwo;
    }

    @Bean(name="timeOutMillis")
    public Integer getTimeOutMillis() {
        return this.timeOutMillis;
    }

    @Bean(name= "connectionExecutor")
    public Executor getConnectionExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("connectionExecutor-%d")
                .setDaemon(false)
                .build();

        return Executors.newFixedThreadPool(10, threadFactory);
    }
}
