package com.work.attendance.controller;
//补签申请
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.entity.*;
import com.work.attendance.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@CrossOrigin // 加上这个注解，允许跨域访问
@RequestMapping("/api/correction")
public class CorrectionController {

    @Autowired
    private CorrectionMapper correctionMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private WorkShiftMapper workShiftMapper;

    @PostMapping("/apply")
    public String apply(@RequestBody CorrectionApply apply) {
        // 检查条件：同一个用户 + 同一天 + 同一种类型（上班/下班）+ 状态是待审批(0)
        LambdaQueryWrapper<CorrectionApply> qw = new LambdaQueryWrapper<>();
        qw.eq(CorrectionApply::getUserId, apply.getUserId())
                .eq(CorrectionApply::getApplyDate, apply.getApplyDate())
                .eq(CorrectionApply::getType, apply.getType())
                .eq(CorrectionApply::getStatus, 0); // 只拦截还在审批中的

        Long count = correctionMapper.selectCount(qw);
        if (count > 0) {
            // 如果已经存在，直接返回提示信息，不执行下面的 insert
            return "EXISTED";
        }

        // 初始状态设为待审批
        apply.setStatus(0);
        apply.setCreateTime(LocalDateTime.now());
        correctionMapper.insert(apply);
        return "SUCCESS";
    }

    @Transactional // 非常重要！保证两张表要么同时成功，要么同时失败
    @PostMapping("/approve")
    public String approve(@RequestParam Long applyId) {
        // 1. 获取补签申请详情
        CorrectionApply apply = correctionMapper.selectById(applyId);
        if (apply == null) return "申请不存在";

        // 2. 更新申请表状态为“已通过”(1)
        apply.setStatus(1);
        correctionMapper.updateById(apply);

        // 3. 核心：去考勤表 (attendance_record) 里“填坑”
        // 先找那个人那天的考勤记录
        LambdaQueryWrapper<AttendanceRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(AttendanceRecord::getUserId, apply.getUserId())
                .eq(AttendanceRecord::getPunchDate, apply.getApplyDate());
        AttendanceRecord record = attendanceMapper.selectOne(qw);

        // 如果没记录，创建一个新的；如果有记录，在原记录上改
        if (record == null) {
            record = new AttendanceRecord();
            record.setUserId(apply.getUserId());
            record.setPunchDate(apply.getApplyDate());
        }
        // 🌟 核心改进：获取该员工当天的排班及班次时间
        // 这里假设你有一个 ScheduleMapper 可以查询排班
        Schedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getUserId, apply.getUserId())
                .eq(Schedule::getWorkDate, apply.getApplyDate()));

        // 默认备用时间（防止万一没排班）
        LocalTime targetInTime = LocalTime.of(9, 0);
        LocalTime targetOutTime = LocalTime.of(18, 0);

        // 如果找到了排班，就用班次里的准确时间
        if (schedule != null ) {
            WorkShift shift = workShiftMapper.selectById(schedule.getShiftId());
            if (shift != null) {
                // 🌟 查到了！现在可以拿到真正的 08:00 了
                targetInTime = LocalTime.parse(shift.getStartTime());
                targetOutTime = LocalTime.parse(shift.getEndTime());
            }
        }

        // 4. 根据类型补时间（使用动态获取的时间）
        record.setStatus(0); // 补签通过通常设为正常
        record.setIsCorrection(true);
        if ("IN".equals(apply.getType())) {
            record.setClockIn(LocalDateTime.of(apply.getApplyDate(), targetInTime));
        } else {
            record.setClockOut(LocalDateTime.of(apply.getApplyDate(), targetOutTime));
        }

        // 5. 保存或更新
        if (record.getId() == null) {
            attendanceMapper.insert(record);
        } else {
            attendanceMapper.updateById(record);
        }

        return "SUCCESS";
    }

    @GetMapping("/list")
    public List<CorrectionApply> list() {
        // 1. 查询所有申请记录
        List<CorrectionApply> list = correctionMapper.selectList(
                new LambdaQueryWrapper<CorrectionApply>().orderByDesc(CorrectionApply::getCreateTime)
        );

        // 2. 遍历列表，手动关联用户信息（如果你的 Entity 里有 user 字段的话）
        for (CorrectionApply apply : list) {
            User user = userMapper.selectById(apply.getUserId());
            // 假设你在 CorrectionApply 实体类里加了一个 @TableField(exist = false) 的 User 对象
            apply.setUser(user);
        }

        return list;
    }

    @PostMapping("/reject")
    public String reject(@RequestParam Long applyId) {
        // 1. 获取申请详情
        CorrectionApply apply = correctionMapper.selectById(applyId);
        if (apply == null) return "申请不存在";

        // 2. 直接修改状态为 2 (已驳回)
        apply.setStatus(2);
        correctionMapper.updateById(apply);

        return "SUCCESS";
    }
}