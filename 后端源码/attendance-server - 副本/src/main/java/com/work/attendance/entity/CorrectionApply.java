package com.work.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("correction_apply")
public class CorrectionApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate applyDate;
    private String type; // IN 或 OUT
    private String reason;
    private Integer status; // 0-待审批, 1-已通过, 2-已驳回
    private LocalDateTime createTime;

    @TableField(exist = false) // 告诉程序：这个字段在数据库表里不存在，别去表里找它
    private User user;
}