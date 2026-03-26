package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.AttendanceRecord;

import java.util.List;

public interface AttendanceService {
    /**
     * 员工上班打卡业务
     * @param userId 用户ID
     * @param currentIp 当前打卡IP
     * @return 统一返回结果
     */
    // 上班打卡
    Result<String> punchIn(Long userId, String currentIp);

    // 【新增】下班打卡
    Result<String> punchOut(Long userId, String currentIp);

    /**
     * 获取员工个人最近的考勤记录（含虚拟缺勤判定）
     */
    Result<List<AttendanceRecord>> getMyRecords(Long userId);

    /**
     * 管理员获取所有人的考勤记录
     */
    Result<List<AttendanceRecord>> getAllRecords();

    // 获取当前请求的 IP
    Result<String> getCurrentIp(String ip);

    // 更新办公室 IP 配置
    Result<String> updateConfig(String ip);
}