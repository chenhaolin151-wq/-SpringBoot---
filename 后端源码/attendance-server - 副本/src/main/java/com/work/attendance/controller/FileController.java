package com.work.attendance.controller;
//【拆分】文件上传（头像）与 Excel 导出
import com.alibaba.excel.EasyExcel;
import com.work.attendance.common.Result;
import com.work.attendance.entity.AttendanceExcelVO;
import com.work.attendance.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/file")
public class FileController {

    @Autowired private FileService fileService;

    /**
     * 仅上传文件（不关联用户，如发表文章插入图片等场景可用）
     */
    @PostMapping("/upload/only")
    public Result<String> uploadOnly(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    /**
     * 上传头像并更新用户信息
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam Long userId) {
        return fileService.uploadAvatar(file, userId);
    }

    /**
     * 导出考勤报表
     */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        // 1. 设置响应头信息
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("考勤月报表_" + System.currentTimeMillis(), "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 2. 从 Service 获取转换好的数据
        List<AttendanceExcelVO> data = fileService.getAttendanceExportData();

        // 3. 调用 EasyExcel 写入流
        EasyExcel.write(response.getOutputStream(), AttendanceExcelVO.class)
                .sheet("考勤明细")
                .doWrite(data);
    }
}