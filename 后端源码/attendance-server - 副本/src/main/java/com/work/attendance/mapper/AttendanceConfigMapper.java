package com.work.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.attendance.entity.AttendanceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AttendanceConfigMapper extends BaseMapper<AttendanceConfig> {

    // 🌟 方便起见，直接写个 SQL 根据 key 查 value
    @Select("SELECT config_value FROM attendance_config WHERE config_key = #{key} LIMIT 1")
    String getValueByKey(String key);
}