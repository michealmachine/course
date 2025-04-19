/**
 * 学习统计相关类型定义
 */

/**
 * 学习热力图视图对象
 * 用于展示按星期几和小时分组的学习活动统计
 */
export interface LearningHeatmapVO {
  /**
   * 课程ID
   */
  courseId: number;

  /**
   * 热力图数据
   * 外层Map: 星期几(1-7) -> 内层Map: 小时(0-23) -> 活动次数
   */
  heatmapData: Record<number, Record<number, number>>;

  /**
   * 最大活动次数
   * 用于前端计算热度颜色
   */
  maxActivityCount: number;
}

/**
 * 日期学习热力图视图对象
 * 用于展示按具体日期分组的学习活动统计
 */
export interface DateLearningHeatmapVO {
  /**
   * 课程ID
   */
  courseId: number;

  /**
   * 热力图数据
   * 按日期分组的学习活动统计
   * 键: 日期字符串 (yyyy-MM-dd格式)
   * 值: 该日期的活动次数
   */
  heatmapData: Record<string, number>;

  /**
   * 最大活动次数
   * 用于前端计算热度颜色
   */
  maxActivityCount: number;
}

/**
 * 每日进度数据
 */
export interface DailyProgressVO {
  /**
   * 日期（yyyy-MM-dd格式）
   */
  date: string;

  /**
   * 平均学习进度（百分比）
   */
  averageProgress: number;

  /**
   * 活跃学员数
   */
  activeUserCount: number;
}

/**
 * 学习进度趋势视图对象
 * 用于展示课程学习进度随时间的变化
 */
export interface LearningProgressTrendVO {
  /**
   * 课程ID
   */
  courseId: number;

  /**
   * 趋势数据
   * 按日期分组的平均学习进度
   */
  progressData: DailyProgressVO[];
}
