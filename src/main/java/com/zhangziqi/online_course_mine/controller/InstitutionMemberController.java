package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 机构成员管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/institutions/members")
@PreAuthorize("hasRole('INSTITUTION')")
@Tag(name = "机构成员管理", description = "机构成员管理相关接口")
public class InstitutionMemberController {
    
    private final InstitutionService institutionService;
    
    /**
     * 获取机构注册码
     *
     * @param authentication 认证信息
     * @return 注册码
     */
    @GetMapping("/register-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构注册码", description = "获取当前用户所属机构的注册码")
    public Result<String> getRegisterCode(Authentication authentication) {
        String username = authentication.getName();
        log.info("获取机构注册码: {}", username);
        return Result.success(institutionService.getInstitutionRegisterCode(username));
    }
} 