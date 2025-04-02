import { Result, Page } from './api'; // Assuming api.ts exports Result and Page

/**
 * 存储增长趋势数据点 VO - 与后端 StorageGrowthPointVO 对应
 */
export interface StorageGrowthPointVO {
  date: string;        // 日期 (YYYY-MM-DD 格式字符串)
  sizeAdded: number;   // 当日新增存储大小（字节）
}

// 可以将其他统计相关的类型也移到这里，如果需要的话
// 例如: MediaActivityCalendarVO 等 