package com.xiao.auth.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置类
 *
 * <p> 单独配置PasswordEncoder，避免循环依赖
 *
 * @author: WuDx
 * @Date: 2026/2/28 14:17
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 14:17] [WuDx] 创建类
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 创建密码编码器Bean
     * BCrypt是一种强哈希算法，每次加密结果都不同
     * 它会自动处理盐值（salt），无需额外配置
     *
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean  // 将方法返回的对象注册为Spring Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder是Spring Security推荐使用的密码编码器
        return new BCryptPasswordEncoder();
    }
}
