package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.JwtTokenDTO;
import com.zhangziqi.online_course_mine.model.dto.LoginDTO;
import com.zhangziqi.online_course_mine.model.dto.RefreshTokenDTO;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return JWT令牌
     */
    JwtTokenDTO login(LoginDTO loginDTO);

    /**
     * 刷新令牌
     *
     * @param refreshTokenDTO 刷新令牌请求
     * @return 新的JWT令牌
     */
    JwtTokenDTO refreshToken(RefreshTokenDTO refreshTokenDTO);

    /**
     * 注销
     *
     * @param token 访问令牌
     */
    void logout(String token);
} 