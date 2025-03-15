package com.zhangziqi.online_course_mine.model.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.util.Set;

/**
 * 课程创建数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateDTO {
    
    /**
     * 课程标题
     */
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 200, message = "课程标题长度不能超过200个字符")
    private String title;
    
    /**
     * 课程描述
     */
    @Size(max = 2000, message = "课程描述长度不能超过2000个字符")
    private String description;
    
    /**
     * 课程分类ID
     */
    private Long categoryId;
    
    /**
     * 课程标签ID列表
     */
    private Set<Long> tagIds;
    
    /**
     * 付费类型 (0-免费, 1-付费)
     */
    @NotNull(message = "付费类型不能为空")
    private Integer paymentType;
    
    /**
     * 课程价格 (付费课程必填)
     */
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    @DecimalMax(value = "100000.0", message = "价格不能超过100000")
    private BigDecimal price;
    
    /**
     * 折扣价格
     */
    @DecimalMin(value = "0.0", message = "折扣价格不能为负数")
    @DecimalMax(value = "100000.0", message = "折扣价格不能超过100000")
    private BigDecimal discountPrice;
    
    /**
     * 难度级别 (1-初级, 2-中级, 3-高级)
     */
    @Min(value = 1, message = "难度级别最小为1")
    @Max(value = 3, message = "难度级别最大为3")
    private Integer difficulty;
    
    /**
     * 适合人群
     */
    @Size(max = 1000, message = "适合人群描述长度不能超过1000个字符")
    private String targetAudience;
    
    /**
     * 学习目标
     */
    @Size(max = 1000, message = "学习目标描述长度不能超过1000个字符")
    private String learningObjectives;
} 