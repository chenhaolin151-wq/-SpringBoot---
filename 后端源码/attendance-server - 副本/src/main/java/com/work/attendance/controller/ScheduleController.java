package com.work.attendance.controller;

import com.work.attendance.common.Result;
import com.work.attendance.entity.Schedule;
import com.work.attendance.entity.User;
import com.work.attendance.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/save")
    public Result<String> save(@RequestBody Schedule schedule) { return scheduleService.saveSchedule(schedule); }

    @GetMapping("/list")
    public Result<List<Schedule>> list(@RequestParam String month) { return scheduleService.getScheduleList(month); }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) { return scheduleService.deleteSchedule(id); }

    @PostMapping("/deleteByDate")
    public Result<String> deleteByDate(@RequestParam String date) { return scheduleService.deleteByDate(date); }

    @GetMapping("/availableUsers")
    public Result<List<User>> getAvailable(@RequestParam(required = false) String workDate) {
        return scheduleService.getAvailableUsers(workDate);
    }
}