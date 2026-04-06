package com.xiao.order.controller;

import com.xiao.client.ProductServiceFeign;
import com.xiao.common.result.Result;
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
     * 调用商品服务
     */
    @GetMapping("/callCf99")
    public Result<String> callCf99() {
        log.info("调用商品服务 cf99 接口");
        String result = productServiceFeign.cf99();
        return Result.success(result);
    }
}
