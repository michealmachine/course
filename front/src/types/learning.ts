/**
 * 用户问题答案DTO
 * 用于提交用户对题目的回答
 */
export interface UserQuestionAnswerDTO {
  /**
   * 题目ID
   */
  questionId: number;
  
  /**
   * 用户答案
   * - 单选题：包含一个选项ID
   * - 多选题：包含多个选项ID
   * - 判断题：包含一个值（true/false）
   * - 填空题：包含一个或多个文本
   * - 简答题：包含一个文本
   */
  answers: string[];
  
  /**
   * 正确答案（用于记录错题时）
   */
  correctAnswers: string[];
  
  /**
   * 题目类型
   */
  questionType: QuestionType;
  
  /**
   * 题目标题
   */
  questionTitle: string;
  
  /**
   * 用户回答用时（毫秒）
   */
  duration: number;
  
  /**
   * 是否做错
   */
  isWrong: boolean;
}

/**
 * 题目类型枚举
 */
export enum QuestionType {
  SINGLE_CHOICE = 'SINGLE_CHOICE',  // 单选题
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE', // 多选题
  TRUE_FALSE = 'TRUE_FALSE',  // 判断题
  FILL_BLANK = 'FILL_BLANK',  // 填空题
  SHORT_ANSWER = 'SHORT_ANSWER' // 简答题
}

/**
 * 问题视图对象
 */
export interface QuestionVO {
  id: number;
  title: string;
  content?: string;
  type: string;
  options?: string[];
  correctOptions?: string[];
  explanation?: string;
  score: number;
}

/**
 * 问题组视图对象
 */
export interface QuestionGroupVO {
  id: number;
  title: string;
  description?: string;
  sectionId: number;
  questions: QuestionVO[];
  totalQuestions: number;
  totalScore: number;
}

/**
 * 章节资源视图对象
 */
export interface SectionResourceVO {
  id: number;
  title: string;
  description?: string;
  type: string;
  resourceType: 'MEDIA' | 'QUESTION_GROUP';
  media?: MediaVO;
  questionGroup?: QuestionGroupVO;
}

/**
 * 章节视图对象
 */
export interface SectionVO {
  id: number;
  title: string;
  description?: string;
  order: number;
  duration: number;
  resourceType?: 'MEDIA' | 'QUESTION_GROUP';
  progress?: number;
  completed?: boolean;
}

/**
 * 章节视图对象
 */
export interface ChapterVO {
  id: number;
  title: string;
  description?: string;
  order: number;
  sections: SectionVO[];
  totalSections: number;
  completedSections: number;
  progress: number;
}

/**
 * 课程结构视图对象
 */
export interface CourseStructureVO {
  course?: {
    id: number;
    title: string;
    coverUrl: string;
    description?: string;
  };
  chapters: ChapterVO[];
  totalChapters?: number;
  totalSections?: number;
  completedSections?: number;
}

/**
 * 用户学习进度视图对象
 */
export interface UserLearningProgressVO {
  userId: number;
  courseId: number;
  lastChapterId: number;
  lastSectionId: number;
  progress: number;
  totalDuration: number;
  lastAccessTime: string;
}

/**
 * 学习位置接口
 */
export interface LearningPosition {
  chapterId: number;
  sectionId: number;
  sectionProgress: number;
}

/**
 * 媒体信息VO
 * 与后端一致的媒体资源类型
 */
export interface MediaVO {
  id: number;                // 媒体ID
  title: string;             // 标题
  description?: string;      // 描述
  type: string;              // 媒体类型
  size?: number;             // 文件大小
  originalFilename?: string; // 原始文件名
  status?: string;           // 状态
  institutionId?: number;    // 机构ID
  uploaderId?: number;       // 上传者ID
  uploadTime?: string;       // 上传时间
  lastAccessTime?: string;   // 最后访问时间
  accessUrl?: string;        // 访问URL（可能为空，需要单独请求）
  
  // 前端扩展字段，处理兼容性
  url?: string;              // 媒体URL
  duration?: number;         // 视频或音频时长（秒）
  coverUrl?: string;         // 媒体封面
} 