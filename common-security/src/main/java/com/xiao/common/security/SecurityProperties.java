package com.xiao.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全配置属性
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
     * 放行路径列表（无需认证）
     */
    private String[] permitAllPatterns = new String[] {
        "/actuator/**"
    };

    /**
     * 权限前缀，默认 ROLE_
     */
    private String authorityPrefix = "ROLE_";

    /**
     * 权限在 JWT 中的字段名
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
