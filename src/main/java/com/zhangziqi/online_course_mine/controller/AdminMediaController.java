package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.vo.MediaActivityCalendarVO;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    public Result<Page<MediaVO>> getAllMediaListByDate(
            @Parameter(description = "日期") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "机构ID（可选）") 
            @RequestParam(required = false) Long institutionId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("管理员根据日期获取媒体列表, 日期: {}, 机构ID: {}", date, institutionId);
        
        Page<MediaVO> mediaList;
        if (institutionId != null) {
            // 如果提供了机构ID，则查询特定机构的数据
            mediaList = mediaService.getMediaListByDate(institutionId, date, pageable);
        } else {
            // 否则查询所有机构的数据
            mediaList = mediaService.getAllMediaListByDate(date, pageable);
        }
        
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
} 