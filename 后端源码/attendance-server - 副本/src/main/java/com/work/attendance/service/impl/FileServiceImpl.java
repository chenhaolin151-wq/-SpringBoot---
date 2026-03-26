package com.work.attendance.service.impl;

import com.work.attendance.common.Result;
import com.work.attendance.entity.AttendanceExcelVO;
import com.work.attendance.entity.AttendanceRecord;
import com.work.attendance.entity.User;
import com.work.attendance.mapper.AttendanceMapper;
import com.work.attendance.mapper.UserMapper;
import com.work.attendance.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    @Autowired private AttendanceMapper attendanceMapper;
    @Autowired private UserMapper userMapper;

    // 统一定义文件存储根目录（毕设建议 D 盘，生产环境建议配置在 yml 中）
    private static final String BASE_PATH = "D:/uploads/";
    private static final String SERVER_URL = "http://localhost:8081/files/";

    @Override
    public Result<String> uploadFile(MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");

        try {
            // 1. 生成唯一文件名，防止重名覆盖
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(BASE_PATH + fileName);

            // 2. 检查并创建目录
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

            // 3. 写入磁盘
            file.transferTo(dest);

            // 4. 返回拼接后的访问路径
            return Result.success(SERVER_URL + fileName);
        } catch (IOException e) {
            return Result.error("文件保存失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> uploadAvatar(MultipartFile file, Long userId) {
        // 1. 先调用上面的通用上传逻辑
        Result<String> uploadResult = uploadFile(file);
        if (uploadResult.getCode() != 200) return uploadResult;

        String avatarUrl = uploadResult.getData();

        // 2. 将 URL 更新到用户表
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setAvatar(avatarUrl);
            userMapper.updateById(user);
            return Result.success(avatarUrl);
        }
        return Result.error("用户不存在，无法更新头像");
    }

    @Override
    public List<AttendanceExcelVO> getAttendanceExportData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // 1. 获取数据库原始考勤记录
        List<AttendanceRecord> records = attendanceMapper.selectList(null);
        List<AttendanceExcelVO> exportList = new ArrayList<>();

        // 2. 转换为 Excel 专用 VO 对象
        for (AttendanceRecord r : records) {
            User user = userMapper.selectById(r.getUserId());
            AttendanceExcelVO vo = new AttendanceExcelVO();

            vo.setRealName(user != null ? user.getRealName() : "未知用户");
            vo.setPunchDate(r.getPunchDate().toString());
            vo.setClockIn(r.getClockIn() != null ? r.getClockIn().format(formatter) : "--");
            vo.setClockOut(r.getClockOut() != null ? r.getClockOut().format(formatter) : "--");

            // 考勤状态判定逻辑
            StringBuilder status = new StringBuilder();
            if (r.getClockIn() != null && r.getClockIn().format(formatter).compareTo("09:00:00") > 0) status.append("迟到 ");
            if (r.getClockOut() != null && r.getClockOut().format(formatter).compareTo("17:00:00") < 0) status.append("早退 ");
            vo.setRemark(status.length() == 0 ? "正常" : status.toString());

            exportList.add(vo);
        }
        return exportList;
    }
}