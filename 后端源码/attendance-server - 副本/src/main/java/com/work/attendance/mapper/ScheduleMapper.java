package com.work.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.attendance.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
}