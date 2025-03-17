package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.SectionVO;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.MediaService;
import com.zhangziqi.online_course_mine.service.QuestionGroupService;
import com.zhangziqi.online_course_mine.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 预览资源控制器
 * 用于课程预览时获取媒体资源和题组资源
 * 支持多种用户角色：管理员、审核员、学员、匿名访问
 */
@Slf4j
@RestController
@RequestMapping("/api/preview/resources")
@RequiredArgsConstructor
@Tag(name = "预览资源", description = "预览模式下获取媒体资源和题组资源相关操作")
public class PreviewResourceController {

    private final SectionService sectionService;
    private final MediaService mediaService;
    private final QuestionGroupService questionGroupService;
    private final StringRedisTemplate redisTemplate;
    private final HttpServletRequest request;
    
    // Redis中预览token的key前缀
    @Value("${app.preview.token-prefix:course:preview:}")
    private String PREVIEW_TOKEN_KEY_PREFIX;
    
    /**
     * 获取小节媒体资源（预览模式）
     * 支持多种用户角色：
     * - 管理员/审核员：直接访问
     * - 学员：需验证是否有权限（已购买或可试看）
     * - 匿名用户：需通过有效的预览令牌访问
     */
    @GetMapping("/sections/{id}/media")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取小节媒体资源", description = "获取指定小节的媒体资源，包含临时访问URL（支持多种角色）")
    public Result<MediaVO> getSectionMedia(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId,
            @Parameter(description = "预览令牌（匿名访问时必须）") @RequestParam(required = false) String token) {
        
        // 验证访问权限
        validateAccessPermission(sectionId, token);
        
        // 获取小节信息
        SectionVO section = sectionService.getSectionById(sectionId);
        if (!"MEDIA".equals(section.getResourceTypeDiscriminator())) {
            log.warn("小节不是媒体类型, 小节ID: {}, 资源类型: {}", sectionId, section.getResourceTypeDiscriminator());
            throw new ResourceNotFoundException("小节非媒体类型资源");
        }
        
        Long mediaId = section.getMediaId();
        if (mediaId == null) {
            log.warn("小节未关联媒体资源, 小节ID: {}", sectionId);
            throw new ResourceNotFoundException("小节未关联媒体资源");
        }
        
        // 获取媒体资源（包含临时访问URL）
        MediaVO mediaVO = mediaService.getMediaByIdForPreview(mediaId);
        
        log.info("成功获取预览媒体资源, 小节ID: {}, 媒体ID: {}", sectionId, mediaId);
        return Result.success(mediaVO);
    }
    
    /**
     * 获取小节题组（预览模式）
     * 支持多种用户角色：
     * - 管理员/审核员：直接访问
     * - 学员：需验证是否有权限（已购买或可试看）
     * - 匿名用户：需通过有效的预览令牌访问
     */
    @GetMapping("/sections/{id}/question-group")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取小节题组", description = "获取指定小节的题组，包含详细题目（支持多种角色）")
    public Result<QuestionGroupVO> getSectionQuestionGroup(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId,
            @Parameter(description = "预览令牌（匿名访问时必须）") @RequestParam(required = false) String token) {
        
        // 验证访问权限
        validateAccessPermission(sectionId, token);
        
        // 获取小节信息
        SectionVO section = sectionService.getSectionById(sectionId);
        if (!"QUESTION_GROUP".equals(section.getResourceTypeDiscriminator())) {
            log.warn("小节不是题组类型, 小节ID: {}, 资源类型: {}", sectionId, section.getResourceTypeDiscriminator());
            throw new ResourceNotFoundException("小节非题组类型资源");
        }
        
        Long questionGroupId = section.getQuestionGroupId();
        if (questionGroupId == null) {
            log.warn("小节未关联题组资源, 小节ID: {}", sectionId);
            throw new ResourceNotFoundException("小节未关联题组资源");
        }
        
        // 获取题组详情，包含题目
        QuestionGroupVO questionGroupVO = questionGroupService.getGroupByIdForPreview(questionGroupId, true);
        
        log.info("成功获取预览题组资源, 小节ID: {}, 题组ID: {}", sectionId, questionGroupId);
        return Result.success(questionGroupVO);
    }
    
    /**
     * 验证访问权限
     * 根据用户角色应用不同的访问策略
     */
    private void validateAccessPermission(Long sectionId, String token) {
        // 尝试获取当前用户信息
        Long userId = null;
        boolean isAuthenticated = false;
        
        try {
            userId = SecurityUtil.getCurrentUserId();
            isAuthenticated = true;
        } catch (Exception e) {
            // 用户未登录，将使用令牌验证
            log.debug("用户未登录，将使用令牌验证");
        }
        
        // 根据角色应用不同的访问策略
        if (isAuthenticated && (SecurityUtil.hasRole("ADMIN") || SecurityUtil.hasRole("REVIEWER"))) {
            // 管理员和审核员直接放行
            log.info("管理员/审核员访问资源, 用户ID: {}, 小节ID: {}", userId, sectionId);
        } else if (isAuthenticated && SecurityUtil.hasRole("STUDENT")) {
            // 学员需要验证权限
            // TODO: 实现学员访问控制逻辑
            // boolean hasAccess = courseAccessService.canStudentAccessSection(userId, sectionId);
            boolean hasAccess = true; // 暂时全部放行，待实现学员访问控制
            
            if (!hasAccess) {
                log.warn("学员无权访问该资源, 用户ID: {}, 小节ID: {}", userId, sectionId);
                throw new BusinessException(403, "无权访问此资源，请购买课程或查看可试看内容");
            }
            log.info("学员访问资源, 用户ID: {}, 小节ID: {}", userId, sectionId);
        } else if (isAuthenticated && SecurityUtil.hasRole("INSTITUTION")) {
            // 机构用户需要验证是否是自己的课程
            // TODO: 实现机构用户访问控制
            log.info("机构用户访问资源, 用户ID: {}, 小节ID: {}", userId, sectionId);
        } else {
            // 匿名访问或其他角色，需要验证预览令牌
            if (token == null || token.trim().isEmpty()) {
                log.warn("访问令牌为空, 小节ID: {}", sectionId);
                throw new BusinessException(401, "访问令牌不能为空");
            }
            
            validatePreviewToken(token, sectionId);
            log.info("通过预览令牌访问资源, 令牌: {}, 小节ID: {}", token, sectionId);
        }
    }
    
    /**
     * 验证预览令牌
     */
    private void validatePreviewToken(String token, Long sectionId) {
        // 从Redis中获取token对应的课程ID
        String courseIdStr = redisTemplate.opsForValue().get(PREVIEW_TOKEN_KEY_PREFIX + token);
        
        if (courseIdStr == null) {
            log.warn("预览令牌不存在或已过期, 令牌: {}", token);
            throw new BusinessException(403, "预览链接不存在或已过期");
        }
        
        // TODO: 验证小节是否属于该课程
        // boolean sectionBelongsToCourse = sectionService.validateSectionBelongsToCourse(sectionId, Long.parseLong(courseIdStr));
        boolean sectionBelongsToCourse = true; // 暂时假设小节属于该课程，待实现验证逻辑
        
        if (!sectionBelongsToCourse) {
            log.warn("小节不属于预览课程, 小节ID: {}, 课程ID: {}", sectionId, courseIdStr);
            throw new BusinessException(403, "无权访问此资源");
        }
    }
} 