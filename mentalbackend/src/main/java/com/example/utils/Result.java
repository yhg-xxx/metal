package com.example.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private Integer code; // 状态码
    private String message; // 响应消息
    private Object data; // 响应数据

    /**
     * 成功响应
     * @param data 响应数据
     * @return Result对象
     */
    public static Result success(Object data) {
        return new Result(200, "success", data);
    }

    /**
     * 成功响应（无数据）
     * @return Result对象
     */
    public static Result success() {
        return new Result(200, "success", null);
    }

    /**
     * 成功响应（自定义消息）
     * @param message 自定义消息
     * @param data 响应数据
     * @return Result对象
     */
    public static Result success(String message, Object data) {
        return new Result(200, message, data);
    }

    /**
     * 失败响应
     * @param code 状态码
     * @param message 错误消息
     * @return Result对象
     */
    public static Result error(Integer code, String message) {
        return new Result(code, message, null);
    }

    /**
     * 失败响应（默认500状态码）
     * @param message 错误消息
     * @return Result对象
     */
    public static Result error(String message) {
        return new Result(500, message, null);
    }

    /**
     * 构建响应数据Map
     * @param key 键
     * @param value 值
     * @return Result对象
     */
    public static Result build(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return new Result(200, "success", map);
    }
}