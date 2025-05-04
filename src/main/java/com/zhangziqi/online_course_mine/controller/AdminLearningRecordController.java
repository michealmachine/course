package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.task.LearningRecordAggregationTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员学习记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/learning-records")
@RequiredArgsConstructor
@Tag(name = "管理员学习记录管理", description = "管理员学习记录相关接口")
public class AdminLearningRecordController {

    private final LearningRecordAggregationTask learningRecordAggregationTask;

    /**
     * 手动触发学习记录聚合任务
     */
    @PostMapping("/aggregate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "手动触发学习记录聚合", description = "管理员手动触发将Redis中的学习记录聚合到数据库的任务")
    public Result<Void> triggerLearningRecordAggregation() {
        String username = SecurityUtil.getCurrentUsername();
        log.info("管理员手动触发学习记录聚合, 用户名: {}", username);

        try {
            // 执行聚合任务，处理当天和昨天的记录
            learningRecordAggregationTask.aggregateAllLearningRecords();
            return Result.success();
        } catch (Exception e) {
            log.error("手动触发学习记录聚合失败", e);
            return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "聚合任务执行失败: " + e.getMessage());
        }
    }
}
