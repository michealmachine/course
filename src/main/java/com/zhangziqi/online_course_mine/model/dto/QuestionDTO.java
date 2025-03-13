package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 题目数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    
    /**
     * 题目ID（创建时为null，更新时必须提供）
     */
    private Long id;
    
    /**
     * 所属机构ID
     */
    @NotNull(message = "机构ID不能为空")
    private Long institutionId;
    
    /**
     * 题目标题
     */
    @NotBlank(message = "题目标题不能为空")
    @Size(max = 255, message = "题目标题长度不能超过255个字符")
    private String title;
    
    /**
     * 题目内容
     */
    @Size(max = 2000, message = "题目内容长度不能超过2000个字符")
    private String content;
    
    /**
     * 题目类型：1-单选题，2-多选题
     */
    @NotNull(message = "题目类型不能为空")
    @Min(value = 1, message = "题目类型值无效")
    @Max(value = 2, message = "题目类型值无效")
    private Integer type;
    
    /**
     * 难度级别：1-简单，2-中等，3-困难
     */
    @NotNull(message = "难度级别不能为空")
    @Min(value = 1, message = "难度级别必须介于1-3之间")
    @Max(value = 3, message = "难度级别必须介于1-3之间")
    private Integer difficulty;
    
    /**
     * 分值
     */
    @NotNull(message = "分值不能为空")
    @Min(value = 1, message = "分值必须大于0")
    @Max(value = 100, message = "分值不能超过100")
    private Integer score;
    
    /**
     * 解析说明
     */
    @Size(max = 2000, message = "解析说明长度不能超过2000个字符")
    private String analysis;
    
    /**
     * 题目选项列表
     */
    @Valid
    @NotNull(message = "题目选项不能为空")
    @Size(min = 2, message = "至少需要2个选项")
    private List<QuestionOptionDTO> options;
} 