package com.work.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AttendanceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendanceServerApplication.class, args);
	}

}
