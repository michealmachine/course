'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse, Page } from '@/types/api';
import { 
  CourseStructureVO, 
  MediaVO, 
  QuestionGroupVO,
  UserQuestionAnswerDTO, 
  SectionResourceVO, 
  UserLearningProgressVO 
} from '@/types/learning';
import { UserCourseVO } from '@/types/userCourse';

// 学习课程结构视图对象（扩展了CourseStructureVO）
export interface LearningCourseStructureVO extends CourseStructureVO {
  userProgress: UserCourseVO;
  currentPosition: {
    chapterId: number;
    sectionId: number;
    sectionProgress: number;
  };
}

// 学习进度更新DTO
export interface LearningProgressUpdateDTO {
  chapterId: number;
  sectionId: number;
  sectionProgress: number;
}

// 学习统计数据VO
export interface LearningStatisticsVO {
  userId: number;
  totalCourses: number;
  completedCourses: number;
  totalLearningDuration: number;
  todayLearningDuration: number;
  weekLearningDuration: number;
  monthLearningDuration: number;
  learningDays: number;
  maxConsecutiveDays: number;
  currentConsecutiveDays: number;
  totalQuestions: number;
  correctQuestions: number;
  wrongQuestions: number;
  courseStatistics: CourseStatisticsVO[];
  dailyLearning: DailyLearningVO[];
}

// 课程学习统计VO
export interface CourseStatisticsVO {
  courseId: number;
  courseTitle: string;
  courseCover: string;
  progress: number;
  learningDuration: number;
  lastLearnTime: number;
}

// 每日学习统计VO
export interface DailyLearningVO {
  date: string;
  duration: number;
}

// 每日学习统计返回的格式
export interface DailyLearningStatVO {
  date: string;
  durationSeconds: number;
  activityCount: number;
}

// 活动类型统计
export interface ActivityTypeStatVO {
  activityType: string;
  activityTypeDescription: string;
  totalDurationSeconds: number;
  activityCount: number;
}

// 学习记录视图对象
export interface LearningRecordVO {
  id: number;
  userId: number;
  courseId: number;
  courseTitle: string;
  chapterId: number;
  chapterTitle: string;
  sectionId: number;
  sectionTitle: string;
  activityType: string;
  activityTypeDescription: string;
  activityStartTime: string;
  activityEndTime: string;
  durationSeconds: number;
  contextData: string;
  createdAt: string;
}

// 学习记录开始DTO
export interface LearningRecordStartDTO {
  courseId: number;
  chapterId?: number;
  sectionId?: number;
  activityType: string;
  contextData?: string;
}

// 学习记录结束DTO
export interface LearningRecordEndDTO {
  contextData?: string;
}

// 学习记录完成DTO
export interface LearningRecordCompletedDTO {
  courseId: number;
  chapterId?: number;
  sectionId?: number;
  activityType: string;
  durationSeconds: number;
  contextData?: string;
}

interface UserQuestionAnswerResultDTO {
  correct: boolean;
  correctAnswers: string[];
  explanation?: string;
}

interface LearningDurationDTO {
  courseId: number;
  sectionId?: number;
  duration: number; // 秒数
}

interface LearningPositionDTO {
  courseId: number;
  chapterId: number;
  sectionId: number;
  sectionProgress: number;
}

/**
 * 学习活动类型枚举
 */
export const LearningActivityType = {
  VIDEO_WATCH: 'VIDEO_WATCH',
  DOCUMENT_READ: 'DOCUMENT_READ',
  QUIZ_ATTEMPT: 'QUIZ_ATTEMPT',
  SECTION_START: 'SECTION_START',
  SECTION_END: 'SECTION_END'
};

/**
 * 学习服务 - 处理学习相关API
 */
const learningService = {
  /**
   * 获取课程结构
   * @param courseId 课程ID
   * @returns 课程结构
   */
  getCourseStructure: async (courseId: string | number): Promise<LearningCourseStructureVO> => {
    try {
      const response: AxiosResponse<ApiResponse<LearningCourseStructureVO>> = 
        await request.get(`/learning/courses/${courseId}`);
      
      console.log('课程结构API响应:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程结构失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },
  
  /**
   * 获取用户课程学习进度
   * @param courseId 课程ID
   * @returns 用户的学习进度
   */
  getUserLearningProgress: async (courseId: string | number): Promise<UserLearningProgressVO> => {
    try {
      const response: AxiosResponse<ApiResponse<UserLearningProgressVO>> = 
        await request.get(`/learning/courses/${courseId}/progress`);
      
      return response.data.data;
    } catch (error) {
      console.error(`获取用户课程学习进度失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },
  
  /**
   * 获取章节媒体资源
   * @param sectionId 章节ID
   * @returns 章节媒体资源
   */
  getSectionMedia: async (sectionId: string | number): Promise<MediaVO> => {
    try {
      const response: AxiosResponse<ApiResponse<MediaVO>> = 
        await request.get(`/learning/sections/${sectionId}/media`);
      
      return response.data.data;
    } catch (error) {
      console.error(`获取章节媒体资源失败, 章节ID: ${sectionId}:`, error);
      throw error;
    }
  },
  
  /**
   * 获取章节问题组
   * @param sectionId 章节ID
   * @returns 章节问题组
   */
  getSectionQuestionGroup: async (sectionId: string | number): Promise<QuestionGroupVO> => {
    try {
      console.log(`开始获取章节问题组, 章节ID: ${sectionId}`);
      const response: AxiosResponse<ApiResponse<QuestionGroupVO>> = 
        await request.get(`/learning/sections/${sectionId}/question-group`);
      
      console.log(`获取章节问题组成功, 章节ID: ${sectionId}, 响应:`, response.data);
      
      // 检查返回的数据格式
      const questionGroup = response.data.data;
      if (!questionGroup) {
        console.error(`章节问题组数据为空, 章节ID: ${sectionId}`);
        throw new Error('题组数据为空');
      }
      
      // 处理后端返回的数据结构
      // 1. 从items数组中提取questions
      if (questionGroup.items && Array.isArray(questionGroup.items)) {
        console.log(`题组包含 ${questionGroup.items.length} 个题目项`);
        
        // 将items中的question对象提取到questions数组
        const questions = questionGroup.items.map(item => {
          if (item.question) {
            // 如果有完整的question对象，直接使用
            return {
              ...item.question,
              // 使用item中的分值覆盖question中的分值（如果存在）
              score: item.score !== undefined ? item.score : item.question.score
            };
          } else {
            // 如果没有完整的question对象，构建一个基本的题目对象
            return {
              id: item.questionId || 0,
              title: '未知题目',
              type: 0,
              score: item.score || 0
            };
          }
        });
        
        // 将提取的题目添加到questionGroup
        questionGroup.questions = questions;
        console.log('提取的题目数据:', questions);
      }
      
      // 2. 处理title和name的兼容性
      if (questionGroup.name && !questionGroup.title) {
        questionGroup.title = questionGroup.name;
      }
      
      // 3. 添加sectionId字段（如果没有）
      if (!questionGroup.sectionId) {
        console.log(`为问题组添加sectionId字段: ${sectionId}`);
        questionGroup.sectionId = Number(sectionId);
      }
      
      // 4. 添加必要的统计字段
      // 使用questionCount或计算questions长度
      questionGroup.totalQuestions = questionGroup.questionCount || 
                                     (questionGroup.questions ? questionGroup.questions.length : 0);
      
      // 计算总分
      if (questionGroup.questions && !questionGroup.totalScore) {
        questionGroup.totalScore = questionGroup.questions.reduce((sum, q) => sum + (q.score || 0), 0);
      }
      
      console.log('处理后的题组数据:', {
        id: questionGroup.id,
        title: questionGroup.title || questionGroup.name,
        sectionId: questionGroup.sectionId,
        questionCount: questionGroup.totalQuestions,
        totalScore: questionGroup.totalScore
      });
      
      return questionGroup;
    } catch (error) {
      console.error(`获取章节问题组失败, 章节ID: ${sectionId}:`, error);
      throw error;
    }
  },
  
  /**
   * 提交问题答案
   * @param sectionId 章节ID
   * @param answer 答案数据
   * @returns 答案结果
   */
  submitQuestionAnswer: async (
    sectionId: string | number, 
    answer: UserQuestionAnswerDTO
  ): Promise<UserQuestionAnswerResultDTO> => {
    try {
      const response: AxiosResponse<ApiResponse<UserQuestionAnswerResultDTO>> = 
        await request.post(`/learning/sections/${sectionId}/questions/${answer.questionId}/answer`, answer);
      
      return response.data.data;
    } catch (error) {
      console.error(`提交问题答案失败, 章节ID: ${sectionId}:`, error);
      throw error;
    }
  },
  
  /**
   * 更新学习进度
   * @param position 学习位置信息
   */
  updateLearningPosition: async (position: LearningPositionDTO): Promise<UserCourseVO> => {
    try {
      const response = await request.put(`/learning/courses/${position.courseId}/progress`, {
        chapterId: position.chapterId,
        sectionId: position.sectionId,
        sectionProgress: position.sectionProgress
      });
      return response.data.data as UserCourseVO;
    } catch (error) {
      console.error('更新学习进度失败:', error);
      throw error;
    }
  },
  
  /**
   * 记录学习时长（旧方法，已废弃，建议使用学习记录相关接口）
   * @param data 学习时长数据
   * @deprecated 使用recordCompletedActivity代替
   */
  recordLearningDuration: async (data: LearningDurationDTO): Promise<UserCourseVO> => {
    try {
      const response = await request.put(`/learning/courses/${data.courseId}/duration?duration=${data.duration}`);
      return response.data.data as UserCourseVO;
    } catch (error) {
      console.error('记录学习时长失败:', error);
      throw error;
    }
  },

  /**
   * 开始学习活动
   * @param dto 学习活动开始数据
   */
  startLearningActivity: async (dto: LearningRecordStartDTO): Promise<LearningRecordVO> => {
    try {
      const response = await request.post('/learning/records/start', dto);
      return response.data.data as LearningRecordVO;
    } catch (error) {
      console.error('开始学习活动失败:', error);
      throw error;
    }
  },

  /**
   * 结束学习活动
   * @param recordId 记录ID
   * @param dto 结束数据（可选）
   */
  endLearningActivity: async (recordId: number, dto?: LearningRecordEndDTO): Promise<LearningRecordVO> => {
    try {
      const response = await request.put(`/learning/records/${recordId}/end`, dto || {});
      return response.data.data as LearningRecordVO;
    } catch (error) {
      console.error(`结束学习活动失败, 记录ID: ${recordId}:`, error);
      throw error;
    }
  },

  /**
   * 记录已完成的学习活动
   * @param dto 已完成活动数据
   */
  recordCompletedActivity: async (dto: LearningRecordCompletedDTO): Promise<LearningRecordVO> => {
    try {
      // 确保数值类型参数
      const sanitizedDto = {
        ...dto,
        courseId: Number(dto.courseId),
        chapterId: dto.chapterId ? Number(dto.chapterId) : undefined,
        sectionId: dto.sectionId ? Number(dto.sectionId) : undefined,
        durationSeconds: Math.max(1, Number(dto.durationSeconds))
      };
      
      console.log('发送学习记录请求, 参数:', JSON.stringify(sanitizedDto));
      
      const response = await request.post('/learning/records/completed', sanitizedDto);
      console.log('学习记录请求成功', response.data);
      return response.data.data as LearningRecordVO;
    } catch (error) {
      console.error('记录已完成学习活动失败:', error);
      // 重试一次
      try {
        console.log('尝试重新记录学习活动...');
        const sanitizedDto = {
          ...dto,
          courseId: Number(dto.courseId),
          chapterId: dto.chapterId ? Number(dto.chapterId) : undefined,
          sectionId: dto.sectionId ? Number(dto.sectionId) : undefined,
          durationSeconds: Math.max(1, Number(dto.durationSeconds))
        };
        const response = await request.post('/learning/records/completed', sanitizedDto);
        console.log('重试记录学习活动成功');
        return response.data.data as LearningRecordVO;
      } catch (retryError) {
        console.error('重试记录学习活动也失败:', retryError);
        throw retryError;
      }
    }
  },

  /**
   * 查找用户当前进行中的活动
   */
  findOngoingActivity: async (): Promise<LearningRecordVO | null> => {
    try {
      const response = await request.get('/learning/records/ongoing');
      return response.data.data as LearningRecordVO | null;
    } catch (error) {
      console.error('查找用户当前进行中的活动失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户学习记录（分页）
   * @param page 页码
   * @param size 每页大小
   */
  getUserLearningRecords: async (page: number = 0, size: number = 10): Promise<Page<LearningRecordVO>> => {
    try {
      const response = await request.get('/learning/records', {
        params: { page, size }
      });
      return response.data.data as Page<LearningRecordVO>;
    } catch (error) {
      console.error('获取用户学习记录失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户课程学习记录（分页）
   * @param courseId 课程ID
   * @param page 页码
   * @param size 每页大小
   */
  getUserCourseLearningRecords: async (
    courseId: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<Page<LearningRecordVO>> => {
    try {
      const response = await request.get(`/learning/courses/${courseId}/records`, {
        params: { page, size }
      });
      return response.data.data as Page<LearningRecordVO>;
    } catch (error) {
      console.error(`获取用户课程学习记录失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取学习热图数据
   * @param startDate 开始日期（YYYY-MM-DD）
   * @param endDate 结束日期（YYYY-MM-DD）
   */
  getLearningHeatmap: async (
    startDate?: string, 
    endDate?: string
  ): Promise<DailyLearningStatVO[]> => {
    try {
      const response = await request.get('/learning/stats/heatmap', {
        params: { startDate, endDate }
      });
      return response.data.data as DailyLearningStatVO[];
    } catch (error) {
      console.error('获取学习热图数据失败:', error);
      throw error;
    }
  },

  /**
   * 获取活动类型统计
   */
  getActivityTypeStats: async (): Promise<ActivityTypeStatVO[]> => {
    try {
      const response = await request.get('/learning/stats/activity-types');
      return response.data.data as ActivityTypeStatVO[];
    } catch (error) {
      console.error('获取活动类型统计失败:', error);
      throw error;
    }
  },

  /**
   * 获取今日学习时长
   */
  getTodayLearningDuration: async (): Promise<number> => {
    try {
      const response = await request.get('/learning/stats/today-duration');
      return response.data.data as number;
    } catch (error) {
      console.error('获取今日学习时长失败:', error);
      throw error;
    }
  },

  /**
   * 获取总学习时长
   */
  getTotalLearningDuration: async (): Promise<number> => {
    try {
      const response = await request.get('/learning/stats/total-duration');
      return response.data.data as number;
    } catch (error) {
      console.error('获取总学习时长失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户学习统计数据
   */
  getLearningStatistics: async (): Promise<LearningStatisticsVO> => {
    try {
      const response = await request.get('/learning/statistics');
      return response.data.data as LearningStatisticsVO;
    } catch (error) {
      console.error('获取用户学习统计数据失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户特定课程的学习统计数据
   * @param courseId 课程ID
   */
  getCourseLearningStatistics: async (courseId: number): Promise<CourseStatisticsVO> => {
    try {
      const response = await request.get(`/learning/courses/${courseId}/statistics`);
      return response.data.data as CourseStatisticsVO;
    } catch (error) {
      console.error(`获取用户课程学习统计数据失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 重置课程学习进度
   * @param courseId 课程ID
   */
  resetCourseProgress: async (courseId: number): Promise<void> => {
    try {
      await request.put(`/learning/courses/${courseId}/reset-progress`);
    } catch (error) {
      console.error(`重置课程学习进度失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }
};

export default learningService; 