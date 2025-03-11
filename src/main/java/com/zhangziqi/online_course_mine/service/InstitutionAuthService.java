package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.InstitutionRegisterDTO;

/**
 * 机构用户注册服务接口
 */
public interface InstitutionAuthService {
    
    /**
     * 使用机构注册码注册用户
     *
     * @param registerDTO 注册参数
     */
    void registerWithInstitutionCode(InstitutionRegisterDTO registerDTO);
} 