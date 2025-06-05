package com.buildermaster.projecttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProjecttrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjecttrackerApplication.class, args);
	}

}
