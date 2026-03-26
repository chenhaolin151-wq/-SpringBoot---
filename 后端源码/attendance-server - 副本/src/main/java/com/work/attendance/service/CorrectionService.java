package com.work.attendance.service;

import com.work.attendance.common.Result;
import com.work.attendance.entity.CorrectionApply;
import java.util.List;

public interface CorrectionService {
    Result<String> apply(CorrectionApply apply);
    Result<String> approve(Long applyId);
    Result<String> reject(Long applyId);
    Result<List<CorrectionApply>> listAll();
}