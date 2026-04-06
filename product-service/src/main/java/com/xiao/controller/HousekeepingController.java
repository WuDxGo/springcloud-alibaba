package com.xiao.controller;

import com.xiao.common.result.Result;
import com.xiao.dto.ServerInfoResponse;
import com.xiao.entity.User;
import com.xiao.service.IHousekeepingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
public class HousekeepingController {

    @Value("${name}")
    private String name;

    @Value("${who}")
    private String who;

    @Value("${my}")
    private String my;

    @Resource
    private IHousekeepingService housekeepingService;

    @GetMapping("/helloWorld")
    public Result<ServerInfoResponse> helloWorld() {
        String hostname;
        String ipaddress;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            ipaddress = ip.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("获取服务器信息失败", e);
            hostname = "N/A";
            ipaddress = "N/A";
        }
        
        ServerInfoResponse response = ServerInfoResponse.builder()
                .my(my)
                .who(who)
                .name(name)
                .hostname(hostname)
                .ipaddress(ipaddress)
                .build();
        
        return Result.success(response);
    }

    /**
     * 九九乘法表
     */
    @GetMapping(value = "/cf99")
    public Result<String> cf99() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= i; j++) {
                sb.append(j).append("*").append(i).append("=").append(i * j).append('\t');
            }
            sb.append("\r\n");
        }
        return Result.success(sb.toString());
    }

    @GetMapping(value = "/getUserList")
    public Result<List<User>> getUserList() {
        log.info("查询用户列表");
        List<User> userList = housekeepingService.getUserList();
        return Result.success(userList);
    }
}
