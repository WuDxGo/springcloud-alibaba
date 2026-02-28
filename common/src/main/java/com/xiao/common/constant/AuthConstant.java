package com.xiao.common.constant;

/**
 * 认证授权相关常量定义
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 15:25
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 15:25] [WuDx] 创建类
 */
public class AuthConstant {

    /**
     * HTTP请求头中的Authorization字段名
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * JWT令牌前缀
     * 格式: "Bearer " + token
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT负载中的用户ID键名
     */
    public static final String USER_ID_KEY = "userId";

    /**
     * JWT负载中的用户名键名
     */
    public static final String USERNAME_KEY = "username";

    /**
     * JWT负载中的角色列表键名
     */
    public static final String ROLES_KEY = "roles";

    /**
     * JWT负载中的权限列表键名
     */
    public static final String PERMISSIONS_KEY = "permissions";

    /**
     * 白名单路径
     * 这些路径不需要认证即可访问
     * 使用Ant路径匹配模式
     */
    public static final String[] WHITE_LIST = {
            "/auth/login",           // 登录接口
            "/auth/register",        // 注册接口
            "/auth/refresh",         // 令牌刷新接口
            "/auth/logout",          // 登出接口
            "/user/register",        // 用户注册接口
            "/user/forgot-password", // 忘记密码接口
            "/product/list",         // 商品列表接口
            "/product/detail/**",    // 商品详情接口
            "/swagger-ui/**",        // Swagger UI
            "/v3/api-docs/**"        // OpenAPI文档
    };

    /**
     * 管理员专用路径
     * 需要ADMIN角色才能访问
     */
    public static final String[] ADMIN_PATHS = {
            "/admin/**",              // 所有管理员接口
            "/user/list",             // 用户列表
            "/order/manage/**"        // 订单管理
    };

    /**
     * 普通用户路径
     * 需要登录才能访问
     */
    public static final String[] USER_PATHS = {
            "/user/profile/**",       // 用户资料
            "/order/create",          // 创建订单
            "/cart/**"                // 购物车
    };
}