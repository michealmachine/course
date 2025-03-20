package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.LearningProgressUpdateDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQuestionAnswerDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.vo.LearningCourseStructureVO;
import com.zhangziqi.online_course_mine.model.vo.LearningStatisticsVO;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.SectionVO;
import com.zhangziqi.online_course_mine.model.vo.UserCourseVO;
import com.zhangziqi.online_course_mine.model.vo.UserWrongQuestionVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.SectionRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.CourseService;
import com.zhangziqi.online_course_mine.service.LearningStatisticsService;
import com.zhangziqi.online_course_mine.service.MediaService;
import com.zhangziqi.online_course_mine.service.QuestionGroupService;
import com.zhangziqi.online_course_mine.service.SectionService;
import com.zhangziqi.online_course_mine.service.UserCourseService;
import com.zhangziqi.online_course_mine.service.WrongQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习控制器
 * 用于管理用户学习过程中的资源访问与进度更新
 */
@Slf4j
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@Tag(name = "学习管理", description = "课程学习过程中的资源访问与进度更新等功能")
public class LearningController {
    
    private final CourseService courseService;
    private final UserCourseService userCourseService;
    private final SectionService sectionService;
    private final MediaService mediaService;
    private final QuestionGroupService questionGroupService;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final SectionRepository sectionRepository;
    private final LearningStatisticsService learningStatisticsService;
    private final WrongQuestionService wrongQuestionService;
    
    /**
     * 获取课程学习结构
     * 包含课程信息、章节信息、小节信息和用户学习进度
     */
    @GetMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取课程学习结构", description = "获取课程的完整结构和用户的学习进度")
    public Result<LearningCourseStructureVO> getCourseStructure(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取课程学习结构, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        // 获取课程实体
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 检查课程状态
        if (!course.getIsPublishedVersion() || course.getStatus() != CourseStatus.PUBLISHED.getValue()) {
            throw new BusinessException(403, "该课程尚未发布，无法学习");
        }
        
        // 检查用户是否已购买课程
        boolean hasPurchased = userCourseService.hasPurchasedCourse(userId, courseId);
        if (!hasPurchased) {
            throw new BusinessException(403, "请先购买课程再进行学习");
        }
        
        // 获取用户的学习记录
        UserCourse userCourse = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到学习记录"));
        
        // 构建课程学习结构VO
        LearningCourseStructureVO learningStructure = LearningCourseStructureVO.fromEntity(course, userCourse);
        
        log.info("成功获取课程学习结构, 用户ID: {}, 课程ID: {}", userId, courseId);
        return Result.success(learningStructure);
    }
    
    /**
     * 获取小节媒体资源
     */
    @GetMapping("/sections/{sectionId}/media")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取小节媒体资源", description = "获取指定小节的媒体资源，包含临时访问URL")
    public Result<MediaVO> getSectionMedia(
            @Parameter(description = "小节ID") @PathVariable Long sectionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取小节媒体资源, 用户ID: {}, 小节ID: {}", userId, sectionId);
        
        // 获取小节信息
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + sectionId));
        
        // 检查小节是否有关联课程
        if (section.getChapter() == null || section.getChapter().getCourse() == null) {
            throw new ResourceNotFoundException("小节未关联有效课程");
        }
        
        Long courseId = section.getChapter().getCourse().getId();
        
        // 检查用户是否已购买课程
        boolean hasPurchased = userCourseService.hasPurchasedCourse(userId, courseId);
        if (!hasPurchased) {
            throw new BusinessException(403, "请先购买课程再访问资源");
        }
        
        // 检查小节资源类型
        if (!"MEDIA".equals(section.getResourceTypeDiscriminator())) {
            throw new ResourceNotFoundException("该小节不是媒体类型资源");
        }
        
        Long mediaId = section.getMediaId();
        if (mediaId == null) {
            throw new ResourceNotFoundException("小节未关联媒体资源");
        }
        
        // 获取媒体资源（包含临时访问URL）
        MediaVO mediaVO = mediaService.getMediaByIdForPreview(mediaId);
        
        log.info("成功获取小节媒体资源, 用户ID: {}, 小节ID: {}, 媒体ID: {}", userId, sectionId, mediaId);
        return Result.success(mediaVO);
    }
    
    /**
     * 获取小节题组资源
     */
    @GetMapping("/sections/{sectionId}/question-group")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取小节题组资源", description = "获取指定小节的题组资源，包含详细题目")
    public Result<QuestionGroupVO> getSectionQuestionGroup(
            @Parameter(description = "小节ID") @PathVariable Long sectionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取小节题组资源, 用户ID: {}, 小节ID: {}", userId, sectionId);
        
        // 获取小节信息
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + sectionId));
        
        // 检查小节是否有关联课程
        if (section.getChapter() == null || section.getChapter().getCourse() == null) {
            throw new ResourceNotFoundException("小节未关联有效课程");
        }
        
        Long courseId = section.getChapter().getCourse().getId();
        
        // 检查用户是否已购买课程
        boolean hasPurchased = userCourseService.hasPurchasedCourse(userId, courseId);
        if (!hasPurchased) {
            throw new BusinessException(403, "请先购买课程再访问资源");
        }
        
        // 检查小节资源类型
        if (!"QUESTION_GROUP".equals(section.getResourceTypeDiscriminator())) {
            throw new ResourceNotFoundException("该小节不是题组类型资源");
        }
        
        Long questionGroupId = section.getQuestionGroupId();
        if (questionGroupId == null) {
            throw new ResourceNotFoundException("小节未关联题组资源");
        }
        
        // 获取题组详情，包含题目
        QuestionGroupVO questionGroupVO = questionGroupService.getGroupByIdForPreview(questionGroupId, true);
        
        log.info("成功获取小节题组资源, 用户ID: {}, 小节ID: {}, 题组ID: {}", userId, sectionId, questionGroupId);
        return Result.success(questionGroupVO);
    }
    
    /**
     * 更新学习进度
     */
    @PutMapping("/courses/{courseId}/progress")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新学习进度", description = "更新当前用户的指定课程学习进度")
    public Result<UserCourseVO> updateLearningProgress(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学习进度更新信息") @RequestBody LearningProgressUpdateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("更新用户课程学习进度, 用户ID: {}, 课程ID: {}, 章节ID: {}, 小节ID: {}, 进度: {}%", 
                userId, courseId, dto.getChapterId(), dto.getSectionId(), dto.getSectionProgress());
        
        UserCourseVO userCourseVO = userCourseService.updateLearningProgress(userId, courseId, dto);
        return Result.success(userCourseVO);
    }
    
    /**
     * 记录学习时长
     */
    @PutMapping("/courses/{courseId}/duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "记录学习时长", description = "记录当前用户的指定课程学习时长")
    public Result<UserCourseVO> recordLearningDuration(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学习时长(秒)") @RequestParam Integer duration) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("记录用户课程学习时长, 用户ID: {}, 课程ID: {}, 时长: {}秒", userId, courseId, duration);
        
        UserCourseVO userCourseVO = userCourseService.recordLearningDuration(userId, courseId, duration);
        return Result.success(userCourseVO);
    }
    
    /**
     * 提交问题答案
     */
    @PostMapping("/sections/{sectionId}/questions/{questionId}/answer")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "提交问题答案", description = "提交用户对指定题目的回答")
    public Result<Void> submitQuestionAnswer(
            @Parameter(description = "小节ID") @PathVariable Long sectionId,
            @Parameter(description = "题目ID") @PathVariable Long questionId,
            @Parameter(description = "答案信息") @RequestBody UserQuestionAnswerDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("提交问题答案, 用户ID: {}, 小节ID: {}, 题目ID: {}", userId, sectionId, questionId);
        
        // 获取课程ID
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在"));
        
        Long courseId = section.getChapter().getCourse().getId();
        
        // 如果答错了，保存到错题本
        if (dto.getIsWrong() != null && dto.getIsWrong()) {
            log.info("用户答错题目, 用户ID: {}, 题目ID: {}, 用户答案: {}", userId, questionId, dto.getAnswers());
            wrongQuestionService.saveWrongQuestion(userId, courseId, sectionId, questionId, dto);
        }
        
        return Result.success();
    }

    /**
     * 学习统计相关API
     */
    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    public Result<LearningStatisticsVO> getLearningStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户学习统计, 用户ID: {}", userId);
        
        LearningStatisticsVO statistics = learningStatisticsService.getUserLearningStatistics(userId);
        return Result.success(statistics);
    }

    /**
     * 获取课程学习统计
     */
    @GetMapping("/courses/{courseId}/statistics")
    @ResponseStatus(HttpStatus.OK)
    public Result<LearningStatisticsVO.CourseStatisticsVO> getCourseLearningStatistics(@PathVariable Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户课程学习统计, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        LearningStatisticsVO.CourseStatisticsVO statistics = 
                learningStatisticsService.getUserCourseLearningStatistics(userId, courseId);
        return Result.success(statistics);
    }

    /**
     * 重置课程学习进度
     */
    @PutMapping("/courses/{courseId}/reset-progress")
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> resetCourseProgress(@PathVariable Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("重置用户课程学习进度, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        learningStatisticsService.resetUserCourseProgress(userId, courseId);
        return Result.success();
    }

    /**
     * 获取用户错题列表(分页)
     */
    @GetMapping("/wrong-questions")
    @ResponseStatus(HttpStatus.OK)
    public Result<Page<UserWrongQuestionVO>> getWrongQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastWrongTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户错题列表(分页), 用户ID: {}, 页码: {}, 每页数量: {}", userId, page, size);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserWrongQuestionVO> wrongQuestions = wrongQuestionService.getUserWrongQuestions(userId, pageable);
        return Result.success(wrongQuestions);
    }

    /**
     * 获取课程错题列表(分页)
     */
    @GetMapping("/courses/{courseId}/wrong-questions")
    @ResponseStatus(HttpStatus.OK)
    public Result<Page<UserWrongQuestionVO>> getCourseWrongQuestions(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastWrongTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户课程错题列表(分页), 用户ID: {}, 课程ID: {}, 页码: {}, 每页数量: {}", 
                userId, courseId, page, size);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserWrongQuestionVO> wrongQuestions = 
                wrongQuestionService.getUserCourseWrongQuestions(userId, courseId, pageable);
        return Result.success(wrongQuestions);
    }

    /**
     * 获取未解决错题列表(分页)
     */
    @GetMapping("/wrong-questions/unresolved")
    @ResponseStatus(HttpStatus.OK)
    public Result<Page<UserWrongQuestionVO>> getUnresolvedWrongQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastWrongTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户未解决错题列表(分页), 用户ID: {}, 页码: {}, 每页数量: {}", userId, page, size);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserWrongQuestionVO> wrongQuestions = 
                wrongQuestionService.getUserUnresolvedWrongQuestions(userId, pageable);
        return Result.success(wrongQuestions);
    }

    /**
     * 将错题标记为已解决
     */
    @PutMapping("/wrong-questions/{wrongQuestionId}/resolve")
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> resolveWrongQuestion(@PathVariable Long wrongQuestionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("将错题标记为已解决, 用户ID: {}, 错题ID: {}", userId, wrongQuestionId);
        
        wrongQuestionService.resolveWrongQuestion(userId, wrongQuestionId);
        return Result.success();
    }

    /**
     * 删除错题
     */
    @DeleteMapping("/wrong-questions/{wrongQuestionId}")
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> deleteWrongQuestion(@PathVariable Long wrongQuestionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("删除错题, 用户ID: {}, 错题ID: {}", userId, wrongQuestionId);
        
        wrongQuestionService.deleteWrongQuestion(userId, wrongQuestionId);
        return Result.success();
    }
    
    /**
     * 删除用户所有错题
     */
    @DeleteMapping("/wrong-questions/all")
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> deleteAllWrongQuestions() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("删除用户所有错题, 用户ID: {}", userId);
        
        wrongQuestionService.deleteAllUserWrongQuestions(userId);
        return Result.success();
    }
    
    /**
     * 删除用户特定课程的所有错题
     */
    @DeleteMapping("/courses/{courseId}/wrong-questions/all")
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> deleteAllCourseWrongQuestions(@PathVariable Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("删除用户课程所有错题, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        wrongQuestionService.deleteAllUserWrongQuestionsByCourse(userId, courseId);
        return Result.success();
    }
} 