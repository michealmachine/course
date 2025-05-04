'use client';

import { request } from './api';
import { ApiResponse, PaginationResult } from '@/types/api';
import { UserCourseVO, LearningProgressDTO, LearningDurationDTO } from '@/types/userCourse';
import { Course } from '@/types/course';
import { AxiosResponse } from 'axios';

/**
 * 创建空的分页结果
 */
const getEmptyPaginationResult = <T>(page: number, size: number): PaginationResult<T> => {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size,
    number: page,
    empty: true,
    first: true,
    last: true,
    numberOfElements: 0,
    pageable: {
      pageNumber: page,
      pageSize: size,
      sort: {
        empty: true,
        sorted: false,
        unsorted: true
      },
      offset: 0,
      paged: true,
      unpaged: false
    }
  };
};

/**
 * 用户课程服务 - 处理用户课程相关API
 */
const userCourseService = {
  /**
   * 获取用户已购课程列表
   * 如果发生错误，返回空数组
   */
  getUserPurchasedCourses: async (): Promise<UserCourseVO[]> => {
    try {
      console.log('正在请求用户已购课程列表...');

      // 添加随机参数避免缓存
      const timestamp = new Date().getTime();
      const response = await request.get<UserCourseVO[]>(`/user-courses?_t=${timestamp}`, {
        silentOnAuthError: true,
        headers: {
          'Cache-Control': 'no-cache',
          'Pragma': 'no-cache'
        }
      });

      console.log('用户已购课程列表响应状态:', response.status);
      console.log('用户已购课程列表响应数据:', response.data);

      if (response?.data?.data && Array.isArray(response.data.data)) {
        console.log('用户已购课程数量:', response.data.data.length);
        console.log('课程详情:', response.data.data);
        return response.data.data;
      } else {
        console.log('用户已购课程列表为空或格式不正确');
        console.log('响应数据结构:', response.data);
        return [];
      }
    } catch (error: any) {
      // 如果是404错误或其他预期错误，返回空数组
      if (error.code === 404 || (error.response && error.response.status === 404)) {
        console.log('用户没有已购课程 (404)');
        return [];
      }

      // 记录详细错误信息
      console.error('获取用户已购课程列表失败:', error);
      console.log('错误详情:', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message
      });

      // 尝试直接从后端获取数据
      try {
        console.log('尝试直接从后端获取数据...');
        const directResponse = await fetch('/api/user-courses', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Cache-Control': 'no-cache'
          }
        });
        const directData = await directResponse.json();
        console.log('直接获取的数据:', directData);

        if (directData && directData.data && Array.isArray(directData.data)) {
          return directData.data;
        }
      } catch (fetchError) {
        console.error('直接获取数据也失败:', fetchError);
      }

      return [];
    }
  },

  /**
   * 分页获取用户已购课程列表
   * 如果发生错误，返回空分页结果
   */
  getUserPurchasedCoursesWithPagination: async (page: number = 0, size: number = 10): Promise<PaginationResult<UserCourseVO>> => {
    try {
      const response = await request.get<PaginationResult<UserCourseVO>>('/user-courses/page', {
        params: { page, size },
        silentOnAuthError: true
      });
      return response.data.data || getEmptyPaginationResult(page, size);
    } catch (error: any) {
      // 记录错误但不抛出，返回空分页结果
      console.error('分页获取用户已购课程列表失败:', error);
      return getEmptyPaginationResult(page, size);
    }
  },

  /**
   * 获取用户课程学习记录
   * 如果用户未购买课程，后端会返回404错误
   */
  getUserCourseRecord: async (courseId: number): Promise<UserCourseVO | null> => {
    try {
      const response = await request.get<UserCourseVO>(`/user-courses/${courseId}`);
      return response.data.data;
    } catch (error: any) {
      // 如果是404错误（未找到学习记录），这是预期的行为
      if (error.code === 404 || (error.response && error.response.status === 404)) {
        return null; // 返回null表示用户未购买课程
      }
      // 其他错误才记录到控制台
      console.error(`获取用户课程学习记录失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 更新用户课程学习进度
   */
  updateLearningProgress: async (courseId: number, chapterId: number, sectionId: number, sectionProgress: number): Promise<UserCourseVO> => {
    try {
      const response = await request.put<UserCourseVO>(
        `/user-courses/${courseId}/progress`,
        {
          chapterId,
          sectionId,
          sectionProgress
        }
      );
      return response.data.data;
    } catch (error) {
      console.error(`更新用户课程学习进度失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 记录用户学习时长
   */
  recordLearningDuration: async (courseId: number, duration: number): Promise<UserCourseVO> => {
    try {
      const response = await request.put<UserCourseVO>(
        `/user-courses/${courseId}/duration?duration=${duration}`
      );
      return response.data.data;
    } catch (error) {
      console.error(`记录用户学习时长失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 分页获取用户有效课程（状态正常的课程）
   * 如果发生错误，返回空分页结果
   */
  getUserValidCoursesWithPagination: async (page: number = 0, size: number = 10): Promise<PaginationResult<Course>> => {
    try {
      const response = await request.get<PaginationResult<Course>>('/user-courses/valid', {
        params: { page, size },
        silentOnAuthError: true
      });
      return response.data.data || getEmptyPaginationResult(page, size);
    } catch (error: any) {
      // 记录错误但不抛出，返回空分页结果
      console.error('分页获取用户有效课程列表失败:', error);
      return getEmptyPaginationResult(page, size);
    }
  },

  /**
   * 获取用户最近学习的课程
   * 如果发生错误，返回空数组
   */
  getRecentLearnedCourses: async (limit: number = 5): Promise<UserCourseVO[]> => {
    try {
      const response = await request.get<UserCourseVO[]>(`/user-courses/recent?limit=${limit}`, {
        silentOnAuthError: true
      });
      return response.data.data || [];
    } catch (error: any) {
      // 记录错误但不抛出，返回空数组
      console.error('获取用户最近学习的课程失败:', error);
      return [];
    }
  }
};

export default userCourseService;