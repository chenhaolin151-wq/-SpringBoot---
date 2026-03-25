package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.WorkShift;
import java.util.List;

/**
 * 班次定义业务接口
 */
public interface ShiftService {
    /**
     * 获取所有班次列表
     */
    Result<List<WorkShift>> getAllShifts();

    /**
     * 保存或更新班次（包含权限校验）
     */
    Result<String> saveShift(WorkShift workShift, String role);

    /**
     * 删除班次
     */
    Result<String> deleteShift(Long id);
}