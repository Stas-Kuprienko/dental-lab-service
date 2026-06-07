package org.lab.uimvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.lab.dental.feignclient")
public class UiMvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(UiMvcApplication.class, args);
	}

}
