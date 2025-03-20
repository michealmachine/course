package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.UserWrongQuestionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户错题实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_wrong_questions")
public class UserWrongQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * 课程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    
    /**
     * 小节ID
     */
    @Column(name = "section_id")
    private Long sectionId;
    
    /**
     * 问题ID
     */
    @Column(name = "question_id")
    private Long questionId;
    
    /**
     * 问题标题
     */
    @Column(name = "question_title", length = 500)
    private String questionTitle;
    
    /**
     * 问题类型
     */
    @Column(name = "question_type", length = 50)
    private String questionType;
    
    /**
     * 正确答案 (JSON字符串)
     */
    @Column(name = "correct_answers", columnDefinition = "TEXT")
    private String correctAnswers;
    
    /**
     * 用户回答 (JSON字符串)
     */
    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;
    
    /**
     * 状态：未解决(0)，已解决(1)
     * @see UserWrongQuestionStatus
     */
    @Column(name = "status")
    private Integer status;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 