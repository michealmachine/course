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
  CANCELED = 3   // 已取消
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