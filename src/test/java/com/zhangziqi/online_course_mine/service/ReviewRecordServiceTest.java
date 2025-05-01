package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.entity.ReviewRecord;
import com.zhangziqi.online_course_mine.model.enums.ReviewResult;
import com.zhangziqi.online_course_mine.model.enums.ReviewType;
import com.zhangziqi.online_course_mine.model.vo.ReviewRecordVO;
import com.zhangziqi.online_course_mine.repository.ReviewRecordRepository;
import com.zhangziqi.online_course_mine.service.impl.ReviewRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewRecordServiceTest {

    @Mock
    private ReviewRecordRepository reviewRecordRepository;

    @InjectMocks
    private ReviewRecordServiceImpl reviewRecordService;

    private ReviewRecord courseReviewRecord;
    private ReviewRecord institutionReviewRecord;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // 创建课程审核记录
        courseReviewRecord = ReviewRecord.builder()
                .id(1L)
                .reviewType(ReviewType.COURSE.getValue())
                .result(ReviewResult.APPROVED.getValue())
                .targetId(100L)
                .targetName("测试课程")
                .reviewerId(200L)
                .reviewerName("审核员A")
                .reviewedAt(now)
                .comment("内容符合要求")
                .institutionId(300L)
                .publishedVersionId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 创建机构审核记录
        institutionReviewRecord = ReviewRecord.builder()
                .id(2L)
                .reviewType(ReviewType.INSTITUTION.getValue())
                .result(ReviewResult.APPROVED.getValue())
                .targetId(400L)
                .targetName("测试机构")
                .reviewerId(200L)
                .reviewerName("审核员A")
                .reviewedAt(now)
                .comment(null)
                .institutionId(500L)
                .publishedVersionId(null)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("创建课程审核记录 - 成功")
    void createCourseReviewRecord_Success() {
        // 准备测试数据
        when(reviewRecordRepository.save(any(ReviewRecord.class))).thenReturn(courseReviewRecord);

        // 执行方法
        ReviewRecordVO result = reviewRecordService.createCourseReviewRecord(
                100L, "测试课程", 200L, "审核员A",
                ReviewResult.APPROVED, "内容符合要求", 300L, 101L);

        // 验证结果
        assertNotNull(result);
        assertEquals(courseReviewRecord.getId(), result.getId());
        assertEquals(courseReviewRecord.getReviewType(), result.getReviewType());
        assertEquals(courseReviewRecord.getResult(), result.getResult());
        assertEquals(courseReviewRecord.getTargetId(), result.getTargetId());
        assertEquals(courseReviewRecord.getTargetName(), result.getTargetName());
        assertEquals(courseReviewRecord.getReviewerId(), result.getReviewerId());
        assertEquals(courseReviewRecord.getReviewerName(), result.getReviewerName());
        assertEquals(courseReviewRecord.getComment(), result.getComment());
        assertEquals(courseReviewRecord.getInstitutionId(), result.getInstitutionId());
        assertEquals(courseReviewRecord.getPublishedVersionId(), result.getPublishedVersionId());

        // 验证方法调用
        verify(reviewRecordRepository).save(any(ReviewRecord.class));
    }

    @Test
    @DisplayName("创建机构审核记录 - 成功")
    void createInstitutionReviewRecord_Success() {
        // 准备测试数据
        when(reviewRecordRepository.save(any(ReviewRecord.class))).thenReturn(institutionReviewRecord);

        // 执行方法
        ReviewRecordVO result = reviewRecordService.createInstitutionReviewRecord(
                400L, "测试机构", 200L, "审核员A",
                ReviewResult.APPROVED, null, 500L);

        // 验证结果
        assertNotNull(result);
        assertEquals(institutionReviewRecord.getId(), result.getId());
        assertEquals(institutionReviewRecord.getReviewType(), result.getReviewType());
        assertEquals(institutionReviewRecord.getResult(), result.getResult());
        assertEquals(institutionReviewRecord.getTargetId(), result.getTargetId());
        assertEquals(institutionReviewRecord.getTargetName(), result.getTargetName());
        assertEquals(institutionReviewRecord.getReviewerId(), result.getReviewerId());
        assertEquals(institutionReviewRecord.getReviewerName(), result.getReviewerName());
        assertEquals(institutionReviewRecord.getComment(), result.getComment());
        assertEquals(institutionReviewRecord.getInstitutionId(), result.getInstitutionId());
        assertNull(result.getPublishedVersionId());

        // 验证方法调用
        verify(reviewRecordRepository).save(any(ReviewRecord.class));
    }

    @Test
    @DisplayName("获取课程审核历史 - 成功")
    void getCourseReviewHistory_Success() {
        // 准备测试数据
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord);
        when(reviewRecordRepository.findByReviewTypeAndTargetIdOrderByReviewedAtDesc(
                eq(ReviewType.COURSE.getValue()), eq(100L))).thenReturn(records);

        // 执行方法
        List<ReviewRecordVO> result = reviewRecordService.getCourseReviewHistory(100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(courseReviewRecord.getId(), result.get(0).getId());
        assertEquals(courseReviewRecord.getTargetId(), result.get(0).getTargetId());

        // 验证方法调用
        verify(reviewRecordRepository).findByReviewTypeAndTargetIdOrderByReviewedAtDesc(
                ReviewType.COURSE.getValue(), 100L);
    }

    @Test
    @DisplayName("获取机构审核历史 - 成功")
    void getInstitutionReviewHistory_Success() {
        // 准备测试数据
        List<ReviewRecord> records = Arrays.asList(institutionReviewRecord);
        when(reviewRecordRepository.findByReviewTypeAndTargetIdOrderByReviewedAtDesc(
                eq(ReviewType.INSTITUTION.getValue()), eq(400L))).thenReturn(records);

        // 执行方法
        List<ReviewRecordVO> result = reviewRecordService.getInstitutionReviewHistory(400L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(institutionReviewRecord.getId(), result.get(0).getId());
        assertEquals(institutionReviewRecord.getTargetId(), result.get(0).getTargetId());

        // 验证方法调用
        verify(reviewRecordRepository).findByReviewTypeAndTargetIdOrderByReviewedAtDesc(
                ReviewType.INSTITUTION.getValue(), 400L);
    }

    @Test
    @DisplayName("获取审核员的审核记录 - 成功")
    void getReviewerRecords_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord, institutionReviewRecord);
        Page<ReviewRecord> recordPage = new PageImpl<>(records, pageable, records.size());

        when(reviewRecordRepository.findByReviewerIdOrderByReviewedAtDesc(
                eq(200L), any(Pageable.class))).thenReturn(recordPage);

        // 执行方法
        Page<ReviewRecordVO> result = reviewRecordService.getReviewerRecords(200L, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(courseReviewRecord.getId(), result.getContent().get(0).getId());
        assertEquals(institutionReviewRecord.getId(), result.getContent().get(1).getId());

        // 验证方法调用
        verify(reviewRecordRepository).findByReviewerIdOrderByReviewedAtDesc(eq(200L), any(Pageable.class));
    }

    @Test
    @DisplayName("获取审核员的审核记录（按类型过滤）- 成功")
    void getReviewerRecordsByType_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord);
        Page<ReviewRecord> recordPage = new PageImpl<>(records, pageable, records.size());

        when(reviewRecordRepository.findByReviewerIdAndReviewTypeOrderByReviewedAtDesc(
                eq(200L), eq(ReviewType.COURSE.getValue()), any(Pageable.class))).thenReturn(recordPage);

        // 执行方法
        Page<ReviewRecordVO> result = reviewRecordService.getReviewerRecordsByType(200L, ReviewType.COURSE, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(courseReviewRecord.getId(), result.getContent().get(0).getId());
        assertEquals(ReviewType.COURSE.getValue(), result.getContent().get(0).getReviewType());

        // 验证方法调用
        verify(reviewRecordRepository).findByReviewerIdAndReviewTypeOrderByReviewedAtDesc(
                eq(200L), eq(ReviewType.COURSE.getValue()), any(Pageable.class));
    }

    @Test
    @DisplayName("获取审核员的审核记录（不过滤类型）- 成功")
    void getReviewerRecordsByType_NoType_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord, institutionReviewRecord);
        Page<ReviewRecord> recordPage = new PageImpl<>(records, pageable, records.size());

        when(reviewRecordRepository.findByReviewerIdOrderByReviewedAtDesc(
                eq(200L), any(Pageable.class))).thenReturn(recordPage);

        // 执行方法
        Page<ReviewRecordVO> result = reviewRecordService.getReviewerRecordsByType(200L, null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // 验证方法调用
        verify(reviewRecordRepository).findByReviewerIdOrderByReviewedAtDesc(eq(200L), any(Pageable.class));
    }

    @Test
    @DisplayName("获取所有审核记录 - 成功")
    void getAllReviewRecords_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord, institutionReviewRecord);
        Page<ReviewRecord> recordPage = new PageImpl<>(records, pageable, records.size());

        when(reviewRecordRepository.findAll(any(Pageable.class))).thenReturn(recordPage);

        // 执行方法
        Page<ReviewRecordVO> result = reviewRecordService.getAllReviewRecords(null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // 验证方法调用
        verify(reviewRecordRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("获取所有审核记录（按类型筛选）- 成功")
    void getAllReviewRecords_WithType_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord);
        Page<ReviewRecord> recordPage = new PageImpl<>(records, pageable, records.size());

        when(reviewRecordRepository.findByReviewTypeOrderByReviewedAtDesc(
                eq(ReviewType.COURSE.getValue()), any(Pageable.class))).thenReturn(recordPage);

        // 执行方法
        Page<ReviewRecordVO> result = reviewRecordService.getAllReviewRecords(ReviewType.COURSE, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(courseReviewRecord.getId(), result.getContent().get(0).getId());

        // 验证方法调用
        verify(reviewRecordRepository).findByReviewTypeOrderByReviewedAtDesc(
                eq(ReviewType.COURSE.getValue()), any(Pageable.class));
    }

    @Test
    @DisplayName("获取机构相关的审核记录 - 成功")
    void getInstitutionReviewRecords_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord);
        Page<ReviewRecord> recordPage = new PageImpl<>(records, pageable, records.size());

        when(reviewRecordRepository.findByInstitutionIdOrderByReviewedAtDesc(
                eq(300L), any(Pageable.class))).thenReturn(recordPage);

        // 执行方法
        Page<ReviewRecordVO> result = reviewRecordService.getInstitutionReviewRecords(300L, null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(courseReviewRecord.getId(), result.getContent().get(0).getId());

        // 验证方法调用
        verify(reviewRecordRepository).findByInstitutionIdOrderByReviewedAtDesc(
                eq(300L), any(Pageable.class));
    }

    @Test
    @DisplayName("获取机构相关的审核记录（按类型筛选）- 成功")
    void getInstitutionReviewRecords_WithType_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewRecord> records = Arrays.asList(courseReviewRecord);
        Page<ReviewRecord> recordPage = new PageImpl<>(records, pageable, records.size());

        when(reviewRecordRepository.findByReviewTypeAndInstitutionIdOrderByReviewedAtDesc(
                eq(ReviewType.COURSE.getValue()), eq(300L), any(Pageable.class))).thenReturn(recordPage);

        // 执行方法
        Page<ReviewRecordVO> result = reviewRecordService.getInstitutionReviewRecords(
                300L, ReviewType.COURSE, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(courseReviewRecord.getId(), result.getContent().get(0).getId());

        // 验证方法调用
        verify(reviewRecordRepository).findByReviewTypeAndInstitutionIdOrderByReviewedAtDesc(
                eq(ReviewType.COURSE.getValue()), eq(300L), any(Pageable.class));
    }
}
