package com.bpce.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
@EnableWebMvc
public class PortailMockerApplication implements WebMvcConfigurer {

	@EventListener(ApplicationReadyEvent.class)
	public void log() {
		System.out.println("Everything is ready \uD83D\uDC4C");
	}

	public static void main(String[] args) {
		SpringApplication.run(PortailMockerApplication.class, args);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}

}
