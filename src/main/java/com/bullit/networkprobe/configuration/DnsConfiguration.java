package com.bullit.networkprobe.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Setter
@Configuration
@ConfigurationProperties(prefix = "dns")
public class DnsConfiguration {
    private String serverOne;
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
