package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.LeaveApply;
import com.work.attendance.entity.User;
import com.work.attendance.mapper.LeaveMapper;
import com.work.attendance.mapper.UserMapper;
import com.work.attendance.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveMapper leaveMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 员工：提交请假申请
     */
    @Override
    public Result<String> applyLeave(LeaveApply leave) {
        // 1. 设置初始状态：0 代表待审批
        leave.setStatus(0);

        // 2. 插入数据库
        int rows = leaveMapper.insert(leave);

        // 3. 根据影响行数判断是否成功
        if (rows > 0) {
            return Result.success("申请已提交，请等待管理员审批");
        } else {
            return Result.error("提交失败，请联系管理员");
        }
    }

    /**
     * 管理员：获取所有人的请假列表
     */
    @Override
    public Result<List<LeaveApply>> getAllLeaves() {
        // 1. 查询所有请假记录
        List<LeaveApply> list = leaveMapper.selectList(null);

        // 2. 关联用户信息：循环遍历请假单，把对应的真实姓名等信息塞进去
        for (LeaveApply item : list) {
            User user = userMapper.selectById(item.getUserId());
            if (user != null) {
                item.setUser(user); // 确保 LeaveApply 实体类里有 User 属性并标记为非数据库字段
            }
        }

        return Result.success(list);
    }

    /**
     * 员工：查看我自己的请假历史
     */
    @Override
    public Result<List<LeaveApply>> getMyLeaves(Long userId) {
        // 使用 LambdaQueryWrapper 构造带条件的查询，按创建时间倒序排列
        List<LeaveApply> list = leaveMapper.selectList(
                new LambdaQueryWrapper<LeaveApply>()
                        .eq(LeaveApply::getUserId, userId)
                        .orderByDesc(LeaveApply::getCreateTime)
        );
        return Result.success(list);
    }

    /**
     * 管理员：审批请假
     */
    @Override
    public Result<String> auditLeave(Long id, Integer status) {
        // 1. 先查出这条申请记录
        LeaveApply leave = leaveMapper.selectById(id);

        if (leave == null) {
            return Result.error("该请假记录不存在");
        }

        // 2. 更新状态（1-批准，2-拒绝）
        leave.setStatus(status);
        int rows = leaveMapper.updateById(leave);

        if (rows > 0) {
            String msg = (status == 1) ? "已批准该申请" : "已拒绝该申请";
            return Result.success(msg);
        } else {
            return Result.error("审批操作失败");
        }
    }
}