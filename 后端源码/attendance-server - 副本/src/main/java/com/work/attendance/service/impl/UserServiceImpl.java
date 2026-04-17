package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.User;
import com.work.attendance.mapper.UserMapper;
import com.work.attendance.service.UserService;
import com.work.attendance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. 根据用户名查询数据库
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username) // 修正：使用参数 username
        );

        // 2. 校验逻辑
        if (user == null) {
            return null;
        }

        if (!user.getPassword().equals(password)) {
            return null;
        }

        // 假设 1 代表离职状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            return null;
        }

        // 3. 登录成功，生成 JWT Token
        // 使用我们之前定义的 JwtUtils [基于前文建议]
        String token = JwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());

        // 4. 组装返回数据
        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", token);
        loginResult.put("user", user); // 返回用户信息供前端存储

        return loginResult;
    }

    @Override
    public Result<String> addUser(User user) {
        user.setPassword("123456"); // 设置默认密码
        user.setStatus(0);          // 设置默认在职状态
        int rows = userMapper.insert(user);
        return rows > 0 ? Result.success("SUCCESS") : Result.error("添加失败");
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
        return rows > 0 ? Result.success("SUCCESS") : Result.error("操作失败");
    }

    @Override
    public Result<String> updateRole(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role); //
        int rows = userMapper.updateById(user);
        return rows > 0 ? Result.success("SUCCESS") : Result.error("操作失败");
    }

    @Override
    public Result<String> update(User user) {
        int rows = userMapper.updateById(user); //
        return rows > 0 ? Result.success("信息更新成功") : Result.error("更新失败");
    }
}