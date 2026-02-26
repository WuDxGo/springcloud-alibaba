package com.xiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"com.xiao.mapper"})
public class DomesticServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DomesticServiceApplication.class, args);
    }
}
