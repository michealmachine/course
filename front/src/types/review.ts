/**
 * 课程审核相关类型定义
 */

import { Course, CourseStatus } from './course';

/**
 * 审核任务状态枚举
 */
export enum ReviewStatus {
  PENDING = 0,       // 待审核
  IN_PROGRESS = 1,   // 审核中
  COMPLETED = 2,     // 已完成
}

/**
 * 审核任务类型枚举
 */
export enum ReviewType {
  COURSE = 0,        // 课程审核
  INSTITUTION = 1,   // 机构审核
}

/**
 * 审核任务模型
 */
export interface ReviewTask {
  id: number;
  reviewType: ReviewType;
  targetId: number;    // 审核对象ID（课程ID或机构ID）
  targetName: string;  // 审核对象名称
  status: ReviewStatus;
  submittedAt: string;
  reviewStartedAt?: string;
  reviewCompletedAt?: string;
  reviewerId?: number;
  reviewerName?: string;
  comment?: string;    // 审核意见
  createdAt: string;
  updatedAt: string;
  
  // 可选的关联信息
  course?: Course;     // 如果是课程审核，包含课程基本信息
}

/**
 * 课程审核响应类型
 */
export interface ReviewResponseDTO {
  comment?: string;      // 审核意见或拒绝原因
}

/**
 * 分页审核任务列表
 */
export interface ReviewTaskPage {
  content: ReviewTask[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
} 