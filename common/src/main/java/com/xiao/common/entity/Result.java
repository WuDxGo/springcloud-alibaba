package com.xiao.common.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果类
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 15:23
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 15:23] [WuDx] 创建类
 */
@Data
public class Result<T> implements Serializable {
    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 200: 成功
     * 400: 请求错误
     * 401: 未认证
     * 403: 无权限
     * 500: 服务器错误
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     * 响应生成的时间
     */
    private Long timestamp;

    /**
     * 私有构造方法
     * 禁止外部直接创建实例
     */
    private Result() {
        // 初始化时间戳
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     *
     * @return Result<Void> 成功结果
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @return Result<T> 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 错误响应（默认500）
     *
     * @param message 错误信息
     * @return Result<T> 错误结果
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 错误响应（自定义状态码）
     *
     * @param code 状态码
     * @param message 错误信息
     * @return Result<T> 错误结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}