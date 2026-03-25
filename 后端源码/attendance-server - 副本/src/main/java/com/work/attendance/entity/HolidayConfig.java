package com.work.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("holiday_config")
public class HolidayConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate holidayDate;
    private String name;
    private Integer isWorkDay; // 0-放假，1-上班
}