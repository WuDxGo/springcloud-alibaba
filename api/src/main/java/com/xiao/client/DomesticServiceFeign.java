package com.xiao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("product-service")
public interface DomesticServiceFeign {
    @RequestMapping("/product/cf99")
    String cf99();
}
