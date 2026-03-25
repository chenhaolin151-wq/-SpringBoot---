package com.work.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("work_shift")
public class WorkShift {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String shiftName; // 班次名称
    private String startTime; // 上班时间，数据库是 Time，Java 用 String 接收比较方便
    private String endTime;   // 下班时间
}