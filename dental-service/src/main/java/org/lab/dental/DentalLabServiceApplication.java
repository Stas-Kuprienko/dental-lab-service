package org.lab.dental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DentalLabServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DentalLabServiceApplication.class, args);
	}
}
