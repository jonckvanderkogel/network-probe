package com.bullit.networkprobe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NetworkProbeApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetworkProbeApplication.class, args);
	}

}
