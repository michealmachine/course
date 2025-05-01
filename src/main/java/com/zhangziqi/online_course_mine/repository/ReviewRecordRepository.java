package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.ReviewRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审核记录数据访问接口
 */
@Repository
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

    /**
     * 根据审核类型和审核对象ID查询审核记录
     *
     * @param reviewType 审核类型
     * @param targetId 审核对象ID
     * @return 审核记录列表
     */
    List<ReviewRecord> findByReviewTypeAndTargetIdOrderByReviewedAtDesc(Integer reviewType, Long targetId);

    /**
     * 根据审核人ID查询审核记录
     *
     * @param reviewerId 审核人ID
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecord> findByReviewerIdOrderByReviewedAtDesc(Long reviewerId, Pageable pageable);

    /**
     * 根据审核人ID和审核类型查询审核记录
     *
     * @param reviewerId 审核人ID
     * @param reviewType 审核类型
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecord> findByReviewerIdAndReviewTypeOrderByReviewedAtDesc(Long reviewerId, Integer reviewType, Pageable pageable);

    /**
     * 根据审核类型查询审核记录
     *
     * @param reviewType 审核类型
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecord> findByReviewTypeOrderByReviewedAtDesc(Integer reviewType, Pageable pageable);

    /**
     * 根据机构ID查询审核记录
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecord> findByInstitutionIdOrderByReviewedAtDesc(Long institutionId, Pageable pageable);

    /**
     * 根据审核类型和机构ID查询审核记录
     *
     * @param reviewType 审核类型
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 审核记录分页
     */
    Page<ReviewRecord> findByReviewTypeAndInstitutionIdOrderByReviewedAtDesc(Integer reviewType, Long institutionId, Pageable pageable);
}
