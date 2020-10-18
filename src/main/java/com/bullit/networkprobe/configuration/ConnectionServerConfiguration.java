package com.bullit.networkprobe.configuration;

import com.bullit.networkprobe.service.ConnectionService;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String URL = "^https:\\/\\/(?:[\\w-]+\\.)+([a-z]|[A-Z]|[0-9]){2,6}$";

    @Pattern(regexp = URL)
    private String serverOne;
    @Pattern(regexp = URL)
    private String serverTwo;
    private Integer timeOutMillis;

    @Bean
    public ConnectionService getConnectionService(@Autowired MDCLogger mdcLogger) {
        return new ConnectionService(serverOne, serverTwo, timeOutMillis, mdcLogger);
    }
}
