'use client';

import { request } from './api';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';
import { Question, QuestionGroup, QuestionGroupDTO, QuestionGroupItemVO } from '@/types/question';

interface GetGroupsParams {
  institutionId?: number;
  keyword?: string;
  page?: number;
  size?: number;
  pageSize?: number;
}

interface QuestionGroupItemDTO {
  groupId: number;
  questionId: number;
  orderIndex?: number;
}

/**
 * 问题组管理服务
 */
const questionGroupService = {
  /**
   * 获取问题组列表
   */
  getQuestionGroupList: async (
    params: { page?: number; pageSize?: number; name?: string; institutionId?: number; }
  ): Promise<PaginationResult<QuestionGroup>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<QuestionGroup>>> = 
        await request.get('/questions/groups', { params });
      return response.data.data;
    } catch (error) {
      console.error('获取问题组列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取问题组详情
   */
  getQuestionGroupById: async (id: number): Promise<QuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionGroup>> = 
        await request.get(`/questions/groups/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取问题组详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建问题组
   */
  createQuestionGroup: async (questionGroup: QuestionGroupDTO): Promise<QuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionGroup>> = 
        await request.post('/questions/groups', questionGroup);
      return response.data.data;
    } catch (error) {
      console.error('创建问题组失败:', error);
      throw error;
    }
  },

  /**
   * 更新问题组
   */
  updateQuestionGroup: async (id: number, questionGroup: QuestionGroupDTO): Promise<QuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionGroup>> = 
        await request.put(`/questions/groups/${id}`, questionGroup);
      return response.data.data;
    } catch (error) {
      console.error(`更新问题组失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除问题组
   */
  deleteQuestionGroup: async (id: number): Promise<void> => {
    try {
      await request.delete(`/questions/groups/${id}`);
    } catch (error) {
      console.error(`删除问题组失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取问题组内的问题列表
   */
  getQuestionsInGroup: async (
    groupId: number,
    params?: { page?: number; pageSize?: number; }
  ): Promise<PaginationResult<Question>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<Question>>> = 
        await request.get(`/questions/groups/${groupId}/questions`, { params });
      return response.data.data;
    } catch (error) {
      console.error(`获取问题组内问题列表失败, 组ID: ${groupId}:`, error);
      throw error;
    }
  },

  /**
   * 添加问题到问题组
   */
  addQuestionsToGroup: async (groupId: number, questionIds: number[]): Promise<void> => {
    try {
      await request.post(`/questions/groups/${groupId}/questions`, questionIds);
    } catch (error) {
      console.error(`添加问题到问题组失败, 组ID: ${groupId}:`, error);
      throw error;
    }
  },

  /**
   * 从问题组中移除问题
   */
  removeQuestionFromGroup: async (groupId: number, questionId: number): Promise<void> => {
    try {
      await request.delete(`/questions/groups/${groupId}/questions/${questionId}`);
    } catch (error) {
      console.error(`从问题组中移除问题失败, 组ID: ${groupId}, 问题ID: ${questionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取题组列表
   */
  getGroups: async (params: GetGroupsParams): Promise<PaginationResult<QuestionGroup>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<QuestionGroup>>> = 
        await request.get('/questions/groups', { params });
      return response.data.data;
    } catch (error) {
      console.error('获取题组列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取题组详情
   */
  getGroupById: async (id: number): Promise<QuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionGroup>> = 
        await request.get(`/questions/groups/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取题组详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建题组
   */
  createGroup: async (groupDTO: QuestionGroupDTO): Promise<QuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionGroup>> = 
        await request.post('/questions/groups', groupDTO);
      return response.data.data;
    } catch (error) {
      console.error('创建题组失败:', error);
      throw error;
    }
  },

  /**
   * 更新题组
   */
  updateGroup: async (id: number, groupDTO: QuestionGroupDTO): Promise<QuestionGroup> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionGroup>> = 
        await request.put(`/questions/groups/${id}`, groupDTO);
      return response.data.data;
    } catch (error) {
      console.error(`更新题组失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除题组
   */
  deleteGroup: async (id: number): Promise<void> => {
    try {
      await request.delete(`/questions/groups/${id}`);
    } catch (error) {
      console.error(`删除题组失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取题组中的题目
   */
  getGroupItems: async (groupId: number): Promise<QuestionGroupItemVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<QuestionGroupItemVO[]>> = 
        await request.get(`/questions/groups/${groupId}/items`);
      return response.data.data;
    } catch (error) {
      console.error(`获取题组题目失败, ID: ${groupId}:`, error);
      throw error;
    }
  },

  /**
   * 添加题目到题组
   */
  addQuestionToGroup: async (groupId: number, questionId: number): Promise<QuestionGroupItemVO> => {
    try {
      const itemDTO: QuestionGroupItemDTO = {
        groupId,
        questionId,
        orderIndex: 0 // 默认添加到最后
      };
      const response: AxiosResponse<ApiResponse<QuestionGroupItemVO>> = 
        await request.post('/questions/groups/items', itemDTO);
      return response.data.data;
    } catch (error) {
      console.error('添加题目到题组失败:', error);
      throw error;
    }
  },

  /**
   * 从题组中移除题目
   */
  removeItemFromGroup: async (groupId: number, itemId: number): Promise<void> => {
    try {
      await request.delete(`/questions/groups/${groupId}/items/${itemId}`);
    } catch (error) {
      console.error('从题组移除题目失败:', error);
      throw error;
    }
  },

  /**
   * 更新题目顺序
   */
  updateItemsOrder: async (groupId: number, itemIds: number[]): Promise<boolean> => {
    try {
      const response: AxiosResponse<ApiResponse<boolean>> = 
        await request.put(`/questions/groups/${groupId}/items/order`, 
          itemIds.map((id, index) => ({
            id,
            orderIndex: index
          }))
        );
      return response.data.data;
    } catch (error) {
      console.error('更新题目顺序失败:', error);
      throw error;
    }
  }
};

export default questionGroupService; 