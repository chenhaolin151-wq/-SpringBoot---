package com.work.attendance.entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AttendanceStatisticsVO {
    // 1. 考勤趋势图数据
    private List<String> dates;      // 日期列表
    private List<Integer> counts;    // 对应日期的正常出勤人数
    private List<Integer> expectedCounts; // 新增：应当到岗人数

    // 2. 状态分布饼图数据
    private Integer normalCount;     // 正常人数
    private Integer lateCount;       // 迟到人数
    private Integer earlyCount;      // 早退人数
    private Integer absentCount;     // 缺勤人数

    // 3. 加班排行榜数据
    private List<String> employeeNames; // 员工姓名列表
    private List<Double> overtimeHours; // 对应的总加班时长
}