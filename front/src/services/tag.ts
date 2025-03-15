'use client';

import { request } from './api';
import { Tag, TagDTO } from '@/types/course';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';

// 简单的内存缓存，避免频繁请求
let tagsCache: Tag[] | null = null;
let lastFetchTime = 0;
const CACHE_TTL = 5 * 60 * 1000; // 5分钟缓存

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
      // 创建后清除缓存
      tagsCache = null;
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
      // 更新后清除缓存
      tagsCache = null;
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
      // 删除后清除缓存
      tagsCache = null;
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
  },

  /**
   * 获取所有标签
   * @param useCache 是否使用缓存（默认使用）
   * @returns 标签列表
   */
  async getAllTags(useCache: boolean = true): Promise<Tag[]> {
    // 如果启用缓存且缓存有效，则使用缓存
    const now = Date.now();
    if (useCache && tagsCache && (now - lastFetchTime < CACHE_TTL)) {
      console.log('使用标签缓存数据');
      return tagsCache;
    }

    try {
      const response = await request.get<{ data: Tag[] }>('/tags');
      const tags = response.data.data || [];
      
      // 更新缓存
      tagsCache = tags;
      lastFetchTime = now;
      
      return tags;
    } catch (error) {
      console.error('获取标签失败:', error);
      // 如果请求失败但缓存存在，返回缓存数据
      if (tagsCache) {
        console.log('请求失败，使用过期的标签缓存');
        return tagsCache;
      }
      throw error;
    }
  },

  /**
   * 创建新标签
   * @param tag 要创建的标签数据
   * @returns 创建的标签
   */
  async createTag(tag: Omit<Tag, 'id'>): Promise<Tag> {
    const response = await request.post<{ data: Tag }>('/tags', tag);
    // 创建后清除缓存
    tagsCache = null;
    return response.data.data;
  },

  /**
   * 更新标签
   * @param id 标签ID
   * @param tag 要更新的标签数据
   * @returns 更新后的标签
   */
  async updateTag(id: number, tag: Partial<Tag>): Promise<Tag> {
    const response = await request.put<{ data: Tag }>(`/tags/${id}`, tag);
    // 更新后清除缓存
    tagsCache = null;
    return response.data.data;
  },

  /**
   * 手动清除缓存
   */
  clearCache(): void {
    tagsCache = null;
    lastFetchTime = 0;
  }
};

export default tagService; 