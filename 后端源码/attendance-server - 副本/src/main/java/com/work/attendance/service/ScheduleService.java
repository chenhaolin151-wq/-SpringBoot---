package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.Schedule;
import com.work.attendance.entity.User;
import java.util.List;

/**
 * 排班管理业务接口
 */
public interface ScheduleService {
    /**
     * 保存或更新排班
     */
    Result<String> saveSchedule(Schedule schedule);

    /**
     * 获取某月的所有排班（含关联的用户和班次信息）
     */
    Result<List<Schedule>> getScheduleList(String month);

    /**
     * 删除单个排班记录
     */
    Result<String> deleteSchedule(Long id);

    /**
     * 按日期批量删除排班
     */
    Result<String> deleteByDate(String date);

    /**
     * 获取可排班的员工列表（过滤掉已离职和当天请假的员工）
     */
    Result<List<User>> getAvailableUsers(String workDate);

    // 个人查询
    Result<List<Schedule>> getMySchedule(Long userId);
}