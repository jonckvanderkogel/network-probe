package com.bullit.networkprobe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NetworkProbeApplicationTest {

    @Test
    public void contextShouldNotLoadWhenPropertiesIncorrect() {
        Exception exception = assertThrows(ConfigurationPropertiesBindException.class, () -> {
            SpringApplication.run(NetworkProbeApplication.class, "--spring.profiles.active=wrong");
        });

        String expectedMessage = "Error creating bean with name 'dnsConfiguration': Could not bind properties to 'DnsConfiguration' : prefix=dns";

        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
