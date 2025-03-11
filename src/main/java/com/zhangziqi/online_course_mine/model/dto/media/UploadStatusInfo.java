package com.zhangziqi.online_course_mine.model.dto.media;

import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 上传状态信息
 * 用于在Redis中保存分片上传的状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadStatusInfo {
    private Long mediaId;            // 媒体ID
    private Long institutionId;      // 机构ID
    private Long uploaderId;         // 上传者ID
    private String uploadId;         // S3上传ID
    private String objectKey;        // S3对象键
    private String filename;         // 原始文件名
    private String contentType;      // 内容类型
    private Long fileSize;           // 文件总大小
    private MediaStatus status;      // 上传状态
    private Integer totalParts;      // 总分片数
    
    @Builder.Default
    private List<PartInfo> completedParts = new ArrayList<>(); // 已完成的分片信息
    
    private LocalDateTime initiatedAt;     // 初始化时间
    private LocalDateTime lastUpdatedAt;   // 最后更新时间
    private LocalDateTime expiresAt;       // 过期时间
    
    /**
     * 分片信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartInfo {
        private Integer partNumber;  // 分片编号
        private String eTag;         // 分片ETag
    }
} 