package com.work.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.attendance.entity.OvertimeApply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OvertimeMapper extends BaseMapper<OvertimeApply> {
}