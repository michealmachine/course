/**
 * 课程评论相关类型定义
 */

/**
 * 评论查询参数
 */
export interface ReviewQueryParams {
  courseId: number;
  page?: number;
  size?: number;
  orderBy?: ReviewSortOrder;
  ratingFilter?: number;
}

/**
 * 评论排序方式
 */
export enum ReviewSortOrder {
  NEWEST = 'newest',
  HIGHEST_RATING = 'highest_rating',
  LOWEST_RATING = 'lowest_rating'
}

/**
 * 评论创建/更新请求参数
 */
export interface ReviewCreateDTO {
  courseId: number;
  rating: number;
  content?: string;
}

/**
 * 单个评论视图对象
 */
export interface ReviewVO {
  id: number;
  courseId: number;
  courseTitle?: string;
  userId: number;
  username: string;
  userAvatar?: string;
  rating: number;
  content?: string;
  likeCount: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 评论统计数据
 */
export interface ReviewStatsVO {
  courseId: number;
  averageRating: number;
  ratingCount: number;
  ratingDistribution: {
    [key: number]: number; // key: 评分(1-5), value: 数量
  };
}

/**
 * 评论区数据（包含评论列表和统计信息）
 */
export interface CourseReviewSectionVO {
  courseId: number;
  stats: ReviewStatsVO;
  reviews: ReviewVO[];
  totalReviews: number;
  currentPage: number;
  totalPages: number;
}

/**
 * 评论分页结果
 */
export interface ReviewPage {
  content: ReviewVO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
} 