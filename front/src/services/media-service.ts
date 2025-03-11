import { http } from '@/lib/http';

// 媒体类型
export enum MediaType {
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  IMAGE = 'IMAGE',
  DOCUMENT = 'DOCUMENT',
  OTHER = 'OTHER'
}

// 媒体状态
export enum MediaStatus {
  UPLOADING = 'UPLOADING', 
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

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
}

// 媒体信息
export interface MediaVO {
    id: number;
    title: string;
    description?: string;
    type: string;
    size: number;
    originalFilename: string;
    status: string;
    institutionId: number;
    uploaderId: number;
    uploadTime: string;
    lastAccessTime: string;
    accessUrl?: string;
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
   * 获取上传状态
   * @param mediaId 媒体ID
   */
  getUploadStatus(mediaId: number): Promise<Result<UploadStatusVO>>;

  /**
   * 继续上传
   * @param mediaId 媒体ID
   */
  resumeUpload(mediaId: number): Promise<Result<UploadInitiationVO>>;

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
    return http.get('/api/media', { params: queryParams });
  }

  async getMediaInfo(mediaId: number): Promise<Result<MediaVO>> {
    return http.get(`/api/media/${mediaId}`);
  }

  async initiateUpload(initDTO: MediaUploadInitDTO): Promise<Result<UploadInitiationVO>> {
    return http.post('/api/media/initiate-upload', initDTO);
  }

  async completeUpload(mediaId: number, dto: CompleteUploadDTO): Promise<Result<MediaVO>> {
    return http.post(`/api/media/${mediaId}/complete`, dto);
  }

  async cancelUpload(mediaId: number): Promise<Result<void>> {
    return http.delete(`/api/media/${mediaId}/cancel`);
  }

  async getUploadStatus(mediaId: number): Promise<Result<UploadStatusVO>> {
    return http.get(`/api/media/upload-status/${mediaId}`);
  }

  async resumeUpload(mediaId: number): Promise<Result<UploadInitiationVO>> {
    return http.get(`/api/media/${mediaId}/resume`);
  }

  async getMediaAccessUrl(mediaId: number, expirationMinutes?: number): Promise<Result<MediaAccessUrlVO>> {
    return http.get(`/api/media/${mediaId}/access`, {
      params: expirationMinutes ? { expirationMinutes } : undefined
    });
  }

  async deleteMedia(mediaId: number): Promise<Result<void>> {
    return http.delete(`/api/media/${mediaId}`);
  }
}

export const mediaService = new MediaServiceImpl(); 