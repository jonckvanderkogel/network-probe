package com.bullit.networkprobe.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;

@Validated
@Setter
@Configuration
@ConfigurationProperties(prefix = "connection")
public class ConnectionServerConfiguration {
    private static final String URL = "^(?:[\\w-]+\\.)+([a-z]|[A-Z]|[0-9]){2,6}$";

    @Pattern(regexp = URL)
    private String serverOne;
    @Pattern(regexp = URL)
    private String serverTwo;
    private Integer port;

    @Bean(name="connectionServerOne")
    public String getServerOne() {
        return this.serverOne;
    }

    @Bean(name="connectionServerTwo")
    public String getServerTwo() {
        return this.serverTwo;
    }

    @Bean(name="port")
    public Integer getPort() {
        return this.port;
    }
}
