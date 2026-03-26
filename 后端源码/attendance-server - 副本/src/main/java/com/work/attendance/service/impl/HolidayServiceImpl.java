package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.HolidayConfig;
import com.work.attendance.mapper.HolidayMapper;
import com.work.attendance.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayMapper holidayMapper;

    @Override
    public Result<String> saveHoliday(HolidayConfig config) {
        // 如果前端传来的名称为空，执行删除逻辑
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            holidayMapper.delete(new LambdaQueryWrapper<HolidayConfig>()
                    .eq(HolidayConfig::getHolidayDate, config.getHolidayDate()));
            return Result.success("SUCCESS");
        }

        // 检查该日期是否已存在配置
        HolidayConfig exists = holidayMapper.selectOne(new LambdaQueryWrapper<HolidayConfig>()
                .eq(HolidayConfig::getHolidayDate, config.getHolidayDate()));

        if (exists != null) {
            config.setId(exists.getId());
            holidayMapper.updateById(config); // 存在则更新
        } else {
            holidayMapper.insert(config); // 不存在则插入
        }
        return Result.success("SUCCESS");
    }

    @Override
    public Result<List<HolidayConfig>> getHolidaysByMonth(String month) {
        // 使用 likeRight 匹配月份字符串（如 "2026-03"）
        List<HolidayConfig> list = holidayMapper.selectList(new LambdaQueryWrapper<HolidayConfig>()
                .likeRight(HolidayConfig::getHolidayDate, month));
        return Result.success(list);
    }

    @Override
    public Result<String> deleteHoliday(Long id) {
        holidayMapper.deleteById(id); //
        return Result.success("SUCCESS");
    }
}