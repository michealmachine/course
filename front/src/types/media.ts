/**
 * 媒体类型
 */
export enum MediaType {
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  IMAGE = 'IMAGE',
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

/**
 * 管理员媒体信息（扩展了MediaVO）
 */
export interface AdminMediaVO extends MediaVO {
  /**
   * 机构名称
   */
  institutionName: string;
  
  /**
   * 上传者用户名
   */
  uploaderUsername: string;
  
  /**
   * 格式化后的文件大小
   */
  formattedSize: string;
}

/**
 * 媒体类型分布详情
 */
export interface TypeDistribution {
  /**
   * 媒体类型
   */
  type: MediaType;
  
  /**
   * 媒体类型显示名称
   */
  typeName: string;
  
  /**
   * 该类型媒体数量
   */
  count: number;
  
  /**
   * 占比
   */
  percentage: number;
}

/**
 * 媒体类型分布统计
 */
export interface MediaTypeDistributionVO {
  /**
   * 总媒体数量
   */
  totalCount: number;
  
  /**
   * 各类型媒体数量
   */
  typeCount: Record<string, number>;
  
  /**
   * 分布详情，用于图表展示
   */
  distribution: TypeDistribution[];
} 