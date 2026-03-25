package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.User;
import com.work.attendance.mapper.UserMapper;
import com.work.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<User> login(User loginUser) {
        // 1. 根据用户名查询数据库
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginUser.getUsername())
        );

        // 2. 校验逻辑
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(loginUser.getPassword())) {
            return Result.error("密码错误");
        }

        // 假设 1 代表离职状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            return Result.error("该账号已锁定（离职状态）");
        }

        return Result.success(user);
    }

    @Override
    public Result<String> addUser(User user) {
        user.setPassword("123456"); // 设置默认密码
        user.setStatus(0);          // 设置默认在职状态
        int rows = userMapper.insert(user);
        return rows > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    @Override
    public Result<List<User>> getAllUsers() {
        return Result.success(userMapper.selectList(null)); //
    }

    @Override
    public Result<String> deleteUser(Long id) {
        int rows = userMapper.deleteById(id); //
        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Override
    public Result<String> updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status); //
        int rows = userMapper.updateById(user);
        return rows > 0 ? Result.success("状态更新成功") : Result.error("操作失败");
    }

    @Override
    public Result<String> updateRole(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role); //
        int rows = userMapper.updateById(user);
        return rows > 0 ? Result.success("角色权限更新成功") : Result.error("操作失败");
    }

    @Override
    public Result<String> update(User user) {
        int rows = userMapper.updateById(user); //
        return rows > 0 ? Result.success("信息更新成功") : Result.error("更新失败");
    }
}