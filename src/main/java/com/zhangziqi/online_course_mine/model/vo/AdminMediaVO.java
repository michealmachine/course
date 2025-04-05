package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 管理员媒体信息VO
 * 扩展了MediaVO，添加了机构名称和上传者名称
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminMediaVO extends MediaVO {
    /**
     * 机构名称
     */
    private String institutionName;
    
    /**
     * 上传者用户名
     */
    private String uploaderUsername;
    
    /**
     * 格式化后的文件大小
     */
    private String formattedSize;
} 