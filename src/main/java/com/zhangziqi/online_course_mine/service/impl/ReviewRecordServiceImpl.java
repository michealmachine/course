package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.model.entity.ReviewRecord;
import com.zhangziqi.online_course_mine.model.enums.ReviewResult;
import com.zhangziqi.online_course_mine.model.enums.ReviewType;
import com.zhangziqi.online_course_mine.model.vo.ReviewRecordVO;
import com.zhangziqi.online_course_mine.repository.ReviewRecordRepository;
import com.zhangziqi.online_course_mine.service.ReviewRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRecordServiceImpl implements ReviewRecordService {

    private final ReviewRecordRepository reviewRecordRepository;

    @Override
    @Transactional
    public ReviewRecordVO createCourseReviewRecord(Long targetId, String targetName, Long reviewerId,
                                                 String reviewerName, ReviewResult result, String comment,
                                                 Long institutionId, Long publishedVersionId) {
        log.info("创建课程审核记录: 课程ID={}, 审核人ID={}, 结果={}", targetId, reviewerId, result);

        ReviewRecord record = ReviewRecord.builder()
                .reviewType(ReviewType.COURSE.getValue())
                .result(result.getValue())
                .targetId(targetId)
                .targetName(targetName)
                .reviewerId(reviewerId)
                .reviewerName(reviewerName)
                .reviewedAt(LocalDateTime.now())
                .comment(comment)
                .institutionId(institutionId)
                .publishedVersionId(publishedVersionId)
                .build();

        ReviewRecord savedRecord = reviewRecordRepository.save(record);
        return ReviewRecordVO.fromEntity(savedRecord);
    }

    @Override
    @Transactional
    public ReviewRecordVO createInstitutionReviewRecord(Long targetId, String targetName, Long reviewerId,
                                                      String reviewerName, ReviewResult result, String comment,
                                                      Long institutionId) {
        log.info("创建机构审核记录: 申请ID={}, 审核人ID={}, 结果={}", targetId, reviewerId, result);

        ReviewRecord record = ReviewRecord.builder()
                .reviewType(ReviewType.INSTITUTION.getValue())
                .result(result.getValue())
                .targetId(targetId)
                .targetName(targetName)
                .reviewerId(reviewerId)
                .reviewerName(reviewerName)
                .reviewedAt(LocalDateTime.now())
                .comment(comment)
                .institutionId(institutionId)
                .build();

        ReviewRecord savedRecord = reviewRecordRepository.save(record);
        return ReviewRecordVO.fromEntity(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRecordVO> getCourseReviewHistory(Long courseId) {
        log.info("获取课程审核历史: 课程ID={}", courseId);

        List<ReviewRecord> records = reviewRecordRepository.findByReviewTypeAndTargetIdOrderByReviewedAtDesc(
                ReviewType.COURSE.getValue(), courseId);

        return records.stream()
                .map(ReviewRecordVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRecordVO> getInstitutionReviewHistory(Long institutionId) {
        log.info("获取机构审核历史: 机构ID={}", institutionId);

        List<ReviewRecord> records = reviewRecordRepository.findByReviewTypeAndTargetIdOrderByReviewedAtDesc(
                ReviewType.INSTITUTION.getValue(), institutionId);

        return records.stream()
                .map(ReviewRecordVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRecordVO> getReviewerRecords(Long reviewerId, Pageable pageable) {
        log.info("获取审核员的审核记录: 审核员ID={}, 分页={}", reviewerId, pageable);

        Page<ReviewRecord> recordPage = reviewRecordRepository.findByReviewerIdOrderByReviewedAtDesc(reviewerId, pageable);

        return recordPage.map(ReviewRecordVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRecordVO> getReviewerRecordsByType(Long reviewerId, ReviewType reviewType, Pageable pageable) {
        log.info("获取审核员的审核记录（按类型过滤）: 审核员ID={}, 审核类型={}, 分页={}", reviewerId, reviewType, pageable);

        Page<ReviewRecord> recordPage;
        if (reviewType != null) {
            // 按审核类型过滤
            recordPage = reviewRecordRepository.findByReviewerIdAndReviewTypeOrderByReviewedAtDesc(
                    reviewerId, reviewType.getValue(), pageable);
        } else {
            // 不过滤审核类型
            recordPage = reviewRecordRepository.findByReviewerIdOrderByReviewedAtDesc(reviewerId, pageable);
        }

        return recordPage.map(ReviewRecordVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRecordVO> getAllReviewRecords(ReviewType reviewType, Pageable pageable) {
        log.info("获取所有审核记录: 审核类型={}, 分页={}", reviewType, pageable);

        Page<ReviewRecord> recordPage;
        if (reviewType != null) {
            recordPage = reviewRecordRepository.findByReviewTypeOrderByReviewedAtDesc(reviewType.getValue(), pageable);
        } else {
            recordPage = reviewRecordRepository.findAll(pageable);
        }

        return recordPage.map(ReviewRecordVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRecordVO> getInstitutionReviewRecords(Long institutionId, ReviewType reviewType, Pageable pageable) {
        log.info("获取机构相关的审核记录: 机构ID={}, 审核类型={}, 分页={}", institutionId, reviewType, pageable);

        Page<ReviewRecord> recordPage;
        if (reviewType != null) {
            recordPage = reviewRecordRepository.findByReviewTypeAndInstitutionIdOrderByReviewedAtDesc(
                    reviewType.getValue(), institutionId, pageable);
        } else {
            recordPage = reviewRecordRepository.findByInstitutionIdOrderByReviewedAtDesc(institutionId, pageable);
        }

        return recordPage.map(ReviewRecordVO::fromEntity);
    }
}
