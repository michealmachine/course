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
     * 发送密码重置邮件
     *
     * @param to 收件人邮箱
     * @param tempPassword 临时密码
     */
    void sendPasswordResetEmail(String to, String tempPassword);

    /**
     * 生成验证码
     *
     * @return 6位数字验证码
     */
    String generateVerificationCode();

    /**
     * 生成临时密码
     *
     * @return 随机临时密码
     */
    String generateTempPassword();

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

    /**
     * 发送机构申请确认邮件
     *
     * @param to 收件人邮箱
     * @param applicationId 申请ID
     * @param institutionName 机构名称
     */
    void sendApplicationConfirmationEmail(String to, String applicationId, String institutionName);

    /**
     * 发送机构申请通过邮件
     *
     * @param to 收件人邮箱
     * @param institutionName 机构名称
     * @param registerCode 注册码
     */
    void sendApplicationApprovedEmail(String to, String institutionName, String registerCode);

    /**
     * 发送机构申请拒绝邮件
     *
     * @param to 收件人邮箱
     * @param institutionName 机构名称
     * @param reason 拒绝原因
     */
    void sendApplicationRejectedEmail(String to, String institutionName, String reason);
}