package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Chapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程结构视图对象
 * 用于一次性返回课程的完整结构，包括章节和小节信息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStructureVO {
    
    /**
     * 课程基本信息
     */
    private CourseVO course;
    
    /**
     * 章节列表，包含小节信息
     */
    @Builder.Default
    private List<ChapterVO> chapters = new ArrayList<>();
    
    /**
     * 从课程实体创建课程结构VO（包含完整层次结构）
     */
    public static CourseStructureVO fromEntity(Course course) {
        if (course == null) {
            return null;
        }
        
        // 构建基本课程信息
        CourseVO courseVO = CourseVO.fromEntity(course);
        
        // 构建章节信息，并包含小节
        List<ChapterVO> chapterVOs = new ArrayList<>();
        if (course.getChapters() != null && !course.getChapters().isEmpty()) {
            chapterVOs = course.getChapters().stream()
                    .map(ChapterVO::fromEntityWithSections)
                    .collect(Collectors.toList());
        }
        
        // 构建完整结构
        return CourseStructureVO.builder()
                .course(courseVO)
                .chapters(chapterVOs)
                .build();
    }
} 