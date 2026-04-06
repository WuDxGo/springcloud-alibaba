package com.xiao.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全配置属性
 * 
 * <h3>配置前缀：</h3>
 * <p>{@code common.security}</p>
 * 
 * <h3>配置示例：</h3>
 * <pre>
 * common:
 *   security:
 *     enabled: true
 *     permit-all-patterns:
 *       - /actuator/**
 *       - /public/**
 *     authority-prefix: ROLE_
 *     authorities-claim-name: authorities
 * </pre>
 *
 * @author xiao
 */
@ConfigurationProperties(prefix = "common.security")
public class SecurityProperties {

    /**
     * 是否启用自动安全配置，默认启用
     */
    private boolean enabled = true;

    /**
     * 放行路径列表（无需认证的路径模式）
     * <p>支持 Ant 风格路径匹配，如：/public/**、/actuator/*</p>
     */
    private String[] permitAllPatterns = new String[] {
        "/actuator/**"
    };

    /**
     * 权限前缀
     * <p>用于匹配 JWT Token 中的权限标识，默认 "ROLE_"</p>
     * <p>例如：JWT 中 authorities=["ROLE_ADMIN"]，则匹配 ROLE_ 前缀后得到 ADMIN 角色</p>
     */
    private String authorityPrefix = "ROLE_";

    /**
     * 权限在 JWT Token 中的字段名称
     * <p>OAuth2 认证服务器颁发的 JWT 中存储权限信息的字段名，默认 "authorities"</p>
     */
    private String authoritiesClaimName = "authorities";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String[] getPermitAllPatterns() {
        return permitAllPatterns;
    }

    public void setPermitAllPatterns(String[] permitAllPatterns) {
        this.permitAllPatterns = permitAllPatterns;
    }

    public String getAuthorityPrefix() {
        return authorityPrefix;
    }

    public void setAuthorityPrefix(String authorityPrefix) {
        this.authorityPrefix = authorityPrefix;
    }

    public String getAuthoritiesClaimName() {
        return authoritiesClaimName;
    }

    public void setAuthoritiesClaimName(String authoritiesClaimName) {
        this.authoritiesClaimName = authoritiesClaimName;
    }
}
