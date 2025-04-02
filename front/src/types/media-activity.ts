/**
 * 媒体活动数据
 */
export interface MediaActivityDTO {
  /**
   * 活动日期
   */
  date: string;
  
  /**
   * 活动数量
   */
  count: number;
  
  /**
   * 文件总大小（字节）
   */
  totalSize: number;
}

/**
 * 媒体活动日历视图对象
 */
export interface MediaActivityCalendarVO {
  /**
   * 日历数据（每日活动）
   */
  calendarData: MediaActivityDTO[];
  
  /**
   * 峰值活动数
   */
  peakCount: number;
  
  /**
   * 最活跃日期
   */
  mostActiveDate?: string;
  
  /**
   * 总活动数
   */
  totalCount: number;
  
  /**
   * 总文件大小（字节）
   */
  totalSize: number;
} 