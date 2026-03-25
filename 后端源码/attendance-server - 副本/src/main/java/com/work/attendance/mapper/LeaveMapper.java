package com.work.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.attendance.entity.LeaveApply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeaveMapper extends BaseMapper<LeaveApply> {
}