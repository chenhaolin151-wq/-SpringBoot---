package com.work.attendance.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class AttendanceExcelVO {
    @ExcelProperty("姓名")
    private String realName;

    @ExcelProperty("打卡日期")
    private String punchDate;

    @ExcelProperty("上班时间")
    private String clockIn;

    @ExcelProperty("下班时间")
    private String clockOut;

    @ExcelProperty("状态备注")
    private String remark; // 用于标注“迟到”、“早退”或“正常”
}