/**
 * 课程状态枚举
 */
export enum CourseStatus {
  DRAFT = 0,           // 草稿
  PENDING_REVIEW = 1,  // 待审核
  REVIEWING = 2,       // 审核中
  REJECTED = 3,        // 已拒绝
  PUBLISHED = 4,       // 已发布
  ARCHIVED = 5         // 已归档
}

/**
 * 课程支付类型枚举
 */
export enum CoursePaymentType {
  FREE = 0,  // 免费
  PAID = 1   // 付费
}

/**
 * 课程难度枚举
 */
export enum CourseDifficulty {
  BEGINNER = 1,    // 初级
  INTERMEDIATE = 2, // 中级
  ADVANCED = 3      // 高级
}
