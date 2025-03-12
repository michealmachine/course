'use client';

import { request } from './api';
import { Tag, TagDTO } from '@/types/course';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';

/**
 * 标签管理服务
 */
const tagService = {
  /**
   * 获取标签列表（分页）
   */
  getTagList: async (keyword?: string, page = 0, size = 20): Promise<PaginationResult<Tag>> => {
    try {
      const params: any = { page, size };
      if (keyword) params.keyword = keyword;
      
      const response: AxiosResponse<ApiResponse<PaginationResult<Tag>>> = 
        await request.get<PaginationResult<Tag>>('/tags', { params });
      return response.data.data;
    } catch (error) {
      console.error('获取标签列表失败:', error);
      throw error;
    }
  },

  /**
   * 根据ID获取标签详情
   */
  getTagById: async (id: number): Promise<Tag> => {
    try {
      const response: AxiosResponse<ApiResponse<Tag>> = await request.get<Tag>(`/tags/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取标签详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 根据名称获取标签
   */
  getTagByName: async (name: string): Promise<Tag> => {
    try {
      const response: AxiosResponse<ApiResponse<Tag>> = await request.get<Tag>(`/tags/name/${encodeURIComponent(name)}`);
      return response.data.data;
    } catch (error) {
      console.error(`根据名称获取标签失败, name: ${name}:`, error);
      throw error;
    }
  },

  /**
   * 创建标签
   */
  createTag: async (tag: TagDTO): Promise<number> => {
    try {
      const response: AxiosResponse<ApiResponse<{id: number}>> = await request.post<{id: number}>('/tags', tag);
      return response.data.data.id;
    } catch (error) {
      console.error('创建标签失败:', error);
      throw error;
    }
  },

  /**
   * 更新标签
   */
  updateTag: async (id: number, tag: TagDTO): Promise<void> => {
    try {
      await request.put(`/tags/${id}`, tag);
    } catch (error) {
      console.error(`更新标签失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除标签
   */
  deleteTag: async (id: number): Promise<void> => {
    try {
      await request.delete(`/tags/${id}`);
    } catch (error) {
      console.error(`删除标签失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取热门标签
   */
  getPopularTags: async (limit = 10): Promise<Tag[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Tag[]>> = 
        await request.get<Tag[]>(`/tags/popular?limit=${limit}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取热门标签失败:`, error);
      throw error;
    }
  },

  /**
   * 检查标签名称是否可用
   */
  isNameAvailable: async (name: string, excludeId?: number): Promise<boolean> => {
    try {
      const params: any = { name };
      if (excludeId) params.excludeId = excludeId;
      
      const response: AxiosResponse<ApiResponse<{available: boolean}>> = 
        await request.get<{available: boolean}>('/tags/check-name', { params });
      return response.data.data.available;
    } catch (error) {
      console.error(`检查标签名称是否可用失败, name: ${name}:`, error);
      throw error;
    }
  },

  /**
   * 批量获取或创建标签
   */
  batchGetOrCreateTags: async (tagNames: string[]): Promise<number[]> => {
    try {
      const response: AxiosResponse<ApiResponse<number[]>> = await request.post<number[]>('/tags/batch', tagNames);
      return response.data.data;
    } catch (error) {
      console.error('批量获取或创建标签失败:', error);
      throw error;
    }
  }
};

export default tagService; 