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
import com.work.attendance.entity.AttendanceStatisticsVO;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.OutputStream;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private OvertimeMapper overtimeMapper;

    /**
     * 校验当前 IP 是否为合法的办公 IP
     *
     * @param currentIp 用户打卡时的实时 IP
     * @return 如果校验失败返回 Result 对象，校验成功返回 null
     */
    private Result<String> validateOfficeIp(String currentIp) {
        String officeIp = attendanceConfigMapper.getValueByKey("OFFICE_IP");
        if (officeIp != null && !officeIp.equals(currentIp)) {
            // 这里的 501 是你约定的特殊状态码，方便前端拦截器或页面逻辑做特殊提示
            return Result.error(501, "IP_LIMIT:" + currentIp);
        }
        return null; // 返回 null 表示校验通过
    }

    //上班打卡
    @Override
    public Result<String> punchIn(Long userId, String currentIp) {
        // 1. 校验 IP
        // 1. 调用提取后的校验逻辑
        Result<String> ipCheck = validateOfficeIp(currentIp);
        if (ipCheck != null) return ipCheck;

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
        // 1. 调用提取后的校验逻辑
        Result<String> ipCheck = validateOfficeIp(currentIp);
        if (ipCheck != null) return ipCheck;

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
        // 1. 获取排班底表（维持原样）
        List<Schedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getUserId, userId)
                        .le(Schedule::getWorkDate, LocalDate.now())
                        .orderByDesc(Schedule::getWorkDate)
                        .last("LIMIT 15")
        );

        if (schedules.isEmpty()) return Result.success(new ArrayList<>());

        // 2. 【优化】提前查出用户信息，避免在循环里重复查
        User user = userMapper.selectById(userId);
        if (user == null) {
            user = new User();
            user.setRealName("已离职员工");
        }

        // 3. 【优化核心】批量获取所有涉及的班次 ID
        List<Long> shiftIds = schedules.stream()
                .map(Schedule::getShiftId)
                .distinct()
                .toList();

        // 一次性查出这些班次，转成 Map <ID, WorkShift实体>
        List<WorkShift> shifts = workShiftMapper.selectBatchIds(shiftIds);
        Map<Long, WorkShift> shiftMap = shifts.stream()
                .collect(Collectors.toMap(WorkShift::getId, s -> s));

        List<AttendanceRecord> resultList = new ArrayList<>();
        for (Schedule schedule : schedules) {
            AttendanceRecord record = attendanceMapper.selectOne(
                    new LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getUserId, userId)
                            .eq(AttendanceRecord::getPunchDate, schedule.getWorkDate())
            );

            if (record == null) {
                record = new AttendanceRecord();
                record.setUserId(userId);
                record.setPunchDate(LocalDate.parse(schedule.getWorkDate()));
                record.setStatus(4);
            }

            // 4. 【优化】从内存/变量中直接设置，不再查库
            record.setUser(user);
            record.setWorkShift(shiftMap.get(schedule.getShiftId())); // 直接从 Map 拿
            resultList.add(record);
        }
        return Result.success(resultList);
    }

    @Override
    public Result<List<AttendanceRecord>> getAllRecords() {
        // 1. 获取所有历史排班记录（底表）
        List<Schedule> allSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .le(Schedule::getWorkDate, LocalDate.now())
                        .orderByDesc(Schedule::getWorkDate)
        );

        if (allSchedules.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 2. 提取所有涉及的 userId 和 shiftId，用于批量查询
        Set<Long> userIds = allSchedules.stream().map(Schedule::getUserId).collect(Collectors.toSet());
        Set<Long> shiftIds = allSchedules.stream().map(Schedule::getShiftId).collect(Collectors.toSet());

        // 3. 批量查询用户并转为 Map <userId, User>
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 4. 批量查询班次并转为 Map <shiftId, WorkShift>
        List<WorkShift> shifts = workShiftMapper.selectBatchIds(shiftIds);
        Map<Long, WorkShift> shiftMap = shifts.stream().collect(Collectors.toMap(WorkShift::getId, s -> s));

        // 5. 【进阶优化】批量查询所有考勤记录，并转为复合 Key Map <"userId_date", AttendanceRecord>
        // 这样连考勤记录的查询也从 N 变成了 1
        List<AttendanceRecord> existingRecords = attendanceMapper.selectList(null);
        Map<String, AttendanceRecord> recordMap = existingRecords.stream()
                .collect(Collectors.toMap(
                        r -> r.getUserId() + "_" + r.getPunchDate(),
                        r -> r,
                        (existing, replacement) -> existing // 避免 Key 重复导致报错
                ));

        List<AttendanceRecord> resultList = new ArrayList<>();
        User resignedUserDefault = new User();
        resignedUserDefault.setRealName("已离职员工");

        // 6. 开始内存装配
        for (Schedule schedule : allSchedules) {
            // 从 Map 中获取考勤记录，匹配不到则视为缺勤
            String key = schedule.getUserId() + "_" + schedule.getWorkDate();
            AttendanceRecord record = recordMap.get(key);

            if (record == null) {
                record = new AttendanceRecord();
                record.setUserId(schedule.getUserId());
                record.setPunchDate(LocalDate.parse(schedule.getWorkDate()));
                record.setStatus(4); // 缺勤状态
            }

            // 关联用户信息（从 Map 获取）
            User user = userMap.get(schedule.getUserId());
            record.setUser(user != null ? user : resignedUserDefault);

            // 关联班次信息（从 Map 获取）
            record.setWorkShift(shiftMap.get(schedule.getShiftId()));

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

    @Override
    public Result<AttendanceStatisticsVO> getStatistics(String month) {
        AttendanceStatisticsVO vo = new AttendanceStatisticsVO();

        // 1. 分别获取“实到”和“应到”的原始统计数据
        List<Map<String, Object>> actualData = attendanceMapper.getMonthlyTrend(month);
        List<Map<String, Object>> expectedData = attendanceMapper.getMonthlyExpectedTrend(month);

        // 2. 将原始数据转为 Map，方便按日期快速查找
        Map<String, Integer> actualMap = new HashMap<>();
        for (Map<String, Object> m : actualData) {
            actualMap.put(m.get("date").toString(), ((Number) m.get("count")).intValue());
        }

        Map<String, Integer> expectedMap = new HashMap<>();
        for (Map<String, Object> m : expectedData) {
            expectedMap.put(m.get("date").toString(), ((Number) m.get("count")).intValue());
        }

        // 3. 【核心优化】合并两边的日期，确保图表横轴包含本月所有涉及的日期
        Set<String> allDateSet = new TreeSet<>(); // 使用 TreeSet 自动按日期排序
        allDateSet.addAll(actualMap.keySet());
        allDateSet.addAll(expectedMap.keySet());

        List<String> dates = new ArrayList<>();
        List<Integer> finalActual = new ArrayList<>();
        List<Integer> finalExpected = new ArrayList<>();

        for (String date : allDateSet) {
            dates.add(date);
            finalActual.add(actualMap.getOrDefault(date, 0));
            finalExpected.add(expectedMap.getOrDefault(date, 0));
        }

        // 4. 将对齐后的三组数据存入 VO
        vo.setDates(dates);
        vo.setCounts(finalActual);
        vo.setExpectedCounts(finalExpected);

        // 5. 获取分布饼图数据（维持原样）
        List<Map<String, Object>> distData = attendanceMapper.getStatusDistribution(month);
        vo.setNormalCount(0);
        vo.setLateCount(0);
        vo.setEarlyCount(0);
        vo.setAbsentCount(0);
        for (Map<String, Object> map : distData) {
            int status = ((Number) map.get("status")).intValue();
            int count = ((Number) map.get("count")).intValue();
            if (status == 0) vo.setNormalCount(count);
            else if (status == 1 || status == 3) vo.setLateCount(vo.getLateCount() + count);
            else if (status == 2 || status == 3) vo.setEarlyCount(vo.getEarlyCount() + count);
            else if (status == 4) vo.setAbsentCount(count);
        }

        // 6. 获取加班排行数据（维持原样）
        List<Map<String, Object>> rankData = overtimeMapper.getOvertimeRank(month);
        List<String> names = new ArrayList<>();
        List<Double> hours = new ArrayList<>();
        for (Map<String, Object> map : rankData) {
            names.add(map.get("name").toString());
            hours.add(Double.valueOf(map.get("total").toString()));
        }
        vo.setEmployeeNames(names);
        vo.setOvertimeHours(hours);

        return Result.success(vo);
    }

    @Override
    public Result<List<AttendanceReportVO>> getMonthlyReport(String month) {
        // 1. 获取该月份所有的排班记录 (底表)
        List<Schedule> schedules = scheduleMapper.selectList(
                new QueryWrapper<Schedule>().apply("DATE_FORMAT(work_date, '%Y-%m') = {0}", month)
        );

        // 2. 获取该月份所有的考勤打卡记录
        List<AttendanceRecord> records = attendanceMapper.selectList(
                new QueryWrapper<AttendanceRecord>().apply("DATE_FORMAT(punch_date, '%Y-%m') = {0}", month)
        );

        // 3. 获取该月份已通过的加班记录
        List<OvertimeApply> otList = overtimeMapper.selectList(
                new QueryWrapper<OvertimeApply>().eq("status", 1).apply("DATE_FORMAT(overtime_date, '%Y-%m') = {0}", month)
        );

        // 4. 获取所有用户 Map
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        // 5. 开始聚合计算
        Map<Long, AttendanceReportVO> reportMap = new HashMap<>();

        // 初始化报表行（基于排班）
        for (Schedule s : schedules) {
            reportMap.putIfAbsent(s.getUserId(), new AttendanceReportVO());
            AttendanceReportVO vo = reportMap.get(s.getUserId());
            vo.setUserId(s.getUserId());
            vo.setRealName(userMap.getOrDefault(s.getUserId(), "未知"));
            vo.setTotalDays(vo.getTotalDays() == null ? 1 : vo.getTotalDays() + 1);
            // 初始化其他数值为 0
            if (vo.getNormalDays() == null) {
                vo.setNormalDays(0);
                vo.setLateCount(0);
                vo.setEarlyCount(0);
                vo.setAbsentDays(0);
                vo.setOvertimeHours(0.0);
            }
        }

        // 统计考勤状态
        for (AttendanceRecord r : records) {
            AttendanceReportVO vo = reportMap.get(r.getUserId());
            if (vo == null) continue;

            if (r.getStatus() == 0) vo.setNormalDays(vo.getNormalDays() + 1);
            else if (r.getStatus() == 1) vo.setLateCount(vo.getLateCount() + 1);
            else if (r.getStatus() == 2) vo.setEarlyCount(vo.getEarlyCount() + 1);
            else if (r.getStatus() == 3) {
                vo.setLateCount(vo.getLateCount() + 1);
                vo.setEarlyCount(vo.getEarlyCount() + 1);
            } else if (r.getStatus() == 4) vo.setAbsentDays(vo.getAbsentDays() + 1);
        }

        // 统计加班时长
        for (OvertimeApply ot : otList) {
            AttendanceReportVO vo = reportMap.get(ot.getUserId());
            if (vo != null) vo.setOvertimeHours(vo.getOvertimeHours() + ot.getDuration());
        }

        return Result.success(new ArrayList<>(reportMap.values()));
    }


    @Override
    @Transactional // 🌟 加上事务，保证批量插入的原子性
    public void autoCheckAbsence(String month) {
        System.out.println("==== 开始执行 [" + month + "] 缺勤数据补全 ====");
        try {
            List<Map<String, Object>> missingList = attendanceMapper.findMonthlyMissingRecords(month);
            System.out.println("检测到该月累计缺勤记录数：" + (missingList == null ? 0 : missingList.size()));

            if (missingList != null && !missingList.isEmpty()) {
                int count = 0;
                for (Map<String, Object> map : missingList) {
                    Long uId = Long.valueOf(map.getOrDefault("userId", map.get("user_id")).toString());
                    String wDate = map.getOrDefault("workDate", map.get("work_date")).toString();

                    AttendanceRecord record = new AttendanceRecord();
                    record.setUserId(uId);
                    record.setPunchDate(LocalDate.parse(wDate.substring(0, 10)));
                    record.setStatus(4); // 缺勤
                    attendanceMapper.insert(record);
                    count++;
                }
                System.out.println("成功补录 " + count + " 条记录。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 供定时任务调用的无参方法（保持每天凌晨自动跑当前月）
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCheckCurrentMonth() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        this.autoCheckAbsence(currentMonth);
    }

    @Override
    public void exportMonthlyReport(String month, OutputStream outputStream) {
        // 1. 复用你已有的逻辑获取报表数据
        Result<List<AttendanceReportVO>> reportResult = this.getMonthlyReport(month);
        List<AttendanceReportVO> data = reportResult.getData();

        // 2. 使用 EasyExcel 将数据写入流
        // 注意：需要在 AttendanceReportVO 类上添加 @ExcelProperty 注解来映射表头
        com.alibaba.excel.EasyExcel.write(outputStream, AttendanceReportVO.class)
                .sheet("月度报表-" + month)
                .doWrite(data);
    }
}

