package com.work.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.work.attendance.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data // 自动生成 Getter/Setter
@TableName("overtime_apply") // 告诉 MyBatis-Plus 对应数据库的哪张表
public class OvertimeApply {

    @TableId(type = IdType.AUTO) // 声明这是主键，且是自增的
    private Long id;

    private Long userId;

    private String overtimeDate; // 加班日期

    private Double duration; // 加班时长

    private String reason; // 加班原因

    private Integer status; // 0待审批 1已通过 2已拒绝

    private LocalDateTime createTime; // 申请创建时间

    // 这个字段在数据库表里没有，是专门给前端显示姓名用的
    @TableField(exist = false) // 告诉程序：这个字段在数据库表里不存在，别去表里找它
    private User user;
}