package com.work.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.attendance.common.Result;
import com.work.attendance.entity.OvertimeApply;
import com.work.attendance.entity.User;
import com.work.attendance.mapper.OvertimeMapper;
import com.work.attendance.mapper.UserMapper;
import com.work.attendance.service.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OvertimeServiceImpl implements OvertimeService {

    @Autowired
    private OvertimeMapper overtimeMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 实现：员工提交申请
     */
    @Override
    public Result<String> applyOvertime(OvertimeApply apply) {
        // 1. 设置默认状态：0 代表待审核
        apply.setStatus(0);

        // 2. 执行 MyBatis Plus 的插入操作
        int rows = overtimeMapper.insert(apply);

        // 3. 返回处理结果
        return rows > 0 ? Result.success("加班申请已提交，请等待审核") : Result.error("提交失败");
    }

    /**
     * 实现：员工查询个人记录
     */
    @Override
    public Result<List<OvertimeApply>> getMyOvertime(Long userId) {
        // 构造条件：用户ID匹配，且按创建时间倒序排列
        List<OvertimeApply> list = overtimeMapper.selectList(
                new LambdaQueryWrapper<OvertimeApply>()
                        .eq(OvertimeApply::getUserId, userId)
                        .orderByDesc(OvertimeApply::getCreateTime)
        );
        return Result.success(list);
    }

    /**
     * 实现：管理员获取所有申请（带用户信息）
     */
    @Override
    public Result<List<OvertimeApply>> getAllOvertime() {
        // 1. 首先查询所有加班记录
        List<OvertimeApply> list = overtimeMapper.selectList(null);

        // 2. 核心优化：遍历列表，关联 User 表获取申请人姓名
        for (OvertimeApply oa : list) {
            User user = userMapper.selectById(oa.getUserId());
            if (user != null) {
                // 将查询到的 User 对象塞进 OvertimeApply 实体（前提是实体中有 @TableField(exist = false) 的 user 属性）
                oa.setUser(user);
            }
        }

        return Result.success(list);
    }

    /**
     * 实现：管理员审批
     */
    @Override
    public Result<String> auditOvertime(Long id, Integer status) {
        // 1. 查找是否存在该记录
        OvertimeApply apply = overtimeMapper.selectById(id);

        if (apply == null) {
            return Result.error("未找到相关加班记录");
        }

        // 2. 更新状态字段
        apply.setStatus(status);
        int rows = overtimeMapper.updateById(apply);

        return rows > 0 ? Result.success("审核操作已完成") : Result.error("操作失败");
    }
}