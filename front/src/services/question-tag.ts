'use client';

import { request } from './api';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';
import { QuestionTag, QuestionTagDTO } from '@/types/question';

/**
 * 问题标签管理服务
 */
const questionTagService = {
  /**
   * 获取问题标签列表
   */
  getQuestionTagList: async (
    params: { page?: number; pageSize?: number; name?: string; institutionId?: number; }
  ): Promise<PaginationResult<QuestionTag>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<QuestionTag>>> = 
        await request.get('/questions/tags', { params });
      return response.data.data;
    } catch (error) {
      console.error('获取问题标签列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取所有问题标签（不分页）
   */
  getAllQuestionTags: async (institutionId: number): Promise<QuestionTag[]> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionTag[]>> = 
        await request.get('/questions/tags/all', { params: { institutionId } });
      return response.data.data;
    } catch (error) {
      console.error('获取所有问题标签失败:', error);
      throw error;
    }
  },

  /**
   * 获取问题标签详情
   */
  getQuestionTagById: async (id: number): Promise<QuestionTag> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionTag>> = 
        await request.get(`/questions/tags/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取问题标签详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建问题标签
   */
  createQuestionTag: async (questionTag: QuestionTagDTO): Promise<QuestionTag> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionTag>> = 
        await request.post('/questions/tags', questionTag);
      return response.data.data;
    } catch (error) {
      console.error('创建问题标签失败:', error);
      throw error;
    }
  },

  /**
   * 更新问题标签
   */
  updateQuestionTag: async (id: number, questionTag: QuestionTagDTO): Promise<QuestionTag> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionTag>> = 
        await request.put(`/questions/tags/${id}`, questionTag);
      return response.data.data;
    } catch (error) {
      console.error(`更新问题标签失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除问题标签
   */
  deleteQuestionTag: async (id: number): Promise<void> => {
    try {
      await request.delete(`/questions/tags/${id}`);
    } catch (error) {
      console.error(`删除问题标签失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 批量删除问题标签
   */
  batchDeleteQuestionTags: async (ids: number[]): Promise<void> => {
    try {
      await request.delete('/questions/tags/batch', { data: ids });
    } catch (error) {
      console.error('批量删除问题标签失败:', error);
      throw error;
    }
  }
};

export default questionTagService; 