/**
 * 审核记录相关类型定义
 */

/**
 * 审核类型枚举
 */
export enum ReviewType {
  COURSE = 0,        // 课程审核
  INSTITUTION = 1,   // 机构审核
}

/**
 * 审核结果枚举
 */
export enum ReviewResult {
  APPROVED = 0,      // 通过
  REJECTED = 1,      // 拒绝
}

/**
 * 审核记录视图对象
 */
export interface ReviewRecordVO {
  id: number;
  reviewType: number;
  result: number;
  targetId: number;
  targetName: string;
  reviewerId: number;
  reviewerName: string;
  reviewedAt: string;
  comment?: string;
  institutionId?: number;
  publishedVersionId?: number;
  createdAt: string;
  updatedAt: string;
}
