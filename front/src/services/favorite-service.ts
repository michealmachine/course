import { request } from '@/services/api';
import { Page, ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';

interface UserFavoriteVO {
  id: number;
  courseId: number;
  courseTitle: string;
  courseCoverImage: string;
  coursePrice: string;
  categoryName: string;
  institutionName: string;
  favoriteTime: string;
}

// 收藏服务
const favoriteService = {
  // 获取收藏列表
  getFavorites: async (page: number, size: number): Promise<Page<UserFavoriteVO>> => {
    try {
      const response: AxiosResponse<ApiResponse<Page<UserFavoriteVO>>> = 
        await request.get(`/favorites?page=${page}&size=${size}`);
      return response.data.data;
    } catch (error) {
      console.error('获取收藏列表失败:', error);
      throw error;
    }
  },

  // 添加收藏
  addFavorite: async (courseId: number): Promise<void> => {
    try {
      await request.post(`/favorites/${courseId}`);
    } catch (error) {
      console.error('添加收藏失败:', error);
      throw error;
    }
  },

  // 取消收藏
  removeFavorite: async (courseId: number): Promise<void> => {
    try {
      await request.delete(`/favorites/${courseId}`);
    } catch (error) {
      console.error('取消收藏失败:', error);
      throw error;
    }
  },

  // 检查是否已收藏
  checkFavorite: async (courseId: number): Promise<boolean> => {
    try {
      const response: AxiosResponse<ApiResponse<boolean>> = 
        await request.get(`/favorites/check/${courseId}`);
      return response.data.data;
    } catch (error) {
      console.error('检查收藏状态失败:', error);
      throw error;
    }
  },

  // 获取收藏数量
  getFavoriteCount: async (): Promise<number> => {
    try {
      const response: AxiosResponse<ApiResponse<number>> = 
        await request.get('/favorites/count');
      return response.data.data;
    } catch (error) {
      console.error('获取收藏数量失败:', error);
      throw error;
    }
  }
};

export default favoriteService;
export type { UserFavoriteVO }; 