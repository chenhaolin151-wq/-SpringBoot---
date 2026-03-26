package com.work.attendance.controller;
//核心考勤（打卡、记录查询）

import com.work.attendance.common.Result;
import com.work.attendance.entity.*;
import com.work.attendance.mapper.*;
import com.work.attendance.service.AttendanceService;
import com.work.attendance.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;


@RestController // 告诉程序：这是一个接口，专门接收前端发来的请求
@CrossOrigin
@RequestMapping("/api/attendance") // 这一组接口的共同“暗号”前缀
public class AttendanceController {

    @Autowired // 自动连接：把刚才写的那个“指挥部”给搬过来用
    private AttendanceMapper attendanceMapper;

    @Autowired
    private UserMapper userMapper; // 记得注入 UserMapper

    @Autowired
    private WorkShiftMapper workShiftMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private AttendanceConfigMapper attendanceConfigMapper;

    //打卡
    @Autowired
    private AttendanceService attendanceService; // 注入新服务

    //上班打卡
    @PostMapping("/punchIn")
    public Result<String> punchIn(@RequestParam Long userId, HttpServletRequest request) {
        // 1. 在 Controller 层只负责获取基础信息（如 IP）
        String currentIp = com.work.attendance.utils.IpUtils.getIpAddr(request);

        // 2. 调用业务层处理逻辑
        return attendanceService.punchIn(userId, currentIp);
    }

    //下班打卡
    @PostMapping("/punchOut")
    public Result<String> punchOut(@RequestParam Long userId, HttpServletRequest request) {
        String currentIp = IpUtils.getIpAddr(request);
        return attendanceService.punchOut(userId, currentIp);
    }

    /**
     * 获取个人记录：现在只负责接收参数并转发给 Service
     */
    @GetMapping("/myRecords")
    public Result<List<AttendanceRecord>> getMyRecords(@RequestParam Long userId) { // 🌟 修改返回类型
        return attendanceService.getMyRecords(userId); // 🌟 直接调用 Service
    }

    /**
     * 获取全员记录：现在只负责转发
     */
    @GetMapping("/allRecords")
    public Result<List<AttendanceRecord>> getAllRecords() { // 🌟 修改返回类型
        return attendanceService.getAllRecords(); // 🌟 直接调用 Service
    }

    @GetMapping("/currentIp")
    public Result<String> getCurrentIp(HttpServletRequest request) {
        // Controller 只负责从 request 中提取最原始的信息
        String ip = com.work.attendance.utils.IpUtils.getIpAddr(request);
        return attendanceService.getCurrentIp(ip);
    }

    @PostMapping("/updateConfig")
    public Result<String> updateConfig(@RequestParam String ip) {
        return attendanceService.updateConfig(ip);
    }
}
