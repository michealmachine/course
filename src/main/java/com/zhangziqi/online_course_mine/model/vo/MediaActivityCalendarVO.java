package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.dto.media.MediaActivityDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 媒体活动日历视图对象
 * 用于生成日历热图数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaActivityCalendarVO {
    
    /**
     * 日历数据（每日活动）
     */
    private List<MediaActivityDTO> calendarData;
    
    /**
     * 峰值活动数
     */
    private Long peakCount;
    
    /**
     * 最活跃日期
     */
    private LocalDate mostActiveDate;
    
    /**
     * 总活动数
     */
    private Long totalCount;
    
    /**
     * 总文件大小（字节）
     */
    private Long totalSize;
} 