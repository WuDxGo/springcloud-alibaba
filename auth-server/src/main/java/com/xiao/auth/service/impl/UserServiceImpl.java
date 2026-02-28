package com.xiao.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.auth.entity.UserEntity;
import com.xiao.auth.mapper.UserMapper;
import com.xiao.auth.service.UserService;
import com.xiao.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 用户服务实现类
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 16:52
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 16:52] [WuDx] 创建类
 */
@Service  // 标记为Service组件，会被Spring自动扫描和注册
@RequiredArgsConstructor  // 为final字段生成构造方法，实现构造器注入
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    /**
     * 用户Mapper，用于数据库操作
     */
    private final UserMapper userMapper;

    /**
     * 密码编码器，用于密码加密和验证
     * 从PasswordEncoderConfig配置类注入
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据用户名加载用户信息
     * 支持用户名、手机号、邮箱登录
     *
     * @param username 用户名/手机号/邮箱
     * @return User 用户信息（包含角色和权限）
     */
    @Override
    public User loadUserByUsername(String username) {
        // 参数校验，如果用户名为空直接返回null
        if (!StringUtils.hasText(username)) {
            return null;
        }

        // 构建查询条件：用户名=username OR 手机号=username OR 邮箱=username
        // LambdaQueryWrapper是MyBatis Plus提供的条件构造器，可以避免硬编码字段名
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username)  // 用户名匹配
                .or()  // 或者
                .eq(UserEntity::getPhone, username)     // 手机号匹配
                .or()  // 或者
                .eq(UserEntity::getEmail, username);    // 邮箱匹配

        // 查询用户
        UserEntity userEntity = this.getOne(wrapper);
        if (userEntity == null) {
            return null;  // 用户不存在
        }

        // 将数据库实体转换为通用用户对象（包含角色和权限）
        return convertToUser(userEntity);
    }

    /**
     * 根据手机号加载用户信息
     *
     * @param phone 手机号
     * @return User 用户信息
     */
    @Override
    public User loadUserByPhone(String phone) {
        // 构建查询条件：手机号=phone
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone);

        // 查询用户
        UserEntity userEntity = this.getOne(wrapper);
        if (userEntity == null) {
            return null;  // 用户不存在
        }

        // 转换为通用用户对象
        return convertToUser(userEntity);
    }

    /**
     * 注册新用户
     *
     * @param user 用户实体
     * @return UserEntity 注册后的用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 事务管理，发生任何异常都回滚
    public UserEntity register(UserEntity user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, user.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");  // 抛出异常，由全局异常处理器处理
        }

        // 加密密码 - 使用PasswordEncoder进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 默认启用状态
        user.setStatus(1);  // 1表示启用

        // 保存用户到数据库
        this.save(user);

        return user;  // 返回注册后的用户（包含自动生成的ID）
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return boolean 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户是否存在
        UserEntity user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码是否正确
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新为新密码（加密后）
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);  // 更新用户信息
    }

    /**
     * 将数据库实体转换为通用用户对象
     * 转换过程中会查询用户的角色和权限信息
     *
     * @param entity 数据库实体
     * @return User 通用用户对象（包含角色和权限）
     */
    private User convertToUser(UserEntity entity) {
        User user = new User();
        // 复制基础属性
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());  // 注意：这里返回的是加密后的密码
        user.setPhone(entity.getPhone());
        user.setEmail(entity.getEmail());
        user.setAvatar(entity.getAvatar());
        user.setStatus(entity.getStatus());
        user.setCreateTime(entity.getCreateTime());
        user.setUpdateTime(entity.getUpdateTime());

        // 查询角色和权限
        Set<String> roles = userMapper.selectRolesByUserId(entity.getId());
        Set<String> permissions = userMapper.selectPermissionsByUserId(entity.getId());

        user.setRoles(roles);
        user.setPermissions(permissions);

        return user;
    }
}