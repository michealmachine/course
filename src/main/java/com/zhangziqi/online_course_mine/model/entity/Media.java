package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 媒体资源实体
 */
@Entity
@Table(name = "media")
@Data
@EqualsAndHashCode(callSuper = false)
public class Media {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 媒体标题
     */
    private String title;
    
    /**
     * 媒体描述
     */
    private String description;
    
    /**
     * 媒体类型（视频、音频、文档）
     */
    @Enumerated(EnumType.STRING)
    private MediaType type;
    
    /**
     * 文件大小（字节）
     */
    private Long size;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 存储路径
     */
    private String storagePath;
    
    /**
     * 媒体状态
     */
    @Enumerated(EnumType.STRING)
    private MediaStatus status;
    
    /**
     * 所属机构
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
    
    /**
     * 上传者ID（只记录ID，不做实体关联）
     */
    @Column(name = "uploader_id")
    private Long uploaderId;
    
    /**
     * 上传时间
     */
    @CreationTimestamp
    private LocalDateTime uploadTime;
    
    /**
     * 最后访问时间
     */
    @UpdateTimestamp
    private LocalDateTime lastAccessTime;
} 