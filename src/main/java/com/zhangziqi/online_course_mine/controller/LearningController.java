package com.zhangziqi.online_course_mine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.LearningProgressUpdateDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordCompletedDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordEndDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordStartDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQuestionAnswerDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.DateLearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningCourseStructureVO;
import com.zhangziqi.online_course_mine.model.vo.LearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningRecordVO;
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
import com.zhangziqi.online_course_mine.service.LearningRecordService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final LearningRecordService learningRecordService;
    private final ObjectMapper objectMapper;

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
     * 记录学习时长（旧方法，建议使用学习记录相关接口）
     * @deprecated 使用学习记录相关接口替代
     */
    @Deprecated
    @PutMapping("/courses/{courseId}/duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "记录学习时长", description = "记录当前用户的指定课程学习时长（已废弃，建议使用学习记录相关接口）")
    public Result<UserCourseVO> recordLearningDuration(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学习时长(秒)") @RequestParam Integer duration) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("记录用户课程学习时长, 用户ID: {}, 课程ID: {}, 时长: {}秒", userId, courseId, duration);

        // 创建一个完成的学习活动记录
        LearningRecordCompletedDTO recordDTO = LearningRecordCompletedDTO.builder()
                .courseId(courseId)
                .activityType(LearningActivityType.VIDEO_WATCH.getCode())
                .durationSeconds(duration)
                .build();

        learningRecordService.recordCompletedActivity(userId, recordDTO);

        // 同时更新旧的学习时长记录
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
        Long chapterId = section.getChapter().getId();

        // 记录测验尝试活动
        LearningRecordCompletedDTO activityDto = LearningRecordCompletedDTO.builder()
                .courseId(courseId)
                .chapterId(chapterId)
                .sectionId(sectionId)
                .activityType(LearningActivityType.QUIZ_ATTEMPT.getCode())
                .durationSeconds(dto.getDuration() != null ? (int)(dto.getDuration() / 1000) : 0)
                .contextData(createQuizContextData(questionId, dto))
                .build();

        LearningRecordVO learningRecord = learningRecordService.recordCompletedActivity(userId, activityDto);

        // 如果答错了，保存到错题本，并关联学习记录ID
        if (dto.getIsWrong() != null && dto.getIsWrong()) {
            log.info("用户答错题目, 用户ID: {}, 题目ID: {}, 用户答案: {}", userId, questionId, dto.getAnswers());
            dto.setLearningRecordId(learningRecord.getId()); // 设置学习记录ID
            wrongQuestionService.saveWrongQuestion(userId, courseId, sectionId, questionId, dto);
        }

        return Result.success();
    }

    /**
     * 创建测验上下文数据
     */
    private String createQuizContextData(Long questionId, UserQuestionAnswerDTO dto) {
        try {
            Map<String, Object> contextData = new HashMap<>();
            contextData.put("questionId", questionId);
            contextData.put("isCorrect", !Boolean.TRUE.equals(dto.getIsWrong()));
            return objectMapper.writeValueAsString(contextData);
        } catch (Exception e) {
            log.error("创建测验上下文数据失败", e);
            return null;
        }
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
     * 开始学习活动
     */
    @PostMapping("/records/start")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "开始学习活动", description = "记录用户开始的学习活动")
    public Result<LearningRecordVO> startLearningActivity(@RequestBody LearningRecordStartDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("开始学习活动, 用户ID: {}, 课程ID: {}, 活动类型: {}", userId, dto.getCourseId(), dto.getActivityType());

        LearningRecordVO record = learningRecordService.startActivity(userId, dto);
        return Result.success(record);
    }

    /**
     * 结束学习活动
     */
    @PutMapping("/records/{recordId}/end")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "结束学习活动", description = "记录用户结束的学习活动")
    public Result<LearningRecordVO> endLearningActivity(
            @PathVariable Long recordId,
            @RequestBody(required = false) LearningRecordEndDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("结束学习活动, 用户ID: {}, 记录ID: {}", userId, recordId);

        LearningRecordVO record = learningRecordService.endActivity(userId, recordId,
                dto != null ? dto : new LearningRecordEndDTO());
        return Result.success(record);
    }

    /**
     * 记录已完成的学习活动
     */
    @PostMapping("/records/completed")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "记录已完成的学习活动", description = "一次性记录已完成的学习活动")
    public Result<LearningRecordVO> recordCompletedActivity(@RequestBody LearningRecordCompletedDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("记录已完成学习活动, 用户ID: {}, 课程ID: {}, 活动类型: {}", userId, dto.getCourseId(), dto.getActivityType());

        LearningRecordVO record = learningRecordService.recordCompletedActivity(userId, dto);
        return Result.success(record);
    }

    /**
     * 查找用户当前进行中的活动
     */
    @GetMapping("/records/ongoing")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查找用户当前进行中的活动", description = "查找用户当前进行中的学习活动")
    public Result<LearningRecordVO> findOngoingActivity() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("查找用户当前进行中的学习活动, 用户ID: {}", userId);

        Optional<LearningRecordVO> record = learningRecordService.findOngoingActivity(userId);
        return record.map(Result::success).orElseGet(() -> Result.success(null));
    }

    /**
     * 获取用户学习记录
     */
    @GetMapping("/records")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户学习记录", description = "分页获取用户的学习记录")
    public Result<Page<LearningRecordVO>> getUserLearningRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户学习记录, 用户ID: {}, 页码: {}, 每页数量: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<LearningRecordVO> records = learningRecordService.getUserActivities(userId, pageable);
        return Result.success(records);
    }

    /**
     * 获取用户课程学习记录
     */
    @GetMapping("/courses/{courseId}/records")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户课程学习记录", description = "分页获取用户特定课程的学习记录")
    public Result<Page<LearningRecordVO>> getUserCourseLearningRecords(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户课程学习记录, 用户ID: {}, 课程ID: {}, 页码: {}, 每页数量: {}",
                userId, courseId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<LearningRecordVO> records = learningRecordService.getUserCourseActivities(userId, courseId, pageable);
        return Result.success(records);
    }

    /**
     * 获取每日学习统计
     */
    @GetMapping("/stats/heatmap")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取学习热图数据", description = "获取用户学习时间的热图数据")
    public Result<List<DailyLearningStatVO>> getLearningHeatmap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 默认获取最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取用户学习热图数据, 用户ID: {}, 开始日期: {}, 结束日期: {}", userId, startDate, endDate);

        List<DailyLearningStatVO> data = learningRecordService.getDailyLearningStats(userId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取学习热力图数据（按星期和小时分组）
     */
    @GetMapping("/stats/heatmap-by-hour")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取学习热力图数据（按星期和小时分组）", description = "获取用户学习活动按星期和小时分组的热力图数据")
    public Result<LearningHeatmapVO> getLearningHeatmapByHour(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 默认获取最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取用户学习热力图数据（按星期和小时分组）, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                userId, startDate, endDate);

        LearningHeatmapVO data = learningRecordService.getUserLearningHeatmap(userId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取学习热力图数据（按日期分组）
     */
    @GetMapping("/stats/heatmap-by-date")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取学习热力图数据（按日期分组）", description = "获取用户学习活动按具体日期分组的热力图数据")
    public Result<DateLearningHeatmapVO> getLearningHeatmapByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 默认获取最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取用户学习热力图数据（按日期分组）, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                userId, startDate, endDate);

        DateLearningHeatmapVO data = learningRecordService.getUserLearningHeatmapByDate(userId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取活动类型统计
     */
    @GetMapping("/stats/activity-types")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取活动类型统计", description = "获取用户各类学习活动的时长统计")
    public Result<List<ActivityTypeStatVO>> getActivityTypeStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户活动类型统计, 用户ID: {}", userId);

        List<ActivityTypeStatVO> data = learningRecordService.getActivityTypeStats(userId);
        return Result.success(data);
    }

    /**
     * 获取今日学习时长
     */
    @GetMapping("/stats/today-duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取今日学习时长", description = "获取用户今日的学习总时长")
    public Result<Long> getTodayLearningDuration() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户今日学习时长, 用户ID: {}", userId);

        Long duration = learningRecordService.getTodayLearningDuration(userId);
        return Result.success(duration);
    }

    /**
     * 获取总学习时长
     */
    @GetMapping("/stats/total-duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取总学习时长", description = "获取用户的学习总时长")
    public Result<Long> getTotalLearningDuration() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户总学习时长, 用户ID: {}", userId);

        Long duration = learningRecordService.getTotalLearningDuration(userId);
        return Result.success(duration);
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