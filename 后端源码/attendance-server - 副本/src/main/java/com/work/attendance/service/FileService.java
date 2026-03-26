package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.AttendanceExcelVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 文件处理业务接口
 */
public interface FileService {

    /**
     * 通用文件上传逻辑（存入本地磁盘）
     * @param file 前端上传的文件对象
     * @return 返回可访问的 URL 地址
     */
    Result<String> uploadFile(MultipartFile file);

    /**
     * 上传并更新用户头像
     * @param file 头像文件
     * @param userId 用户ID
     * @return 最新的头像 URL
     */
    Result<String> uploadAvatar(MultipartFile file, Long userId);

    /**
     * 获取用于 Excel 导出的格式化数据
     * @return 考勤导出视图对象列表
     */
    List<AttendanceExcelVO> getAttendanceExportData();
}