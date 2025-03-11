package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 媒体信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaVO {
    private Long id;                // 媒体ID
    private String title;           // 标题
    private String description;     // 描述
    private String type;            // 媒体类型
    private Long size;              // 文件大小
    private String originalFilename; // 原始文件名
    private String status;          // 状态
    private Long institutionId;     // 机构ID
    private Long uploaderId;        // 上传者ID
    private LocalDateTime uploadTime; // 上传时间
    private LocalDateTime lastAccessTime; // 最后访问时间
    private String accessUrl;       // 访问URL（可能为空，需要单独请求）
} 