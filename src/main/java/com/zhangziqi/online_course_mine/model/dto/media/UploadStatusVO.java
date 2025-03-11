package com.zhangziqi.online_course_mine.model.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传状态响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadStatusVO {
    private Long mediaId;                    // 媒体ID
    private String status;                   // 上传状态
    private Integer totalParts;              // 总分片数
    private Integer completedParts;          // 已完成分片数
    private Double progressPercentage;       // 上传进度百分比
    private List<Integer> completedPartNumbers; // 已完成的分片编号列表
    private LocalDateTime initiatedAt;       // 初始化时间
    private LocalDateTime lastUpdatedAt;     // 最后更新时间
    private LocalDateTime expiresAt;         // 过期时间
} 