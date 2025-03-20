import { Page } from './api';

/**
 * 错题状态枚举
 */
export enum WrongQuestionStatus {
  UNRESOLVED = 0, // 未解决
  RESOLVED = 1    // 已解决
}

/**
 * 用户错题视图对象
 */
export interface UserWrongQuestionVO {
  /**
   * 错题ID
   */
  id: number;
  
  /**
   * 用户ID
   */
  userId: number;
  
  /**
   * 课程ID
   */
  courseId: number;
  
  /**
   * 课程标题
   */
  courseTitle: string;
  
  /**
   * 小节ID
   */
  sectionId: number;
  
  /**
   * 问题ID
   */
  questionId: number;
  
  /**
   * 问题标题
   */
  questionTitle: string;
  
  /**
   * 问题类型
   */
  questionType: string;
  
  /**
   * 用户答案
   */
  userAnswers: string[];
  
  /**
   * 正确答案
   */
  correctAnswers: string[];
  
  /**
   * 状态：0-未解决，1-已解决
   */
  status: WrongQuestionStatus;
  
  /**
   * 创建时间
   */
  createdAt: string;
  
  /**
   * 更新时间
   */
  updatedAt: string;
}

/**
 * 错题查询参数
 */
export interface WrongQuestionQueryParams {
  /**
   * 页码
   */
  page?: number;
  
  /**
   * 每页数量
   */
  size?: number;
  
  /**
   * 排序字段
   */
  sortBy?: string;
  
  /**
   * 排序方向
   */
  direction?: 'asc' | 'desc';
}

/**
 * 错题分页结果
 */
export type WrongQuestionPageResult = Page<UserWrongQuestionVO>; 