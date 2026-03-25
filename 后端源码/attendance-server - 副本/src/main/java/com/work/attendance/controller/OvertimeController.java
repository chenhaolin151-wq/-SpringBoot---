package com.work.attendance.controller;

import com.work.attendance.common.Result;
import com.work.attendance.entity.OvertimeApply;
import com.work.attendance.service.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/overtime") // 模块独立前缀
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;

    /**
     * 接口：提交申请
     */
    @PostMapping("/apply")
    public Result<String> applyOvertime(@RequestBody OvertimeApply apply) {
        return overtimeService.applyOvertime(apply);
    }

    /**
     * 接口：查询个人加班
     */
    @GetMapping("/my")
    public Result<List<OvertimeApply>> getMyOvertime(@RequestParam Long userId) {
        return overtimeService.getMyOvertime(userId);
    }

    /**
     * 接口：管理员获取全部
     */
    @GetMapping("/all")
    public Result<List<OvertimeApply>> getAllOvertime() {
        return overtimeService.getAllOvertime();
    }

    /**
     * 接口：审批加班
     */
    @PostMapping("/audit")
    public Result<String> auditOvertime(@RequestParam Long id, @RequestParam Integer status) {
        return overtimeService.auditOvertime(id, status);
    }
}