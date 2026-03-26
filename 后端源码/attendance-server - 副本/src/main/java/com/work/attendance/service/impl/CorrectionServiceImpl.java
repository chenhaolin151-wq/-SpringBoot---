package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.AttendanceRecord;
import com.work.attendance.entity.CorrectionApply;
import com.work.attendance.entity.Schedule;
import com.work.attendance.entity.WorkShift;
import com.work.attendance.mapper.*;
import com.work.attendance.service.CorrectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CorrectionServiceImpl implements CorrectionService {

    @Autowired
    private CorrectionMapper correctionMapper;
    @Autowired private AttendanceMapper attendanceMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private WorkShiftMapper workShiftMapper;

    @Override
    public Result<String> apply(CorrectionApply apply) {
        // 检查是否重复申请
        Long count = correctionMapper.selectCount(new LambdaQueryWrapper<CorrectionApply>()
                .eq(CorrectionApply::getUserId, apply.getUserId())
                .eq(CorrectionApply::getApplyDate, apply.getApplyDate())
                .eq(CorrectionApply::getType, apply.getType())
                .eq(CorrectionApply::getStatus, 0));

        if (count > 0) return Result.error(500, "EXISTED");

        apply.setStatus(0);
        apply.setCreateTime(LocalDateTime.now());
        correctionMapper.insert(apply);
        return Result.success("SUCCESS");
    }

    @Transactional
    @Override
    public Result<String> approve(Long applyId) {
        // 1. 获取申请详情
        CorrectionApply apply = correctionMapper.selectById(applyId);
        if (apply == null) return Result.error("申请不存在");
        if (apply.getStatus() != 0) return Result.error("该申请已处理，请勿重复操作");

        // 2. 获取排班及规定时间
        Schedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getUserId, apply.getUserId())
                .eq(Schedule::getWorkDate, apply.getApplyDate()));

        // 默认时间（保底方案）
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        if (schedule != null) {
            WorkShift shift = workShiftMapper.selectById(schedule.getShiftId());
            if (shift != null) {
                try {
                    startTime = LocalTime.parse(shift.getStartTime());
                    endTime = LocalTime.parse(shift.getEndTime());
                } catch (Exception e) {
                    // 防止数据库时间格式不标准导致崩溃
                    System.err.println("时间格式解析失败，使用默认班次时间");
                }
            }
        }

        // 3. 获取或创建考勤记录
        AttendanceRecord record = attendanceMapper.selectOne(new LambdaQueryWrapper<AttendanceRecord>()
                .eq(AttendanceRecord::getUserId, apply.getUserId())
                .eq(AttendanceRecord::getPunchDate, apply.getApplyDate()));

        if (record == null) {
            record = new AttendanceRecord();
            record.setUserId(apply.getUserId());
            record.setPunchDate(apply.getApplyDate());
        }

        // 4. 更新补签时间及标记
        record.setIsCorrection(true);
        if ("IN".equals(apply.getType())) {
            record.setClockIn(LocalDateTime.of(apply.getApplyDate(), startTime));
        } else {
            record.setClockOut(LocalDateTime.of(apply.getApplyDate(), endTime));
        }

        // 5. 【核心重构】重新判定考勤状态
        // 逻辑：0-正常, 1-迟到, 2-早退, 3-迟到+早退
        int finalStatus = 0;

        // 判定迟到
        if (record.getClockIn() != null && record.getClockIn().toLocalTime().isAfter(startTime)) {
            finalStatus += 1;
        }
        // 判定早退
        if (record.getClockOut() != null && record.getClockOut().toLocalTime().isBefore(endTime)) {
            finalStatus += 2;
        }

        record.setStatus(finalStatus);

        // 6. 保存数据并更新申请状态
        if (record.getId() == null) attendanceMapper.insert(record);
        else attendanceMapper.updateById(record);

        apply.setStatus(1); // 已通过
        correctionMapper.updateById(apply);

        return Result.success("SUCCESS");
    }
    @Override
    public Result<String> reject(Long applyId) {
        CorrectionApply apply = correctionMapper.selectById(applyId);
        if (apply == null) return Result.error("申请不存在");
        apply.setStatus(2);
        correctionMapper.updateById(apply);
        return Result.success("SUCCESS");
    }

    @Override
    public Result<List<CorrectionApply>> listAll() {
        List<CorrectionApply> list = correctionMapper.selectList(new LambdaQueryWrapper<CorrectionApply>()
                .orderByDesc(CorrectionApply::getCreateTime));
        // 关联用户信息展示
        for (CorrectionApply apply : list) {
            apply.setUser(userMapper.selectById(apply.getUserId()));
        }
        return Result.success(list);
    }
}