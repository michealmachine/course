package com.zhangziqi.online_course_mine.model.dto.section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * 小节资源数据传输对象
 * 
 * @deprecated 此类已弃用，推荐使用 {@link com.zhangziqi.online_course_mine.service.SectionService#setMediaResource(Long, Long, String)} 方法
 */
@Deprecated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionResourceDTO {
    
    /**
     * 小节ID
     */
    @NotNull(message = "小节ID不能为空")
    private Long sectionId;
    
    /**
     * 媒体资源ID
     */
    @NotNull(message = "媒体资源ID不能为空")
    private Long mediaId;
    
    /**
     * 资源类型(primary, supplementary, homework, reference)
     */
    @NotNull(message = "资源类型不能为空")
    private String resourceType;
    
    /**
     * 排序索引
     */
    @Min(value = 0, message = "排序索引不能为负数")
    private Integer orderIndex;
} 