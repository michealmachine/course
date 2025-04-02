package com.zhangziqi.online_course_mine.model.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 媒体活动数据传输对象
 * 用于表示某日期的媒体活动数量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaActivityDTO {
    
    /**
     * 活动日期
     */
    private LocalDate date;
    
    /**
     * 活动数量
     */
    private Long count;
    
    /**
     * 总大小（字节）
     */
    private Long totalSize;
} 