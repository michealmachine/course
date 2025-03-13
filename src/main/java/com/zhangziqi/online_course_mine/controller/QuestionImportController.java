package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.QuestionImportResultVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.QuestionImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 试题导入控制器
 */
@RestController
@RequestMapping("/api/questions/import")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "试题导入", description = "试题批量导入相关接口")
public class QuestionImportController {

    private final QuestionImportService questionImportService;
    
    @Value("${question.import.default-batch-size:50}")
    private Integer defaultBatchSize;
    
    /**
     * 下载试题导入模板
     */
    @GetMapping("/template")
    @Operation(summary = "下载试题导入模板", description = "下载批量导入试题的Excel模板文件")
    @PreAuthorize("hasAuthority('QUESTION_MANAGE')")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        questionImportService.generateExcelTemplate(response);
    }
    
    /**
     * 导入试题Excel
     */
    @PostMapping
    @Operation(summary = "导入试题", description = "批量导入试题Excel文件")
    @PreAuthorize("hasAuthority('QUESTION_MANAGE')")
    public Result<QuestionImportResultVO> importQuestions(
            @Parameter(description = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "机构ID", required = true)
            @RequestParam("institutionId") Long institutionId,
            
            @Parameter(description = "批处理大小，默认为配置中的值")
            @RequestParam(value = "batchSize", required = false) Integer batchSize,
            
            @AuthenticationPrincipal Authentication authentication) throws IOException {
        
        // 获取当前用户ID
        Long userId = Long.valueOf(authentication.getName());
        
        // 如果未指定批处理大小，使用默认值
        Integer actualBatchSize = batchSize != null ? batchSize : defaultBatchSize;
        
        // 执行导入
        QuestionImportResultVO result = questionImportService.importQuestions(
                file, institutionId, userId, actualBatchSize);
        
        return Result.success(result);
    }
} 