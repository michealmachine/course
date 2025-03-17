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
  description?: string;
  parentId?: number;
  parentName?: string;
  level?: number;
  iconUrl?: string;
  orderIndex?: number;
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
  DRAFT = 0,              // 草稿
  PENDING_REVIEW = 1,     // 待审核
  REVIEWING = 2,          // 审核中
  REJECTED = 3,           // 已拒绝
  PUBLISHED = 4,          // 已发布
  UNPUBLISHED = 5         // 已下线
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

// 机构简要信息
export interface InstitutionVO {
  id: number;
  name: string;
  logo?: string;
}

// 课程模型
export interface Course {
  id: number;
  title: string;
  description?: string;
  coverUrl?: string;
  status: number;
  versionType: number;
  isPublishedVersion?: boolean;
  publishedVersionId?: number; // 指向发布版本的ID
  creatorId?: number;
  creatorName?: string;
  institution?: InstitutionVO;
  category?: Category;
  tags?: Tag[];
  paymentType: number;
  price?: number;
  discountPrice?: number;
  difficulty?: number;
  targetAudience?: string;
  learningObjectives?: string;
  totalLessons?: number;
  totalDuration?: number;
  totalChapters?: number;
  totalSections?: number;
  studentCount?: number;      // 学习人数
  averageRating?: number;     // 平均评分(1-5星)
  ratingCount?: number;       // 评分人数
  submittedAt?: string;
  reviewStartedAt?: string;
  reviewedAt?: string;
  publishedAt?: string;
  reviewComment?: string;
  reviewerId?: number;
  reviewerName?: string;
  createdAt?: string;
  updatedAt?: string;
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
  orderIndex: number;
  accessType: number;
  courseId: number;
  courseName?: string;
  sections?: Section[];
  createdAt?: string;
  updatedAt?: string;
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
  orderIndex: number;
  contentType: string; // video, document, audio, text, image, mixed
  chapterId: number;
  chapterTitle?: string;
  
  // 访问类型
  accessType?: number;
  
  // 预计学习时间（分钟）
  estimatedMinutes?: number;
  
  // 资源类型鉴别器：MEDIA, QUESTION_GROUP, NONE
  resourceTypeDiscriminator?: string;
  
  // 直接关联的媒体资源（仅当resourceTypeDiscriminator为MEDIA时有效）
  media?: MediaVO;
  
  // 媒体资源ID（仅当resourceTypeDiscriminator为MEDIA时有效）
  mediaId?: number;
  
  // 媒体资源类型(primary, supplementary, homework, reference)
  mediaResourceType?: string;
  
  // 直接关联的题目组（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
  questionGroup?: QuestionGroupVO;
  
  // 题目组ID（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
  questionGroupId?: number;
  
  // 题目组配置（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
  randomOrder?: boolean;
  orderByDifficulty?: boolean;
  showAnalysis?: boolean;
  
  // 已弃用：使用直接关联的媒体资源或题目组替代
  resources?: SectionResource[];
  // 已弃用：使用直接关联的媒体资源或题目组替代
  questionGroups?: SectionQuestionGroup[];
  
  createdTime?: number;
  updatedTime?: number;
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

// 媒体信息
export interface MediaVO {
  id: number;
  title: string;
  description?: string;
  type?: string;
  size?: number;
  originalFilename?: string;
  status?: string;
  institutionId?: number;
  uploaderId?: number;
  uploadTime?: number;
  lastAccessTime?: number;
  accessUrl?: string;
}

// 小节资源模型
export interface SectionResource {
  id: number;
  sectionId: number;
  mediaId: number;
  media?: MediaVO; // 媒体信息
  resourceType: string; // primary, supplementary, homework, reference
  orderIndex: number;
  createdTime?: number;
  updatedTime?: number;
}

// 小节资源创建/更新请求参数
export interface SectionResourceDTO {
  sectionId: number;
  mediaId: number;
  resourceType: string;
  orderIndex?: number;
}

// 题目组简要信息
export interface QuestionGroupVO {
  id: number;
  name: string;
  description?: string;
  institutionId?: number;
  questionCount?: number;
  creatorId?: number;
  creatorName?: string;
  createdTime?: number;
  updatedTime?: number;
}

// 小节题目组模型
export interface SectionQuestionGroup {
  id: number;
  sectionId: number;
  questionGroupId: number;
  questionGroup?: QuestionGroupVO; // 题目组信息
  orderIndex: number;
  randomOrder: boolean;
  orderByDifficulty: boolean;
  showAnalysis: boolean;
  createdTime?: number;
  updatedTime?: number;
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

// 小节题目组配置DTO
export interface SectionQuestionGroupConfigDTO {
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

/**
 * 课程结构视图对象
 */
export interface CourseStructureVO {
  course: Course;
  chapters: ChapterVO[];
}

/**
 * 章节视图对象
 */
export interface ChapterVO {
  id: number;
  title: string;
  description?: string;
  order: number;
  accessType: number;
  estimatedMinutes?: number;
  sections: SectionVO[];
}

/**
 * 小节视图对象
 */
export interface SectionVO {
  id: number;
  title: string;
  description?: string;
  order: number;
  duration?: number;
  resourceTypeDiscriminator: 'MEDIA' | 'QUESTION_GROUP' | 'NONE';
  mediaId?: number;
  questionGroupId?: number;
  accessType?: number;
}

// 课程查询参数
export interface CourseQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: CourseStatus;
  categoryId?: number;
  difficulty?: CourseDifficulty;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

// 课程列表响应
export interface CourseListResponse {
  content: Course[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  empty: boolean;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort?: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
} 