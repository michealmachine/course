package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
            helper.setFrom("no-reply@example.com");
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
            helper.setFrom("no-reply@example.com");
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
} 