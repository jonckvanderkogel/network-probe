package com.bullit.caiwayprobe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CaiwayProbeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaiwayProbeApplication.class, args);
	}

}
