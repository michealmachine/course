package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.media.*;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.vo.MediaActivityCalendarVO;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.model.vo.StorageGrowthPointVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 媒体服务接口
 */
public interface MediaService {
    
    /**
     * 初始化上传（返回所有分片的预签名URL）
     *
     * @param dto 上传初始化参数
     * @param institutionId 机构ID
     * @param uploaderId 上传者ID
     * @return 上传初始化结果
     */
    UploadInitiationVO initiateUpload(
            MediaUploadInitDTO dto, Long institutionId, Long uploaderId);
    
    /**
     * 完成上传
     *
     * @param mediaId 媒体ID
     * @param institutionId 机构ID
     * @param dto 完成上传请求参数
     * @return 媒体信息
     */
    MediaVO completeUpload(Long mediaId, Long institutionId, CompleteUploadDTO dto);
    
    /**
     * 取消上传
     *
     * @param mediaId 媒体ID
     * @param institutionId 机构ID
     */
    void cancelUpload(Long mediaId, Long institutionId);
    
    /**
     * 获取媒体访问URL
     *
     * @param mediaId 媒体ID
     * @param institutionId 机构ID
     * @param expirationMinutes URL有效期（分钟）
     * @return 预签名URL
     */
    String getMediaAccessUrl(Long mediaId, Long institutionId, Long expirationMinutes);
    
    /**
     * 获取媒体信息
     *
     * @param mediaId 媒体ID
     * @param institutionId 机构ID
     * @return 媒体信息
     */
    MediaVO getMediaInfo(Long mediaId, Long institutionId);
    
    /**
     * 获取媒体信息（预览模式，不验证机构ID）
     *
     * @param mediaId 媒体ID
     * @return 包含临时访问URL的媒体信息
     */
    MediaVO getMediaByIdForPreview(Long mediaId);
    
    /**
     * 分页获取机构媒体列表
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 媒体列表
     */
    Page<MediaVO> getMediaList(Long institutionId, Pageable pageable);
    
    /**
     * 分页获取机构媒体列表（支持类型和文件名筛选）
     *
     * @param institutionId 机构ID
     * @param type 媒体类型（可选）
     * @param filename 文件名关键词（可选）
     * @param pageable 分页参数
     * @return 媒体列表
     */

    @Transactional(readOnly = true)
    Page<MediaVO> getMediaList(Long institutionId, MediaType type, String filename, Pageable pageable);

    /**
     * 删除媒体文件
     *
     * @param mediaId 媒体ID
     * @param institutionId 机构ID
     */
    void deleteMedia(Long mediaId, Long institutionId);
    
    /**
     * 获取指定机构的媒体活动日历数据（热图数据）
     *
     * @param institutionId 机构ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 媒体活动日历数据
     */
    MediaActivityCalendarVO getMediaActivityCalendar(
            Long institutionId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取所有机构的媒体活动日历数据（管理员使用）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 媒体活动日历数据
     */
    MediaActivityCalendarVO getAllMediaActivityCalendar(
            LocalDate startDate, LocalDate endDate);

    /**
     * 根据日期获取指定机构的媒体列表
     *
     * @param institutionId 机构ID
     * @param date 日期
     * @param pageable 分页参数
     * @return 媒体列表
     */
    Page<MediaVO> getMediaListByDate(
            Long institutionId, LocalDate date, Pageable pageable);

    /**
     * 根据日期获取所有机构的媒体列表（管理员使用）
     *
     * @param date 日期
     * @param pageable 分页参数
     * @return 媒体列表
     */
    Page<MediaVO> getAllMediaListByDate(
            LocalDate date, Pageable pageable);
            
    /**
     * 获取所有机构的媒体列表（管理员使用，支持筛选）
     *
     * @param type 媒体类型（可选）
     * @param filename 文件名关键词（可选）
     * @param pageable 分页参数
     * @return 媒体列表
     */
    Page<MediaVO> getAllMediaList(MediaType type, String filename, Pageable pageable);

    /**
     * 获取系统存储增长趋势数据 (管理员使用)
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param granularity 聚合粒度 (目前仅支持 DAILY)
     * @return 存储增长趋势数据点列表
     */
    List<StorageGrowthPointVO> getStorageGrowthTrend(
            LocalDate startDate, LocalDate endDate, ChronoUnit granularity);
} 