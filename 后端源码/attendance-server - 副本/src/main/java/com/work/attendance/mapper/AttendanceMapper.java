package com.work.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.attendance.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper // 告诉 Spring：这是一个指挥部，负责去数据库干活
public interface AttendanceMapper extends BaseMapper<AttendanceRecord> {
    // AttendanceMapper.java

    @Select("SELECT punch_date as date, COUNT(*) as count FROM attendance_record " +
            "WHERE status != 4 AND DATE_FORMAT(punch_date, '%Y-%m') = #{month} " +
            "GROUP BY punch_date ORDER BY punch_date")
    List<Map<String, Object>> getMonthlyTrend(@Param("month") String month);

    @Select("SELECT status, COUNT(*) as count FROM attendance_record " +
            "WHERE DATE_FORMAT(punch_date, '%Y-%m') = #{month} " +
            "GROUP BY status")
    List<Map<String, Object>> getStatusDistribution(@Param("month") String month);

    @Select("SELECT work_date as date, COUNT(*) as count FROM schedule " +
            "WHERE DATE_FORMAT(work_date, '%Y-%m') = #{month} " +
            "GROUP BY work_date ORDER BY work_date")
    List<Map<String, Object>> getMonthlyExpectedTrend(@Param("month") String month);

    // AttendanceMapper.java 中添加
    @Select("SELECT s.user_id, s.work_date FROM schedule s " +
            "LEFT JOIN attendance_record ar ON s.user_id = ar.user_id AND s.work_date = ar.punch_date " +
            "WHERE s.work_date = #{date} AND ar.id IS NULL")
    List<Map<String, Object>> findMissingRecords(@Param("date") String date);
}