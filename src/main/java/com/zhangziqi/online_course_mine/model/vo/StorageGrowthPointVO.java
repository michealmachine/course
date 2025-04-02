package com.zhangziqi.online_course_mine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 存储增长趋势数据点 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "存储增长趋势数据点")
public class StorageGrowthPointVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "当日新增存储大小（字节）")
    private Long sizeAdded;
} 