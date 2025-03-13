package com.zhangziqi.online_course_mine.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.zhangziqi.online_course_mine.excel.QuestionExcelListener;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.excel.QuestionExcelData;
import com.zhangziqi.online_course_mine.model.vo.QuestionImportResultVO;
import com.zhangziqi.online_course_mine.service.QuestionImportService;
import com.zhangziqi.online_course_mine.service.QuestionService;
import com.zhangziqi.online_course_mine.service.QuestionTagService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 试题导入服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionImportServiceImpl implements QuestionImportService {

    private final QuestionService questionService;
    private final QuestionTagService questionTagService;
    
    @Qualifier("importTaskExecutor")
    private final Executor importTaskExecutor;

    @Value("${question.import.max-rows:1000}")
    private Integer maxImportRows;
    
    @Value("${question.import.concurrent-enabled:true}")
    private Boolean concurrentEnabled;

    /**
     * 生成试题Excel导入模板
     */
    @Override
    public void generateExcelTemplate(HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        
        // 设置文件名
        String fileName = URLEncoder.encode("试题导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        try {
            // 创建示例数据
            ArrayList<QuestionExcelData> demoData = new ArrayList<>();
            demoData.add(createSingleChoiceDemo());
            demoData.add(createMultipleChoiceDemo());
            
            // 使用EasyExcel写入数据
            ExcelWriterBuilder writerBuilder = EasyExcel.write(response.getOutputStream(), QuestionExcelData.class)
                    .autoCloseStream(false)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()); // 自适应列宽
            
            writerBuilder.sheet("试题模板").doWrite(demoData);
        } catch (Exception e) {
            log.error("生成Excel模板失败", e);
            // 重置响应
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println("生成Excel模板失败: " + e.getMessage());
        }
    }

    /**
     * 导入试题Excel
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionImportResultVO importQuestions(
            MultipartFile file, 
            Long institutionId, 
            Long userId, 
            Integer batchSize) throws IOException {
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new BusinessException("文件格式不正确，仅支持Excel文件(.xlsx或.xls)");
        }
        
        // 验证文件大小
        if (file.getSize() > 10 * 1024 * 1024) { // 最大10MB
            throw new BusinessException("文件过大，请控制在10MB以内");
        }
        
        // 使用输入流读取Excel
        try (InputStream inputStream = file.getInputStream()) {
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 如果启用并发，则使用异步方式处理
            if (concurrentEnabled && batchSize != null && batchSize > 0) {
                return processWithConcurrent(inputStream, institutionId, userId, batchSize, startTime);
            } else {
                return processSequentially(inputStream, institutionId, userId, batchSize, startTime);
            }
        } catch (Exception e) {
            log.error("导入试题Excel失败", e);
            throw new BusinessException("导入试题失败: " + e.getMessage());
        }
    }
    
    /**
     * 顺序处理导入
     */
    private QuestionImportResultVO processSequentially(
            InputStream inputStream, 
            Long institutionId, 
            Long userId, 
            Integer batchSize,
            long startTime) {
        
        // 创建Excel解析监听器
        QuestionExcelListener listener = new QuestionExcelListener(
                questionService, questionTagService, institutionId, userId, batchSize);
        
        // 读取Excel数据
        EasyExcel.read(inputStream, QuestionExcelData.class, listener)
                .headRowNumber(1) // 表头行数
                .ignoreEmptyRow(true) // 忽略空行
                .autoCloseStream(true)
                .sheet() // 默认读取第一个sheet
                .doRead();
        
        // 记录结束时间并计算耗时
        long endTime = System.currentTimeMillis();
        QuestionImportResultVO result = listener.getResult();
        result.setDuration(endTime - startTime);
        
        return result;
    }
    
    /**
     * 并发处理导入
     */
    private QuestionImportResultVO processWithConcurrent(
            InputStream inputStream, 
            Long institutionId, 
            Long userId, 
            Integer batchSize,
            long startTime) throws Exception {
        
        // 创建结果对象
        QuestionImportResultVO result = QuestionImportResultVO.builder()
                .totalCount(0)
                .successCount(0)
                .failureCount(0)
                .failureItems(new ArrayList<>())
                .build();
        
        // 读取全部数据
        List<QuestionExcelData> allData = EasyExcel.read(inputStream)
                .head(QuestionExcelData.class)
                .sheet()
                .headRowNumber(1)
                .doReadSync();
        
        // 总数量
        int totalCount = allData.size();
        result.setTotalCount(totalCount);
        
        if (totalCount == 0) {
            result.setDuration(System.currentTimeMillis() - startTime);
            return result;
        }
        
        // 验证最大行数
        if (maxImportRows != null && totalCount > maxImportRows) {
            throw new BusinessException("导入数据过多，每次最多导入 " + maxImportRows + " 条记录");
        }
        
        // 按照批次大小分组
        int actualBatchSize = Math.min(batchSize, 100); // 限制最大批次大小
        int batchCount = (totalCount + actualBatchSize - 1) / actualBatchSize; // 向上取整
        
        // 创建异步任务
        ArrayList<CompletableFuture<QuestionImportResultVO>> futures = new ArrayList<>();
        
        for (int i = 0; i < batchCount; i++) {
            int fromIndex = i * actualBatchSize;
            int toIndex = Math.min((i + 1) * actualBatchSize, totalCount);
            
            // 获取当前批次数据
            List<QuestionExcelData> batchData = allData.subList(fromIndex, toIndex);
            
            // 创建并提交异步任务
            CompletableFuture<QuestionImportResultVO> future = processBatchAsync(
                    batchData, institutionId, userId, i + 1);
            
            futures.add(future);
        }
        
        // 等待所有任务完成并合并结果
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        // 合并所有批次的处理结果
        allOf.thenRun(() -> {
            futures.forEach(future -> {
                try {
                    QuestionImportResultVO batchResult = future.get();
                    result.setSuccessCount(result.getSuccessCount() + batchResult.getSuccessCount());
                    result.setFailureCount(result.getFailureCount() + batchResult.getFailureCount());
                    result.getFailureItems().addAll(batchResult.getFailureItems());
                } catch (Exception e) {
                    log.error("获取批次处理结果异常", e);
                }
            });
        }).get(); // 等待所有合并完成
        
        // 记录结束时间并计算耗时
        long endTime = System.currentTimeMillis();
        result.setDuration(endTime - startTime);
        
        return result;
    }
    
    /**
     * 异步处理一个批次的数据
     */
    @Async("importTaskExecutor")
    public CompletableFuture<QuestionImportResultVO> processBatchAsync(
            List<QuestionExcelData> batchData, 
            Long institutionId, 
            Long userId,
            int batchIndex) {
        
        log.info("开始处理批次 {}, 数据量: {}", batchIndex, batchData.size());
        
        // 创建结果对象
        QuestionImportResultVO result = QuestionImportResultVO.builder()
                .totalCount(batchData.size())
                .successCount(0)
                .failureCount(0)
                .failureItems(new ArrayList<>())
                .build();
        
        // 处理每条数据
        for (int i = 0; i < batchData.size(); i++) {
            QuestionExcelData excelData = batchData.get(i);
            try {
                // 计算在Excel中的行号（表头占一行，数据从第二行开始，且每个批次的起始行不同）
                int rowIndex = i + 2 + (batchIndex - 1) * batchData.size();
                
                // 转换数据
                com.zhangziqi.online_course_mine.model.dto.QuestionDTO questionDTO = 
                        convertToQuestionDTO(excelData);
                
                // 保存题目
                com.zhangziqi.online_course_mine.model.vo.QuestionVO savedQuestion = 
                        questionService.createQuestion(questionDTO, userId);
                
                // 处理标签
                if (savedQuestion != null && savedQuestion.getId() != null && 
                    excelData.getTags() != null && !excelData.getTags().trim().isEmpty()) {
                    processQuestionTags(savedQuestion.getId(), excelData.getTags(), 
                            institutionId, userId);
                }
                
                // 记录成功
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                // 记录失败
                result.setFailureCount(result.getFailureCount() + 1);
                
                // 计算在Excel中的行号
                int rowIndex = i + 2 + (batchIndex - 1) * batchData.size();
                
                // 添加失败信息
                result.getFailureItems().add(
                    QuestionImportResultVO.FailureItem.builder()
                        .rowIndex(rowIndex)
                        .title(excelData.getTitle() != null ? excelData.getTitle() : "未知标题")
                        .errorMessage(e.getMessage())
                        .build()
                );
                
                log.error("导入试题失败，批次: {}, 行: {}, 标题: {}, 错误: {}", 
                        batchIndex, rowIndex, excelData.getTitle(), e.getMessage());
            }
        }
        
        log.info("批次 {} 处理完成，总计: {}, 成功: {}, 失败: {}", 
                batchIndex, result.getTotalCount(), result.getSuccessCount(), result.getFailureCount());
        
        return CompletableFuture.completedFuture(result);
    }
    
    /**
     * 处理题目标签
     */
    private void processQuestionTags(Long questionId, String tagsStr, Long institutionId, Long userId) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return;
        }
        
        String[] tagNames = tagsStr.split(",");
        for (String tagName : tagNames) {
            tagName = tagName.trim();
            if (tagName.isEmpty()) {
                continue;
            }
            
            try {
                // 尝试获取或创建标签
                com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO tagDTO = 
                    com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO.builder()
                        .institutionId(institutionId)
                        .name(tagName)
                        .build();
                
                com.zhangziqi.online_course_mine.model.vo.QuestionTagVO tag = 
                    questionTagService.createTag(tagDTO, userId);
                
                // 关联标签到题目
                if (tag != null && tag.getId() != null) {
                    questionTagService.addTagToQuestion(questionId, tag.getId(), institutionId);
                }
            } catch (Exception e) {
                log.warn("为题目 {} 添加标签 {} 失败: {}", questionId, tagName, e.getMessage());
            }
        }
    }
    
    /**
     * 转换Excel数据为题目DTO
     */
    private com.zhangziqi.online_course_mine.model.dto.QuestionDTO convertToQuestionDTO(
            QuestionExcelData excelData) {
        
        // 验证必填字段
        if (excelData.getTitle() == null || excelData.getTitle().trim().isEmpty()) {
            throw new BusinessException("题目标题不能为空");
        }
        
        if (excelData.getContent() == null || excelData.getContent().trim().isEmpty()) {
            throw new BusinessException("题目内容不能为空");
        }
        
        if (excelData.getType() == null || (excelData.getType() != 1 && excelData.getType() != 2)) {
            throw new BusinessException("题目类型必须为1(单选题)或2(多选题)");
        }
        
        if (excelData.getDifficulty() == null || 
            excelData.getDifficulty() < 1 || 
            excelData.getDifficulty() > 3) {
            throw new BusinessException("难度级别必须为1(简单)、2(中等)或3(困难)");
        }
        
        if (excelData.getScore() == null || excelData.getScore() < 1 || excelData.getScore() > 100) {
            throw new BusinessException("分值必须在1-100之间");
        }
        
        // 构建选项列表
        ArrayList<com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO> options = new ArrayList<>();
        String correctAnswer = excelData.getCorrectAnswer().toUpperCase();
        
        // 验证选项和正确答案
        if (excelData.getOptionA() == null || excelData.getOptionA().trim().isEmpty() ||
            excelData.getOptionB() == null || excelData.getOptionB().trim().isEmpty()) {
            throw new BusinessException("至少需要提供A、B两个选项");
        }
        
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            throw new BusinessException("正确答案不能为空");
        }
        
        // 单选题校验
        if (excelData.getType() == 1 && correctAnswer.length() > 1) {
            throw new BusinessException("单选题只能有一个正确答案");
        }
        
        // 多选题校验
        if (excelData.getType() == 2 && correctAnswer.length() < 2) {
            throw new BusinessException("多选题至少需要两个正确答案");
        }
        
        // 添加选项A
        options.add(com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO.builder()
                .content(excelData.getOptionA())
                .isCorrect(correctAnswer.contains("A"))
                .orderIndex(0)
                .build());
        
        // 添加选项B
        options.add(com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO.builder()
                .content(excelData.getOptionB())
                .isCorrect(correctAnswer.contains("B"))
                .orderIndex(1)
                .build());
        
        // 添加选项C（如果存在）
        if (excelData.getOptionC() != null && !excelData.getOptionC().trim().isEmpty()) {
            options.add(com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO.builder()
                    .content(excelData.getOptionC())
                    .isCorrect(correctAnswer.contains("C"))
                    .orderIndex(2)
                    .build());
        } else if (correctAnswer.contains("C")) {
            throw new BusinessException("选项C不存在，无法设为正确答案");
        }
        
        // 添加选项D（如果存在）
        if (excelData.getOptionD() != null && !excelData.getOptionD().trim().isEmpty()) {
            options.add(com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO.builder()
                    .content(excelData.getOptionD())
                    .isCorrect(correctAnswer.contains("D"))
                    .orderIndex(3)
                    .build());
        } else if (correctAnswer.contains("D")) {
            throw new BusinessException("选项D不存在，无法设为正确答案");
        }
        
        // 添加选项E（如果存在）
        if (excelData.getOptionE() != null && !excelData.getOptionE().trim().isEmpty()) {
            options.add(com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO.builder()
                    .content(excelData.getOptionE())
                    .isCorrect(correctAnswer.contains("E"))
                    .orderIndex(4)
                    .build());
        } else if (correctAnswer.contains("E")) {
            throw new BusinessException("选项E不存在，无法设为正确答案");
        }
        
        // 添加选项F（如果存在）
        if (excelData.getOptionF() != null && !excelData.getOptionF().trim().isEmpty()) {
            options.add(com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO.builder()
                    .content(excelData.getOptionF())
                    .isCorrect(correctAnswer.contains("F"))
                    .orderIndex(5)
                    .build());
        } else if (correctAnswer.contains("F")) {
            throw new BusinessException("选项F不存在，无法设为正确答案");
        }
        
        // 创建题目DTO
        return com.zhangziqi.online_course_mine.model.dto.QuestionDTO.builder()
                .title(excelData.getTitle())
                .content(excelData.getContent())
                .type(excelData.getType())
                .difficulty(excelData.getDifficulty())
                .score(excelData.getScore())
                .analysis(excelData.getAnalysis())
                .options(options)
                .institutionId(null) // 由调用方设置
                .build();
    }

    /**
     * 创建单选题示例数据
     */
    private QuestionExcelData createSingleChoiceDemo() {
        return QuestionExcelData.builder()
                .title("示例单选题")
                .content("下列关于Java语言特点的说法，正确的是?")
                .type(1) // 单选题
                .difficulty(1) // 简单
                .score(5) // 5分
                .analysis("Java语言是面向对象的编程语言，具有跨平台特性。")
                .optionA("Java是编译型语言")
                .optionB("Java程序可以跨平台运行")
                .optionC("Java不支持多线程")
                .optionD("Java不是面向对象语言")
                .correctAnswer("B") // 正确答案是B
                .tags("Java,编程基础") // 标签
                .build();
    }

    /**
     * 创建多选题示例数据
     */
    private QuestionExcelData createMultipleChoiceDemo() {
        return QuestionExcelData.builder()
                .title("示例多选题")
                .content("以下哪些是Java的基本数据类型?")
                .type(2) // 多选题
                .difficulty(2) // 中等
                .score(10) // 10分
                .analysis("Java的基本数据类型包括：byte、short、int、long、float、double、char和boolean。")
                .optionA("int")
                .optionB("String")
                .optionC("double")
                .optionD("boolean")
                .optionE("Integer")
                .correctAnswer("ACD") // 正确答案是A、C、D
                .tags("Java,数据类型") // 标签
                .build();
    }
} 