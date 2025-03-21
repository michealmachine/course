'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse } from '@/types/api';
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

interface UserQuestionAnswerResultDTO {
  correct: boolean;
  correctAnswers: string[];
  explanation?: string;
}

interface LearningDurationDTO {
  courseId: number;
  sectionId: number;
  duration: number; // 秒数
}

interface LearningPositionDTO {
  courseId: number;
  chapterId: number;
  sectionId: number;
  sectionProgress: number;
}

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
  updateLearningPosition: async (position: LearningPositionDTO): Promise<void> => {
    try {
      await request.put(`/learning/courses/${position.courseId}/progress`, {
        chapterId: position.chapterId,
        sectionId: position.sectionId,
        sectionProgress: position.sectionProgress
      });
    } catch (error) {
      console.error('更新学习进度失败:', error);
      throw error;
    }
  },
  
  /**
   * 记录学习时长
   * @param data 学习时长数据
   */
  recordLearningDuration: async (data: LearningDurationDTO): Promise<void> => {
    try {
      await request.put(`/learning/courses/${data.courseId}/duration?duration=${data.duration}`);
    } catch (error) {
      console.error('记录学习时长失败:', error);
      throw error;
    }
  }
};

export default learningService; 