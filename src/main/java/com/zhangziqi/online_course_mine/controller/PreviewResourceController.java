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
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {

        
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
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {

        
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
    

} 