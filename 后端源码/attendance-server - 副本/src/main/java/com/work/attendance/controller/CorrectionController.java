package com.work.attendance.controller;
//补签
import com.work.attendance.common.Result;
import com.work.attendance.entity.CorrectionApply;
import com.work.attendance.service.CorrectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/correction")
public class CorrectionController {
    @Autowired
    private CorrectionService correctionService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody CorrectionApply apply) {
        return correctionService.apply(apply);
    }

    @PostMapping("/approve")
    public Result<String> approve(@RequestParam Long id) { // 参数名统一为 id
        return correctionService.approve(id);
    }

    @PostMapping("/reject")
    public Result<String> reject(@RequestParam Long id) { // 参数名统一为 id
        return correctionService.reject(id);
    }

    @GetMapping("/list")
    public Result<List<CorrectionApply>> list() {
        return correctionService.listAll();
    }
}