'use client';

import { request } from './api';
import { Tag, TagDTO } from '@/types/course';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';
import { useCacheStore } from '@/stores/cache-store';

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
      
      const response = await request.get<PaginationResult<Tag>>('/tags', { params });
      const result = (response.data as unknown as ApiResponse<PaginationResult<Tag>>).data;
      return result || {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 0,
        number: 0,
        first: true,
        last: true,
        empty: true,
        numberOfElements: 0
      };
    } catch (error) {
      console.error('获取标签列表失败:', error);
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 0,
        number: 0,
        first: true,
        last: true,
        empty: true,
        numberOfElements: 0
      };
    }
  },

  /**
   * 根据ID获取标签详情
   */
  getTagById: async (id: number): Promise<Tag | null> => {
    // 先从缓存中查找
    const cachedTag = useCacheStore.getState().getTagById(id);
    if (cachedTag) {
      return cachedTag;
    }

    try {
      const response = await request.get<Tag>(`/tags/${id}`);
      const tag = (response.data as unknown as ApiResponse<Tag>).data;
      return tag || null;
    } catch (error) {
      console.error(`获取标签详情失败, ID: ${id}:`, error);
      return null;
    }
  },

  /**
   * 根据名称获取标签
   */
  getTagByName: async (name: string): Promise<Tag | null> => {
    try {
      const response = await request.get<Tag>(`/tags/name/${encodeURIComponent(name)}`);
      const tag = (response.data as unknown as ApiResponse<Tag>).data;
      return tag || null;
    } catch (error) {
      console.error(`根据名称获取标签失败, name: ${name}:`, error);
      return null;
    }
  },

  /**
   * 创建标签
   */
  createTag: async (tag: TagDTO): Promise<number> => {
    try {
      const response = await request.post<{id: number}>('/tags', tag);
      const result = (response.data as unknown as ApiResponse<{id: number}>).data;
      // 创建后清除缓存
      useCacheStore.getState().clearTagsCache();
      return result.id;
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
      useCacheStore.getState().clearTagsCache();
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
      useCacheStore.getState().clearTagsCache();
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
      const response = await request.get<Tag[]>(`/tags/popular?limit=${limit}`);
      const tags = (response.data as unknown as ApiResponse<Tag[]>).data;
      return tags || [];
    } catch (error) {
      console.error(`获取热门标签失败:`, error);
      return [];
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
      const response = await request.post<number[]>('/tags/batch', tagNames);
      const ids = (response.data as unknown as ApiResponse<number[]>).data;
      // 批量操作后清除缓存
      useCacheStore.getState().clearTagsCache();
      return ids || [];
    } catch (error) {
      console.error('批量获取或创建标签失败:', error);
      return [];
    }
  },

  /**
   * 获取所有标签
   * @returns 标签列表
   */
  async getAllTags(): Promise<Tag[]> {
    const cacheStore = useCacheStore.getState();
    
    // 如果缓存有效，使用缓存
    if (cacheStore.isTagsCacheValid()) {
      return cacheStore.tags || [];
    }

    try {
      const response = await request.get<Tag[]>('/tags');
      const tags = (response.data as unknown as ApiResponse<Tag[]>).data || [];
      
      // 更新缓存
      cacheStore.setTags(tags);
      
      return tags;
    } catch (error) {
      console.error('获取标签失败:', error);
      // 如果请求失败但缓存存在，返回缓存数据
      if (cacheStore.tags) {
        console.log('请求失败，使用过期的标签缓存');
        return cacheStore.tags;
      }
      throw error;
    }
  },

  /**
   * 手动清除缓存
   */
  clearCache(): void {
    useCacheStore.getState().clearTagsCache();
  }
};

export default tagService; 