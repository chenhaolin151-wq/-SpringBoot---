package com.work.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.entity.User;
import com.work.attendance.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public Object login(@RequestBody User loginUser) {
        // 1. 根据用户名查询数据库
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginUser.getUsername())
        );

        // 2. 校验逻辑
        if (user == null) {
            return "FAILED: 用户不存在";
        }
        if (!user.getPassword().equals(loginUser.getPassword())) {
            return "FAILED: 密码错误";
        }

        // 假设 1 代表离职
        if (user.getStatus() != null && user.getStatus() == 1) {
            return null;
        }
        // 3. 登录成功，返回用户信息（实际项目会返回 Token，毕设这样写够用了）
        return user;
    }

    @PostMapping("/add")
    public String addUser(@RequestBody User user) {
        // 1. 设置默认密码（比如 123456），建议生产环境进行加密
        user.setPassword("123456");

        // 2. 设置默认状态（比如 0 代表在职）
        user.setStatus(0);

        // 3. 执行插入
        userMapper.insert(user);

        return "SUCCESS";
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        // 使用 MyBatis Plus 直接查询所有用户
        return userMapper.selectList(null);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        // 逻辑：直接调用 MyBatis Plus 的删除方法
        userMapper.deleteById(id);
        return "SUCCESS";
    }

    @PutMapping("/updateStatus")
    public String updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        // 🌟 只更新 status 字段
        userMapper.updateById(user);
        return "SUCCESS";
    }

    @PutMapping("/updateRole")
    public String updateRole(@RequestParam Long id, @RequestParam String role) {
        // 假设你能从 Session 或 Token 拿到当前操作人
        // if (!currentUser.getRole().equals("ADMIN_HR")) return "无权操作";

        User user = new User();
        user.setId(id);
        user.setRole(role);
        userMapper.updateById(user);
        return "SUCCESS";
    }

    @PostMapping("/update")
    public String update(@RequestBody User user) {
        // 🌟 核心：通过 ID 更新用户信息
        // 这里建议使用 Mybatis-Plus 的 updateById
        System.out.println("收到更新请求：" + user);
        int rows = userMapper.updateById(user);
        System.out.println("影响行数：" + rows);
        return rows > 0 ? "SUCCESS" : "ERROR";
    }
}