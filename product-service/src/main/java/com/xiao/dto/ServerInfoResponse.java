package com.xiao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务器信息响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfoResponse {
    
    /**
     * 服务器名称
     */
    private String my;
    
    /**
     * 服务器标识
     */
    private String who;
    
    /**
     * 应用名称
     */
    private String name;
    
    /**
     * 主机名
     */
    private String hostname;
    
    /**
     * IP 地址
     */
    private String ipaddress;
}
