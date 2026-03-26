package com.work.attendance.service.impl;

import com.work.attendance.common.Result;
import com.work.attendance.entity.WorkShift;
import com.work.attendance.mapper.WorkShiftMapper;
import com.work.attendance.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private WorkShiftMapper workShiftMapper;

    @Override
    public Result<List<WorkShift>> getAllShifts() {
        return Result.success(workShiftMapper.selectList(null));
    }

    @Override
    public Result<String> saveShift(WorkShift workShift, String role) {
        // 权限硬编码校验：只有 ADMIN_HR 角色能操作
        if (!"ADMIN_HR".equals(role)) {
            return Result.error("权限不足，无法修改班次定义");
        }

        if (workShift.getId() == null) {
            workShiftMapper.insert(workShift); // 新增
        } else {
            workShiftMapper.updateById(workShift); // 修改
        }
        return Result.success("班次保存成功");
    }

    @Override
    public Result<String> deleteShift(Long id) {
        workShiftMapper.deleteById(id);
        return Result.success("班次删除成功");
    }
}