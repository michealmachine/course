package com.zhangziqi.online_course_mine.model.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 上传初始化响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadInitiationVO {
    private Long mediaId;                        // 媒体ID
    private String uploadId;                     // 上传ID
    private Integer totalParts;                  // 总分片数
    private Long chunkSize;                      // 分片大小（字节）
    private List<PresignedUrlInfo> presignedUrls; // 预签名URL列表
} 