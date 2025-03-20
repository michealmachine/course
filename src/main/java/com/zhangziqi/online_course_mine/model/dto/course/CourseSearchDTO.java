package com.zhangziqi.online_course_mine.model.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程搜索数据传输对象
 * 用于封装课程搜索参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程搜索DTO")
public class CourseSearchDTO {
    
    /**
     * 搜索关键词，用于匹配课程标题、描述
     */
    @Schema(description = "搜索关键词", example = "Java编程")
    private String keyword;
    
    /**
     * 课程分类ID
     */
    @Schema(description = "课程分类ID", example = "1")
    private Long categoryId;
    
    /**
     * 课程标签ID列表
     */
    @Schema(description = "课程标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;
    
    /**
     * 课程难度级别 (1-初级, 2-中级, 3-高级)
     */
    @Schema(description = "课程难度级别(1-初级, 2-中级, 3-高级)", example = "2")
    private Integer difficulty;
    
    /**
     * 最低价格
     */
    @Schema(description = "最低价格", example = "0")
    private BigDecimal minPrice;
    
    /**
     * 最高价格
     */
    @Schema(description = "最高价格", example = "100")
    private BigDecimal maxPrice;
    
    /**
     * 付费类型 (0-免费, 1-付费)
     */
    @Schema(description = "付费类型(0-免费, 1-付费)", example = "0")
    private Integer paymentType;
    
    /**
     * 机构ID
     */
    @Schema(description = "机构ID", example = "1")
    private Long institutionId;

    /**
     * 排序方式
     */
    @Schema(description = "排序方式", example = "rating")
    private String sortBy;
    
    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer page;
    
    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;
} 