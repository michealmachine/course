package com.zhangziqi.online_course_mine.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.QuestionDTO;
import com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO;
import com.zhangziqi.online_course_mine.model.excel.QuestionExcelData;
import com.zhangziqi.online_course_mine.model.vo.QuestionImportResultVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionTagVO;
import com.zhangziqi.online_course_mine.service.QuestionService;
import com.zhangziqi.online_course_mine.service.QuestionTagService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 试题Excel解析监听器
 */
@Slf4j
public class QuestionExcelListener extends AnalysisEventListener<QuestionExcelData> {

    private final QuestionService questionService;
    private final QuestionTagService questionTagService;
    private final Long institutionId;
    private final Long userId;
    private final Integer batchSize;
    private final TransactionTemplate transactionTemplate;
    
    /**
     * 导入结果
     */
    @Getter
    private final QuestionImportResultVO result;
    
    /**
     * 批处理记录
     */
    private final List<QuestionExcelData> dataList = new ArrayList<>();
    
    /**
     * 机构所有标签缓存
     */
    private final Map<String, QuestionTagVO> tagCache = new HashMap<>();
    
    /**
     * 统计信息
     */
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);

    public QuestionExcelListener(QuestionService questionService, 
                             QuestionTagService questionTagService,
                             Long institutionId,
                             Long userId,
                             Integer batchSize,
                             TransactionTemplate transactionTemplate) {
        this.questionService = questionService;
        this.questionTagService = questionTagService;
        this.institutionId = institutionId;
        this.userId = userId;
        this.batchSize = batchSize == null ? 50 : batchSize;
        this.transactionTemplate = transactionTemplate;
        this.result = QuestionImportResultVO.builder()
                .totalCount(0)
                .successCount(0)
                .failureCount(0)
                .failureItems(new ArrayList<>())
                .build();
        
        // 预加载机构所有标签
        loadAllTags();
    }

    /**
     * 每解析一行数据，会调用此方法
     */
    @Override
    public void invoke(QuestionExcelData data, AnalysisContext context) {
        // 记录总行数
        totalCount.incrementAndGet();
        
        // 添加到批处理列表
        dataList.add(data);
        
        // 达到批处理大小，执行批量保存
        if (dataList.size() >= batchSize) {
            saveData();
            dataList.clear();
        }
    }

    /**
     * 所有数据解析完成后调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理剩余数据
        if (!dataList.isEmpty()) {
            saveData();
            dataList.clear();
        }
        
        // 更新结果统计
        result.setTotalCount(totalCount.get());
        result.setSuccessCount(successCount.get());
        result.setFailureCount(failureCount.get());
        
        log.info("试题导入完成，总计: {}，成功: {}，失败: {}", 
                totalCount.get(), successCount.get(), failureCount.get());
    }

    /**
     * 批量保存数据
     */
    private void saveData() {
        // 记录当前批次的行号
        Map<QuestionExcelData, Integer> rowIndexMap = new HashMap<>();
        int rowIndex = 1; // 表头为第1行，数据从第2行开始
        
        for (int i = 0; i < dataList.size(); i++) {
            // 当前数据在Excel中的行号
            rowIndex = i + 2; // +2是因为表头占一行，且索引从0开始
            rowIndexMap.put(dataList.get(i), rowIndex);
        }
        
        // 处理每条数据
        for (QuestionExcelData excelData : dataList) {
            final int currentRowIndex = rowIndexMap.get(excelData);
            try {
                // 使用事务模板，每个题目一个独立事务
                Boolean success = transactionTemplate.execute(new TransactionCallback<Boolean>() {
                    @Override
                    public Boolean doInTransaction(TransactionStatus status) {
                        try {
                            // 校验和转换数据
                            QuestionDTO questionDTO = convertToQuestionDTO(excelData);
                            
                            // 保存题目
                            com.zhangziqi.online_course_mine.model.vo.QuestionVO savedQuestion = questionService.createQuestion(questionDTO, userId);
                            
                            // 处理标签
                            if (savedQuestion != null && savedQuestion.getId() != null && 
                                excelData.getTags() != null && !excelData.getTags().trim().isEmpty()) {
                                processQuestionTags(savedQuestion.getId(), excelData.getTags());
                            }
                            
                            return true;
                        } catch (Exception e) {
                            // 明确设置事务回滚
                            status.setRollbackOnly();
                            log.error("保存题目时出错，行号: {}, 标题: {}, 错误: {}", 
                                    currentRowIndex, excelData.getTitle(), e.getMessage());
                            throw e; // 重新抛出以触发事务回滚
                        }
                    }
                });
                
                // 记录成功
                if (success != null && success) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                // 记录失败
                failureCount.incrementAndGet();
                
                // 添加失败信息
                result.getFailureItems().add(
                    QuestionImportResultVO.FailureItem.builder()
                        .rowIndex(currentRowIndex)
                        .title(StringUtils.defaultIfBlank(excelData.getTitle(), "未知标题"))
                        .errorMessage(e.getMessage())
                        .build()
                );
                
                log.error("导入试题失败，行号: {}, 标题: {}, 错误: {}", 
                    currentRowIndex, excelData.getTitle(), e.getMessage());
            }
        }
    }

    /**
     * 转换Excel数据为题目DTO
     */
    private QuestionDTO convertToQuestionDTO(QuestionExcelData excelData) {
        // 基础数据校验
        validateExcelData(excelData);
        
        // 构建选项列表
        List<QuestionOptionDTO> options = buildOptions(excelData);
        
        // 创建题目DTO
        return QuestionDTO.builder()
                .institutionId(institutionId)
                .title(excelData.getTitle())
                .content(excelData.getContent())
                .type(excelData.getType())
                .difficulty(excelData.getDifficulty())
                .score(excelData.getScore())
                .analysis(excelData.getAnalysis())
                .options(options)
                .answer(excelData.getCorrectAnswer())
                .build();
    }

    /**
     * 校验Excel数据
     */
    private void validateExcelData(QuestionExcelData excelData) {
        // 标题校验
        if (StringUtils.isBlank(excelData.getTitle())) {
            throw new BusinessException("题目标题不能为空");
        }
        
        // 内容校验
        if (StringUtils.isBlank(excelData.getContent())) {
            throw new BusinessException("题目内容不能为空");
        }
        
        // 类型校验
        if (excelData.getType() == null || excelData.getType() < 0 || excelData.getType() > 4) {
            throw new BusinessException("题目类型必须为0(单选题)、1(多选题)、2(判断题)、3(填空题)或4(简答题)");
        }
        
        // 难度校验
        if (excelData.getDifficulty() == null || 
            excelData.getDifficulty() < 1 || 
            excelData.getDifficulty() > 3) {
            throw new BusinessException("难度级别必须为1(简单)、2(中等)或3(困难)");
        }
        
        // 分值校验
        if (excelData.getScore() == null || excelData.getScore() < 1 || excelData.getScore() > 100) {
            throw new BusinessException("分值必须在1-100之间");
        }
        
        // 根据题目类型进行特定校验
        switch (excelData.getType()) {
            case 0: // 单选题
            case 1: // 多选题
                validateChoiceQuestion(excelData);
                break;
            case 2: // 判断题
                validateTrueFalseQuestion(excelData);
                break;
            case 3: // 填空题
                validateFillBlankQuestion(excelData);
                break;
            case 4: // 简答题
                // 简答题不需要选项和正确答案，只需要题干和解析
                break;
        }
    }
    
    /**
     * 校验选择题(单选题和多选题)
     */
    private void validateChoiceQuestion(QuestionExcelData excelData) {
        // 选项校验
        if (StringUtils.isBlank(excelData.getOptionA()) || StringUtils.isBlank(excelData.getOptionB())) {
            throw new BusinessException("选择题至少需要提供A、B两个选项");
        }
        
        // 正确答案校验
        if (StringUtils.isBlank(excelData.getCorrectAnswer())) {
            throw new BusinessException("正确答案不能为空");
        }
        
        // 单选题答案校验
        if (excelData.getType() == 0 && excelData.getCorrectAnswer().length() > 1) {
            throw new BusinessException("单选题只能有一个正确答案");
        }
        
        // 多选题答案校验
        if (excelData.getType() == 1 && excelData.getCorrectAnswer().length() < 2) {
            throw new BusinessException("多选题至少需要两个正确选项");
        }
        
        // 验证答案有效性
        validateCorrectAnswer(excelData);
    }
    
    /**
     * 校验判断题
     */
    private void validateTrueFalseQuestion(QuestionExcelData excelData) {
        // 判断题必须只有A和B两个选项，分别代表"正确"和"错误"
        if (StringUtils.isBlank(excelData.getOptionA()) || StringUtils.isBlank(excelData.getOptionB())) {
            throw new BusinessException("判断题必须提供A(正确)和B(错误)两个选项");
        }
        
        // 如果选项A不是"正确"，选项B不是"错误"，进行提示
        if (!"正确".equals(excelData.getOptionA()) || !"错误".equals(excelData.getOptionB())) {
            log.warn("判断题选项A应为'正确'，选项B应为'错误'");
        }
        
        // 正确答案校验，只能是A或B
        if (StringUtils.isBlank(excelData.getCorrectAnswer()) || 
            (!excelData.getCorrectAnswer().equalsIgnoreCase("A") && 
             !excelData.getCorrectAnswer().equalsIgnoreCase("B"))) {
            throw new BusinessException("判断题答案只能是A(正确)或B(错误)");
        }
    }
    
    /**
     * 校验填空题
     */
    private void validateFillBlankQuestion(QuestionExcelData excelData) {
        // 填空题必须有正确答案，但不需要选项
        if (StringUtils.isBlank(excelData.getCorrectAnswer())) {
            throw new BusinessException("填空题必须提供正确答案");
        }
    }

    /**
     * 验证正确答案的有效性(仅适用于选择题)
     */
    private void validateCorrectAnswer(QuestionExcelData excelData) {
        // 对于非选择题类型，不需要验证选项
        if (excelData.getType() > 1) {
            return;
        }
        
        String correctAnswer = excelData.getCorrectAnswer().toUpperCase();
        
        // 验证每个答案选项是否存在
        for (char c : correctAnswer.toCharArray()) {
            if (c < 'A' || c > 'F') {
                throw new BusinessException("正确答案格式无效，应为A-F的字母组合");
            }
            
            // 检查选项是否存在
            switch (c) {
                case 'A':
                    if (StringUtils.isBlank(excelData.getOptionA())) {
                        throw new BusinessException("选项A不存在，无法设为正确答案");
                    }
                    break;
                case 'B':
                    if (StringUtils.isBlank(excelData.getOptionB())) {
                        throw new BusinessException("选项B不存在，无法设为正确答案");
                    }
                    break;
                case 'C':
                    if (StringUtils.isBlank(excelData.getOptionC())) {
                        throw new BusinessException("选项C不存在，无法设为正确答案");
                    }
                    break;
                case 'D':
                    if (StringUtils.isBlank(excelData.getOptionD())) {
                        throw new BusinessException("选项D不存在，无法设为正确答案");
                    }
                    break;
                case 'E':
                    if (StringUtils.isBlank(excelData.getOptionE())) {
                        throw new BusinessException("选项E不存在，无法设为正确答案");
                    }
                    break;
                case 'F':
                    if (StringUtils.isBlank(excelData.getOptionF())) {
                        throw new BusinessException("选项F不存在，无法设为正确答案");
                    }
                    break;
                default:
                    throw new BusinessException("未知的选项标识: " + c);
            }
        }
    }

    /**
     * 构建选项列表
     */
    private List<QuestionOptionDTO> buildOptions(QuestionExcelData excelData) {
        List<QuestionOptionDTO> options = new ArrayList<>();
        
        // 简答题不需要选项
        if (excelData.getType() == 4) {
            return options;
        }
        
        // 填空题不需要选项
        if (excelData.getType() == 3) {
            return options;
        }
        
        String correctAnswer = excelData.getCorrectAnswer() != null ? excelData.getCorrectAnswer().toUpperCase() : "";
        
        // 添加选项A
        if (StringUtils.isNotBlank(excelData.getOptionA())) {
            options.add(QuestionOptionDTO.builder()
                    .content(excelData.getOptionA())
                    .isCorrect(correctAnswer.contains("A"))
                    .orderIndex(0)
                    .build());
        }
        
        // 添加选项B
        if (StringUtils.isNotBlank(excelData.getOptionB())) {
            options.add(QuestionOptionDTO.builder()
                    .content(excelData.getOptionB())
                    .isCorrect(correctAnswer.contains("B"))
                    .orderIndex(1)
                    .build());
        }
        
        // 添加选项C
        if (StringUtils.isNotBlank(excelData.getOptionC())) {
            options.add(QuestionOptionDTO.builder()
                    .content(excelData.getOptionC())
                    .isCorrect(correctAnswer.contains("C"))
                    .orderIndex(2)
                    .build());
        }
        
        // 添加选项D
        if (StringUtils.isNotBlank(excelData.getOptionD())) {
            options.add(QuestionOptionDTO.builder()
                    .content(excelData.getOptionD())
                    .isCorrect(correctAnswer.contains("D"))
                    .orderIndex(3)
                    .build());
        }
        
        // 添加选项E
        if (StringUtils.isNotBlank(excelData.getOptionE())) {
            options.add(QuestionOptionDTO.builder()
                    .content(excelData.getOptionE())
                    .isCorrect(correctAnswer.contains("E"))
                    .orderIndex(4)
                    .build());
        }
        
        // 添加选项F
        if (StringUtils.isNotBlank(excelData.getOptionF())) {
            options.add(QuestionOptionDTO.builder()
                    .content(excelData.getOptionF())
                    .isCorrect(correctAnswer.contains("F"))
                    .orderIndex(5)
                    .build());
        }
        
        return options;
    }
    
    /**
     * 加载机构所有标签
     */
    private void loadAllTags() {
        try {
            // 获取机构的所有标签
            List<QuestionTagVO> tags = questionTagService.getAllTags(institutionId);
            
            // 缓存标签
            for (QuestionTagVO tag : tags) {
                tagCache.put(tag.getName(), tag);
            }
            
            log.info("已加载机构标签 {} 个", tagCache.size());
        } catch (Exception e) {
            log.error("加载机构标签失败", e);
        }
    }
    
    /**
     * 处理题目标签
     */
    private void processQuestionTags(Long questionId, String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return;
        }
        
        // 拆分标签
        String[] tagNames = tagsStr.split(",");
        for (String tagName : tagNames) {
            tagName = tagName.trim();
            if (tagName.isEmpty()) {
                continue;
            }
            
            try {
                // 使用新的事务处理标签
                final String finalTagName = tagName;
                transactionTemplate.execute(new TransactionCallback<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus status) {
                        try {
                            // 检查标签是否存在
                            Long tagId = getOrCreateTag(finalTagName);
                            
                            // 关联标签到题目
                            if (tagId != null) {
                                questionTagService.addTagToQuestion(questionId, tagId, institutionId);
                            }
                            return null;
                        } catch (Exception e) {
                            status.setRollbackOnly();
                            throw e;
                        }
                    }
                });
            } catch (Exception e) {
                // 记录警告但不影响主流程
                log.warn("为题目 {} 添加标签 {} 失败: {}", questionId, tagName, e.getMessage());
            }
        }
    }
    
    /**
     * 获取或创建标签
     */
    private Long getOrCreateTag(String tagName) {
        // 先从缓存中查找
        if (tagCache.containsKey(tagName)) {
            return tagCache.get(tagName).getId();
        }
        
        try {
            // 创建新标签
            com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO tagDTO = 
                com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO.builder()
                    .institutionId(institutionId)
                    .name(tagName)
                    .build();
            
            com.zhangziqi.online_course_mine.model.vo.QuestionTagVO newTag = 
                questionTagService.createTag(tagDTO, userId);
            
            // 添加到缓存
            if (newTag != null) {
                tagCache.put(tagName, newTag);
                return newTag.getId();
            }
        } catch (Exception e) {
            log.error("创建标签 {} 失败: {}", tagName, e.getMessage());
        }
        
        return null;
    }
} 