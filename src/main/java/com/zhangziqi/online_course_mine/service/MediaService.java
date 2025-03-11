package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.media.*;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * 分页获取机构媒体列表
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 媒体列表
     */
    Page<MediaVO> getMediaList(Long institutionId, Pageable pageable);
    
    /**
     * 删除媒体文件
     *
     * @param mediaId 媒体ID
     * @param institutionId 机构ID
     */
    void deleteMedia(Long mediaId, Long institutionId);
} 