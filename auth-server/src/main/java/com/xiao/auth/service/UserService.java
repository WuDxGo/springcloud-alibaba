package com.xiao.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.auth.entity.UserEntity;
import com.xiao.common.entity.User;

/**
 * 用户服务接口
 * 继承IService以获得MyBatis Plus的通用服务方法
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 16:52
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 16:52] [WuDx] 创建类
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 根据用户名加载用户信息（包含角色和权限）
     *
     * @param username 用户名（也可以是手机号、邮箱）
     * @return User 包含完整信息的用户对象
     */
    User loadUserByUsername(String username);

    /**
     * 根据手机号加载用户信息
     *
     * @param phone 手机号
     * @return User 用户信息
     */
    User loadUserByPhone(String phone);

    /**
     * 注册新用户
     *
     * @param user 用户实体
     * @return UserEntity 注册后的用户信息
     */
    UserEntity register(UserEntity user);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return boolean 是否修改成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}