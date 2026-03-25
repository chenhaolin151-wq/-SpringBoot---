package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.User;
import java.util.List;

/**
 * 用户管理业务接口
 */
public interface UserService {
    /**
     * 用户登录逻辑
     */
    Result<User> login(User loginUser);

    /**
     * 新增用户（设置默认密码和状态）
     */
    Result<String> addUser(User user);

    /**
     * 获取所有用户列表
     */
    Result<List<User>> getAllUsers();

    /**
     * 根据ID删除用户
     */
    Result<String> deleteUser(Long id);

    /**
     * 更新用户状态（在职/离职）
     */
    Result<String> updateStatus(Long id, Integer status);

    /**
     * 更新用户角色
     */
    Result<String> updateRole(Long id, String role);

    /**
     * 通用用户信息更新
     */
    Result<String> update(User user);
}