package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习课程结构视图对象
 * 用于返回课程的完整结构，包括用户的学习进度信息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LearningCourseStructureVO extends CourseStructureVO {
    
    /**
     * 用户的学习进度信息
     */
    private UserCourseVO userProgress;
    
    /**
     * 当前学习位置
     */
    private CurrentLearningPositionVO currentPosition;
    
    /**
     * 从课程实体和用户课程记录创建学习课程结构VO
     */
    public static LearningCourseStructureVO fromEntity(Course course, UserCourse userCourse) {
        if (course == null) {
            return null;
        }
        
        // 构建基本课程信息
        CourseStructureVO baseStructure = CourseStructureVO.fromEntity(course);
        
        // 构建当前学习位置
        CurrentLearningPositionVO position = new CurrentLearningPositionVO();
        if (userCourse != null) {
            position.setChapterId(userCourse.getCurrentChapterId());
            position.setSectionId(userCourse.getCurrentSectionId());
            position.setSectionProgress(userCourse.getCurrentSectionProgress());
        }
        
        // 使用适当的构建器模式构建LearningCourseStructureVO
        return LearningCourseStructureVO.builder()
                .course(baseStructure.getCourse())
                .chapters(baseStructure.getChapters())
                .userProgress(userCourse != null ? UserCourseVO.fromEntity(userCourse) : null)
                .currentPosition(position)
                .build();
    }
    
    /**
     * 当前学习位置视图对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentLearningPositionVO {
        
        /**
         * 当前章节ID
         */
        private Long chapterId;
        
        /**
         * 当前小节ID
         */
        private Long sectionId;
        
        /**
         * 当前小节进度百分比
         */
        private Integer sectionProgress;
    }
} 