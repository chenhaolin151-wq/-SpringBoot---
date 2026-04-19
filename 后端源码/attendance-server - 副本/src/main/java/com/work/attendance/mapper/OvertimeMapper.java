package com.work.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.attendance.entity.OvertimeApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OvertimeMapper extends BaseMapper<OvertimeApply> {
    // OvertimeMapper.java
    @Select("SELECT u.real_name as name, SUM(o.duration) as total " +
            "FROM overtime_apply o " +
            "JOIN user u ON o.user_id = u.id " +
            "WHERE o.status = 1 AND DATE_FORMAT(o.overtime_date, '%Y-%m') = #{month} " +
            "GROUP BY o.user_id " +
            "ORDER BY total DESC " +
            "LIMIT 5")
    List<Map<String, Object>> getOvertimeRank(@Param("month") String month);

}