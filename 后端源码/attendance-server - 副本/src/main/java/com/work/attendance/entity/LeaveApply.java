package com.work.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.work.attendance.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("leave_apply")
public class LeaveApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Integer status; // 0待处理 1通过 2拒绝
    private LocalDateTime createTime;
    private String attachment; // 附件地址

    @TableField(exist = false)
    private User user; // 同样留一个空位存用户信息
}