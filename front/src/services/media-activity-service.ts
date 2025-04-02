'use client';

import { http } from '@/lib/http';
import type { Result, Page } from '@/types/api';
import type { MediaVO } from '@/types/media';
import type { MediaActivityCalendarVO } from '@/types/media-activity';

/**
 * 媒体活动服务接口
 */
export interface MediaActivityService {
  /**
   * 获取媒体活动日历数据
   * @param startDate 开始日期 (YYYY-MM-DD)
   * @param endDate 结束日期 (YYYY-MM-DD)
   * @returns 媒体活动日历数据
   */
  getMediaActivityCalendar(startDate: string, endDate: string): Promise<MediaActivityCalendarVO>;

  /**
   * 根据日期获取媒体列表
   * @param date 日期 (YYYY-MM-DD)
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @returns 媒体列表分页
   */
  getMediaListByDate(date: string, pageNum?: number, pageSize?: number): Promise<Page<MediaVO>>;
}

/**
 * 媒体活动服务实现
 */
class MediaActivityServiceImpl implements MediaActivityService {
  /**
   * 获取媒体活动日历数据
   * @param startDate 开始日期 (YYYY-MM-DD)
   * @param endDate 结束日期 (YYYY-MM-DD)
   * @returns 媒体活动日历数据
   */
  async getMediaActivityCalendar(startDate: string, endDate: string): Promise<MediaActivityCalendarVO> {
    try {
      const response = await http.get<Result<MediaActivityCalendarVO>>(
        `/api/media/activities/calendar?startDate=${startDate}&endDate=${endDate}`
      );
      return response.data;
    } catch (error) {
      console.error('获取媒体活动日历数据失败:', error);
      throw error;
    }
  }

  /**
   * 根据日期获取媒体列表
   * @param date 日期 (YYYY-MM-DD)
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @returns 媒体列表分页
   */
  async getMediaListByDate(date: string, pageNum = 1, pageSize = 10): Promise<Page<MediaVO>> {
    try {
      const response = await http.get<Result<Page<MediaVO>>>(
        `/api/media/by-date?date=${date}&pageNum=${pageNum}&pageSize=${pageSize}`
      );
      return response.data;
    } catch (error) {
      console.error('根据日期获取媒体列表失败:', error);
      throw error;
    }
  }
}

/**
 * 管理员媒体活动服务接口
 */
export interface AdminMediaActivityService {
  /**
   * 获取所有机构的媒体活动日历数据
   * @param startDate 开始日期 (YYYY-MM-DD)
   * @param endDate 结束日期 (YYYY-MM-DD)
   * @returns 媒体活动日历数据
   */
  getAllMediaActivityCalendar(startDate: string, endDate: string): Promise<MediaActivityCalendarVO>;

  /**
   * 根据日期获取所有机构的媒体列表
   * @param date 日期 (YYYY-MM-DD)
   * @param institutionId 机构ID（可选）
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @returns 媒体列表分页
   */
  getAllMediaListByDate(date: string, institutionId?: number, pageNum?: number, pageSize?: number): Promise<Page<MediaVO>>;

  /**
   * 获取所有机构的媒体列表
   * @param institutionId 机构ID（可选）
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @returns 媒体列表分页
   */
  getAllMediaList(institutionId?: number, pageNum?: number, pageSize?: number): Promise<Page<MediaVO>>;
}

/**
 * 管理员媒体活动服务实现
 */
class AdminMediaActivityServiceImpl implements AdminMediaActivityService {
  /**
   * 获取所有机构的媒体活动日历数据
   * @param startDate 开始日期 (YYYY-MM-DD)
   * @param endDate 结束日期 (YYYY-MM-DD)
   * @returns 媒体活动日历数据
   */
  async getAllMediaActivityCalendar(startDate: string, endDate: string): Promise<MediaActivityCalendarVO> {
    try {
      const response = await http.get<Result<MediaActivityCalendarVO>>(
        `/api/admin/media/activities/calendar?startDate=${startDate}&endDate=${endDate}`
      );
      return response.data;
    } catch (error) {
      console.error('获取所有机构的媒体活动日历数据失败:', error);
      throw error;
    }
  }

  /**
   * 根据日期获取所有机构的媒体列表
   * @param date 日期 (YYYY-MM-DD)
   * @param institutionId 机构ID（可选）
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @returns 媒体列表分页
   */
  async getAllMediaListByDate(date: string, institutionId?: number, pageNum = 1, pageSize = 10): Promise<Page<MediaVO>> {
    try {
      let url = `/api/admin/media/by-date?date=${date}&pageNum=${pageNum}&pageSize=${pageSize}`;
      if (institutionId) {
        url += `&institutionId=${institutionId}`;
      }
      const response = await http.get<Result<Page<MediaVO>>>(url);
      return response.data;
    } catch (error) {
      console.error('根据日期获取所有机构的媒体列表失败:', error);
      throw error;
    }
  }

  /**
   * 获取所有机构的媒体列表
   * @param institutionId 机构ID（可选）
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @returns 媒体列表分页
   */
  async getAllMediaList(institutionId?: number, pageNum = 1, pageSize = 10): Promise<Page<MediaVO>> {
    try {
      let url = `/api/admin/media?pageNum=${pageNum}&pageSize=${pageSize}`;
      if (institutionId) {
        url += `&institutionId=${institutionId}`;
      }
      const response = await http.get<Result<Page<MediaVO>>>(url);
      return response.data;
    } catch (error) {
      console.error('获取所有机构的媒体列表失败:', error);
      throw error;
    }
  }
}

export const mediaActivityService = new MediaActivityServiceImpl();
export const adminMediaActivityService = new AdminMediaActivityServiceImpl(); 