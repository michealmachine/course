package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.QuestionDTO;
import com.zhangziqi.online_course_mine.model.vo.QuestionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 题目管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "题目管理", description = "题目创建、管理相关操作")
public class QuestionController {
    
    private final QuestionService questionService;
    
    /**
     * 创建题目
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "创建题目", description = "创建新的题目，包括单选题和多选题")
    public Result<QuestionVO> createQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        // 获取当前用户ID和机构ID
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        // 确保DTO中的机构ID与当前用户的机构ID一致
        questionDTO.setInstitutionId(institutionId);
        
        log.info("创建题目, 用户ID: {}, 机构ID: {}, 题目类型: {}", 
                currentUserId, institutionId, questionDTO.getType());
        
        QuestionVO question = questionService.createQuestion(questionDTO, currentUserId);
        return Result.success(question);
    }
    
    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新题目", description = "更新现有题目的内容和选项")
    public Result<QuestionVO> updateQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id, 
            @Valid @RequestBody QuestionDTO questionDTO) {
        // 获取当前用户ID和机构ID
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        // 确保DTO中的ID与路径参数一致
        if (!id.equals(questionDTO.getId())) {
            return Result.fail(HttpStatus.BAD_REQUEST.value(), "请求参数不一致");
        }
        
        // 确保DTO中的机构ID与当前用户的机构ID一致
        questionDTO.setInstitutionId(institutionId);
        
        log.info("更新题目, ID: {}, 用户ID: {}, 机构ID: {}", 
                id, currentUserId, institutionId);
        
        QuestionVO updatedQuestion = questionService.updateQuestion(questionDTO, currentUserId);
        return Result.success(updatedQuestion);
    }
    
    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取题目详情", description = "获取指定题目的详细信息")
    public Result<QuestionVO> getQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取题目详情, ID: {}, 机构ID: {}", id, institutionId);
        
        QuestionVO question = questionService.getQuestionById(id, institutionId);
        return Result.success(question);
    }
    
    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除题目", description = "删除指定的题目")
    public Result<Void> deleteQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除题目, ID: {}, 用户ID: {}, 机构ID: {}", 
                id, currentUserId, institutionId);
        
        questionService.deleteQuestion(id, institutionId, currentUserId);
        return Result.success();
    }
    
    /**
     * 分页查询题目列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取题目列表", description = "分页获取题目列表，可按类型、难度和关键词筛选")
    public Result<Page<QuestionVO>> getQuestions(
            @Parameter(description = "题目类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "难度级别") @RequestParam(required = false) Integer difficulty,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取题目列表, 机构ID: {}, 类型: {}, 难度: {}, 关键词: {}", 
                institutionId, type, difficulty, keyword);
        
        Page<QuestionVO> questions = questionService.getQuestions(
                institutionId, type, difficulty, keyword, pageable);
        return Result.success(questions);
    }
    
    /**
     * 随机获取题目
     */
    @GetMapping("/random")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "随机获取题目", description = "随机获取指定数量的题目，可按类型筛选")
    public Result<List<QuestionVO>> getRandomQuestions(
            @Parameter(description = "题目类型") @RequestParam Integer type,
            @Parameter(description = "题目数量") @RequestParam(defaultValue = "5") int count) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("随机获取题目, 机构ID: {}, 类型: {}, 数量: {}", 
                institutionId, type, count);
        
        List<QuestionVO> questions = questionService.getRandomQuestions(institutionId, type, count);
        return Result.success(questions);
    }
} 