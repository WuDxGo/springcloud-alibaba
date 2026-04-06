package com.xiao.order;

import com.xiao.common.feign.FeignTokenInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 *
 * <p> 作用：提供订单管理功能
 * <p> 功能：创建订单、查询订单、订单管理等
 *
 * @author: WuDx
 * @Date: 2026/2/28 10:35
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 10:35] [WuDx] 创建类
 */
@SpringBootApplication
@EnableFeignClients(
    basePackages = "com.xiao.client",
    defaultConfiguration = com.xiao.common.feign.FeignConfig.class
)
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
