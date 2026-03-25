package com.work.attendance.controller;
//核心考勤（打卡、记录查询）
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.*;
import com.work.attendance.mapper.*;
import com.work.attendance.service.AttendanceService;
import com.work.attendance.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;


@RestController // 告诉程序：这是一个接口，专门接收前端发来的请求
@CrossOrigin
@RequestMapping("/api/attendance") // 这一组接口的共同“暗号”前缀
public class AttendanceController {

    @Autowired // 自动连接：把刚才写的那个“指挥部”给搬过来用
    private AttendanceMapper attendanceMapper;

    @Autowired
    private UserMapper userMapper; // 记得注入 UserMapper

    @Autowired
    private WorkShiftMapper workShiftMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private AttendanceConfigMapper attendanceConfigMapper;

    //打卡
    @Autowired
    private AttendanceService attendanceService; // 注入新服务

    //上班打卡
    @PostMapping("/punchIn")
    public Result<String> punchIn(@RequestParam Long userId, HttpServletRequest request) {
        // 1. 在 Controller 层只负责获取基础信息（如 IP）
        String currentIp = com.work.attendance.utils.IpUtils.getIpAddr(request);

        // 2. 调用业务层处理逻辑
        return attendanceService.punchIn(userId, currentIp);
    }

    //下班打卡
    @PostMapping("/punchOut")
    public Result<String> punchOut(@RequestParam Long userId, HttpServletRequest request) {
        String currentIp = IpUtils.getIpAddr(request);
        return attendanceService.punchOut(userId, currentIp);
    }

    @GetMapping("/myRecords")
    public List<AttendanceRecord> getMyRecords(@RequestParam Long userId) {
        // 1. 先查出该员工最近的排班（比如最近15天），作为“底表”
        List<Schedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getUserId, userId)
                        .le(Schedule::getWorkDate, LocalDate.now()) // 只看今天及以前的
                        .orderByDesc(Schedule::getWorkDate)
                        .last("LIMIT 15")
        );

        List<AttendanceRecord> resultList = new ArrayList<>();

        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                user = new User();
                user.setRealName("已离职员工");
            }

            // 2. 遍历排班，去匹配打卡记录
            for (Schedule schedule : schedules) {
                // 拿着排班的日期去打卡表找记录
                AttendanceRecord record = attendanceMapper.selectOne(
                        new LambdaQueryWrapper<AttendanceRecord>()
                                .eq(AttendanceRecord::getUserId, userId)
                                .eq(AttendanceRecord::getPunchDate, schedule.getWorkDate())
                );

                // 3. 【核心修改】如果打卡记录不存在，手动创建一个“虚拟记录”代表缺勤
                if (record == null) {
                    record = new AttendanceRecord();
                    record.setUserId(userId);
                    // 修改这一行
                    record.setPunchDate(LocalDate.parse(schedule.getWorkDate()));
                    record.setStatus(4); // 约定：状态4表示“缺勤”
                }

                // 4. 无论是否有打卡，都把用户信息和班次要求塞进去
                record.setUser(user);

                // 关联具体的班次时间（9:00 - 18:00）
                WorkShift shift = workShiftMapper.selectById(schedule.getShiftId());
                record.setWorkShift(shift);

                resultList.add(record);
            }
        } catch (Exception e) {
            System.err.println("查询出错：" + e.getMessage());
        }

        // 返回组装好的结果
        return resultList;
    }

    @GetMapping("/allRecords")
    public List<AttendanceRecord> getAllRecords() {
        // 1. 获取所有排班记录（作为底表，比如查最近7天的所有排班）
        List<Schedule> allSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .le(Schedule::getWorkDate, LocalDate.now()) // 只看今天及以前
                        .orderByDesc(Schedule::getWorkDate)
        );

        List<AttendanceRecord> resultList = new ArrayList<>();

        for (Schedule schedule : allSchedules) {
            // 2. 拿着排班去匹配打卡记录
            AttendanceRecord record = attendanceMapper.selectOne(
                    new LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getUserId, schedule.getUserId())
                            .eq(AttendanceRecord::getPunchDate, schedule.getWorkDate())
            );

            // 3. 如果没打卡记录，创建一个“缺勤”虚拟记录
            if (record == null) {
                record = new AttendanceRecord();
                record.setUserId(schedule.getUserId());
                record.setPunchDate(LocalDate.parse(schedule.getWorkDate()));
                record.setStatus(4); // 4 代表缺勤
            }

            // 4. 关联用户信息（处理你之前的“离职员工”逻辑）
            User user = userMapper.selectById(schedule.getUserId());
            if (user != null) {
                record.setUser(user);
            } else {
                User virtualUser = new User();
                virtualUser.setRealName("已离职员工");
                record.setUser(virtualUser);
            }

            // 5. 关联班次信息（让管理员看到他该上什么班）
            WorkShift shift = workShiftMapper.selectById(schedule.getShiftId());
            record.setWorkShift(shift);

            resultList.add(record);
        }

        return resultList;
    }

    // 1. 获取当前访问者的 IP (给管理员看，方便他一键设置)
    @GetMapping("/currentIp")
    public String getCurrentIp(HttpServletRequest request) {
        return com.work.attendance.utils.IpUtils.getIpAddr(request);
    }

    // 2. 更新办公地点 IP 配置
    @PostMapping("/updateConfig")
    public String updateConfig(@RequestParam String ip) {
        AttendanceConfig config = new AttendanceConfig();
        config.setConfigKey("OFFICE_IP");
        config.setConfigValue(ip);

        // 使用 MyBatis-Plus 的 UpdateWrapper 确保更新 Key 为 OFFICE_IP 的那行
        UpdateWrapper<AttendanceConfig> uw = new UpdateWrapper<>();
        uw.eq("config_key", "OFFICE_IP");

        int rows = attendanceConfigMapper.update(config, uw);
        return rows > 0 ? "SUCCESS" : "ERROR";
    }
}