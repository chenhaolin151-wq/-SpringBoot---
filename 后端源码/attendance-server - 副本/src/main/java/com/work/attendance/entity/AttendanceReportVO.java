package com.work.attendance.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class AttendanceReportVO {
    private Long userId;
    @ExcelProperty("员工姓名")
    private String realName;     // 员工姓名

    @ExcelProperty("应出勤(天)")
    private Integer totalDays;   // 应当出勤天数（排班天数）

    @ExcelProperty("正常出勤(天)")
    private Integer normalDays;  // 正常出勤天数

    @ExcelProperty("迟到次数")
    private Integer lateCount;   // 迟到次数

    @ExcelProperty("早退次数")
    private Integer earlyCount;  // 早退次数

    @ExcelProperty("缺勤天数")
    private Integer absentDays;  // 缺勤天数

    @ExcelProperty("加班时长(小时)")
    private Double overtimeHours;// 加班总时长
}