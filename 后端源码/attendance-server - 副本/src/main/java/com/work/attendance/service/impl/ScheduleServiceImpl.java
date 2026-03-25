package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.LeaveApply;
import com.work.attendance.entity.Schedule;
import com.work.attendance.entity.User;
import com.work.attendance.entity.WorkShift;
import com.work.attendance.mapper.LeaveMapper;
import com.work.attendance.mapper.ScheduleMapper;
import com.work.attendance.mapper.UserMapper;
import com.work.attendance.mapper.WorkShiftMapper;
import com.work.attendance.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private WorkShiftMapper workShiftMapper;
    @Autowired private LeaveMapper leaveMapper;

    @Override
    public Result<String> saveSchedule(Schedule schedule) {
        // 逻辑：如果该员工该日期已排班，则更新；否则插入
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", schedule.getUserId())
                .eq("work_date", schedule.getWorkDate());

        Schedule exist = scheduleMapper.selectOne(queryWrapper);
        if (exist != null) {
            schedule.setId(exist.getId());
            scheduleMapper.updateById(schedule);
        } else {
            scheduleMapper.insert(schedule);
        }
        return Result.success("排班成功");
    }

    /**
     * 核心优化逻辑：通过批量查询减少数据库 IO 压力
     */
    @Override
    public Result<List<Schedule>> getScheduleList(String month) {
        // 1. 获取当月所有排班记录
        List<Schedule> list = scheduleMapper.selectList(new QueryWrapper<Schedule>().like("work_date", month));
        if (list == null || list.isEmpty()) return Result.success(new ArrayList<>());

        // 2. 【性能优化】收集所有相关的 UserID 和 ShiftID
        Set<Long> userIds = list.stream().map(Schedule::getUserId).collect(Collectors.toSet());
        Set<Long> shiftIds = list.stream().map(Schedule::getShiftId).collect(Collectors.toSet());

        // 3. 【性能优化】一次性查出所有涉及的用户和班次信息
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
        Map<Long, WorkShift> shiftMap = workShiftMapper.selectBatchIds(shiftIds).stream()
                .collect(Collectors.toMap(WorkShift::getId, s -> s));

        // 4. 在内存中完成数据装配
        for (Schedule s : list) {
            s.setRealName(userMap.get(s.getUserId()));
            s.setWorkShift(shiftMap.get(s.getShiftId()));
        }
        return Result.success(list);
    }

    @Override
    public Result<String> deleteSchedule(Long id) {
        scheduleMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> deleteByDate(String date) {
        scheduleMapper.delete(new LambdaQueryWrapper<Schedule>().eq(Schedule::getWorkDate, date));
        return Result.success("批量删除成功");
    }

    @Override
    public Result<List<User>> getAvailableUsers(String workDate) {
        // 1. 查找在职员工
        List<User> activeUsers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getStatus, 0));

        // 2. 如果提供了日期，排除掉当天请假的员工
        if (workDate != null) {
            List<Long> leaveUserIds = leaveMapper.selectList(new QueryWrapper<LeaveApply>()
                            .eq("status", 1) // 1=审批通过
                            .le("start_date", workDate)
                            .ge("end_date", workDate))
                    .stream().map(LeaveApply::getUserId).collect(Collectors.toList());

            activeUsers = activeUsers.stream()
                    .filter(u -> !leaveUserIds.contains(u.getId()))
                    .collect(Collectors.toList());
        }
        return Result.success(activeUsers);
    }

    @Override
    public Result<List<Schedule>> getMySchedule(Long userId) {
        // 查询该用户未来 30 天的排班情况
        List<Schedule> list = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getUserId, userId)
                        .ge(Schedule::getWorkDate, LocalDate.now())
                        .orderByAsc(Schedule::getWorkDate)
                        .last("LIMIT 30")
        );
        // 关联班次信息
        for (Schedule s : list) {
            s.setWorkShift(workShiftMapper.selectById(s.getShiftId()));
        }
        return Result.success(list);
    }
}