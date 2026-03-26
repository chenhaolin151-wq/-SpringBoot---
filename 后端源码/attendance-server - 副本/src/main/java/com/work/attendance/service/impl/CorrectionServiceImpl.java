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

    @Transactional // 保证申请状态更新与考勤补录的原子性
    @Override
    public Result<String> approve(Long applyId) {
        CorrectionApply apply = correctionMapper.selectById(applyId);
        if (apply == null) return Result.error("申请不存在");

        apply.setStatus(1);
        correctionMapper.updateById(apply);

        // 获取该日期该用户的排班，以确定标准上下班时间
        Schedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getUserId, apply.getUserId())
                .eq(Schedule::getWorkDate, apply.getApplyDate()));

        LocalTime targetInTime = LocalTime.of(9, 0);
        LocalTime targetOutTime = LocalTime.of(18, 0);

        if (schedule != null) {
            WorkShift shift = workShiftMapper.selectById(schedule.getShiftId());
            if (shift != null) {
                targetInTime = LocalTime.parse(shift.getStartTime());
                targetOutTime = LocalTime.parse(shift.getEndTime());
            }
        }

        // 更新考勤表
        AttendanceRecord record = attendanceMapper.selectOne(new LambdaQueryWrapper<AttendanceRecord>()
                .eq(AttendanceRecord::getUserId, apply.getUserId())
                .eq(AttendanceRecord::getPunchDate, apply.getApplyDate()));

        if (record == null) {
            record = new AttendanceRecord();
            record.setUserId(apply.getUserId());
            record.setPunchDate(apply.getApplyDate());
        }

        record.setStatus(0); // 补签后设为正常
        record.setIsCorrection(true);
        if ("IN".equals(apply.getType())) {
            record.setClockIn(LocalDateTime.of(apply.getApplyDate(), targetInTime));
        } else {
            record.setClockOut(LocalDateTime.of(apply.getApplyDate(), targetOutTime));
        }

        if (record.getId() == null) attendanceMapper.insert(record);
        else attendanceMapper.updateById(record);

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