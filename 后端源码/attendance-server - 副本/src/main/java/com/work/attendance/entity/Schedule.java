package com.work.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;     // 员工ID
    private String workDate; // 日期，如 2026-03-16
    private Long shiftId;    // 关联班次的ID

    // --- 毕设加分点：关联显示 ---
    @TableField(exist = false)
    private String realName;  // 员工姓名（不需要存数据库，方便前端显示）
    @TableField(exist = false)
    private WorkShift workShift; // 班次详细信息
}