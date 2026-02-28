package com.xiao.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证服务器启动类
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 16:45
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 16:45] [WuDx] 创建类
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.xiao.auth.mapper"})
@ComponentScan(basePackages = {"com.xiao.auth", "com.xiao.common"})
public class AuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
