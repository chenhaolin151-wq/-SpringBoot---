package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.HolidayConfig;
import java.util.List;

public interface HolidayService {
    /**
     * 保存或修改节假日（若名称为空则视为删除）
     */
    Result<String> saveHoliday(HolidayConfig config);

    /**
     * 获取某月的所有节假日
     */
    Result<List<HolidayConfig>> getHolidaysByMonth(String month);

    /**
     * 删除特定节假日
     */
    Result<String> deleteHoliday(Long id);
}