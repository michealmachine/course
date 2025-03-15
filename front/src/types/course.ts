// 标签类型定义
export interface Tag {
  id: number;
  name: string;
  description?: string;
  useCount?: number;
  courseCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

// 标签创建/更新请求参数
export interface TagDTO {
  id?: number;
  name: string;
  description?: string;
}

// 分类类型定义
export interface Category {
  id: number;
  name: string;
  code: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  level?: number;
  orderIndex?: number;
  enabled?: boolean;
  icon?: string;
  courseCount?: number;
  childrenCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

// 分类创建/更新请求参数
export interface CategoryDTO {
  id?: number;
  name: string;
  code: string;
  description?: string;
  parentId?: number;
  orderIndex?: number;
  enabled?: boolean;
  icon?: string;
}

// 分类树结构
export interface CategoryTree {
  id: number;
  name: string;
  code: string;
  description?: string;
  level?: number;
  orderIndex?: number;
  enabled?: boolean;
  icon?: string;
  courseCount?: number;
  children?: CategoryTree[];
  fullPath?: string;
}

// 课程状态枚举
export enum CourseStatus {
  DRAFT = 0,         // 草稿
  REVIEWING = 1,     // 审核中
  PUBLISHED = 2,     // 已发布
  REJECTED = 3       // 已拒绝
}

// 付费类型枚举
export enum CoursePaymentType {
  FREE = 0,          // 免费
  PAID = 1           // 付费
}

// 章节访问类型枚举
export enum ChapterAccessType {
  FREE_TRIAL = 0,    // 免费试学
  PAID_ONLY = 1      // 付费访问
}

// 课程难度枚举
export enum CourseDifficulty {
  BEGINNER = 1,      // 初级
  INTERMEDIATE = 2,  // 中级
  ADVANCED = 3       // 高级
}

// 课程模型
export interface Course {
  id: number;
  title: string;
  description?: string;
  institutionId: number;
  categoryId?: number;
  category?: Category;
  coverImageUrl?: string;
  paymentType: CoursePaymentType;
  price?: number;
  discountPrice?: number;
  difficulty?: CourseDifficulty;
  targetAudience?: string;
  learningObjectives?: string;
  status: CourseStatus;
  createdAt: string;
  updatedAt: string;
  createdBy?: number;
  reviewerId?: number;
  reviewComment?: string;
  tags?: Tag[];
}

// 课程创建/更新请求参数
export interface CourseCreateDTO {
  title: string;
  description?: string;
  categoryId?: number;
  tagIds?: number[];
  paymentType: CoursePaymentType;
  price?: number;
  discountPrice?: number;
  difficulty?: CourseDifficulty;
  targetAudience?: string;
  learningObjectives?: string;
}

// 章节模型
export interface Chapter {
  id: number;
  title: string;
  description?: string;
  courseId: number;
  orderIndex: number;
  accessType: ChapterAccessType;
  estimatedMinutes?: number;
  createdAt: string;
  updatedAt: string;
  sections?: Section[];
}

// 章节创建/更新请求参数
export interface ChapterCreateDTO {
  title: string;
  description?: string;
  courseId: number;
  orderIndex?: number;
  accessType?: ChapterAccessType;
  estimatedMinutes?: number;
}

// 章节排序请求参数
export interface ChapterOrderDTO {
  id: number;
  orderIndex: number;
}

// 小节模型
export interface Section {
  id: number;
  title: string;
  description?: string;
  chapterId: number;
  orderIndex: number;
  contentType: string; // video, document, audio, text, image, mixed
  accessType?: ChapterAccessType; // 访问类型
  estimatedMinutes?: number; // 预计学习时间
  createdAt: string;
  updatedAt: string;
  resources?: SectionResource[];
  questionGroups?: SectionQuestionGroup[];
}

// 小节创建/更新请求参数
export interface SectionCreateDTO {
  title: string;
  description?: string;
  chapterId: number;
  orderIndex?: number;
  contentType: string;
  accessType?: ChapterAccessType; // 访问类型
  estimatedMinutes?: number; // 预计学习时间
}

// 小节排序请求参数
export interface SectionOrderDTO {
  id: number;
  orderIndex: number;
}

// 小节资源模型
export interface SectionResource {
  id: number;
  sectionId: number;
  mediaId: number;
  resourceType: string; // primary, supplementary, homework, reference
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
  media?: any; // 媒体信息
}

// 小节资源创建/更新请求参数
export interface SectionResourceDTO {
  sectionId: number;
  mediaId: number;
  resourceType: string;
  orderIndex?: number;
}

// 小节题目组模型
export interface SectionQuestionGroup {
  id: number;
  sectionId: number;
  questionGroupId: number;
  orderIndex: number;
  randomOrder: boolean;
  orderByDifficulty: boolean;
  showAnalysis: boolean;
  createdAt: string;
  updatedAt: string;
  questionGroup?: any; // 题目组信息
}

// 小节题目组创建/更新请求参数
export interface SectionQuestionGroupDTO {
  sectionId: number;
  questionGroupId: number;
  orderIndex?: number;
  randomOrder?: boolean;
  orderByDifficulty?: boolean;
  showAnalysis?: boolean;
}

// 课程预览URL
export interface PreviewUrlVO {
  url: string;
  expireTime: string;
  courseId: number;
  courseTitle: string;
} 