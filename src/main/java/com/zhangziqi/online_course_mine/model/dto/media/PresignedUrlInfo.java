package com.zhangziqi.online_course_mine.model.dto.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预签名URL信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlInfo {
    private Integer partNumber; // 分片编号
    private String url;         // 预签名URL
} 