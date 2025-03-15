'use client';

import { request } from './api';
import { 
  Chapter, 
  ChapterCreateDTO, 
  ChapterOrderDTO,
  ChapterAccessType
} from '@/types/course';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';

// 章节缓存
interface ChapterCache {
  byId: Record<number, Chapter>;
  byCourse: Record<number, Chapter[]>;
  timestamp: Record<string, number>;
}

// 缓存过期时间（毫秒）
const CACHE_EXPIRY = 60 * 1000; // 1分钟

// 初始化缓存
const cache: ChapterCache = {
  byId: {},
  byCourse: {},
  timestamp: {}
};

// 检查缓存是否过期
const isCacheExpired = (key: string): boolean => {
  const timestamp = cache.timestamp[key];
  if (!timestamp) return true;
  return Date.now() - timestamp > CACHE_EXPIRY;
};

// 设置缓存
const setCache = (key: string, data: any) => {
  cache.timestamp[key] = Date.now();
  return data;
};

/**
 * 章节管理服务
 */
const chapterService = {
  /**
   * 创建章节
   */
  createChapter: async (chapter: ChapterCreateDTO): Promise<Chapter> => {
    try {
      const response: AxiosResponse<ApiResponse<Chapter>> = await request.post<Chapter>('/chapters', chapter);
      const newChapter = response.data.data;
      
      // 更新缓存
      cache.byId[newChapter.id] = newChapter;
      
      // 清除课程章节列表缓存
      delete cache.byCourse[chapter.courseId];
      
      return newChapter;
    } catch (error) {
      console.error('创建章节失败:', error);
      throw error;
    }
  },

  /**
   * 根据ID获取章节详情
   */
  getChapterById: async (id: number): Promise<Chapter> => {
    try {
      // 检查缓存
      const cacheKey = `chapter_${id}`;
      if (cache.byId[id] && !isCacheExpired(cacheKey)) {
        return cache.byId[id];
      }
      
      const response: AxiosResponse<ApiResponse<Chapter>> = await request.get<Chapter>(`/chapters/${id}`);
      const chapter = response.data.data;
      
      // 更新缓存
      cache.byId[id] = setCache(cacheKey, chapter);
      
      return chapter;
    } catch (error) {
      console.error(`获取章节详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 更新章节
   */
  updateChapter: async (id: number, chapter: ChapterCreateDTO): Promise<Chapter> => {
    try {
      const response: AxiosResponse<ApiResponse<Chapter>> = await request.put<Chapter>(`/chapters/${id}`, chapter);
      const updatedChapter = response.data.data;
      
      // 更新缓存
      cache.byId[id] = updatedChapter;
      
      // 清除课程章节列表缓存
      delete cache.byCourse[chapter.courseId];
      
      return updatedChapter;
    } catch (error) {
      console.error(`更新章节失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除章节
   */
  deleteChapter: async (id: number): Promise<void> => {
    try {
      // 先获取章节信息，以便清除相关缓存
      let courseId;
      if (cache.byId[id]) {
        courseId = cache.byId[id].courseId;
      }
      
      await request.delete(`/chapters/${id}`);
      
      // 清除缓存
      delete cache.byId[id];
      
      // 清除课程章节列表缓存
      if (courseId) {
        delete cache.byCourse[courseId];
      }
    } catch (error) {
      console.error(`删除章节失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取课程下的章节列表
   */
  getChaptersByCourse: async (courseId: number): Promise<Chapter[]> => {
    try {
      // 检查缓存
      const cacheKey = `course_${courseId}_chapters`;
      if (cache.byCourse[courseId] && !isCacheExpired(cacheKey)) {
        return cache.byCourse[courseId];
      }
      
      const response: AxiosResponse<ApiResponse<Chapter[]>> = 
        await request.get<Chapter[]>(`/chapters/course/${courseId}`);
      const chapters = response.data.data;
      
      // 更新缓存
      cache.byCourse[courseId] = setCache(cacheKey, chapters);
      
      // 同时更新单个章节缓存
      chapters.forEach(chapter => {
        cache.byId[chapter.id] = chapter;
        setCache(`chapter_${chapter.id}`, chapter);
      });
      
      return chapters;
    } catch (error) {
      console.error(`获取课程章节列表失败, courseId: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 更新章节访问类型
   */
  updateAccessType: async (id: number, accessType: ChapterAccessType): Promise<Chapter> => {
    try {
      const response: AxiosResponse<ApiResponse<Chapter>> = 
        await request.put<Chapter>(`/chapters/${id}/access-type?accessType=${accessType}`);
      return response.data.data;
    } catch (error) {
      console.error(`更新章节访问类型失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 调整章节顺序
   */
  reorderChapters: async (courseId: number, chapterOrders: ChapterOrderDTO[]): Promise<Chapter[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Chapter[]>> = 
        await request.put<Chapter[]>(`/chapters/course/${courseId}/reorder`, chapterOrders);
      const chapters = response.data.data;
      
      // 更新缓存
      cache.byCourse[courseId] = chapters;
      setCache(`course_${courseId}_chapters`, chapters);
      
      // 更新单个章节缓存
      chapters.forEach(chapter => {
        cache.byId[chapter.id] = chapter;
        setCache(`chapter_${chapter.id}`, chapter);
      });
      
      return chapters;
    } catch (error) {
      console.error(`调整章节顺序失败, courseId: ${courseId}:`, error);
      throw error;
    }
  },
  
  /**
   * 清除缓存
   */
  clearCache: () => {
    cache.byId = {};
    cache.byCourse = {};
    cache.timestamp = {};
  }
};

export default chapterService; 