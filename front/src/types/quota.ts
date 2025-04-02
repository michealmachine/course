import { Page } from './api';

/**
 * 配额类型枚举
 */
export enum QuotaType {
  TOTAL = 0,  // 总配额
  VIDEO = 1,  // 视频配额
  DOCUMENT = 2  // 文档配额
}

/**
 * 配额申请状态枚举
 */
export enum QuotaApplicationStatus {
  PENDING = 0,  // 待审核
  APPROVED = 1,  // 已通过
  REJECTED = 2,  // 已拒绝
}

/**
 * 配额申请DTO - 与后端QuotaApplicationDTO对应
 */
export interface QuotaApplicationDTO {
  quotaType: QuotaType;       // 配额类型
  requestedBytes: number;     // 申请容量（字节）
  reason: string;             // 申请原因
}

/**
 * 配额申请视图对象
 */
export interface QuotaApplicationVO {
  id: number;
  applicationId: string;  // 申请编号
  institutionId: number;  // 机构ID
  institutionName: string;  // 机构名称
  applicantId: number;  // 申请人ID
  applicantUsername: string;  // 申请人用户名
  quotaType: QuotaType;  // 配额类型
  requestedBytes: number;  // 申请容量（字节）
  reason: string;  // 申请原因
  status: QuotaApplicationStatus;  // 申请状态
  reviewerId?: number;  // 审核人ID
  reviewerUsername?: string;  // 审核人用户名
  reviewComment?: string;  // 审核意见
  createdAt: string;  // 创建时间
  reviewedAt?: string;  // 审核时间
}

/**
 * 配额信息 - 与后端QuotaInfoVO对应
 */
export interface QuotaInfoVO {
  type: string;               // 配额类型
  typeName: string;           // 配额类型名称
  totalQuota: number;         // 总配额（字节）
  usedQuota: number;          // 已用配额（字节）
  lastUpdatedTime: string;    // 最后更新时间
  availableQuota: number;     // 可用配额（字节）
  usagePercentage: number;    // 使用百分比
}

/**
 * 配额申请查询参数
 */
export interface QuotaApplicationQueryParams {
  status?: QuotaApplicationStatus; // 状态
  pageNum?: number;           // 页码
  pageSize?: number;          // 每页大小
}

/**
 * 配额申请响应分页结果
 */
export type QuotaApplicationPage = Page<QuotaApplicationVO>;

/**
 * 配额使用情况
 */
export interface QuotaUsage {
  quotaType: QuotaType;  // 配额类型
  totalBytes: number;  // 总配额（字节）
  usedBytes: number;  // 已使用配额（字节）
  percent: number;  // 使用百分比
}

/**
 * 新建配额申请请求
 */
export interface CreateQuotaApplicationRequest {
  quotaType: QuotaType;
  requestedBytes: number;
  reason: string;
}

/**
 * 配额分布VO - 与后端QuotaDistributionVO对应
 * 用于配额类型分布饼图展示
 */
export interface QuotaDistributionVO {
  type: string;           // 配额类型
  name: string;           // 配额类型名称
  usedQuota: number;      // 已使用配额（字节）
  percentage: number;     // 占总使用量的百分比
}

/**
 * 存储配额统计VO - 与后端QuotaStatsVO对应
 * 用于单个机构的配额统计展示
 */
export interface QuotaStatsVO {
  totalQuota: QuotaInfoVO;               // 总体配额使用情况
  typeQuotas: QuotaInfoVO[];             // 各类型配额使用情况
  distribution: QuotaDistributionVO[];   // 配额类型分布数据（用于饼图）
}

/**
 * 机构配额统计VO - 与后端InstitutionQuotaStatsVO对应
 * 用于管理员查看所有机构的配额统计
 */
export interface InstitutionQuotaStatsVO {
  totalUsage: TotalQuotaUsageVO;                     // 所有机构的总配额使用情况
  institutions: InstitutionQuotaVO[];                // 各机构配额使用情况列表
  distribution: InstitutionQuotaDistributionVO[];    // 机构配额分布数据（用于饼图）
}

/**
 * 总体配额使用情况 - 与后端InstitutionQuotaStatsVO.TotalQuotaUsageVO对应
 */
export interface TotalQuotaUsageVO {
  totalQuota: number;       // 所有机构总配额（字节）
  usedQuota: number;        // 所有机构已用配额（字节）
  availableQuota: number;   // 所有机构可用配额（字节）
  usagePercentage: number;  // 使用百分比
  institutionCount: number; // 机构数量
}

/**
 * 机构配额使用情况 - 与后端InstitutionQuotaStatsVO.InstitutionQuotaVO对应
 */
export interface InstitutionQuotaVO {
  institutionId: number;    // 机构ID
  institutionName: string;  // 机构名称
  totalQuota: number;       // 总配额（字节）
  usedQuota: number;        // 已用配额（字节）
  availableQuota: number;   // 可用配额（字节）
  usagePercentage: number;  // 使用百分比
  lastUpdatedTime: string;  // 上次更新时间
}

/**
 * 机构配额分布 - 与后端InstitutionQuotaStatsVO.InstitutionQuotaDistributionVO对应
 */
export interface InstitutionQuotaDistributionVO {
  institutionId: number;    // 机构ID
  institutionName: string;  // 机构名称
  usedQuota: number;        // 已使用配额（字节）
  percentage: number;       // 占总使用量的百分比
} 