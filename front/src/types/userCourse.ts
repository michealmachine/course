// 用户课程状态枚举
export enum UserCourseStatus {
  NORMAL = 0,    // 正常学习
  EXPIRED = 1,   // 已过期
  REFUNDED = 2   // 已退款
}

// 用户课程关系值对象
export interface UserCourseVO {
  id: number;
  userId: number;
  userName: string;
  courseId: number;
  courseTitle: string;
  courseCover?: string;
  institutionId: number;
  institutionName: string;
  purchasedAt: string;
  orderId?: number;
  orderNo?: string;
  expireAt?: string;
  progress: number;
  status: UserCourseStatus;
  lastLearnAt?: string;
  learnDuration: number;
  createdAt: string;
  updatedAt: string;
}

// 学习进度更新请求
export interface LearningProgressDTO {
  chapterId: number;
  sectionId: number;
  sectionProgress: number;
}

// 学习时长记录请求
export interface LearningDurationDTO {
  duration: number;
} 