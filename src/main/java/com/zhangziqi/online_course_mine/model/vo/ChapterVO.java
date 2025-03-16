package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.enums.ChapterAccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 章节值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterVO {

    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer accessType;
    private Long courseId;
    private String courseName;
    private List<SectionVO> sections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为VO
     */
    public static ChapterVO fromEntity(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        
        ChapterVO.ChapterVOBuilder builder = ChapterVO.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .orderIndex(chapter.getOrderIndex())
                .accessType(chapter.getAccessType())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt());
        
        // 设置课程信息
        if (chapter.getCourse() != null) {
            builder.courseId(chapter.getCourse().getId());
            builder.courseName(chapter.getCourse().getTitle());
        }
        
        // 章节不包含小节时返回空列表
        builder.sections(new ArrayList<>());
        
        return builder.build();
    }
    
    /**
     * 从实体转换为VO，包含小节信息
     */
    public static ChapterVO fromEntityWithSections(Chapter chapter) {
        ChapterVO chapterVO = fromEntity(chapter);
        
        if (chapterVO != null && chapter.getSections() != null && !chapter.getSections().isEmpty()) {
            chapterVO.setSections(chapter.getSections().stream()
                    .map(SectionVO::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return chapterVO;
    }
    
    /**
     * 获取访问类型枚举
     */
    public ChapterAccessType getAccessTypeEnum() {
        return ChapterAccessType.getByValue(this.accessType);
    }
} 