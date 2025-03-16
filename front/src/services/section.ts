'use client';

import { request } from './api';
import { 
  Section, 
  SectionCreateDTO, 
  SectionOrderDTO,
  SectionResource,
  SectionResourceDTO,
  SectionQuestionGroup,
  SectionQuestionGroupDTO,
  SectionQuestionGroupConfigDTO
} from '@/types/course';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';
import { isCacheExpired, setCache, MAX_CACHE_AGE } from '@/utils/cache';

// 缓存对象
const cache: {
  byId: Record<number, Section>,
  byChapter: Record<number, Section[]>
} = {
  byId: {},
  byChapter: {}
};

export const sectionService = {
  /**
   * 创建小节
   */
  createSection: async (section: SectionCreateDTO): Promise<Section> => {
    try {
      const response: AxiosResponse<ApiResponse<Section>> = await request.post<Section>('/sections', section);
      const newSection = response.data.data;
      
      // 更新缓存
      cache.byId[newSection.id] = newSection;
      
      // 清除相关章节的小节列表缓存
      delete cache.byChapter[section.chapterId];
      
      return newSection;
    } catch (error) {
      console.error('创建小节失败:', error);
      throw error;
    }
  },

  /**
   * 获取小节详情
   */
  getSectionById: async (id: number): Promise<Section> => {
    try {
      // 检查缓存
      const cacheKey = `section_${id}`;
      if (cache.byId[id] && !isCacheExpired(cacheKey)) {
        return cache.byId[id];
      }
      
      const response: AxiosResponse<ApiResponse<Section>> = await request.get<Section>(`/sections/${id}`);
      const section = response.data.data;
      
      // 更新缓存
      cache.byId[id] = setCache(cacheKey, section);
      
      return section;
    } catch (error) {
      console.error(`获取小节详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 更新小节
   */
  updateSection: async (id: number, section: SectionCreateDTO): Promise<Section> => {
    try {
      const response: AxiosResponse<ApiResponse<Section>> = await request.put<Section>(`/sections/${id}`, section);
      const updatedSection = response.data.data;
      
      // 更新缓存
      cache.byId[id] = updatedSection;
      
      // 清除相关章节的小节列表缓存
      delete cache.byChapter[section.chapterId];
      
      return updatedSection;
    } catch (error) {
      console.error(`更新小节失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除小节
   */
  deleteSection: async (id: number): Promise<void> => {
    try {
      await request.delete(`/sections/${id}`);
      
      // 从缓存中移除
      if (cache.byId[id]) {
        const chapterId = cache.byId[id].chapterId;
        delete cache.byId[id];
        
        // 清除相关章节的小节列表缓存
        if (chapterId) {
          delete cache.byChapter[chapterId];
        }
      }
    } catch (error) {
      console.error(`删除小节失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取章节下的小节列表
   */
  getSectionsByChapter: async (chapterId: number): Promise<Section[]> => {
    try {
      // 检查缓存
      const cacheKey = `chapter_${chapterId}_sections`;
      if (cache.byChapter[chapterId] && !isCacheExpired(cacheKey)) {
        return cache.byChapter[chapterId];
      }
      
      const response: AxiosResponse<ApiResponse<Section[]>> = 
        await request.get<Section[]>(`/sections/chapter/${chapterId}`);
      const sections = response.data.data;
      
      // 更新缓存
      cache.byChapter[chapterId] = setCache(cacheKey, sections);
      
      // 同时更新单个小节缓存
      sections.forEach(section => {
        cache.byId[section.id] = section;
        setCache(`section_${section.id}`, section);
      });
      
      return sections;
    } catch (error) {
      console.error(`获取章节下的小节列表失败, chapterId: ${chapterId}:`, error);
      throw error;
    }
  },

  /**
   * 获取课程下的所有小节
   */
  getSectionsByCourse: async (courseId: number): Promise<Section[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Section[]>> = 
        await request.get<Section[]>(`/sections/course/${courseId}`);
      const sections = response.data.data;
      
      // 更新单个小节缓存
      sections.forEach(section => {
        cache.byId[section.id] = section;
        setCache(`section_${section.id}`, section);
      });
      
      return sections;
    } catch (error) {
      console.error(`获取课程下的所有小节失败, courseId: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 调整小节顺序
   */
  reorderSections: async (chapterId: number, sectionOrders: SectionOrderDTO[]): Promise<Section[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Section[]>> = 
        await request.put<Section[]>(`/sections/chapter/${chapterId}/reorder`, sectionOrders);
      const sections = response.data.data;
      
      // 更新缓存
      cache.byChapter[chapterId] = sections;
      setCache(`chapter_${chapterId}_sections`, sections);
      
      // 更新单个小节缓存
      sections.forEach(section => {
        cache.byId[section.id] = section;
        setCache(`section_${section.id}`, section);
      });
      
      return sections;
    } catch (error) {
      console.error(`调整小节顺序失败, chapterId: ${chapterId}:`, error);
      throw error;
    }
  },
  
  /**
   * 设置小节媒体资源（直接关联）
   */
  setMediaResource: async (sectionId: number, mediaId: number, resourceType: string): Promise<Section> => {
    try {
      const response: AxiosResponse<ApiResponse<Section>> = 
        await request.put<Section>(`/sections/${sectionId}/media/${mediaId}?resourceType=${resourceType}`);
      const updatedSection = response.data.data;
      
      // 更新缓存
      cache.byId[sectionId] = updatedSection;
      
      return updatedSection;
    } catch (error) {
      console.error(`设置小节媒体资源失败, sectionId: ${sectionId}, mediaId: ${mediaId}:`, error);
      throw error;
    }
  },
  
  /**
   * 移除小节媒体资源（直接关联）
   */
  removeMediaResource: async (sectionId: number): Promise<Section> => {
    try {
      const response: AxiosResponse<ApiResponse<Section>> = 
        await request.delete<Section>(`/sections/${sectionId}/media`);
      const updatedSection = response.data.data;
      
      // 更新缓存
      cache.byId[sectionId] = updatedSection;
      
      return updatedSection;
    } catch (error) {
      console.error(`移除小节媒体资源失败, sectionId: ${sectionId}:`, error);
      throw error;
    }
  },
  
  /**
   * 设置小节题目组（直接关联）
   */
  setQuestionGroup: async (
    sectionId: number, 
    questionGroupId: number, 
    config?: SectionQuestionGroupConfigDTO
  ): Promise<Section> => {
    try {
      const response: AxiosResponse<ApiResponse<Section>> = 
        await request.put<Section>(`/sections/${sectionId}/question-group/${questionGroupId}`, config || {});
      const updatedSection = response.data.data;
      
      // 更新缓存
      cache.byId[sectionId] = updatedSection;
      
      return updatedSection;
    } catch (error) {
      console.error(`设置小节题目组失败, sectionId: ${sectionId}, questionGroupId: ${questionGroupId}:`, error);
      throw error;
    }
  },
  
  /**
   * 移除小节题目组（直接关联）
   */
  removeQuestionGroup: async (sectionId: number): Promise<Section> => {
    try {
      const response: AxiosResponse<ApiResponse<Section>> = 
        await request.delete<Section>(`/sections/${sectionId}/question-group`);
      const updatedSection = response.data.data;
      
      // 更新缓存
      cache.byId[sectionId] = updatedSection;
      
      return updatedSection;
    } catch (error) {
      console.error(`移除小节题目组失败, sectionId: ${sectionId}:`, error);
      throw error;
    }
  },

  /**
   * 添加小节资源
   * @deprecated 使用 setMediaResource 替代
   */
  addSectionResource: async (resource: SectionResourceDTO): Promise<SectionResource> => {
    try {
      const response: AxiosResponse<ApiResponse<SectionResource>> = 
        await request.post<SectionResource>('/sections/resources', resource);
      
      // 清除相关小节缓存
      delete cache.byId[resource.sectionId];
      
      return response.data.data;
    } catch (error) {
      console.error(`添加小节资源失败, sectionId: ${resource.sectionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取小节资源列表
   * @deprecated 资源现在直接存储在小节对象中
   */
  getSectionResources: async (sectionId: number): Promise<SectionResource[]> => {
    try {
      const response: AxiosResponse<ApiResponse<SectionResource[]>> = 
        await request.get<SectionResource[]>(`/sections/${sectionId}/resources`);
      return response.data.data;
    } catch (error) {
      console.error(`获取小节资源列表失败, sectionId: ${sectionId}:`, error);
      throw error;
    }
  },

  /**
   * 删除小节资源
   * @deprecated 使用 removeMediaResource 替代
   */
  deleteSectionResource: async (resourceId: number): Promise<void> => {
    try {
      await request.delete(`/sections/resources/${resourceId}`);
    } catch (error) {
      console.error(`删除小节资源失败, resourceId: ${resourceId}:`, error);
      throw error;
    }
  },

  /**
   * 添加小节题目组
   * @deprecated 使用 setQuestionGroup 替代
   */
  addSectionQuestionGroup: async (questionGroup: SectionQuestionGroupDTO): Promise<SectionQuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<SectionQuestionGroup>> = 
        await request.post<SectionQuestionGroup>('/sections/question-groups', questionGroup);
      
      // 清除相关小节缓存
      delete cache.byId[questionGroup.sectionId];
      
      return response.data.data;
    } catch (error) {
      console.error(`添加小节题目组失败, sectionId: ${questionGroup.sectionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取小节题目组列表
   * @deprecated 题目组现在直接存储在小节对象中
   */
  getSectionQuestionGroups: async (sectionId: number): Promise<SectionQuestionGroup[]> => {
    try {
      const response: AxiosResponse<ApiResponse<SectionQuestionGroup[]>> = 
        await request.get<SectionQuestionGroup[]>(`/sections/${sectionId}/question-groups`);
      return response.data.data;
    } catch (error) {
      console.error(`获取小节题目组列表失败, sectionId: ${sectionId}:`, error);
      throw error;
    }
  },

  /**
   * 更新小节题目组
   * @deprecated 使用 setQuestionGroup 替代
   */
  updateSectionQuestionGroup: async (
    sectionId: number, 
    questionGroupId: number, 
    questionGroup: SectionQuestionGroupDTO
  ): Promise<SectionQuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<SectionQuestionGroup>> = 
        await request.put<SectionQuestionGroup>(`/sections/${sectionId}/question-groups/${questionGroupId}`, questionGroup);
      
      // 清除相关小节缓存
      delete cache.byId[sectionId];
      
      return response.data.data;
    } catch (error) {
      console.error(`更新小节题目组失败, sectionId: ${sectionId}, questionGroupId: ${questionGroupId}:`, error);
      throw error;
    }
  },

  /**
   * 删除小节题目组
   * @deprecated 使用 removeQuestionGroup 替代
   */
  deleteSectionQuestionGroup: async (sectionId: number, questionGroupId: number): Promise<void> => {
    try {
      await request.delete(`/sections/${sectionId}/question-groups/${questionGroupId}`);
      
      // 清除相关小节缓存
      delete cache.byId[sectionId];
    } catch (error) {
      console.error(`删除小节题目组失败, sectionId: ${sectionId}, questionGroupId: ${questionGroupId}:`, error);
      throw error;
    }
  }
};

export default sectionService; 