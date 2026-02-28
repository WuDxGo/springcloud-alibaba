package com.xiao.user.controller;

import com.xiao.common.entity.Result;
import com.xiao.common.entity.User;
import com.xiao.user.security.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户资料控制器
 *
 * <p> 作用：提供用户资料相关的REST API
 * <p> 功能：获取用户信息、更新资料、修改密码
 *
 * @author: WuDx
 * @Date: 2026/2/28 10:57
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 10:57] [WuDx] 创建类
 */
@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping
    public Result<User> getProfile() {
        // 从SecurityContext获取当前用户名
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询用户信息
        User user = userProfileService.getUserByUsername(username);
        return Result.success(user);
    }

    /**
     * 更新用户资料
     *
     * @param user 新的用户信息
     * @return 更新结果
     */
    @PutMapping
    public Result<?> updateProfile(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        userProfileService.updateProfile(username, user);
        return Result.success();
    }

    /**
     * 修改密码
     *
     * @param passwords 密码信息（包含旧密码和新密码）
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> passwords) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");

        userProfileService.changePassword(username, oldPassword, newPassword);
        return Result.success();
    }
}