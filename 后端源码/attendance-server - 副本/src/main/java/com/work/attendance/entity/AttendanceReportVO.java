package com.work.attendance.entity;

import lombok.Data;

@Data
public class AttendanceReportVO {
    private Long userId;
    private String realName;     // 员工姓名
    private Integer totalDays;   // 应当出勤天数（排班天数）
    private Integer normalDays;  // 正常出勤天数
    private Integer lateCount;   // 迟到次数
    private Integer earlyCount;  // 早退次数
    private Integer absentDays;  // 缺勤天数
    private Double overtimeHours;// 加班总时长
}