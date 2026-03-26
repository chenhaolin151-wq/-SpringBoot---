package com.work.attendance.controller;

import com.work.attendance.common.Result;
import com.work.attendance.entity.User;
import com.work.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService; // 🌟 注入 Service，不再直接注入 Mapper

    @PostMapping("/login")
    public Result<User> login(@RequestBody User loginUser) {
        // 🌟 直接调用 Service 里的标准逻辑
        return userService.login(loginUser);
    }

    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @GetMapping("/all")
    public Result<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @PutMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        return userService.updateStatus(id, status);
    }

    @PutMapping("/updateRole")
    public Result<String> updateRole(@RequestParam Long id, @RequestParam String role) {
        return userService.updateRole(id, role);
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody User user) {
        return userService.update(user);
    }
}