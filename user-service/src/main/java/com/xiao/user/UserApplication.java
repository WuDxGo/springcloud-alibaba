package com.xiao.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 *
 * <p> 作用：提供用户资料管理功能
 * <p> 功能：用户信息查询、修改、密码修改等
 *
 * @author: WuDx
 * @Date: 2026/2/28 10:35
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 10:35] [WuDx] 创建类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xiao.user.mapper")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
