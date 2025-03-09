package com.zhangziqi.online_course_mine.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);

    /**
     * 发送邮箱更新验证码邮件
     *
     * @param to 新邮箱地址
     * @param code 验证码
     */
    void sendEmailUpdateCode(String to, String code);

    /**
     * 生成验证码
     *
     * @return 6位数字验证码
     */
    String generateVerificationCode();

    /**
     * 保存验证码到Redis
     *
     * @param email 邮箱
     * @param code 验证码
     */
    void saveVerificationCode(String email, String code);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean validateVerificationCode(String email, String code);
} 