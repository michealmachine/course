'use client';

import { request } from './api';
import { Category, CategoryDTO, CategoryTree } from '@/types/course';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';
import { useCacheStore } from '@/stores/cache-store';

/**
 * 分类管理服务
 */
const categoryService = {
  /**
   * 获取分类列表（分页）
   */
  getCategoryList: async (keyword?: string, page = 0, size = 10): Promise<PaginationResult<Category>> => {
    try {
      const params: any = { page, size };
      if (keyword) params.keyword = keyword;
      
      const response: AxiosResponse<ApiResponse<PaginationResult<Category>>> = 
        await request.get<PaginationResult<Category>>('/categories', { params });
      return response.data.data;
    } catch (error) {
      console.error('获取分类列表失败:', error);
      throw error;
    }
  },

  /**
   * 根据ID获取分类详情
   */
  getCategoryById: async (id: number): Promise<Category | null> => {
    // 先从缓存中查找
    const cachedCategory = useCacheStore.getState().getCategoryById(id);
    if (cachedCategory) {
      return cachedCategory;
    }

    try {
      const response: AxiosResponse<ApiResponse<Category>> = 
        await request.get<Category>(`/categories/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取分类详情失败, ID: ${id}:`, error);
      return null;
    }
  },

  /**
   * 根据编码获取分类
   */
  getCategoryByCode: async (code: string): Promise<Category> => {
    try {
      const response: AxiosResponse<ApiResponse<Category>> = await request.get<Category>(`/categories/code/${code}`);
      return response.data.data;
    } catch (error) {
      console.error(`根据编码获取分类失败, code: ${code}:`, error);
      throw error;
    }
  },

  /**
   * 创建分类
   */
  createCategory: async (category: CategoryDTO): Promise<number> => {
    try {
      const response: AxiosResponse<ApiResponse<{id: number}>> = await request.post<{id: number}>('/categories', category);
      // 创建后清除缓存
      useCacheStore.getState().clearCategoriesCache();
      return response.data.data.id;
    } catch (error) {
      console.error('创建分类失败:', error);
      throw error;
    }
  },

  /**
   * 更新分类
   */
  updateCategory: async (id: number, category: CategoryDTO): Promise<void> => {
    try {
      await request.put(`/categories/${id}`, category);
      // 更新后清除缓存
      useCacheStore.getState().clearCategoriesCache();
    } catch (error) {
      console.error(`更新分类失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除分类
   */
  deleteCategory: async (id: number): Promise<void> => {
    try {
      await request.delete(`/categories/${id}`);
      // 删除后清除缓存
      useCacheStore.getState().clearCategoriesCache();
    } catch (error) {
      console.error(`删除分类失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取所有根分类
   */
  getRootCategories: async (): Promise<Category[]> => {
    try {
      const response = await request.get<Category[]>('/categories/roots');
      const categories = (response.data as unknown as ApiResponse<Category[]>).data || [];
      return categories;
    } catch (error) {
      console.error('获取根分类失败:', error);
      return [];
    }
  },

  /**
   * 获取子分类
   */
  getChildCategories: async (parentId: number): Promise<Category[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Category[]>> = 
        await request.get<Category[]>(`/categories/children/${parentId}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取子分类失败, parentId: ${parentId}:`, error);
      throw error;
    }
  },

  /**
   * 获取分类树
   */
  getCategoryTree: async (): Promise<CategoryTree[]> => {
    try {
      const response: AxiosResponse<ApiResponse<CategoryTree[]>> = 
        await request.get<CategoryTree[]>('/categories/tree');
      return response.data.data || [];
    } catch (error) {
      console.error('获取分类树失败:', error);
      return [];
    }
  },

  /**
   * 检查分类编码是否可用
   */
  isCodeAvailable: async (code: string, excludeId?: number): Promise<boolean> => {
    try {
      const params: any = { code };
      if (excludeId) params.excludeId = excludeId;
      
      const response: AxiosResponse<ApiResponse<{available: boolean}>> = 
        await request.get<{available: boolean}>('/categories/check-code', { params });
      return response.data.data.available;
    } catch (error) {
      console.error(`检查分类编码是否可用失败, code: ${code}:`, error);
      throw error;
    }
  },

  /**
   * 更新分类状态
   */
  updateCategoryStatus: async (id: number, enabled: boolean): Promise<void> => {
    try {
      await request.put(`/categories/${id}/status?enabled=${enabled}`);
      // 更新后清除缓存
      useCacheStore.getState().clearCategoriesCache();
    } catch (error) {
      console.error(`更新分类状态失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 更新分类排序
   */
  updateCategoryOrder: async (id: number, orderIndex: number): Promise<void> => {
    try {
      await request.put(`/categories/${id}/order?orderIndex=${orderIndex}`);
      // 更新后清除缓存
      useCacheStore.getState().clearCategoriesCache();
    } catch (error) {
      console.error(`更新分类排序失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取所有分类列表
   */
  getAllCategories: async (): Promise<Category[]> => {
    const cacheStore = useCacheStore.getState();
    
    // 如果缓存有效，使用缓存
    if (cacheStore.isCategoriesCacheValid()) {
      return cacheStore.categories || [];
    }

    try {
      const response = await request.get<Category[]>('/categories');
      const categories = (response.data as unknown as ApiResponse<Category[]>).data || [];
      
      // 更新缓存
      cacheStore.setCategories(categories);
      
      return categories;
    } catch (error) {
      console.error('获取分类列表失败:', error);
      // 如果请求失败但缓存存在，返回缓存数据
      if (cacheStore.categories) {
        console.log('请求失败，使用过期的分类缓存');
        return cacheStore.categories;
      }
      return [];
    }
  },

  /**
   * 手动清除缓存
   */
  clearCache(): void {
    useCacheStore.getState().clearCategoriesCache();
  }
};

export default categoryService; 