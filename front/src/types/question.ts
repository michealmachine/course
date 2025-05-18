// 问题类型
export interface Question {
  id: number;
  title: string;
  description?: string;
  type: QuestionType;
  difficulty: QuestionDifficulty;
  options?: QuestionOption[];
  answer?: string;
  analysis?: string;
  institutionId: number;
  createdBy: number;
  createdAt: string;
  updatedAt?: string;
  tagIds?: number[];
  tags?: QuestionTag[];
  score?: number;
}

// 问题创建/更新DTO
export interface QuestionDTO {
  title: string;
  description?: string;
  type: QuestionType;
  difficulty: QuestionDifficulty;
  options?: QuestionOptionDTO[];
  answer?: string;
  analysis?: string;
  institutionId: number;
  tagIds?: number[];
  content?: string;
  score?: number;
}

// 题目类型枚举
export enum QuestionType {
  SINGLE_CHOICE = 0,
  MULTIPLE_CHOICE = 1,
  TRUE_FALSE = 2,
  FILL_BLANK = 3,
  SHORT_ANSWER = 4
}

// 题目难度枚举
export enum QuestionDifficulty {
  EASY = 1,
  MEDIUM = 2,
  HARD = 3
}

// 问题选项
export interface QuestionOption {
  id: number;
  content: string;
  isCorrect: boolean;
  questionId: number;
  optionOrder: number;
}

// 问题选项DTO
export interface QuestionOptionDTO {
  content: string;
  isCorrect: boolean;
  orderIndex: number;  // 与后端保持一致，使用orderIndex而不是optionOrder
  optionOrder?: number; // 保留兼容性
}

// 问题标签
export interface QuestionTag {
  id: number;
  name: string;
  description?: string;
  institutionId: number;
  createdAt?: string;
  updatedAt?: string;
}

// 问题标签DTO
export interface QuestionTagDTO {
  name: string;
  institutionId: number;
}

// 问题组
export interface QuestionGroup {
  id: number;
  name: string;
  description?: string;
  institutionId: number;
  questionCount: number;
  creatorId: number;
  createdAt: string;
  updatedAt: string;
}

// 问题组DTO
export interface QuestionGroupDTO {
  name: string;
  description?: string;
  institutionId: number;
}

// 问题查询参数
export interface QuestionQueryParams {
  page?: number;
  pageSize?: number;
  title?: string;
  keyword?: string;
  search?: string;
  type?: QuestionType;
  difficulty?: QuestionDifficulty;
  institutionId?: number;
  tagIds?: number[];
  groupId?: number;
}

/**
 * 试题导入结果
 */
export interface QuestionImportResultVO {
  /**
   * 总条目数
   */
  totalCount: number;

  /**
   * 成功导入数
   */
  successCount: number;

  /**
   * 失败数
   */
  failureCount: number;

  /**
   * 导入用时(毫秒)
   */
  duration: number;

  /**
   * 失败记录列表
   */
  failureItems: FailureItem[];
}

/**
 * 导入失败记录项
 */
export interface FailureItem {
  /**
   * Excel行号(从1开始)
   */
  rowIndex: number;

  /**
   * 题目标题
   */
  title: string;

  /**
   * 错误信息
   */
  errorMessage: string;
}

/**
 * 题目组项视图对象
 */
export interface QuestionGroupItemVO {
  /**
   * 项目ID
   */
  id: number;

  /**
   * 题目组ID
   */
  groupId: number;

  /**
   * 题目
   */
  question: Question;

  /**
   * 在组中的顺序
   */
  orderIndex: number;

  /**
   * 难度级别（可覆盖题目原始难度）
   */
  difficulty?: number;

  /**
   * 分值（可覆盖题目原始分值）
   */
  score?: number;
}

// 题组中的题目
export interface QuestionGroupItem {
  id: number;
  groupId: number;
  questionId: number;
  orderIndex: number;
  question: Question;
  createdAt: string;
  updatedAt: string;
}