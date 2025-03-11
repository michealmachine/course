package com.zhangziqi.online_course_mine.model.dto.media;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CompleteUploadDTO {
    
    /**
     * 上传ID
     */
    @NotNull(message = "上传ID不能为空")
    private String uploadId;
    
    /**
     * 已完成的分片信息列表
     */
    @NotEmpty(message = "分片信息不能为空")
    private List<PartInfo> completedParts;
    
    @Data
    public static class PartInfo {
        /**
         * 分片编号
         */
        private int partNumber;
        
        /**
         * 分片的ETag
         */
        private String eTag;
    }
} 