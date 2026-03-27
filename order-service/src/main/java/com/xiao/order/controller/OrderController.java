package com.xiao.order.controller;

import com.xiao.client.ProductServiceFeign;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    private ProductServiceFeign productServiceFeign;

    /**
     * 调用 domestic-service
     */
    @GetMapping("/callCf99")
    public String callCf99() {
        return productServiceFeign.cf99();
    }
}
