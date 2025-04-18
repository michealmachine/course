/**
 * 机构学习统计相关类型定义
 */

import { Page } from './api';
import { DailyLearningStatVO, ActivityTypeStatVO } from '../services/learning-service';

/**
 * 课程学习统计VO
 */
export interface CourseStatisticsVO {
  /**
   * 课程ID
   */
  courseId: number;
  
  /**
   * 课程标题
   */
  courseTitle: string;
  
  /**
   * 学习人数
   * 在该课程中有学习记录的不同用户数量
   */
  learnerCount: number;
  
  /**
   * 总学习时长（秒）
   * 所有用户在该课程中的学习时长总和
   */
  totalDuration: number;
  
  /**
   * 学习活动次数
   * 所有用户在该课程中的学习活动总次数
   */
  activityCount: number;
  
  /**
   * 完成人数
   * 学习进度达到100%的用户数量
   */
  completionCount: number;
  
  /**
   * 平均学习进度
   * 所有学习该课程的用户的平均学习进度（百分比）
   */
  averageProgress: number;
}

/**
 * 活跃用户VO
 */
export interface ActiveUserVO {
  /**
   * 用户ID
   */
  userId: number;
  
  /**
   * 用户名
   */
  username: string;
  
  /**
   * 学习时长（秒）
   */
  learningDuration: number;
  
  /**
   * 学习活动次数
   */
  activityCount: number;
}

/**
 * 学生学习统计VO
 * 用于展示课程中每个学生的学习详情
 */
export interface StudentLearningVO {
  /**
   * 用户ID
   */
  userId: number;
  
  /**
   * 用户名
   */
  username: string;
  
  /**
   * 学习时长（秒）
   */
  learningDuration: number;
  
  /**
   * 学习进度（百分比）
   */
  progress: number;
  
  /**
   * 学习活动次数
   */
  activityCount: number;
  
  /**
   * 最后学习时间，ISO日期字符串
   */
  lastLearnTime: string;
}

/**
 * 机构学习统计数据VO
 * 用于向机构展示学习反馈数据
 */
export interface InstitutionLearningStatisticsVO {
  /**
   * 机构ID
   */
  institutionId: number;
  
  /**
   * 机构名称
   */
  institutionName: string;
  
  /**
   * 总学习人数
   * 在该机构课程中有学习记录的不同用户数量
   */
  totalLearners: number;
  
  /**
   * 总学习课程数
   * 该机构有学习记录的课程数量
   */
  totalActiveCourses: number;
  
  /**
   * 总学习时长（秒）
   * 所有用户在该机构课程中的学习时长总和
   */
  totalLearningDuration: number;
  
  /**
   * 今日学习时长（秒）
   * 今日所有用户在该机构课程中的学习时长总和
   */
  todayLearningDuration: number;
  
  /**
   * 本周学习时长（秒）
   * 本周所有用户在该机构课程中的学习时长总和
   */
  weekLearningDuration: number;
  
  /**
   * 本月学习时长（秒）
   * 本月所有用户在该机构课程中的学习时长总和
   */
  monthLearningDuration: number;
  
  /**
   * 总题目尝试次数
   * 所有用户在该机构课程中尝试回答题目的总次数
   */
  totalQuestionAttempts: number;
  
  /**
   * 课程学习统计列表
   * 按课程分组的学习统计信息
   */
  courseStatistics: CourseStatisticsVO[];
  
  /**
   * 每日学习统计（过去30天）
   * 按日期分组的学习时长和活动次数
   */
  dailyLearning: DailyLearningStatVO[];
  
  /**
   * 活动类型统计
   * 按学习活动类型分组的统计信息
   */
  activityTypeStats: ActivityTypeStatVO[];
  
  /**
   * 最活跃用户统计
   * 学习时长最长的用户列表
   */
  mostActiveUsers: ActiveUserVO[];
}

/**
 * 学习时长统计响应
 */
export interface LearningDurationResponse {
  /**
   * 今日学习时长（秒）
   */
  todayLearningDuration: number;
  
  /**
   * 总学习时长（秒）
   */
  totalLearningDuration: number;
  
  /**
   * 总学习人数
   */
  totalLearners: number;
} 