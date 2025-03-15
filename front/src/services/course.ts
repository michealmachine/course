'use client';

import { request } from './api';
import { 
  Course, CourseCreateDTO, 
  PreviewUrlVO, 
  CourseStatus,
  CoursePaymentType
} from '@/types/course';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';

/**
 * 课程管理服务
 */
const courseService = {
  /**
   * 获取课程列表（分页）
   */
  getCourseList: async (page = 0, size = 10, keyword?: string, status?: CourseStatus): Promise<PaginationResult<Course>> => {
    try {
      const params: any = { page, size };
      if (keyword) params.keyword = keyword;
      if (status !== undefined) params.status = status;
      
      const response: AxiosResponse<ApiResponse<PaginationResult<Course>>> = 
        await request.get<PaginationResult<Course>>('/courses', { params });
      
      // 确保响应数据符合预期格式
      if (response?.data?.data) {
        return response.data.data;
      }
      
      // 如果响应格式不符，返回一个空的分页结果
      console.warn('API响应格式不符合预期:', response);
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
        },
        sort: {
          empty: true,
          sorted: false,
          unsorted: true
        }
      };
    } catch (error) {
      console.error('获取课程列表失败:', error);
      // 出错时返回空结果而不是抛出异常，让UI层处理
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
        },
        sort: {
          empty: true,
          sorted: false,
          unsorted: true
        }
      };
    }
  },

  /**
   * 根据ID获取课程详情
   */
  getCourseById: async (id: number): Promise<Course> => {
    try {
      const response: AxiosResponse<ApiResponse<Course>> = await request.get<Course>(`/courses/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建课程
   */
  createCourse: async (course: CourseCreateDTO): Promise<Course> => {
    try {
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>('/courses', course);
      return response.data.data;
    } catch (error) {
      console.error('创建课程失败:', error);
      throw error;
    }
  },

  /**
   * 更新课程
   */
  updateCourse: async (id: number, course: CourseCreateDTO): Promise<Course> => {
    try {
      const response: AxiosResponse<ApiResponse<Course>> = await request.put<Course>(`/courses/${id}`, course);
      return response.data.data;
    } catch (error) {
      console.error(`更新课程失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除课程
   */
  deleteCourse: async (id: number): Promise<void> => {
    try {
      await request.delete(`/courses/${id}`);
    } catch (error) {
      console.error(`删除课程失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 更新课程封面
   */
  updateCourseCover: async (id: number, file: File): Promise<Course> => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>(
        `/courses/${id}/cover`, 
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );
      return response.data.data;
    } catch (error) {
      console.error(`更新课程封面失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 更新课程支付设置
   */
  updatePaymentSettings: async (
    id: number, 
    paymentType: CoursePaymentType, 
    price?: number, 
    discountPrice?: number
  ): Promise<Course> => {
    try {
      const params: any = { paymentType };
      if (price !== undefined) params.price = price;
      if (discountPrice !== undefined) params.discountPrice = discountPrice;
      
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>(
        `/courses/${id}/payment`, 
        null, 
        { params }
      );
      return response.data.data;
    } catch (error) {
      console.error(`更新课程支付设置失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 提交课程审核
   */
  submitForReview: async (id: number): Promise<Course> => {
    try {
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>(`/courses/${id}/submit`);
      return response.data.data;
    } catch (error) {
      console.error(`提交课程审核失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 生成课程预览URL
   */
  generatePreviewUrl: async (id: number): Promise<PreviewUrlVO> => {
    try {
      const response: AxiosResponse<ApiResponse<PreviewUrlVO>> = await request.get<PreviewUrlVO>(`/courses/${id}/preview`);
      return response.data.data;
    } catch (error) {
      console.error(`生成课程预览URL失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 访问课程预览
   */
  previewCourse: async (token: string): Promise<Course> => {
    try {
      const response: AxiosResponse<ApiResponse<Course>> = await request.get<Course>(`/courses/preview/${token}`);
      return response.data.data;
    } catch (error) {
      console.error(`访问课程预览失败, token: ${token}:`, error);
      throw error;
    }
  },

  /**
   * 开始审核课程 (仅限管理员)
   */
  startReview: async (id: number): Promise<Course> => {
    try {
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>(`/courses/${id}/review/start`);
      return response.data.data;
    } catch (error) {
      console.error(`开始审核课程失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 通过课程审核 (仅限管理员)
   */
  approveCourse: async (id: number, comment?: string): Promise<Course> => {
    try {
      const params: any = {};
      if (comment) params.comment = comment;
      
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>(
        `/courses/${id}/review/approve`, 
        null, 
        { params }
      );
      return response.data.data;
    } catch (error) {
      console.error(`通过课程审核失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 拒绝课程审核 (仅限管理员)
   */
  rejectCourse: async (id: number, reason: string): Promise<Course> => {
    try {
      const params: any = { reason };
      
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>(
        `/courses/${id}/review/reject`, 
        null, 
        { params }
      );
      return response.data.data;
    } catch (error) {
      console.error(`拒绝课程审核失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 重新编辑被拒绝的课程
   */
  reEditRejectedCourse: async (id: number): Promise<Course> => {
    try {
      const response: AxiosResponse<ApiResponse<Course>> = await request.post<Course>(`/courses/${id}/re-edit`);
      return response.data.data;
    } catch (error) {
      console.error(`重新编辑被拒绝的课程失败, ID: ${id}:`, error);
      throw error;
    }
  }
};

export default courseService; 