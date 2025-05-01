'use client';

import { request } from './api';
import { ApiResponse, Page } from '@/types/api';
import { ReviewRecordVO, ReviewType } from '@/types/review-record';
import { AxiosResponse } from 'axios';

/**
 * 审核记录服务
 */
export const reviewRecordService = {
  /**
   * 获取课程审核历史
   * @param courseId 课程ID
   * @returns 审核记录列表
   */
  async getCourseReviewHistory(courseId: number): Promise<ReviewRecordVO[]> {
    try {
      const response: AxiosResponse<ApiResponse<ReviewRecordVO[]>> =
        await request.get<ReviewRecordVO[]>(`/review-records/courses/${courseId}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程审核历史失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取机构审核历史
   * @param institutionId 机构ID
   * @returns 审核记录列表
   */
  async getInstitutionReviewHistory(institutionId: number): Promise<ReviewRecordVO[]> {
    try {
      const response: AxiosResponse<ApiResponse<ReviewRecordVO[]>> =
        await request.get<ReviewRecordVO[]>(`/review-records/institutions/${institutionId}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构审核历史失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取审核员的审核记录
   * @param page 页码
   * @param size 每页大小
   * @param reviewType 审核类型（可选）
   * @returns 审核记录分页
   */
  async getReviewerRecords(
    page: number = 0,
    size: number = 10,
    reviewType?: ReviewType
  ): Promise<Page<ReviewRecordVO>> {
    try {
      let url = `/review-records/reviewer?page=${page}&size=${size}`;
      if (reviewType !== undefined) {
        url += `&reviewType=${reviewType}`;
      }

      const response: AxiosResponse<ApiResponse<Page<ReviewRecordVO>>> =
        await request.get<Page<ReviewRecordVO>>(url);
      return response.data.data;
    } catch (error) {
      console.error(`获取审核员的审核记录失败:`, error);
      throw error;
    }
  },

  /**
   * 获取所有审核记录（管理员使用）
   * @param reviewType 审核类型（可选）
   * @param page 页码
   * @param size 每页大小
   * @returns 审核记录分页
   */
  async getAllReviewRecords(
    reviewType?: ReviewType,
    page: number = 0,
    size: number = 10
  ): Promise<Page<ReviewRecordVO>> {
    try {
      let url = `/review-records/all?page=${page}&size=${size}`;
      if (reviewType !== undefined) {
        url += `&reviewType=${reviewType}`;
      }

      const response: AxiosResponse<ApiResponse<Page<ReviewRecordVO>>> =
        await request.get<Page<ReviewRecordVO>>(url);
      return response.data.data;
    } catch (error) {
      console.error(`获取所有审核记录失败:`, error);
      throw error;
    }
  },

  /**
   * 获取机构相关的审核记录
   * @param institutionId 机构ID
   * @param reviewType 审核类型（可选）
   * @param page 页码
   * @param size 每页大小
   * @returns 审核记录分页
   */
  async getInstitutionReviewRecords(
    institutionId: number,
    reviewType?: ReviewType,
    page: number = 0,
    size: number = 10
  ): Promise<Page<ReviewRecordVO>> {
    try {
      let url = `/review-records/institution/${institutionId}?page=${page}&size=${size}`;
      if (reviewType !== undefined) {
        url += `&reviewType=${reviewType}`;
      }

      const response: AxiosResponse<ApiResponse<Page<ReviewRecordVO>>> =
        await request.get<Page<ReviewRecordVO>>(url);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构相关的审核记录失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  }
};

export default reviewRecordService;
