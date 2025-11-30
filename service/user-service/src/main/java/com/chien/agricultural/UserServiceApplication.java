package com.chien.agricultural;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@EnableDiscoveryClient
@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(UserServiceApplication.class, args);

		// In ra ngay sau khi ứng dụng khởi động xong
		System.out.println("\n=== ACTIVE PROFILES ===");
		Arrays.stream(context.getEnvironment().getActiveProfiles())
				.forEach(System.out::println);

		System.out.println("\n=== DATASOURCE URL ===");
		System.out.println(context.getEnvironment().getProperty("spring.datasource.url"));
		System.out.println("=====================================\n");
	}

}
