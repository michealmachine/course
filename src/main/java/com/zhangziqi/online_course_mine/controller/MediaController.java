package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.media.MediaUploadInitDTO;
import com.zhangziqi.online_course_mine.model.dto.media.PartCompletionDTO;
import com.zhangziqi.online_course_mine.model.dto.media.UploadInitiationVO;
import com.zhangziqi.online_course_mine.model.dto.media.UploadStatusVO;
import com.zhangziqi.online_course_mine.model.dto.media.CompleteUploadDTO;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.model.vo.QuotaInfoVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.MediaService;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 媒体控制器
 * 处理媒体上传和管理请求
 */
@Slf4j
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "媒体管理", description = "媒体文件上传、管理相关操作")
public class MediaController {
    
    private final MediaService mediaService;
    private final StorageQuotaService storageQuotaService;
    
    /**
     * 初始化上传
     */
    @PostMapping("/initiate-upload")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "初始化上传", description = "初始化分片上传，返回上传ID和预签名URL")
    public Result<UploadInitiationVO> initiateUpload(@Valid @RequestBody MediaUploadInitDTO dto) {
        // 获取当前用户ID和机构ID
        Long userId = SecurityUtil.getCurrentUserId();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("初始化上传, 用户ID: {}, 机构ID: {}, 文件名: {}, 文件大小: {}", 
                userId, institutionId, dto.getFilename(), dto.getFileSize());
        
        UploadInitiationVO result = mediaService.initiateUpload(dto, institutionId, userId);
        
        return Result.success(result);
    }
    
    /**
     * 完成上传
     */
    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "完成上传", description = "通知服务器所有分片已上传完成，请求合并分片")
    public Result<MediaVO> completeUpload(
            @Parameter(description = "媒体ID") @PathVariable("id") Long mediaId,
            @Valid @RequestBody CompleteUploadDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("完成上传, mediaId: {}, institutionId: {}, uploadId: {}, completedParts: {}", 
                mediaId, institutionId, dto.getUploadId(), dto.getCompletedParts().size());
        
        MediaVO media = mediaService.completeUpload(mediaId, institutionId, dto);
        
        return Result.success(media);
    }
    
    /**
     * 取消上传
     */
    @DeleteMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "取消上传", description = "取消上传并清理已上传的分片")
    public Result<Void> cancelUpload(
            @Parameter(description = "媒体ID") @PathVariable("id") Long mediaId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("取消上传, 媒体ID: {}, 机构ID: {}", mediaId, institutionId);
        
        mediaService.cancelUpload(mediaId, institutionId);
        
        return Result.success();
    }
    
    /**
     * 获取媒体信息
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取媒体信息", description = "获取指定媒体的详细信息")
    public Result<MediaVO> getMediaInfo(
            @Parameter(description = "媒体ID") @PathVariable("id") Long mediaId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("获取媒体信息, mediaId: {}, institutionId: {}, userId: {}, 请求路径: {}", 
                mediaId, institutionId, userId, "/api/media/" + mediaId);
        
        try {
            MediaVO media = mediaService.getMediaInfo(mediaId, institutionId);
            log.info("成功获取媒体信息: {}", media);
            return Result.success(media);
        } catch (Exception e) {
            log.error("获取媒体信息失败, mediaId: {}, institutionId: {}, error: {}", 
                    mediaId, institutionId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 获取媒体访问URL
     */
    @GetMapping("/{id}/access")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取媒体访问URL", description = "获取媒体文件的临时访问URL")
    public Result<Map<String, String>> getMediaAccessUrl(
            @Parameter(description = "媒体ID") @PathVariable("id") Long mediaId,
            @Parameter(description = "URL有效期（分钟）") @RequestParam(required = false, defaultValue = "60") Long expirationMinutes) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        String url = mediaService.getMediaAccessUrl(mediaId, institutionId, expirationMinutes);
        
        Map<String, String> result = new HashMap<>();
        result.put("accessUrl", url);
        
        return Result.success(result);
    }
    
    /**
     * 获取媒体列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取媒体列表", description = "分页获取机构的媒体文件列表")
    public Result<Page<MediaVO>> getMediaList(
            @PageableDefault(size = 10) Pageable pageable) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        Page<MediaVO> mediaList = mediaService.getMediaList(institutionId, pageable);
        
        return Result.success(mediaList);
    }
    
    /**
     * 获取存储配额信息
     */
    @GetMapping("/quota")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取存储配额信息", description = "获取机构的存储配额使用情况")
    public Result<List<QuotaInfoVO>> getStorageQuota() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        List<QuotaInfoVO> quotaInfo = storageQuotaService.getAllQuotas(institutionId);
        
        return Result.success(quotaInfo);
    }
    
    /**
     * 删除媒体文件
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除媒体文件", description = "删除指定的媒体文件并释放存储配额")
    public Result<Void> deleteMedia(
            @Parameter(description = "媒体ID") @PathVariable("id") Long mediaId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除媒体文件, mediaId: {}, institutionId: {}", mediaId, institutionId);
        
        mediaService.deleteMedia(mediaId, institutionId);
        
        return Result.success();
    }
} 