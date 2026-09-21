package com.likhith.contractforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ContractforgeAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContractforgeAiApplication.class, args);
	}

}
