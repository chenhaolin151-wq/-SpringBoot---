package com.work.attendance.controller;
//请假申请与审批
import com.work.attendance.common.Result;
import com.work.attendance.entity.LeaveApply;
import com.work.attendance.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/leave") // 独立的前缀，前端 API 需改为 /api/leave/xxx
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    /**
     * 员工：提交申请
     */
    @PostMapping("/apply")
    public Result<String> applyLeave(@RequestBody LeaveApply leave) {
        return leaveService.applyLeave(leave);
    }

    /**
     * 管理员：查看所有申请
     */
    @GetMapping("/all")
    public Result<List<LeaveApply>> getAllLeaves() {
        return leaveService.getAllLeaves();
    }

    /**
     * 员工：查看个人申请
     */
    @GetMapping("/my")
    public Result<List<LeaveApply>> getMyLeaves(@RequestParam Long userId) {
        return leaveService.getMyLeaves(userId);
    }

    /**
     * 管理员：审核申请
     */
    @PostMapping("/audit")
    public Result<String> auditLeave(@RequestParam Long id, @RequestParam Integer status) {
        return leaveService.auditLeave(id, status);
    }
}