package com.roome;

import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {S3AutoConfiguration.class}) // temp: s3 에러로 인한 자동 설정 제외
public class RoomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomeApplication.class, args);
	}

}
