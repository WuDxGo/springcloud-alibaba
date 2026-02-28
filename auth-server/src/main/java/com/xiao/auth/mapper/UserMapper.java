package com.xiao.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.auth.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * 用户Mapper接口
 * 继承MyBatis Plus的BaseMapper，获得基础的CRUD方法
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 16:52
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 16:52] [WuDx] 创建类
 */
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return Set<String> 角色代码集合
     */
    @Select("SELECT r.role_code FROM user_role ur " +
            "LEFT JOIN role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId}")
    Set<String> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return Set<String> 权限代码集合
     */
    @Select("SELECT p.permission_code FROM user_role ur " +
            "LEFT JOIN role_permission rp ON ur.role_id = rp.role_id " +
            "LEFT JOIN permission p ON rp.permission_id = p.id " +
            "WHERE ur.user_id = #{userId}")
    Set<String> selectPermissionsByUserId(@Param("userId") Long userId);
}