package com.work.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("attendance_config")
public class AttendanceConfig {
    @TableId
    private Integer id;
    private String configKey;
    private String configValue;
    private String remark;
}