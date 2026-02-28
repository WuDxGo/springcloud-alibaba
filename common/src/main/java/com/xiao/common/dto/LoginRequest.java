package com.xiao.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 15:34
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 15:34] [WuDx] 创建类
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
