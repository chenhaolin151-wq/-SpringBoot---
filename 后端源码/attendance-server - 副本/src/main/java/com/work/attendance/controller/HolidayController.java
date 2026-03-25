package com.work.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.entity.HolidayConfig;
import com.work.attendance.mapper.HolidayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/holiday")
public class HolidayController {

    @Autowired
    private HolidayMapper holidayMapper;

    // 1. 保存或修改节假日设定
    @PostMapping("/save")
    public String save(@RequestBody HolidayConfig config) {
        // 1. 核心改进：逻辑判断 —— 如果前端传来的名称为空，直接执行删除并返回
        if (config.getName() == null || config.getName().trim().equals("")) {
            LambdaQueryWrapper<HolidayConfig> deleteQw = new LambdaQueryWrapper<>();
            deleteQw.eq(HolidayConfig::getHolidayDate, config.getHolidayDate());
            holidayMapper.delete(deleteQw);
            return "SUCCESS";
        }

        // 2. 原有逻辑：先检查该日期是否已经存在
        LambdaQueryWrapper<HolidayConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(HolidayConfig::getHolidayDate, config.getHolidayDate());
        HolidayConfig exists = holidayMapper.selectOne(qw);

        if (exists != null) {
            // 存在就更新（比如把“放假”改成“春节”）
            config.setId(exists.getId());
            holidayMapper.updateById(config);
        } else {
            // 不存在就插入新记录
            holidayMapper.insert(config);
        }
        return "SUCCESS";
    }

    // 2. 获取指定月份的所有节假日设定
    @GetMapping("/list")
    public List<HolidayConfig> list(@RequestParam String month) {
        // month 格式如 "2026-03"
        return holidayMapper.selectList(new LambdaQueryWrapper<HolidayConfig>()
                .likeRight(HolidayConfig::getHolidayDate, month));
    }

    // 3. 删除设定（变回普通日期）
    @DeleteMapping("/delete")
    public String delete(@RequestParam Long id) {
        holidayMapper.deleteById(id);
        return "SUCCESS";
    }
}