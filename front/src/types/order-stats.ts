/**
 * 收入趋势数据
 */
export interface IncomeTrendVO {
  /**
   * 日期（格式：yyyy-MM-dd）
   */
  date: string;
  
  /**
   * 收入金额
   */
  income: number;
  
  /**
   * 退款金额
   */
  refund: number;
  
  /**
   * 净收入金额（收入-退款）
   */
  netIncome: number;
}

/**
 * 订单状态分布数据
 */
export interface OrderStatusDistributionVO {
  /**
   * 订单状态值
   */
  status: number;
  
  /**
   * 订单状态名称
   */
  statusName: string;
  
  /**
   * 订单数量
   */
  count: number;
  
  /**
   * 百分比（0-100）
   */
  percentage: number;
}

/**
 * 课程收入排行数据
 */
export interface CourseIncomeRankingVO {
  /**
   * 课程ID
   */
  courseId: number;
  
  /**
   * 课程标题
   */
  courseTitle: string;
  
  /**
   * 课程封面
   */
  courseCover: string;
  
  /**
   * 收入金额
   */
  income: number;
}

/**
 * 管理员视图的课程收入排行数据
 */
export interface AdminCourseIncomeRankingVO extends CourseIncomeRankingVO {
  /**
   * 机构ID
   */
  institutionId: number;
  
  /**
   * 机构名称
   */
  institutionName: string;
}

/**
 * 平台收入统计数据
 */
export interface PlatformIncomeStatsVO {
  /**
   * 总收入
   */
  totalIncome: number;
  
  /**
   * 总退款
   */
  totalRefund: number;
  
  /**
   * 净收入（总收入-总退款）
   */
  netIncome: number;
  
  /**
   * 订单总数
   */
  orderCount: number;
  
  /**
   * 已支付订单数
   */
  paidOrderCount: number;
  
  /**
   * 退款订单数
   */
  refundOrderCount: number;
}
