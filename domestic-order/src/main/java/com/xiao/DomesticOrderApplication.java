package com.xiao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.xiao.client")
public class DomesticOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(DomesticOrderApplication.class, args);
    }
}
