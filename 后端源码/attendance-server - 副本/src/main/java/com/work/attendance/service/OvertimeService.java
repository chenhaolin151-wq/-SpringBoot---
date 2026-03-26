package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.OvertimeApply;
import java.util.List;

/**
 * 加班业务逻辑接口
 */
public interface OvertimeService {

    /**
     * 员工：提交加班申请
     * @param apply 加班申请实体
     * @return 统一封装的执行结果
     */
    Result<String> applyOvertime(OvertimeApply apply);

    /**
     * 员工：查看个人的所有加班记录
     * @param userId 员工ID
     * @return 该员工的加班历史列表
     */
    Result<List<OvertimeApply>> getMyOvertime(Long userId);

    /**
     * 管理员：获取全公司的加班申请（用于审批界面）
     * @return 包含用户信息的加班申请列表
     */
    Result<List<OvertimeApply>> getAllOvertime();

    /**
     * 管理员：审核员工的加班申请
     * @param id 申请记录主键ID
     * @param status 目标状态（如：1-已批准，2-已拒绝）
     * @return 审核结果反馈
     */
    Result<String> auditOvertime(Long id, Integer status);
}