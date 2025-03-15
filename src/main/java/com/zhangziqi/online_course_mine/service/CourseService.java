package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.course.*;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.vo.PreviewUrlVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * 课程服务接口
 */
public interface CourseService {

    /**
     * 创建课程
     *
     * @param dto 课程创建DTO
     * @param creatorId 创建者ID
     * @param institutionId 机构ID
     * @return 创建的课程
     */
    Course createCourse(CourseCreateDTO dto, Long creatorId, Long institutionId);

    /**
     * 更新课程
     *
     * @param id 课程ID
     * @param dto 课程更新DTO
     * @param institutionId 机构ID
     * @return 更新后的课程
     */
    Course updateCourse(Long id, CourseCreateDTO dto, Long institutionId);

    /**
     * 获取课程详情
     *
     * @param id 课程ID
     * @return 课程
     */
    Course getCourseById(Long id);

    /**
     * 获取机构下的课程列表
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> getCoursesByInstitution(Long institutionId, Pageable pageable);

    /**
     * 删除课程
     *
     * @param id 课程ID
     */
    void deleteCourse(Long id);

    /**
     * 更新课程封面
     *
     * @param id 课程ID
     * @param file 封面图片文件
     * @return 更新后的课程
     * @throws IOException 如果文件处理出错
     */
    Course updateCourseCover(Long id, MultipartFile file) throws IOException;

    /**
     * 提交课程审核
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    Course submitForReview(Long id);

    /**
     * 开始审核课程
     *
     * @param id 课程ID
     * @param reviewerId 审核人ID
     * @return 更新后的课程
     */
    Course startReview(Long id, Long reviewerId);

    /**
     * 通过课程审核
     *
     * @param id 课程ID
     * @param comment 审核意见
     * @param reviewerId 审核人ID
     * @return 更新后的课程
     */
    Course approveCourse(Long id, String comment, Long reviewerId);

    /**
     * 拒绝课程审核
     *
     * @param id 课程ID
     * @param reason 拒绝原因
     * @param reviewerId 审核人ID
     * @return 更新后的课程
     */
    Course rejectCourse(Long id, String reason, Long reviewerId);

    /**
     * 下线课程
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    Course unpublishCourse(Long id);

    /**
     * 重新上线课程
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    Course republishCourse(Long id);

    /**
     * 重新编辑被拒绝的课程
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    Course reEditRejectedCourse(Long id);

    /**
     * 生成课程预览URL
     *
     * @param id 课程ID
     * @return 预览URL
     */
    PreviewUrlVO generatePreviewUrl(Long id);

    /**
     * 根据预览token获取课程
     *
     * @param token 预览token
     * @return 课程
     */
    Course getCourseByPreviewToken(String token);

    /**
     * 更新课程支付设置
     *
     * @param id 课程ID
     * @param paymentType 支付类型
     * @param price 价格
     * @param discountPrice 折扣价格
     * @return 更新后的课程
     */
    Course updatePaymentSettings(Long id, Integer paymentType, 
        java.math.BigDecimal price, java.math.BigDecimal discountPrice);
} 