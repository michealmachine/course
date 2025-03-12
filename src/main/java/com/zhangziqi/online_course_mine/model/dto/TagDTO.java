package com.zhangziqi.online_course_mine.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 标签数据传输对象
 */
@Data
public class TagDTO {

    /**
     * 标签ID（更新时使用）
     */
    private Long id;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String name;

    /**
     * 标签描述
     */
    @Size(max = 255, message = "标签描述不能超过255个字符")
    private String description;
} 