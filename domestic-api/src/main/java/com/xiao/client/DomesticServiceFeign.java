package com.xiao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("domestic-service")
public interface DomesticServiceFeign {
    @RequestMapping("/service/cf99")
    String cf99();
}
