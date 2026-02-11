package com.xiao.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
//@RequestMapping("/service")
@RefreshScope
public class HousekeepingController {

    @Value("${name}")
    private String name;

    @Value("${who}")
    private String who;

    @Value("${my}")
    private String my;

    @GetMapping("/helloWorld")
    public String helloWorld() {
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
        return "{\"my\": \"" + my + "\", \"who\": \"" + who + "\", \"name\": \"" + name + "\", \"hostname\": \"" + hostname + "\", \"ipaddress\": \"" + ipaddress + "\"}";
    }

    /**
     * 九九乘法表
     *
     * @return
     */
    @GetMapping(value = "/cf99")
    public String cf99() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= i; j++) {
                sb.append(j).append("*").append(i).append("=").append(i * j).append('\t');
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
