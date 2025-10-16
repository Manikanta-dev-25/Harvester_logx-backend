package com.mani;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.mani")
public class FarmerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FarmerBackendApplication.class, args);
		System.out.println("Farmer backend running");
	}

}
