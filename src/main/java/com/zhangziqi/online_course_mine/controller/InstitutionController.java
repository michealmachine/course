package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.InstitutionApplyDTO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionApplicationVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.CaptchaService;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 机构申请控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/institutions")
@Tag(name = "机构申请", description = "机构申请相关接口")
public class InstitutionController {
    
    private final InstitutionService institutionService;
    private final CaptchaService captchaService;
    
    /**
     * 申请创建机构
     *
     * @param applyDTO 申请参数
     * @return 申请ID
     */
    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "申请创建机构", description = "提交机构入驻申请，需等待审核员审核")
    public Result<String> applyInstitution(@Valid @RequestBody InstitutionApplyDTO applyDTO) {
        // 验证图形验证码
        if (!captchaService.validateCaptcha(applyDTO.getCaptchaKey(), applyDTO.getCaptchaCode())) {
            return Result.fail("验证码错误");
        }
        
        log.info("申请创建机构: {}", applyDTO.getName());
        String applicationId = institutionService.applyInstitution(applyDTO);
        return Result.success(applicationId);
    }
    
    /**
     * 查询申请状态
     *
     * @param applicationId 申请ID
     * @param email 联系邮箱
     * @return 申请状态
     */
    @GetMapping("/application-status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "查询申请状态", description = "通过申请ID和邮箱查询机构申请状态")
    public Result<InstitutionApplicationVO> getApplicationStatus(
            @Parameter(description = "申请ID") @RequestParam String applicationId,
            @Parameter(description = "联系邮箱") @RequestParam String email) {
        log.info("查询申请状态: {}, {}", applicationId, email);
        return Result.success(
            institutionService.getApplicationStatus(applicationId, email)
        );
    }
} 