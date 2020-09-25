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
@ConfigurationProperties(prefix = "dns")
public class DnsConfiguration {
    private static final String IP_ADDRESS = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    @Pattern(regexp = IP_ADDRESS)
    private String serverOne;
    @Pattern(regexp = IP_ADDRESS)
    private String serverTwo;

    @Bean(name="dnsServerOne")
    public String getServerOne() {
        return this.serverOne;
    }

    @Bean(name="dnsServerTwo")
    public String getServerTwo() {
        return this.serverTwo;
    }
}
