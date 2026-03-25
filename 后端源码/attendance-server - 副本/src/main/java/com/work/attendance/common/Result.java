package com.work.attendance.common;

import lombok.Data;

/**
 * 统一后端返回结果类
 * 用于规范所有 API 的返回格式
 */
@Data
public class Result<T> {
    private Integer code; // 状态码：200-成功，500-失败
    private String msg;    // 提示信息
    private T data;        // 具体数据

    // 成功且不带数据的返回
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    // 成功且带数据的返回
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 失败的返回
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    // 自定义状态码的失败返回
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}