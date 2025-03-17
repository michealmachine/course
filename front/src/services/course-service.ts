'use client';

import { request } from './api';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';
import { Course, CoursePaymentType, CourseDifficulty } from '@/types/course';
import { Category } from '@/types/course';
import { Tag } from '@/types/course';

// 搜索参数接口
interface CourseSearchParams {
  keyword?: string;
  categoryId?: number;
  tagIds?: number[];
  difficulty?: CourseDifficulty;
  paymentType?: CoursePaymentType;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: string;
  page?: number;
  pageSize?: number;
}

/**
 * 课程服务
 */
const courseService = {
  /**
   * 搜索课程
   */
  searchCourses: async (params: CourseSearchParams): Promise<PaginationResult<Course>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<Course>>> = 
        await request.post('/courses/search', params);
      return response.data.data;
    } catch (error) {
      console.error('搜索课程失败:', error);
      throw error;
    }
  },

  /**
   * 获取热门课程
   */
  getHotCourses: async (): Promise<Course[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Course[]>> = 
        await request.get('/courses/hot');
      return response.data.data;
    } catch (error) {
      console.error('获取热门课程失败:', error);
      throw error;
    }
  },

  /**
   * 获取最新课程
   */
  getLatestCourses: async (): Promise<Course[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Course[]>> = 
        await request.get('/courses/latest');
      return response.data.data;
    } catch (error) {
      console.error('获取最新课程失败:', error);
      throw error;
    }
  },

  /**
   * 获取所有分类
   */
  getAllCategories: async (): Promise<Category[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Category[]>> = 
        await request.get('/courses/categories');
      return response.data.data;
    } catch (error) {
      console.error('获取课程分类失败:', error);
      throw error;
    }
  },

  /**
   * 获取所有标签
   */
  getAllTags: async (): Promise<Tag[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Tag[]>> = 
        await request.get('/courses/tags');
      return response.data.data;
    } catch (error) {
      console.error('获取课程标签失败:', error);
      throw error;
    }
  }
};

export default courseService; 