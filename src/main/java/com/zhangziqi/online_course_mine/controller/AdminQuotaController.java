package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.InstitutionQuotaStatsVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员配额统计控制器
 * 用于管理员查看所有机构的配额统计信息
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/quota")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员配额统计", description = "管理员查看所有机构配额统计相关接口")
public class AdminQuotaController {

    private final StorageQuotaService storageQuotaService;
    
    /**
     * 获取所有机构的配额统计信息
     *
     * @return 所有机构的配额统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取所有机构配额统计", description = "管理员获取所有机构的配额使用统计和分布信息")
    public Result<InstitutionQuotaStatsVO> getAllInstitutionsQuotaStats() {
        log.info("管理员请求所有机构的配额统计信息");
        InstitutionQuotaStatsVO stats = storageQuotaService.getAllInstitutionsQuotaStats();
        return Result.success(stats);
    }
} 