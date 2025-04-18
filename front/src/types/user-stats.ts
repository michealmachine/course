// 用户统计相关类型定义

/**
 * 角色分布详情
 */
export interface RoleDistribution {
  /**
   * 角色ID
   */
  roleId: number;
  
  /**
   * 角色名称
   */
  roleName: string;
  
  /**
   * 角色代码
   */
  roleCode: string;
  
  /**
   * 该角色用户数量
   */
  userCount: number;
  
  /**
   * 占比
   */
  percentage: number;
}

/**
 * 每日用户注册数据
 */
export interface DailyRegistration {
  /**
   * 日期（yyyy-MM-dd格式）
   */
  date: string;
  
  /**
   * 注册用户数
   */
  count: number;
}

/**
 * 每日活跃用户数据
 */
export interface DailyActiveUsers {
  /**
   * 日期（yyyy-MM-dd格式）
   */
  date: string;
  
  /**
   * 活跃用户数
   */
  count: number;
}

/**
 * 用户角色分布统计VO
 */
export interface UserRoleDistributionVO {
  /**
   * 总用户数
   */
  totalUserCount: number;
  
  /**
   * 各角色用户分布
   */
  roleDistributions: RoleDistribution[];
}

/**
 * 用户增长统计VO
 */
export interface UserGrowthStatsVO {
  /**
   * 总用户数
   */
  totalUserCount: number;
  
  /**
   * 今日新增用户数
   */
  todayNewUsers: number;
  
  /**
   * 本周新增用户数
   */
  weekNewUsers: number;
  
  /**
   * 本月新增用户数
   */
  monthNewUsers: number;
  
  /**
   * 日增长率
   */
  dailyGrowthRate: number;
  
  /**
   * 周增长率
   */
  weeklyGrowthRate: number;
  
  /**
   * 月增长率
   */
  monthlyGrowthRate: number;
  
  /**
   * 每日用户注册数据（过去30天）
   */
  dailyRegistrations: DailyRegistration[];
}

/**
 * 用户状态统计VO
 */
export interface UserStatusStatsVO {
  /**
   * 总用户数
   */
  totalUserCount: number;
  
  /**
   * 正常状态用户数（status=1）
   */
  activeUserCount: number;
  
  /**
   * 禁用状态用户数（status=0）
   */
  disabledUserCount: number;
  
  /**
   * 正常用户占比
   */
  activeUserPercentage: number;
  
  /**
   * 禁用用户占比
   */
  disabledUserPercentage: number;
}

/**
 * 用户活跃度统计VO
 */
export interface UserActivityStatsVO {
  /**
   * 总用户数
   */
  totalUserCount: number;
  
  /**
   * 活跃用户数（过去30天有登录记录）
   */
  activeUserCount: number;
  
  /**
   * 非活跃用户数（过去30天无登录记录）
   */
  inactiveUserCount: number;
  
  /**
   * 今日活跃用户数
   */
  todayActiveUsers: number;
  
  /**
   * 过去7天活跃用户数
   */
  weekActiveUsers: number;
  
  /**
   * 过去30天活跃用户数
   */
  monthActiveUsers: number;
  
  /**
   * 活跃用户占比
   */
  activeUserPercentage: number;
  
  /**
   * 每日活跃用户数据（过去30天）
   */
  dailyActiveUsers: DailyActiveUsers[];
  
  /**
   * 用户活跃时间分布（一天24小时）
   */
  hourlyActiveDistribution: Record<number, number>;
  
  /**
   * 用户活跃时间分布（一周7天）
   */
  weekdayActiveDistribution: Record<number, number>;
}

/**
 * 用户统计综合VO
 */
export interface UserStatsVO {
  /**
   * 用户角色分布统计
   */
  roleDistribution: UserRoleDistributionVO;
  
  /**
   * 用户增长统计
   */
  growthStats: UserGrowthStatsVO;
  
  /**
   * 用户状态统计
   */
  statusStats: UserStatusStatsVO;
  
  /**
   * 用户活跃度统计
   */
  activityStats: UserActivityStatsVO;
} 