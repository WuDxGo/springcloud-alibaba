package com.xiao.order.controller;

import com.xiao.client.DomesticServiceFeign;
import com.xiao.common.entity.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    private DomesticServiceFeign domesticServiceFeign;

    /**
     * 创建订单
     *
     * @param orderData 订单数据
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody Map<String, Object> orderData) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("用户 {} 创建订单: {}", username, orderData);

        // TODO: 实际业务逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", System.currentTimeMillis());  // 模拟订单ID
        result.put("status", "CREATED");                     // 订单状态

        return Result.success(result);
    }

    /**
     * 查询订单列表
     *
     * @return 订单列表
     */
    @GetMapping("/list")
    public Result<?> getOrderList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("用户 {} 查询订单列表", username);

        // TODO: 实际业务逻辑
        return Result.success("订单列表功能开发中");
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Result<?> getOrderDetail(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("用户 {} 查询订单详情: {}", username, orderId);

        // TODO: 实际业务逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "PAID");
        result.put("amount", 299.99);

        return Result.success(result);
    }

    /**
     * 调用domestic-service
     */
    @GetMapping("/callCf99")
    public String callCf99() {
        return domesticServiceFeign.cf99();
    }
}
