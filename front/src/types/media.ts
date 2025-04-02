/**
 * 媒体类型
 */
export enum MediaType {
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  DOCUMENT = 'DOCUMENT'
}

/**
 * 媒体状态
 */
export enum MediaStatus {
  UPLOADING = 'UPLOADING',
  PROCESSING = 'PROCESSING',
  READY = 'READY',
  ERROR = 'ERROR'
}

/**
 * 媒体信息
 */
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