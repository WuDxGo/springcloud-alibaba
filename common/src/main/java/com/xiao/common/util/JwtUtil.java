package com.xiao.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 15:26
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 15:26] [WuDx] 创建类
 */
@Component  // 将该类注册为Spring Bean，方便在其他组件中注入使用
public class JwtUtil {

    /**
     * JWT签名密钥 - 从配置文件读取
     * 用于签名和验证JWT，必须保密
     */
    @Value("${jwt.secret:mySecretKeyForJWTTokenGeneration2025WithStrongEncryption}")
    private String secret;

    /**
     * JWT过期时间（秒）- 从配置文件读取
     * 默认24小时（86400秒）
     */
    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * 获取签名密钥对象
     * 使用新API: Keys.hmacShaKeyFor()
     * 将字符串密钥转换为JWT规范要求的Key对象
     *
     * @return SecretKey 签名密钥对象，用于JWT的签名和验证
     */
    private SecretKey getSigningKey() {
        // 将密钥字符串转换为字节数组
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 使用HMAC-SHA算法创建密钥
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT令牌（带自定义负载）
     * 使用新API: Jwts.builder() 链式调用
     *
     * @param username 用户名，作为主题
     * @param claims   自定义负载数据，如用户ID、角色、权限等
     * @return String JWT令牌
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)                    // 设置自定义负载（新API）
                .subject(username)                  // 设置主题（用户名）（新API）
                .issuedAt(new Date(System.currentTimeMillis()))  // 设置签发时间（新API）
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))  // 设置过期时间（新API）
                .signWith(getSigningKey())          // 设置签名密钥（新API，默认使用HS256）
                .compact();                          // 构建并返回JWT字符串
    }

    /**
     * 生成JWT令牌（无自定义负载）
     * 重载方法，简化只有用户名的情况
     *
     * @param username 用户名
     * @return String JWT令牌
     */
    public String generateToken(String username) {
        return generateToken(username, new HashMap<>());
    }

    /**
     * 验证JWT令牌有效性
     * 使用新API: Jwts.parser().verifyWith().build().parseSignedClaims()
     *
     * @param token JWT令牌
     * @return Boolean true-有效，false-无效
     */
    public Boolean validateToken(String token) {
        try {
            // 尝试解析令牌，如果解析成功且签名验证通过，则令牌有效
            Jwts.parser()
                    .verifyWith(getSigningKey())     // 设置验证密钥（新API）
                    .build()
                    .parseSignedClaims(token);       // 解析JWT并验证签名（新API）
            return true;
        } catch (Exception e) {
            // 任何异常（签名错误、过期、格式错误等）都表示令牌无效
            return false;
        }
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return String 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return Date 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取指定的声明数据
     * 这是一个通用方法，通过函数式接口灵活提取任意数据
     *
     * @param token          JWT令牌
     * @param claimsResolver 声明解析函数，定义如何从Claims中提取数据
     * @return T 指定类型的数据
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中获取所有声明数据
     * 包括标准声明（iss、sub、exp等）和自定义声明
     * 使用新API: parseSignedClaims().getPayload()
     *
     * @param token JWT令牌
     * @return Claims 所有声明对象
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())         // 设置验证密钥（新API）
                .build()
                .parseSignedClaims(token)            // 解析JWT（新API）
                .getPayload();                        // 获取负载部分（新API）
    }

    /**
     * 判断令牌是否已过期
     * 私有方法，供内部调用
     *
     * @param token JWT令牌
     * @return Boolean true-已过期 false-未过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 从令牌中获取用户ID
     * 从自定义声明中提取userId字段
     *
     * @param token JWT令牌
     * @return Long 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * 从令牌中获取角色列表
     * 从自定义声明中提取roles字段
     *
     * @param token JWT令牌
     * @return List<String> 角色列表
     */
    @SuppressWarnings("unchecked")  // 抑制未检查的类型转换警告
    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("roles", List.class));
    }

    /**
     * 从令牌中获取权限列表
     * 从自定义声明中提取permissions字段
     *
     * @param token JWT令牌
     * @return List<String> 权限列表
     */
    @SuppressWarnings("unchecked")  // 抑制未检查的类型转换警告
    public List<String> getPermissionsFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("permissions", List.class));
    }
}