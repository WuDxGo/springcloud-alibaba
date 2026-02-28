package com.xiao.auth.controller;

import com.xiao.auth.entity.UserEntity;
import com.xiao.auth.service.UserService;
import com.xiao.common.dto.LoginRequest;
import com.xiao.common.dto.LoginResponse;
import com.xiao.common.dto.RegisterRequest;
import com.xiao.common.entity.Result;
import com.xiao.common.entity.User;
import com.xiao.common.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 处理用户登录、注册、登出、令牌刷新等认证相关请求
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 17:21
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 17:21] [WuDx] 创建类
 */
@RestController  // 标记为RESTful控制器
@RequestMapping("/auth")  // 基础路径，所有接口都以/auth开头
@RequiredArgsConstructor  // 为final字段生成构造函数
public class AuthController {

    /**
     * Spring Security认证管理器
     * 用于处理用户认证请求
     */
    private final AuthenticationManager authenticationManager;

    /**
     * 用户服务
     * 用于用户信息的CRUD操作
     */
    private final UserService userService;

    /**
     * JWT工具类
     * 用于生成和解析JWT令牌
     */
    private final JwtUtil jwtUtil;

    /**
     * Redis模板
     * 用于在Redis中存储和管理用户令牌
     * 实现令牌的主动失效（登出）功能
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 用户登录接口
     *
     * @param request 登录请求对象，包含用户名和密码
     * @return Result<LoginResponse> 登录结果，包含JWT令牌和用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. 创建认证令牌
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        // 2. 执行认证
        // authenticationManager会调用UserDetailsServiceImpl加载用户信息
        // 并使用PasswordEncoder比对密码
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 3. 将认证信息存入SecurityContext
        // 这样在同一个线程中可以随时获取当前用户信息
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. 获取认证后的用户详情
        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // 5. 加载完整的用户信息（包括角色、权限等）
        User user = userService.loadUserByUsername(userDetails.getUsername());

        // 6. 构建JWT的负载数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());              // 用户ID
        claims.put("roles", user.getRoles());            // 用户角色列表
        claims.put("permissions", user.getPermissions()); // 用户权限列表

        // 7. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), claims);

        // 8. 将令牌存入Redis
        // 键名格式：token:{userId}
        String redisKey = "token:" + user.getId();
        // 设置24小时过期，与JWT过期时间保持一致
        redisTemplate.opsForValue().set(redisKey, token, 24, TimeUnit.HOURS);

        // 9. 构建用户信息响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())              // 用户ID
                .username(user.getUsername())  // 用户名
                .phone(user.getPhone())        // 手机号
                .email(user.getEmail())        // 邮箱
                .avatar(user.getAvatar())      // 头像URL
                .build();

        // 10. 构建登录响应
        LoginResponse response = LoginResponse.builder()
                .token(token)                   // JWT令牌
                .tokenType("Bearer")             // 令牌类型
                .expiresIn(86400L)               // 过期时间（秒）
                .userInfo(userInfo)               // 用户信息
                .build();

        // 11. 返回成功响应
        return Result.success(response);
    }

    /**
     * 用户注册接口
     *
     * @param request 注册请求对象
     * @return Result<?> 注册结果
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        // 1. 创建用户实体
        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(request, user);

        // 2. 调用服务层注册用户
        UserEntity registered = userService.register(user);

        // 3. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("userId", registered.getId());
        result.put("username", registered.getUsername());

        return Result.success(result);
    }

    /**
     * 用户登出接口
     * 通过删除Redis中的令牌实现登出
     *
     * @param token 请求头中的Authorization令牌
     * @return Result<?> 登出结果
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String token) {
        // 1. 移除Bearer前缀，获取纯净的JWT
        String jwt = token.replace("Bearer ", "");

        // 2. 从JWT中提取用户名
        String username = jwtUtil.getUsernameFromToken(jwt);

        // 3. 如果用户名存在，删除Redis中的令牌
        if (username != null) {
            // 加载用户信息
            User user = userService.loadUserByUsername(username);
            if (user != null) {
                // 删除Redis中存储的令牌
                redisTemplate.delete("token:" + user.getId());
            }
        }

        // 4. 返回成功结果
        return Result.success();
    }

    /**
     * 令牌刷新接口
     * 使用旧的令牌换取新的令牌
     *
     * @param token 请求头中的旧令牌
     * @return Result<Map<String, String>> 包含新令牌的响应
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestHeader("Authorization") String token) {
        // 1. 提取纯净的JWT
        String jwt = token.replace("Bearer ", "");

        // 2. 验证令牌有效性
        if (!jwtUtil.validateToken(jwt)) {
            return Result.error(401, "无效的token");
        }

        // 3. 从旧令牌中提取用户名
        String username = jwtUtil.getUsernameFromToken(jwt);

        // 4. 重新加载用户信息
        User user = userService.loadUserByUsername(username);

        // 5. 验证用户是否存在
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        // 6. 构建新的JWT负载
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles());
        claims.put("permissions", user.getPermissions());

        // 7. 生成新令牌
        String newToken = jwtUtil.generateToken(username, claims);

        // 8. 更新Redis中的令牌
        redisTemplate.opsForValue().set("token:" + user.getId(), newToken, 24, TimeUnit.HOURS);

        // 9. 构建返回数据
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);

        // 10. 返回成功响应
        return Result.success(result);
    }

    /**
     * 令牌验证接口
     * 用于网关或其他服务验证令牌有效性
     *
     * @param token 请求头中的令牌
     * @return Result<?> 验证结果，包含用户信息
     */
    @GetMapping("/verify")
    public Result<?> verify(@RequestHeader("Authorization") String token) {
        // 1. 提取纯净的JWT
        String jwt = token.replace("Bearer ", "");

        // 2. 验证令牌签名和格式
        if (!jwtUtil.validateToken(jwt)) {
            return Result.error(401, "无效的token");
        }

        // 3. 提取用户名
        String username = jwtUtil.getUsernameFromToken(jwt);

        // 4. 加载用户信息
        User user = userService.loadUserByUsername(username);

        // 5. 验证用户是否存在
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        // 6. 验证Redis中的令牌是否匹配
        String redisToken = redisTemplate.opsForValue().get("token:" + user.getId());
        if (!jwt.equals(redisToken)) {
            return Result.error(401, "token已失效");
        }

        // 7. 构建返回的用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("roles", user.getRoles());
        result.put("permissions", user.getPermissions());

        // 8. 返回成功响应
        return Result.success(result);
    }
}