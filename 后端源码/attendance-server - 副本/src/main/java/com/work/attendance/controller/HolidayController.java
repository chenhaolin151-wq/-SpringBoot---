package com.work.attendance.controller;
//节假日
import com.work.attendance.common.Result;
import com.work.attendance.entity.HolidayConfig;
import com.work.attendance.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/holiday")
public class HolidayController {
    @Autowired
    private HolidayService holidayService;

    @PostMapping("/save")
    public Result<String> save(@RequestBody HolidayConfig config) {
        return holidayService.saveHoliday(config);
    }

    @GetMapping("/list")
    public Result<List<HolidayConfig>> list(@RequestParam String month) {
        return holidayService.getHolidaysByMonth(month);
    }

    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        return holidayService.deleteHoliday(id);
    }
}