'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';
import { StorageGrowthPointVO } from '@/types/stats';
import { MediaType, MediaVO, AdminMediaVO, MediaStatus } from '@/types/media';

// 通用响应类型
export interface Result<T> {
    code: number;
    message: string;
    data: T;
}

// 分页响应
export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

// 媒体查询参数
export interface MediaQueryParams {
    page?: number;
    size?: number;
    sort?: string[];
    type?: MediaType;
    filename?: string;
}

// 添加高级查询参数
export interface AdvancedMediaQueryParams extends MediaQueryParams {
  institutionName?: string;
  uploadStartTime?: string;
  uploadEndTime?: string;
  minSize?: number;
  maxSize?: number;
}

// 媒体访问URL响应
export interface MediaAccessUrlVO {
    accessUrl: string;
}

// 上传初始化请求
export interface MediaUploadInitDTO {
    filename: string;
    title: string;
    description?: string;
    contentType: string;
    fileSize: number;
    chunkSize?: number;
}

// 上传初始化响应
export interface UploadInitiationVO {
    mediaId: number;
    uploadId: string;
    totalParts: number;
    chunkSize: number;
    presignedUrls: PresignedUrlInfo[];
}

// 预签名URL信息
export interface PresignedUrlInfo {
    partNumber: number;
    url: string;
}

// 上传状态响应
export interface UploadStatusVO {
    mediaId: number;
    status: string;
    totalParts: number;
    completedParts: number;
    progressPercentage: number;
    completedPartNumbers: number[];
    initiatedAt: string;
    lastUpdatedAt: string;
    expiresAt: string;
}

// 完成上传请求
export interface CompleteUploadDTO {
    uploadId: string;
    completedParts: Array<{
        partNumber: number;
        etag: string;
    }>;
}

export interface MediaService {
  /**
   * 分页获取媒体列表
   * @param params 查询参数
   */
  getMediaList(params: MediaQueryParams): Promise<Result<Page<MediaVO>>>;

  /**
   * 获取媒体详情
   * @param mediaId 媒体ID
   */
  getMediaInfo(mediaId: number): Promise<Result<MediaVO>>;

  /**
   * 初始化上传
   * @param initDTO 初始化上传请求参数
   */
  initiateUpload(initDTO: MediaUploadInitDTO): Promise<Result<UploadInitiationVO>>;

  /**
   * 完成上传
   * @param mediaId 媒体ID
   * @param dto 完成上传请求参数
   */
  completeUpload(mediaId: number, dto: CompleteUploadDTO): Promise<Result<MediaVO>>;

  /**
   * 取消上传
   * @param mediaId 媒体ID
   */
  cancelUpload(mediaId: number): Promise<Result<void>>;

  /**
   * 获取媒体访问URL
   * @param mediaId 媒体ID
   * @param expirationMinutes URL有效期（分钟）
   */
  getMediaAccessUrl(mediaId: number, expirationMinutes?: number): Promise<Result<MediaAccessUrlVO>>;

  /**
   * 删除媒体
   * @param mediaId 媒体ID
   */
  deleteMedia(mediaId: number): Promise<Result<void>>;

  /**
   * 获取管理员视角的媒体列表 (支持过滤)
   * @param params 查询参数 (包括 type, filename, page, size)
   */
  getAdminMediaList(params: MediaQueryParams): Promise<Result<Page<MediaVO>>>;

  /**
   * 获取管理员视角的媒体列表 (支持高级筛选)
   * @param params 高级查询参数
   */
  getAdvancedMediaList(params: AdvancedMediaQueryParams): Promise<Result<Page<AdminMediaVO>>>;

  /**
   * 获取系统存储增长趋势
   * @param startDate 开始日期 (YYYY-MM-DD)
   * @param endDate 结束日期 (YYYY-MM-DD)
   * @param granularity 时间粒度 (目前仅支持 'DAYS')
   */
  getStorageGrowthTrend(startDate: string, endDate: string, granularity?: string): Promise<Result<StorageGrowthPointVO[]>>;
}

class MediaServiceImpl implements MediaService {
  async getMediaList(params: MediaQueryParams): Promise<Result<Page<MediaVO>>> {
    console.log('MediaService.getMediaList请求参数:', params);

    // 确保将type作为查询参数传递
    const queryParams: any = { ...params };

    // 如果传递了type参数，确保它被正确地作为查询参数传递
    if (params.type) {
      queryParams.type = params.type;
    }

    console.log('发送到API的查询参数:', queryParams);
    const response: AxiosResponse<ApiResponse<Page<MediaVO>>> = await request.get('/media', { params: queryParams });
    return response.data as Result<Page<MediaVO>>;
  }

  async getMediaInfo(mediaId: number): Promise<Result<MediaVO>> {
    const response: AxiosResponse<ApiResponse<MediaVO>> = await request.get(`/media/${mediaId}`);
    return response.data as Result<MediaVO>;
  }

  async initiateUpload(initDTO: MediaUploadInitDTO): Promise<Result<UploadInitiationVO>> {
    const response: AxiosResponse<ApiResponse<UploadInitiationVO>> = await request.post('/media/initiate-upload', initDTO);
    return response.data as Result<UploadInitiationVO>;
  }

  async completeUpload(mediaId: number, dto: CompleteUploadDTO): Promise<Result<MediaVO>> {
    const response: AxiosResponse<ApiResponse<MediaVO>> = await request.post(`/media/${mediaId}/complete`, dto);
    return response.data as Result<MediaVO>;
  }

  async cancelUpload(mediaId: number): Promise<Result<void>> {
    const response: AxiosResponse<ApiResponse<void>> = await request.delete(`/media/${mediaId}/cancel`);
    return response.data as Result<void>;
  }

  async getMediaAccessUrl(mediaId: number, expirationMinutes?: number): Promise<Result<MediaAccessUrlVO>> {
    const response: AxiosResponse<ApiResponse<MediaAccessUrlVO>> = await request.get(`/media/${mediaId}/access`, {
      params: expirationMinutes ? { expirationMinutes } : undefined
    });
    return response.data as Result<MediaAccessUrlVO>;
  }

  async deleteMedia(mediaId: number): Promise<Result<void>> {
    const response: AxiosResponse<ApiResponse<void>> = await request.delete(`/media/${mediaId}`);
    return response.data as Result<void>;
  }

  async getAdminMediaList(params: MediaQueryParams): Promise<Result<Page<MediaVO>>> {
    console.log('MediaService.getAdminMediaList 请求参数:', params);
    // 管理员接口路径为 /admin/media
    const response: AxiosResponse<ApiResponse<Page<MediaVO>>> = await request.get('/admin/media', { params });
    return response.data as Result<Page<MediaVO>>;
  }

  async getAdvancedMediaList(params: AdvancedMediaQueryParams): Promise<Result<Page<AdminMediaVO>>> {
    console.log('MediaService.getAdvancedMediaList 请求参数:', params);
    // 高级筛选接口路径为 /admin/media/advanced
    const response: AxiosResponse<ApiResponse<Page<AdminMediaVO>>> = await request.get('/admin/media/advanced', { params });
    return response.data as Result<Page<AdminMediaVO>>;
  }

  async getStorageGrowthTrend(startDate: string, endDate: string, granularity: string = 'DAYS'): Promise<Result<StorageGrowthPointVO[]>> {
    console.log('MediaService.getStorageGrowthTrend 请求参数:', { startDate, endDate, granularity });
    // 存储增长趋势接口路径
    const response: AxiosResponse<ApiResponse<StorageGrowthPointVO[]>> = await request.get('/admin/media/stats/storage-growth', {
      params: { startDate, endDate, granularity }
    });
    return response.data as Result<StorageGrowthPointVO[]>;
  }
}

export const mediaService = new MediaServiceImpl();