package com.roome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class RoomeApplication {

  public static void main(String[] args) {
    SpringApplication.run(RoomeApplication.class, args);
  }

}
