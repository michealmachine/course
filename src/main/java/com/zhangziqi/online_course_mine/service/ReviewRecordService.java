package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.entity.ReviewRecord;
import com.zhangziqi.online_course_mine.model.enums.ReviewResult;
import com.zhangziqi.online_course_mine.model.enums.ReviewType;
import com.zhangziqi.online_course_mine.model.vo.ReviewRecordVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 审核记录服务接口
 */
public interface ReviewRecordService {

    /**
     * 创建课程审核记录
     *
     * @param targetId 课程ID
     * @param targetName 课程名称
     * @param reviewerId 审核人ID
     * @param reviewerName 审核人姓名
     * @param result 审核结果
     * @param comment 审核意见
     * @param institutionId 机构ID
     * @param publishedVersionId 发布版本ID
     * @return 审核记录
     */
    ReviewRecordVO createCourseReviewRecord(Long targetId, String targetName, Long reviewerId,
                                          String reviewerName, ReviewResult result, String comment,
                                          Long institutionId, Long publishedVersionId);

    /**
     * 创建机构审核记录
     *
     * @param targetId 机构申请ID
     * @param targetName 机构名称
     * @param reviewerId 审核人ID
     * @param reviewerName 审核人姓名
     * @param result 审核结果
     * @param comment 审核意见
     * @param institutionId 机构ID（审核通过后）
     * @return 审核记录
     */
    ReviewRecordVO createInstitutionReviewRecord(Long targetId, String targetName, Long reviewerId,
                                               String reviewerName, ReviewResult result, String comment,
                                               Long institutionId);

    /**
     * 获取课程审核历史
     *
     * @param courseId 课程ID
     * @return 审核记录列表
     */
    List<ReviewRecordVO> getCourseReviewHistory(Long courseId);

    /**
     * 获取机构审核历史
     *
     * @param institutionId 机构ID
     * @return 审核记录列表
     */
    List<ReviewRecordVO> getInstitutionReviewHistory(Long institutionId);

    /**
     * 获取审核员的审核记录
     *
     * @param reviewerId 审核员ID
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecordVO> getReviewerRecords(Long reviewerId, Pageable pageable);

    /**
     * 获取审核员的审核记录（按类型过滤）
     *
     * @param reviewerId 审核员ID
     * @param reviewType 审核类型（可选）
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecordVO> getReviewerRecordsByType(Long reviewerId, ReviewType reviewType, Pageable pageable);

    /**
     * 获取所有审核记录（管理员使用）
     *
     * @param reviewType 审核类型（可选）
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecordVO> getAllReviewRecords(ReviewType reviewType, Pageable pageable);

    /**
     * 获取机构相关的审核记录
     *
     * @param institutionId 机构ID
     * @param reviewType 审核类型（可选）
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecordVO> getInstitutionReviewRecords(Long institutionId, ReviewType reviewType, Pageable pageable);
}
