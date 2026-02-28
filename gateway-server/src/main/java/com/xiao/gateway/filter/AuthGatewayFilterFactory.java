package com.xiao.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiao.common.constant.AuthConstant;
import com.xiao.common.entity.Result;
import com.xiao.common.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 认证过滤器
 *
 * <p> 网关层的核心过滤器，负责所有请求的认证和授权
 *
 * @author: WuDx
 * @Date: 2026/2/28 09:07
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 09:07] [WuDx] 创建类
 */
@Slf4j
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;

    /**
     * 响应式Redis模板
     */
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Jackson对象映射器
     */
    private final ObjectMapper objectMapper;

    /**
     * Ant路径匹配器
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 构造器注入
     *
     * @param jwtUtil JWT工具类
     * @param redisTemplate Redis模板
     * @param objectMapper JSON对象映射器
     */
    public AuthGatewayFilterFactory(JwtUtil jwtUtil,
                                    ReactiveStringRedisTemplate redisTemplate,
                                    ObjectMapper objectMapper) {
        super(Config.class);  // 调用父类构造器，传入配置类类型
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        log.info("AuthGatewayFilterFactory 构造函数被调用 - 过滤器工厂已创建");
    }

    /**
     * 初始化方法
     * 在Bean创建完成后执行，用于验证过滤器工厂是否被正确注册
     */
    @PostConstruct
    public void init() {
        log.info("AuthGatewayFilterFactory 初始化完成 - Bean名称: {}, 配置中应使用名称: 'Auth'",
                this.getClass().getSimpleName());
    }

    /**
     * 创建网关过滤器
     * 这是GatewayFilterFactory的核心方法，返回实际的过滤器
     * 当路由配置中引用该过滤器时，此方法会被调用
     *
     * @param config 过滤器配置对象
     * @return GatewayFilter 网关过滤器
     */
    @Override
    public GatewayFilter apply(Config config) {
        log.info("AuthGatewayFilterFactory.apply() 被调用 - 过滤器已应用到路由");

        // 返回一个Lambda表达式实现的GatewayFilter
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("进入过滤器执行逻辑，路径: {}", path);

            // 1. 白名单放行
            if (isWhiteList(path)) {
                log.debug("白名单路径放行: {}", path);
                return chain.filter(exchange);
            }

            // 2. 获取token
            String token = getToken(request);
            if (!StringUtils.hasText(token)) {
                log.warn("未提供认证令牌, 路径: {}", path);
                return unauthorizedResponse(exchange, "未提供认证令牌");
            }

            // 3. 验证token格式和签名
            if (!jwtUtil.validateToken(token)) {
                log.warn("无效的认证令牌, 路径: {}", path);
                return unauthorizedResponse(exchange, "无效的认证令牌");
            }

            // 4. 从token中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            List<String> roles = jwtUtil.getRolesFromToken(token);

            if (userId == null) {
                log.warn("令牌中未包含用户ID, 路径: {}", path);
                return unauthorizedResponse(exchange, "无效的认证令牌");
            }

            // 5. 验证Redis中的token - 使用正确的响应式处理方式
            String redisKey = "token:" + userId;
            log.debug("验证Redis中的token, key: {}", redisKey);

            return redisTemplate.opsForValue().get(redisKey)
                    // 将Redis中的token转换为验证结果
                    .map(redisToken -> {
                        if (redisToken == null) {
                            log.debug("Redis中未找到token: {}", redisKey);
                            return false;
                        }
                        boolean isValid = token.equals(redisToken);
                        if (!isValid) {
                            log.warn("令牌不匹配, Redis中的token与请求token不一致");
                        }
                        return isValid;
                    })
                    .defaultIfEmpty(false)  // Redis查询结果为null时返回false
                    .flatMap(isValid -> {
                        // 根据验证结果决定后续操作
                        if (!isValid) {
                            return unauthorizedResponse(exchange, "令牌已失效");
                        }

                        // 检查权限
                        if (!checkPermission(path, roles)) {
                            log.warn("权限不足, userId: {}, path: {}, roles: {}", userId, path, roles);
                            return forbiddenResponse(exchange, "权限不足");
                        }

                        log.info("认证成功, userId: {}, username: {}, path: {}", userId, username, path);

                        // 传递用户信息到下游服务
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-User-Id", String.valueOf(userId))      // 用户ID
                                .header("X-User-Name", username)                   // 用户名
                                .header("X-User-Roles", roles != null ? String.join(",", roles) : "")  // 角色列表
                                .build();

                        // 继续执行过滤器链
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });
        };
    }

    /**
     * 从请求头中提取JWT令牌
     *
     * @param request HTTP请求
     * @return String JWT令牌，如果没有则返回null
     */
    private String getToken(ServerHttpRequest request) {
        // 1. 获取Authorization头
        List<String> headers = request.getHeaders().get(AuthConstant.AUTHORIZATION_HEADER);

        // 2. 检查Authorization头是否存在
        if (headers == null || headers.isEmpty()) {
            return null;
        }

        // 3. 获取第一个Authorization头
        String authHeader = headers.get(0);

        // 4. 检查是否以Bearer开头
        if (authHeader.startsWith(AuthConstant.TOKEN_PREFIX)) {
            // 5. 移除Bearer前缀，返回纯净的JWT
            return authHeader.substring(AuthConstant.TOKEN_PREFIX.length());
        }

        return null;
    }

    /**
     * 检查路径是否在白名单中
     *
     * @param path 请求路径
     * @return boolean true-白名单路径，不需要认证
     */
    private boolean isWhiteList(String path) {
        for (String pattern : AuthConstant.WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户权限
     *
     * @param path 请求路径
     * @param roles 用户角色列表
     * @return boolean true-有权限，false-无权限
     */
    private boolean checkPermission(String path, List<String> roles) {
        // 检查管理员路径
        if (path.startsWith("/admin/")) {
            return roles != null && roles.contains("ADMIN");
        }
        // 其他路径默认放行
        return true;
    }

    /**
     * 返回401未授权响应
     *
     * @param exchange Web交换对象
     * @param message 错误消息
     * @return Mono<Void> 响应流
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<?> result = Result.error(401, message);
        return writeResponse(response, result);
    }

    /**
     * 返回403禁止访问响应
     *
     * @param exchange Web交换对象
     * @param message 错误消息
     * @return Mono<Void> 响应流
     */
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<?> result = Result.error(403, message);
        return writeResponse(response, result);
    }

    /**
     * 将结果对象写入HTTP响应
     *
     * @param response HTTP响应对象
     * @param result 结果对象
     * @return Mono<Void> 响应流
     */
    private Mono<Void> writeResponse(ServerHttpResponse response, Result<?> result) {
        try {
            byte[] bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer))
                    .doOnError(e -> log.error("写入响应失败", e));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return Mono.error(e);
        }
    }

    /**
     * 过滤器配置类
     * 可以在这里定义可配置的属性
     * 这些属性可以在路由配置中设置
     */
    public static class Config {
        // 可以添加配置属性，如：是否启用、白名单等
    }
}