package com.zhangziqi.online_course_mine.exception;

/**
 * 服务层业务逻辑异常
 */
public class ServiceException extends RuntimeException {

    /**
     * 错误码
     */
    private String code;

    /**
     * 构造方法
     *
     * @param message 错误信息
     */
    public ServiceException(String message) {
        super(message);
        this.code = "SERVICE_ERROR";
    }

    /**
     * 构造方法
     *
     * @param message 错误信息
     * @param code 错误码
     */
    public ServiceException(String message, String code) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     *
     * @param message 错误信息
     * @param cause 错误原因
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = "SERVICE_ERROR";
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getCode() {
        return code;
    }
} 