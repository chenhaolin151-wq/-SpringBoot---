package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.*;
import com.work.attendance.mapper.*;
import com.work.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private AttendanceConfigMapper attendanceConfigMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private WorkShiftMapper workShiftMapper;
    @Autowired
    private UserMapper userMapper;


    //上班打卡
    @Override
    public Result<String> punchIn(Long userId, String currentIp) {
        // 1. 校验 IP
        String officeIp = attendanceConfigMapper.getValueByKey("OFFICE_IP");
        if (officeIp != null && !officeIp.equals(currentIp)) {
            return Result.error(501, "IP_LIMIT:" + currentIp); // 这里的 501 方便前端特殊处理
        }

        // 2. 准备时间数据
        LocalDate todayDate = LocalDate.now();
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalTime nowTime = LocalTime.now();

        // 3. 检查排班
        QueryWrapper<Schedule> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("work_date", todayDate.toString());
        Schedule schedule = scheduleMapper.selectOne(query);

        if (schedule == null) {
            return Result.error("今日未给你排班，无需打卡");
        }

        // 4. 检查是否重复打卡
        AttendanceRecord existing = attendanceMapper.selectOne(
                new LambdaQueryWrapper<AttendanceRecord>()
                        .eq(AttendanceRecord::getUserId, userId)
                        .eq(AttendanceRecord::getPunchDate, todayDate)
        );
        if (existing != null) {
            return Result.error("您今天已经打过上班卡了，无需重复打卡！");
        }

        // 5. 判定状态（迟到/正常）
        WorkShift shift = workShiftMapper.selectById(schedule.getShiftId());
        LocalTime startTime = LocalTime.parse(shift.getStartTime());
        Integer statusCode = nowTime.isAfter(startTime) ? 1 : 0;
        String statusText = (statusCode == 1) ? "迟到" : "正常";

        // 6. 存入数据库
        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(userId);
        record.setPunchDate(todayDate);
        record.setClockIn(nowDateTime);
        record.setStatus(statusCode);
        attendanceMapper.insert(record);

        return Result.success("打卡成功！状态：" + statusText + "（规定时间：" + shift.getStartTime() + "）");
    }

    // 在 AttendanceServiceImpl 类中添加以下方法实现下班打卡
    @Override
    public Result<String> punchOut(Long userId, String currentIp) {
        // 1. IP 检查
        String officeIp = attendanceConfigMapper.getValueByKey("OFFICE_IP");
        if (officeIp != null && !officeIp.equals(currentIp)) {
            return Result.error(501, "IP_LIMIT:" + currentIp);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalTime nowTime = LocalTime.now();

        // 2. 查找今日上班打卡记录
        AttendanceRecord record = attendanceMapper.selectOne(
                new LambdaQueryWrapper<AttendanceRecord>()
                        .eq(AttendanceRecord::getUserId, userId)
                        .eq(AttendanceRecord::getPunchDate, today)
        );

        if (record == null) {
            return Result.error("错误：你今天还没有上班打卡，请先进行上班打卡！");
        }

        if (record.getClockOut() != null) {
            return Result.error("你今天已经打过下班卡了，请勿重复操作。");
        }

        // 3. 判定早退逻辑
        // 查找排班计划
        QueryWrapper<Schedule> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("work_date", today.toString());
        Schedule schedule = scheduleMapper.selectOne(query);

        if (schedule != null) {
            // 获取对应班次的规定下班时间
            WorkShift shift = workShiftMapper.selectById(schedule.getShiftId());
            LocalTime endTime = LocalTime.parse(shift.getEndTime());

            // 如果当前时间早于规定下班时间，更新考勤状态
            if (nowTime.isBefore(endTime)) {
                // 状态转换：0(正常)->2(早退)，1(迟到)->3(迟到+早退)
                if (record.getStatus() == 0) {
                    record.setStatus(2);
                } else if (record.getStatus() == 1) {
                    record.setStatus(3);
                }
            }
        }

        // 4. 更新下班打卡数据
        record.setClockOut(nowDateTime);
        attendanceMapper.updateById(record);

        return Result.success("下班打卡成功！打卡时间：" + nowTime.toString().substring(0, 5));
    }

    @Override
    public Result<List<AttendanceRecord>> getMyRecords(Long userId) {
        // 1. 获取该员工最近15天的排班作为“底表”
        List<Schedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getUserId, userId)
                        .le(Schedule::getWorkDate, LocalDate.now())
                        .orderByDesc(Schedule::getWorkDate)
                        .last("LIMIT 15")
        );

        List<AttendanceRecord> resultList = new ArrayList<>();
        User user = userMapper.selectById(userId);
        if (user == null) {
            user = new User();
            user.setRealName("已离职员工");
        }

        // 2. 遍历排班，匹配真实打卡记录
        for (Schedule schedule : schedules) {
            AttendanceRecord record = attendanceMapper.selectOne(
                    new LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getUserId, userId)
                            .eq(AttendanceRecord::getPunchDate, schedule.getWorkDate())
            );

            // 3. 如果没有打卡记录，手动创建一个“虚拟记录”代表缺勤 (状态4)
            if (record == null) {
                record = new AttendanceRecord();
                record.setUserId(userId);
                record.setPunchDate(LocalDate.parse(schedule.getWorkDate()));
                record.setStatus(4);
            }

            // 4. 关联用户和班次要求
            record.setUser(user);
            record.setWorkShift(workShiftMapper.selectById(schedule.getShiftId()));
            resultList.add(record);
        }
        return Result.success(resultList);
    }

    @Override
    public Result<List<AttendanceRecord>> getAllRecords() {
        // 1. 获取所有历史排班记录
        List<Schedule> allSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .le(Schedule::getWorkDate, LocalDate.now())
                        .orderByDesc(Schedule::getWorkDate)
        );

        List<AttendanceRecord> resultList = new ArrayList<>();
        for (Schedule schedule : allSchedules) {
            // 2. 匹配打卡记录
            AttendanceRecord record = attendanceMapper.selectOne(
                    new LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getUserId, schedule.getUserId())
                            .eq(AttendanceRecord::getPunchDate, schedule.getWorkDate())
            );

            if (record == null) {
                record = new AttendanceRecord();
                record.setUserId(schedule.getUserId());
                record.setPunchDate(LocalDate.parse(schedule.getWorkDate()));
                record.setStatus(4);
            }

            // 3. 关联用户信息（处理离职人员逻辑）
            User user = userMapper.selectById(schedule.getUserId());
            if (user != null) {
                record.setUser(user);
            } else {
                User virtualUser = new User();
                virtualUser.setRealName("已离职员工");
                record.setUser(virtualUser);
            }

            record.setWorkShift(workShiftMapper.selectById(schedule.getShiftId()));
            resultList.add(record);
        }
        return Result.success(resultList);
    }

    @Override
    public Result<String> getCurrentIp(String ip) {
        // 逻辑极其简单，但为了格式统一，我们依然在 Service 中处理
        return Result.success(ip);
    }

    @Override
    public Result<String> updateConfig(String ip) {
        AttendanceConfig config = new AttendanceConfig();
        config.setConfigKey("OFFICE_IP");
        config.setConfigValue(ip);

        UpdateWrapper<AttendanceConfig> uw = new UpdateWrapper<>();
        uw.eq("config_key", "OFFICE_IP");

        int rows = attendanceConfigMapper.update(config, uw);
        return rows > 0 ? Result.success("更新成功") : Result.error("配置更新失败");
    }
}