package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.vo.AdminMediaVO;
import com.zhangziqi.online_course_mine.model.vo.MediaActivityCalendarVO;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.model.vo.MediaTypeDistributionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.StorageGrowthPointVO;
import com.zhangziqi.online_course_mine.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 管理员媒体控制器
 * 处理管理员查看所有机构媒体请求
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员媒体管理", description = "管理员查看所有机构媒体文件相关操作")
public class AdminMediaController {
    
    private final MediaService mediaService;
    
    /**
     * 获取所有机构的媒体活动日历数据
     */
    @GetMapping("/activities/calendar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取所有机构媒体活动日历数据", description = "管理员获取所有机构媒体活动的日历热图数据")
    public Result<MediaActivityCalendarVO> getAllMediaActivityCalendar(
            @Parameter(description = "开始日期") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("管理员获取所有机构媒体活动日历数据, 开始日期: {}, 结束日期: {}", startDate, endDate);
        
        MediaActivityCalendarVO calendarData = mediaService.getAllMediaActivityCalendar(startDate, endDate);
        
        return Result.success(calendarData);
    }
    
    /**
     * 根据日期获取所有机构的媒体列表
     */
    @GetMapping("/by-date")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "根据日期获取所有机构媒体列表", description = "管理员获取特定日期上传的所有机构媒体文件列表")
    public Result<Page<AdminMediaVO>> getAllMediaListByDate(
            @Parameter(description = "日期") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "机构ID（可选）") 
            @RequestParam(required = false) Long institutionId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("管理员根据日期获取媒体列表, 日期: {}, 机构ID: {}", date, institutionId);
        
        Page<AdminMediaVO> mediaList = mediaService.getAdminMediaListByDate(date, pageable);
        
        return Result.success(mediaList);
    }
    
    /**
     * 获取所有机构的媒体列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取所有机构媒体列表", description = "管理员分页获取所有机构的媒体文件列表，支持按类型和文件名筛选")
    public Result<Page<MediaVO>> getAllMediaList(
            @Parameter(description = "机构ID（可选）") 
            @RequestParam(required = false) Long institutionId,
            @Parameter(description = "媒体类型（可选）") 
            @RequestParam(required = false) MediaType type,
            @Parameter(description = "文件名关键词（可选）") 
            @RequestParam(required = false) String filename,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("管理员获取媒体列表 - 机构ID: {}, 类型: {}, 文件名关键词: {}", 
                institutionId, type, filename);
        
        Page<MediaVO> mediaList;
        
        try {
            if (institutionId != null) {
                // 如果提供了机构ID，则查询特定机构的数据（支持筛选）
                mediaList = mediaService.getMediaList(institutionId, type, filename, pageable);
                log.info("管理员查询特定机构媒体列表 - 机构ID: {}, 总记录数: {}", 
                        institutionId, mediaList.getTotalElements());
            } else {
                // 查询所有机构的数据（支持筛选）
                mediaList = mediaService.getAllMediaList(type, filename, pageable);
                log.info("管理员查询所有机构媒体列表 - 类型: {}, 文件名: {}, 总记录数: {}", 
                        type, filename, mediaList.getTotalElements());
            }
            
            return Result.success(mediaList);
        } catch (Exception e) {
            log.error("管理员获取媒体列表失败 - 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取系统存储增长趋势
     */
    @GetMapping("/stats/storage-growth")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取系统存储增长趋势", description = "管理员获取指定时间范围内的存储增长趋势（基于上传）")
    public Result<List<StorageGrowthPointVO>> getStorageGrowth(
            @Parameter(description = "开始日期 (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期 (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "时间粒度 (DAILY)")
            @RequestParam(defaultValue = "DAYS") ChronoUnit granularity) {
        
        log.info("管理员获取存储增长趋势, 开始: {}, 结束: {}, 粒度: {}", startDate, endDate, granularity);
        
        // Basic validation for date range
        if (startDate.isAfter(endDate)) {
            return Result.fail("开始日期不能晚于结束日期");
        }
        
        // TODO: Add more robust granularity handling/validation later
        if (granularity != ChronoUnit.DAYS) {
             return Result.fail("当前仅支持 DAILY 粒度");
        }

        List<StorageGrowthPointVO> trendData = mediaService.getStorageGrowthTrend(startDate, endDate, granularity);
        
        return Result.success(trendData);
    }
    
    /**
     * 获取所有机构的媒体列表（高级筛选）
     */
    @GetMapping("/advanced")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取所有机构媒体列表（高级筛选）", description = "管理员分页获取所有机构的媒体文件列表，支持高级筛选条件")
    public Result<Page<AdminMediaVO>> getAdvancedMediaList(
            @Parameter(description = "媒体类型（可选）") 
            @RequestParam(required = false) MediaType type,
            @Parameter(description = "文件名关键词（可选）") 
            @RequestParam(required = false) String filename,
            @Parameter(description = "机构名称关键词（可选）") 
            @RequestParam(required = false) String institutionName,
            @Parameter(description = "上传开始时间（可选）") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uploadStartTime,
            @Parameter(description = "上传结束时间（可选）") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uploadEndTime,
            @Parameter(description = "最小文件大小（字节，可选）") 
            @RequestParam(required = false) Long minSize,
            @Parameter(description = "最大文件大小（字节，可选）") 
            @RequestParam(required = false) Long maxSize,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("管理员高级筛选媒体列表 - 类型: {}, 文件名: {}, 机构名称: {}, 上传时间: {} 至 {}, 大小: {} 至 {}", 
                type, filename, institutionName, uploadStartTime, uploadEndTime, minSize, maxSize);
        
        // 如果没有指定上传结束时间，但指定了开始时间，设置结束时间为当前时间
        if (uploadStartTime != null && uploadEndTime == null) {
            uploadEndTime = LocalDateTime.now();
        }
        
        // 如果指定了上传日期但没有包含时间，为开始日期设置一天的开始时间，为结束日期设置一天的结束时间
        if (uploadStartTime != null && uploadStartTime.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            uploadStartTime = uploadStartTime.toLocalDate().atStartOfDay();
        }
        
        if (uploadEndTime != null && uploadEndTime.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            uploadEndTime = uploadEndTime.toLocalDate().atTime(LocalTime.MAX);
        }
        
        try {
            Page<AdminMediaVO> mediaList = mediaService.getAdminMediaList(
                    type, filename, institutionName, uploadStartTime, uploadEndTime, minSize, maxSize, pageable);
            
            log.info("管理员高级筛选查询结果 - 总记录数: {}", mediaList.getTotalElements());
            
            return Result.success(mediaList);
        } catch (Exception e) {
            log.error("管理员高级筛选媒体列表失败 - 错误: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取媒体类型分布统计
     */
    @GetMapping("/stats/type-distribution")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取媒体类型分布统计", description = "管理员获取所有或指定机构的媒体类型分布统计")
    public Result<MediaTypeDistributionVO> getMediaTypeDistribution(
            @Parameter(description = "机构ID（可选，不提供则统计所有机构）") 
            @RequestParam(required = false) Long institutionId) {
        
        log.info("管理员获取媒体类型分布统计 - 机构ID: {}", institutionId);
        
        MediaTypeDistributionVO distribution = mediaService.getMediaTypeDistribution(institutionId);
        
        return Result.success(distribution);
    }
    
    /**
     * 获取各机构的媒体存储占用统计
     */
    @GetMapping("/stats/institution-usage")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取各机构的媒体存储占用统计", description = "管理员获取各机构的媒体存储占用情况")
    public Result<Map<String, Long>> getInstitutionStorageUsage() {
        
        log.info("管理员获取各机构的媒体存储占用统计");
        
        Map<String, Long> usageMap = mediaService.getInstitutionStorageUsage();
        
        return Result.success(usageMap);
    }
} 