package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.LeaveApply;
import java.util.List;

/**
 * 请假业务逻辑接口
 */
public interface LeaveService {

    /**
     * 员工提交请假申请
     */
    Result<String> applyLeave(LeaveApply leave);

    /**
     * 管理员获取所有请假申请（包含用户信息）
     */
    Result<List<LeaveApply>> getAllLeaves();

    /**
     * 员工获取自己的请假记录
     */
    Result<List<LeaveApply>> getMyLeaves(Long userId);

    /**
     * 管理员审核请假申请
     */
    Result<String> auditLeave(Long id, Integer status);
}