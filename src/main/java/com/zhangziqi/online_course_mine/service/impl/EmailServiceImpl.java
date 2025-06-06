package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String emailFrom;

    /**
     * Redis中验证码的key前缀
     */
    @Setter
    private String verificationCodePrefix = "email:verification:";

    /**
     * 验证码有效期（分钟）
     */
    @Setter
    private long verificationCodeExpiration = 5;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject("在线课程平台 - 邮箱验证码");

            // 使用Thymeleaf模板引擎渲染邮件内容
            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("expirationMinutes", verificationCodeExpiration);
            String content = templateEngine.process("email/verification-code", context);

            helper.setText(content, true);
            mailSender.send(message);
            log.info("验证码邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("验证码邮件发送失败: {}", to, e);
            throw new RuntimeException("验证码邮件发送失败", e);
        }
    }

    @Override
    public void sendEmailUpdateCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject("在线课程平台 - 邮箱更新验证码");

            // 使用Thymeleaf模板引擎渲染邮件内容
            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("expirationMinutes", verificationCodeExpiration);
            String content = templateEngine.process("email/email-update-code", context);

            helper.setText(content, true);
            mailSender.send(message);
            log.info("邮箱更新验证码邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("邮箱更新验证码邮件发送失败: {}", to, e);
            throw new RuntimeException("邮箱更新验证码邮件发送失败", e);
        }
    }

    @Override
    public String generateVerificationCode() {
        Random random = new Random();
        // 生成6位数字验证码
        return String.format("%06d", random.nextInt(1000000));
    }

    @Override
    public String generateTempPassword() {
        // 生成8位随机密码，包含数字、大小写字母
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        // 确保至少包含一个数字
        sb.append(random.nextInt(10));

        // 确保至少包含一个大写字母
        sb.append(chars.charAt(random.nextInt(26)));

        // 确保至少包含一个小写字母
        sb.append(chars.charAt(26 + random.nextInt(26)));

        // 添加剩余的随机字符
        for (int i = 3; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        // 打乱顺序
        char[] tempPassword = sb.toString().toCharArray();
        for (int i = 0; i < tempPassword.length; i++) {
            int j = random.nextInt(tempPassword.length);
            char temp = tempPassword[i];
            tempPassword[i] = tempPassword[j];
            tempPassword[j] = temp;
        }

        return new String(tempPassword);
    }

    @Override
    public void saveVerificationCode(String email, String code) {
        String key = verificationCodePrefix + email;
        redisTemplate.opsForValue().set(key, code, verificationCodeExpiration, TimeUnit.MINUTES);
        log.info("验证码已保存到Redis: {}", email);
    }

    @Override
    public boolean validateVerificationCode(String email, String code) {
        String key = verificationCodePrefix + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode != null && savedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    @Override
    public void sendApplicationConfirmationEmail(String to, String applicationId, String institutionName) {
        Context context = new Context();
        context.setVariable("applicationId", applicationId);
        context.setVariable("institutionName", institutionName);

        String content = templateEngine.process("email/application-confirmation", context);
        sendHtmlMail(to, "机构入驻申请确认", content);
    }

    @Override
    public void sendApplicationApprovedEmail(String to, String institutionName, String registerCode) {
        Context context = new Context();
        context.setVariable("registerCode", registerCode);
        context.setVariable("institutionName", institutionName);

        String content = templateEngine.process("email/application-approved", context);
        sendHtmlMail(to, "机构入驻申请已通过", content);
    }

    @Override
    public void sendApplicationRejectedEmail(String to, String institutionName, String reason) {
        Context context = new Context();
        context.setVariable("institutionName", institutionName);
        context.setVariable("reason", reason);

        String content = templateEngine.process("email/application-rejected", context);
        sendHtmlMail(to, "机构入驻申请未通过", content);
    }

    @Override
    public void sendPasswordResetEmail(String to, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject("在线课程平台 - 密码重置");

            // 使用Thymeleaf模板引擎渲染邮件内容
            Context context = new Context();
            context.setVariable("tempPassword", tempPassword);
            String content = templateEngine.process("email/password-reset", context);

            helper.setText(content, true);
            mailSender.send(message);
            log.info("密码重置邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("密码重置邮件发送失败: {}", to, e);
            throw new RuntimeException("密码重置邮件发送失败", e);
        }
    }

    /**
     * 发送HTML格式邮件
     *
     * @param to 收件人
     * @param subject 主题
     * @param content HTML内容
     */
    private void sendHtmlMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("HTML邮件发送成功: {}, 主题: {}", to, subject);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败: {}, 主题: {}", to, subject, e);
            throw new RuntimeException("HTML邮件发送失败", e);
        }
    }
}