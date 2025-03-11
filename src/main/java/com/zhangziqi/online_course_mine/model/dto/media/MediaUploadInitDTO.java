package com.zhangziqi.online_course_mine.model.dto.media;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 媒体上传初始化DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadInitDTO {
    
    @NotBlank(message = "标题不能为空")
    private String title;                // 媒体标题
    
    private String description;          // 媒体描述（可选）
    
    @NotBlank(message = "文件名不能为空")
    private String filename;             // 原始文件名
    
    @NotBlank(message = "内容类型不能为空")
    private String contentType;          // 内容类型
    
    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    private Long fileSize;               // 文件大小（字节）
    
    private Integer chunkSize;           // 分片大小（字节，可选）
} 