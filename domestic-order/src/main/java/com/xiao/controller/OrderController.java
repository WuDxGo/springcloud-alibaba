package com.xiao.controller;

import com.xiao.client.DomesticServiceFeign;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
//@RequestMapping("/order")
public class OrderController {
    @Resource
    private DomesticServiceFeign domesticServiceFeign;

    @GetMapping("/helloOrder")
    public String helloOrder() {
        InetAddress ip;
        String hostname;
        String ipaddress;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            ipaddress = ip.getHostAddress();
        } catch (UnknownHostException e) {
            hostname = "N/A";
            ipaddress = "N/A";
        }
        return "{\"hostname\": \"" + hostname + "\", \"ipaddress\": \"" + ipaddress + "\"}";
    }

    /**
     * 调用domestic-service
     */
    @GetMapping("/call_cf99")
    public String call_cf99() {
        return domesticServiceFeign.cf99();
    }
}
