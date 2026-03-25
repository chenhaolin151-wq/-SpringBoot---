package com.work.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.attendance.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 告诉 Spring：这是一个指挥部，负责去数据库干活
public interface AttendanceMapper extends BaseMapper<AttendanceRecord> {
    // 这里竟然是空的？
    // 没错！因为我们继承了 BaseMapper，MyBatis Plus 会自动帮我们写好：
    // 插入(insert)、删除(delete)、修改(update)、查询(select) 等所有基础功能。
}