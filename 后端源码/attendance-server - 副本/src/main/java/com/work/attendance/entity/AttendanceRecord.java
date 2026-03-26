package com.work.attendance.entity;

// 引入工具包
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data // 重点：这个注解由 Lombok 提供，有了它，你就不需要手动写 get/set 方法了，代码很干净
@TableName("attendance_record") // 重点：告诉程序，这个类对应数据库里哪张表
public class AttendanceRecord {

    @TableId(type = IdType.AUTO) // 告诉程序，ID 是自动增长的
    private Long id;

    @TableField("user_id") // 对应数据库 user_id
    private Long userId;     // 员工的身份证号（ID）

    @TableField("punch_date") // 对应数据库 punch_date
    private LocalDate punchDate; // 打卡的日期（比如 2026-03-13）

    private LocalDateTime clockIn; // 具体的打卡时间（比如 08:55:30）

    private Integer status;  // 状态：0代表正常，1代表迟到

    private LocalDateTime clockOut; // 下班打卡时间

    private Boolean isCorrection; // 或者 Integer isCorrection;

    // 在 AttendanceRecord 类中添加
    @TableField(exist = false)
    private User user; // 存放关联的用户信息

    @TableField(exist = false)
    private WorkShift workShift;
}