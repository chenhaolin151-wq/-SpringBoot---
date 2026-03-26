package com.work.attendance.controller;
//班次设置
import com.work.attendance.common.Result;
import com.work.attendance.entity.WorkShift;
import com.work.attendance.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/shift")
public class ShiftController {
    @Autowired
    private ShiftService shiftService;

    @GetMapping("/all")
    public Result<List<WorkShift>> getAll() { return shiftService.getAllShifts(); }

    @PostMapping("/save")
    public Result<String> save(@RequestBody WorkShift shift, @RequestParam String role) {
        return shiftService.saveShift(shift, role);
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) { return shiftService.deleteShift(id); }
}