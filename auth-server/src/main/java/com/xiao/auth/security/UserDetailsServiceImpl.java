package com.xiao.auth.security;

import com.xiao.auth.service.UserService;
import com.xiao.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security用户详情服务实现类
 * 负责根据用户名加载用户信息，用于认证过程
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 16:52
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 16:52] [WuDx] 创建类
 */
@Service
@RequiredArgsConstructor  // 为final字段生成构造函数
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 用户服务，用于从数据库加载用户信息
     */
    private final UserService userService;

    /**
     * 根据用户名加载用户详情
     * 这是Spring Security认证过程中的核心方法
     *
     * @param username 用户名（也可以是手机号、邮箱，由UserService支持）
     * @return UserDetails Spring Security的用户详情对象
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 调用用户服务加载用户信息
        User user = userService.loadUserByUsername(username);

        // 2. 验证用户是否存在
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 3. 检查用户状态（是否被禁用）
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 4. 构建权限集合（包括角色和具体权限）
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // 5. 添加角色（Spring Security中角色需要加"ROLE_"前缀）
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            authorities.addAll(user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet()));
        }

        // 6. 添加具体权限（不需要加前缀）
        if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
            authorities.addAll(user.getPermissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }

        // 7. 创建并返回Spring Security的UserDetails对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),  // 用户名
                user.getPassword(),  // 密码（已加密）
                authorities          // 权限集合
        );
    }
}