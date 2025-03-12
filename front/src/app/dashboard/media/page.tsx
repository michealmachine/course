'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { 
  Upload, 
  FileVideo, 
  FileImage, 
  FileAudio, 
  File, 
  FileText, 
  Trash2, 
  RefreshCw,
  Plus,
  Video,
  Music,
  Image,
  Play
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { mediaService, MediaType, MediaStatus, MediaQueryParams } from '@/services/media-service';
import { Badge } from '@/components/ui/badge';

// 媒体类型
const MEDIA_TYPES = {
  VIDEO: 'VIDEO',
  AUDIO: 'AUDIO',
  IMAGE: 'IMAGE',
  DOCUMENT: 'DOCUMENT',
  OTHER: 'OTHER'
};

// 媒体状态
const MEDIA_STATUS = {
  UPLOADING: 'UPLOADING', 
  PROCESSING: 'PROCESSING',
  READY: 'READY',
  ERROR: 'ERROR'
};

// 分页参数
interface PaginationParams {
  page: number;
  size: number;
  sort?: string;
}

// 媒体列表项
interface MediaItem {
  id: number;
  title: string;
  description?: string;
  type: string; // 'VIDEO', 'AUDIO', 'IMAGE', 'DOCUMENT', 'OTHER'
  size: number;
  originalFilename: string;
  status: string;
  institutionId: number;
  uploaderId: number;
  uploadTime: string;
  lastAccessTime?: string;
  accessUrl?: string;
}

// 文件上传初始化请求
interface UploadInitRequest {
  title: string;
  description?: string;
  filename: string;
  contentType: string;
  fileSize: number;
  chunkSize?: number;
}

// 媒体资源页面
export default function MediaPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<string>('all');
  const [isLoading, setIsLoading] = useState(true);
  const [mediaList, setMediaList] = useState<MediaItem[]>([]);
  const [totalItems, setTotalItems] = useState(0);
  const [pagination, setPagination] = useState<MediaQueryParams>({ page: 0, size: 10 });
  const [typeStats, setTypeStats] = useState<Record<string, number>>({
    'all': 0,
    [MediaType.VIDEO]: 0,
    [MediaType.AUDIO]: 0,
    [MediaType.IMAGE]: 0,
    [MediaType.DOCUMENT]: 0
  });
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadingMediaId, setUploadingMediaId] = useState<number | null>(null);
  const [uploadTitle, setUploadTitle] = useState('');
  const [uploadDescription, setUploadDescription] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  
  // 加载媒体列表
  useEffect(() => {
    fetchMediaList();
  }, [pagination, activeTab]);
  
  // 获取媒体列表
  const fetchMediaList = async () => {
    setIsLoading(true);
    try {
      // 构建查询参数
      const queryParams: any = {
        page: pagination.page,
        size: pagination.size
      };
      
      // 如果不是"全部"，则添加类型筛选条件
      if (activeTab !== 'all') {
        // 确保类型参数正确设置
        queryParams.type = activeTab;
        console.log(`筛选媒体类型: ${activeTab}`);
      }
      
      console.log('发送到后端的查询参数:', JSON.stringify(queryParams));
      
      // 调用API获取媒体列表
      const response = await mediaService.getMediaList(queryParams);
      
      // 检查响应数据
      if (response && response.data) {
        const items = response.data.content;
        const total = response.data.totalElements;
        
        console.log(`获取到 ${items.length} 条媒体项，总计 ${total} 项`);
        
        // 检查响应数据是否符合筛选条件
        if (items.length > 0) {
          const typeSet = new Set(items.map(item => item.type));
          console.log('返回的媒体类型:', Array.from(typeSet));
          
          // 如果指定了特定类型，验证响应数据是否符合筛选条件
          if (activeTab !== 'all') {
            const matchingItems = items.filter(item => item.type === activeTab);
            console.log(`筛选类型 ${activeTab} 的匹配项: ${matchingItems.length}/${items.length}`);
            
            // 如果有数据但没有匹配项，说明后端筛选可能有问题
            if (matchingItems.length === 0 && items.length > 0) {
              console.warn('警告: 后端返回的数据可能未正确筛选');
              
              // 在前端手动筛选
              console.log(`手动筛选 ${activeTab} 类型的媒体`);
              setMediaList(matchingItems);
              setTotalItems(matchingItems.length);
              updateTypeStats(matchingItems, matchingItems.length);
      setIsLoading(false);
              return;
            }
            
            // 如果存在非匹配项，在控制台警告
            if (matchingItems.length < items.length) {
              console.warn(`警告: 后端返回了 ${items.length - matchingItems.length} 个不匹配的项目`);
              
              // 在前端手动筛选
              console.log(`手动筛选 ${activeTab} 类型的媒体`);
              setMediaList(matchingItems);
              setTotalItems(matchingItems.length);
              updateTypeStats(matchingItems, matchingItems.length);
              setIsLoading(false);
              return;
            }
          }
        }
        
        // 更新UI
        setMediaList(items);
        setTotalItems(total);
        updateTypeStats(items, total);
        
        // 为文档和其他媒体类型预加载访问URL
        items.forEach(item => {
          if ((item.type === MediaType.VIDEO || 
               item.type === MediaType.IMAGE) && !item.accessUrl) {
            preloadMediaAccessUrl(item.id);
          }
        });
      } else {
        console.warn('获取媒体列表返回无数据');
        setMediaList([]);
        setTotalItems(0);
      }
    } catch (error) {
      console.error('获取媒体列表失败:', error);
      toast.error('获取媒体列表失败');
      setMediaList([]);
      setTotalItems(0);
    } finally {
      setIsLoading(false);
    }
  };
  
  // 预加载媒体访问URL
  const preloadMediaAccessUrl = async (mediaId: number) => {
    try {
      const response = await mediaService.getMediaAccessUrl(mediaId);
      if (response && response.data && response.data.accessUrl) {
        console.log(`预加载媒体(ID:${mediaId})访问URL成功`);
        // 更新mediaList中对应项的accessUrl
        setMediaList(prev => 
          prev.map(item => 
            item.id === mediaId ? { ...item, accessUrl: response.data.accessUrl } : item
          )
        );
      }
    } catch (error) {
      console.error(`预加载媒体(ID:${mediaId})访问URL失败:`, error);
    }
  };
  
  // 更新类型统计数据的函数
  const updateTypeStats = (mediaItems: MediaItem[], total: number) => {
    // 如果是"全部"标签，计算各类型数量
    if (activeTab === 'all' && mediaItems.length > 0) {
      const stats: Record<string, number> = {
        'all': total,
        [MediaType.VIDEO]: 0,
        [MediaType.AUDIO]: 0,
        [MediaType.IMAGE]: 0,
        [MediaType.DOCUMENT]: 0,
        [MediaType.OTHER]: 0,
      };
      
      // 计算每种类型的数量
      mediaItems.forEach(item => {
        if (Object.values(MediaType).includes(item.type as MediaType)) {
          stats[item.type] += 1;
        } else {
          stats[MediaType.OTHER] += 1;
        }
      });
      
      setTypeStats(stats);
      console.log('媒体类型统计:', stats);
    } else if (activeTab !== 'all') {
      // 如果是特定类型标签，只更新当前类型的统计
      setTypeStats(prev => ({
        ...prev,
        [activeTab]: mediaItems.length
      }));
    }
  };
  
  // 处理页码更改
  const handlePageChange = (newPage: number) => {
    setPagination({ ...pagination, page: newPage });
  };
  
  // 处理媒体类型过滤
  const handleTabChange = (value: string) => {
    // 记录从哪个类型切换到哪个类型
    console.log(`媒体类型筛选: 从 ${activeTab} 切换到 ${value}`);
    
    if (value === activeTab) {
      // 如果点击当前活动标签，不做操作
      return;
    }
    
    // 设置新的活动标签
    setActiveTab(value);
    
    // 清空当前列表，以明确显示加载状态
    setMediaList([]);
    
    // 重置到第一页
    setPagination({ ...pagination, page: 0 });
    
    // 显示加载提示
    toast.info(`正在加载${getMediaTypeName(value)}资源...`);
  };
  
  // 获取媒体缩略图或占位图标
  const getMediaPreview = (media: MediaItem) => {
    // 针对不同类型返回不同的预览元素
    switch (media.type) {
      case MediaType.VIDEO:
        return (
          <div className="relative w-full aspect-video bg-blue-50 dark:bg-blue-950/20 rounded-t-lg overflow-hidden flex items-center justify-center">
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="bg-blue-100 dark:bg-blue-900/30 rounded-full p-4">
                <Video className="h-12 w-12 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-center pb-4 z-10">
              <Button 
                variant="secondary" 
                size="sm"
                className="opacity-85"
                onClick={(e) => {
                  e.stopPropagation();
                  router.push(`/dashboard/media/${media.id}`);
                }}
              >
                查看详情
              </Button>
            </div>
          </div>
        );
      case MediaType.IMAGE:
        return (
          <div className="relative w-full aspect-video bg-purple-50 dark:bg-purple-950/20 rounded-t-lg overflow-hidden flex items-center justify-center">
            <div className="bg-purple-100 dark:bg-purple-900/30 rounded-full p-4">
              <Image className="h-12 w-12 text-purple-600 dark:text-purple-400" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-center pb-4 z-10">
              <Button 
                variant="secondary" 
                size="sm"
                className="opacity-85"
                onClick={(e) => {
                  e.stopPropagation();
                  router.push(`/dashboard/media/${media.id}`);
                }}
              >
                查看详情
              </Button>
            </div>
          </div>
        );
      case MediaType.AUDIO:
        return (
          <div className="relative w-full aspect-video bg-green-50 dark:bg-green-950/20 rounded-t-lg flex items-center justify-center">
            <div className="bg-green-100 dark:bg-green-900/30 rounded-full p-4">
              <Music className="h-12 w-12 text-green-600 dark:text-green-400" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-center pb-4 z-10">
              <Button 
                variant="secondary" 
                size="sm"
                className="opacity-85"
                onClick={(e) => {
                  e.stopPropagation();
                  router.push(`/dashboard/media/${media.id}`);
                }}
              >
                查看详情
              </Button>
            </div>
          </div>
        );
      case MediaType.DOCUMENT:
        return (
          <div className="relative w-full aspect-video bg-orange-50 dark:bg-orange-950/20 rounded-t-lg flex items-center justify-center overflow-hidden">
            <div className="bg-orange-100 dark:bg-orange-900/30 rounded-full p-4">
              <FileText className="h-12 w-12 text-orange-600 dark:text-orange-400" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-center pb-4 z-10">
              <Button 
                variant="secondary" 
                size="sm"
                className="opacity-85"
                onClick={(e) => {
                  e.stopPropagation();
                  router.push(`/dashboard/media/${media.id}`);
                }}
              >
                查看详情
              </Button>
            </div>
          </div>
        );
      case MediaType.OTHER:
        return (
          <div className="relative w-full aspect-video bg-gray-50 dark:bg-gray-800/50 rounded-t-lg flex items-center justify-center">
            <div className="bg-gray-100 dark:bg-gray-700 rounded-full p-4">
              <File className="h-12 w-12 text-gray-600 dark:text-gray-400" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-center pb-4 z-10">
              <Button 
                variant="secondary" 
                size="sm"
                className="opacity-85"
                onClick={(e) => {
                  e.stopPropagation();
                  router.push(`/dashboard/media/${media.id}`);
                }}
              >
                查看详情
              </Button>
            </div>
          </div>
        );
      default:
        // 未知类型
        return (
          <div className="relative w-full aspect-video bg-slate-50 dark:bg-slate-800/50 rounded-t-lg flex items-center justify-center">
            <div className="bg-slate-100 dark:bg-slate-700 rounded-full p-4">
              <File className="h-12 w-12 text-slate-600 dark:text-slate-400" />
            </div>
            <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-center pb-4 z-10">
              <Button 
                variant="secondary" 
                size="sm"
                className="opacity-85"
                onClick={(e) => {
                  e.stopPropagation();
                  router.push(`/dashboard/media/${media.id}`);
                }}
              >
                查看详情
              </Button>
            </div>
          </div>
        );
    }
  };
  
  // 格式化文件大小
  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 B';
    
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    
    return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${sizes[i]}`;
  };

  // 格式化日期
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric'
    });
  };

  // 获取媒体类型名称
  const getMediaTypeName = (type: string): string => {
    const typeNames: Record<string, string> = {
      [MediaType.VIDEO]: '视频',
      [MediaType.AUDIO]: '音频',
      [MediaType.IMAGE]: '图片',
      [MediaType.DOCUMENT]: '文档',
      [MediaType.OTHER]: '其他',
    };
    
    return typeNames[type] || '未知类型';
  };

  // 获取状态徽章
  const getStatusBadge = (status: string) => {
    let variant: 'default' | 'secondary' | 'destructive' | 'outline' = 'outline';
    let label = '未知';
    
    switch (status) {
      case MediaStatus.UPLOADING:
        variant = 'secondary';
        label = '上传中';
        break;
      case MediaStatus.PROCESSING:
        variant = 'secondary';
        label = '处理中';
        break;
      case MediaStatus.COMPLETED:
        variant = 'default';
        label = '已完成';
        break;
      case MediaStatus.FAILED:
        variant = 'destructive';
        label = '失败';
        break;
    }
    
    return <Badge variant={variant}>{label}</Badge>;
  };

  // 处理文件选择
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
    }
  };

  // 初始化上传
  const initiateUpload = async () => {
    if (!selectedFile) {
      toast.error('请选择需要上传的文件');
      return;
    }
    
    if (!uploadTitle.trim()) {
      toast.error('请输入资源标题');
      return;
    }
    
    setIsUploading(true);
    setUploadProgress(0);
    
    try {
      // 获取文件类型
      const fileType = getFileType(selectedFile.type, selectedFile.name);
      
      // 初始化上传
      const initResponse = await mediaService.initiateUpload({
        title: uploadTitle,
        description: uploadDescription || undefined,
        filename: selectedFile.name,
        fileSize: selectedFile.size,
        contentType: selectedFile.type,
      });
      
      if (initResponse.data) {
        setUploadingMediaId(initResponse.data.mediaId);
        
        // 开始上传分片
        await uploadParts(
          initResponse.data.mediaId,
          initResponse.data.uploadId,
          initResponse.data.presignedUrls.reduce((acc, curr) => {
            acc[curr.partNumber] = curr.url;
            return acc;
          }, {} as Record<number, string>), 
          selectedFile
        );
        
        // 不再需要状态检查，由前端自己管理状态
        // startStatusCheck(initResponse.data.mediaId);
      }
    } catch (error: any) {
      console.error('初始化上传失败:', error);
      setIsUploading(false);
      
      if (error.response?.status === 413) {
        toast.error('文件大小超出限制，请选择较小的文件');
      } else if (error.response?.status === 415) {
        toast.error('不支持的文件类型，请选择其他格式');
      } else if (error.response?.status === 507) {
        toast.error('存储空间不足，请联系管理员');
      } else if (error.response?.data?.message) {
        toast.error('上传失败：' + error.response.data.message);
      } else {
        toast.error('初始化上传失败，请稍后重试');
      }
    }
  };

  // 获取文件类型
  const getFileType = (mimeType: string, fileName: string): string => {
    if (mimeType.startsWith('video/')) {
      return MediaType.VIDEO;
    } else if (mimeType.startsWith('audio/')) {
      return MediaType.AUDIO;
    } else if (mimeType.startsWith('image/')) {
      return MediaType.IMAGE;
    } else if (mimeType.startsWith('application/') || fileName.endsWith('.pdf') || 
               fileName.endsWith('.doc') || fileName.endsWith('.docx') || 
               fileName.endsWith('.xls') || fileName.endsWith('.xlsx') || 
               fileName.endsWith('.ppt') || fileName.endsWith('.pptx') || 
               fileName.endsWith('.txt')) {
      return MediaType.DOCUMENT;
    } else {
      return MediaType.DOCUMENT; // 默认为文档类型
    }
  };

  // 上传分片
  const uploadParts = async (
    mediaId: number,
    uploadId: string,
    presignedUrls: Record<number, string>, 
    file: File
  ) => {
    try {
      // 计算总分片数
      const totalParts = Object.keys(presignedUrls).length;
      const chunkSize = Math.ceil(file.size / totalParts);
      console.log(`开始分片上传 - 总分片数: ${totalParts}, 文件大小: ${file.size}`);
      
      // 用于保存已完成分片的信息
      const completedParts: Array<{partNumber: number, etag: string}> = [];
      
      // 依次上传分片，使用for循环而非并发，避免并发上传可能导致的问题
      for (const [partNumberStr, url] of Object.entries(presignedUrls)) {
        const partNumber = parseInt(partNumberStr);
        const start = (partNumber - 1) * chunkSize;
        const end = Math.min(start + chunkSize, file.size);
        const chunk = file.slice(start, end);
        
        try {
          // 上传分片到预签名URL
          const response = await fetch(url, {
            method: 'PUT',
            body: chunk,
            headers: {
              'Content-Type': 'application/octet-stream'
            }
          });
          
          if (!response.ok) {
            throw new Error(`分片 ${partNumber} 上传失败: ${response.status} ${response.statusText}`);
          }
          
          // 获取ETag并处理
          let eTag = response.headers.get('ETag') || response.headers.get('etag') || '';
          const cleanETag = eTag.replace(/^"|"$/g, '');
          
          // 记录分片信息
          completedParts.push({
            partNumber,
            etag: cleanETag
          });
          
          // 更新进度
          setUploadProgress(Math.floor((partNumber / totalParts) * 90));
        } catch (error) {
          console.error(`分片 ${partNumber} 上传失败:`, error);
          throw error;
        }
      }
      
      // 按分片编号排序
      completedParts.sort((a, b) => a.partNumber - b.partNumber);
      
      // 构建请求对象
      const completeRequest = {
        uploadId,
        completedParts
      };
      
      // 完成上传，合并分片
      console.log('所有分片上传完成，发送合并请求...');
      const completeResult = await mediaService.completeUpload(mediaId, completeRequest);
      
      if (!completeResult.data) {
        throw new Error(completeResult.message || '完成上传失败');
      }
      
      console.log('合并上传成功');
      
      // 刷新媒体列表
      fetchMediaList();
      setUploadProgress(100);
      toast.success('上传成功', {
        description: '文件已成功上传到服务器'
      });
      
      // 重置表单和对话框
      resetUploadState();
      setUploadDialogOpen(false);
    } catch (error) {
      console.error('分片上传失败:', error);
      toast.error('上传失败', {
        description: error instanceof Error ? error.message : String(error)
      });
      
      // 取消上传
      await cancelUpload();
    } finally {
      setIsUploading(false);
    }
  };

  // 取消上传
  const cancelUpload = async () => {
    if (uploadingMediaId) {
      try {
        await mediaService.cancelUpload(uploadingMediaId);
        toast.success('已取消上传');
      } catch (error) {
        console.error('取消上传失败:', error);
        toast.error('取消上传失败，请刷新页面后重试');
      }
    }
    
    setIsUploading(false);
    setUploadDialogOpen(false);
    resetUploadState();
  };
  
  // 重置上传状态
  const resetUploadState = () => {
    setIsUploading(false);
    setUploadProgress(0);
    setUploadingMediaId(null);
    setUploadTitle('');
    setUploadDescription('');
    setSelectedFile(null);
    
    // 重置文件输入
    const fileInput = document.getElementById('file') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  };
  
  // 更新页面标题和描述
  const getPageTitle = () => {
    if (activeTab === 'all') {
      return '媒体资源管理';
    }
    return `${getMediaTypeName(activeTab)}资源管理`;
  };
  
  const getPageDescription = () => {
    if (activeTab === 'all') {
      return '管理您的视频、音频、图片和文档资源';
    }
    return `管理您的${getMediaTypeName(activeTab)}资源${totalItems > 0 ? `，共 ${totalItems} 项` : ''}`;
  };
  
  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            {getPageTitle()}
          </h1>
          <p className="text-muted-foreground">
            {getPageDescription()}
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => fetchMediaList()}
            disabled={isLoading}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            刷新
          </Button>
          <Dialog open={uploadDialogOpen} onOpenChange={setUploadDialogOpen}>
            <DialogTrigger asChild>
              <Button size="sm">
                <Upload className="h-4 w-4 mr-2" />
                上传资源
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
              <DialogHeader>
                <DialogTitle>上传资源</DialogTitle>
                <DialogDescription>
                  上传视频、音频、图片或文档资源。大文件将会使用分片上传。
                </DialogDescription>
              </DialogHeader>
              
              <div className="grid gap-4 py-4">
                <div className="grid gap-2">
                  <Label htmlFor="title">标题</Label>
                  <Input 
                    id="title" 
                    placeholder="输入资源标题" 
                    value={uploadTitle}
                    onChange={(e) => setUploadTitle(e.target.value)}
                    disabled={isUploading}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="description">描述（可选）</Label>
                  <Input 
                    id="description" 
                    placeholder="输入资源描述" 
                    value={uploadDescription}
                    onChange={(e) => setUploadDescription(e.target.value)}
                    disabled={isUploading}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="file">选择文件</Label>
                  <Input 
                    id="file" 
                    type="file" 
                    className="cursor-pointer"
                    onChange={handleFileChange}
                    disabled={isUploading}
                  />
                  {selectedFile && (
                    <p className="text-sm text-muted-foreground">
                      文件大小: {formatFileSize(selectedFile.size)}
                    </p>
                  )}
                </div>
                
                {isUploading && (
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>上传进度</span>
                      <span>{uploadProgress}%</span>
                    </div>
                    <div className="w-full bg-slate-200 rounded-full h-2.5 dark:bg-slate-700">
                      <div 
                        className="bg-blue-600 h-2.5 rounded-full" 
                        style={{ width: `${uploadProgress}%` }}
                      ></div>
                    </div>
                  </div>
                )}
              </div>
              
              <DialogFooter>
                {isUploading ? (
                  <Button variant="destructive" onClick={cancelUpload}>
                    取消上传
                  </Button>
                ) : (
                  <>
                    <Button variant="outline" onClick={() => setUploadDialogOpen(false)}>
                      取消
                    </Button>
                    <Button onClick={initiateUpload}>
                      开始上传
                    </Button>
                  </>
                )}
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </div>
      
      <Tabs defaultValue="all" value={activeTab} onValueChange={handleTabChange}>
        <div className="flex justify-between items-center mb-2">
          <TabsList className="grid grid-cols-6">
            <TabsTrigger value="all">
              全部
              {typeStats.all > 0 && 
                <span className="ml-1 text-xs bg-muted rounded-full px-1.5 py-0.5">{typeStats.all}</span>
              }
            </TabsTrigger>
            <TabsTrigger value={MediaType.VIDEO}>
              视频
              {typeStats[MediaType.VIDEO] > 0 && 
                <span className="ml-1 text-xs bg-primary/20 text-primary rounded-full px-1.5 py-0.5">
                  {typeStats[MediaType.VIDEO]}
                </span>
              }
            </TabsTrigger>
            <TabsTrigger value={MediaType.AUDIO}>
              音频
              {typeStats[MediaType.AUDIO] > 0 && 
                <span className="ml-1 text-xs bg-primary/20 text-primary rounded-full px-1.5 py-0.5">
                  {typeStats[MediaType.AUDIO]}
                </span>
              }
            </TabsTrigger>
            <TabsTrigger value={MediaType.IMAGE}>
              图片
              {typeStats[MediaType.IMAGE] > 0 && 
                <span className="ml-1 text-xs bg-primary/20 text-primary rounded-full px-1.5 py-0.5">
                  {typeStats[MediaType.IMAGE]}
                </span>
              }
            </TabsTrigger>
            <TabsTrigger value={MediaType.DOCUMENT}>
              文档
              {typeStats[MediaType.DOCUMENT] > 0 && 
                <span className="ml-1 text-xs bg-primary/20 text-primary rounded-full px-1.5 py-0.5">
                  {typeStats[MediaType.DOCUMENT]}
                </span>
              }
            </TabsTrigger>
            <TabsTrigger value={MediaType.OTHER}>
              其他
              {typeStats[MediaType.OTHER] > 0 && 
                <span className="ml-1 text-xs bg-primary/20 text-primary rounded-full px-1.5 py-0.5">
                  {typeStats[MediaType.OTHER]}
                </span>
              }
            </TabsTrigger>
        </TabsList>
          
          {!isLoading && activeTab !== 'all' && (
            <p className="text-sm text-muted-foreground">
              当前显示: <span className="font-medium">{getMediaTypeName(activeTab)}</span> · 共 {totalItems} 项
            </p>
          )}
        </div>
        
        <div className="mt-4">
          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="rounded-lg border bg-card text-card-foreground shadow">
                  <div className="p-6 space-y-2">
                    <Skeleton className="h-10 w-10 rounded" />
                    <Skeleton className="h-4 w-1/2" />
                    <Skeleton className="h-4 w-3/4" />
                  </div>
                </div>
              ))}
            </div>
          ) : mediaList.length === 0 ? (
            <div className="flex flex-col items-center justify-center p-8 text-center border rounded-lg">
              <div className="mb-4 bg-muted rounded-full p-3">
                <File className="h-6 w-6 text-muted-foreground" />
              </div>
              <h3 className="text-lg font-semibold">没有{activeTab !== 'all' ? getMediaTypeName(activeTab) : '媒体'}资源</h3>
              <p className="text-sm text-muted-foreground mb-4">
                {activeTab !== 'all' 
                  ? `您目前没有任何${getMediaTypeName(activeTab)}资源。`
                  : '您目前没有上传任何媒体资源。'
                }
              </p>
              <Button size="sm" onClick={() => setUploadDialogOpen(true)}>
                <Upload className="h-4 w-4 mr-2" />
                上传资源
              </Button>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
                {mediaList.map((media) => (
                  <Card 
                    key={media.id} 
                    className="overflow-hidden group hover:border-primary/50 transition-all duration-200 hover:shadow-md h-full"
                    onClick={() => router.push(`/dashboard/media/${media.id}`)}
                  >
                    {/* 媒体预览区域 */}
                    <div className="media-preview-container relative">
                      {getMediaPreview(media)}
                    </div>
                    
                    <CardHeader className="p-4 pb-0">
                      <div className="flex items-start justify-between">
                        <div className="space-y-1 flex-1 min-w-0">
                          <CardTitle className="text-base line-clamp-1 group-hover:text-primary transition-colors">
                            {media.title}
                          </CardTitle>
                          {media.description && (
                            <CardDescription className="line-clamp-2 text-xs">
                              {media.description}
                            </CardDescription>
                          )}
                        </div>
                        <div className="ml-2 mt-1 flex-shrink-0">
                          <Button 
                            variant="ghost" 
                            size="icon" 
                            className="h-7 w-7 opacity-0 group-hover:opacity-100 transition-opacity"
                            onClick={(e) => {
                              e.stopPropagation();
                              try {
                                // 打开确认对话框
                                if (confirm('确定要删除这个文件吗？此操作无法撤销。')) {
                                  mediaService.deleteMedia(media.id).then(() => {
                                toast.success('删除成功');
                                fetchMediaList();
                                  });
                                }
                              } catch (error) {
                                console.error('删除失败:', error);
                                toast.error('删除失败');
                              }
                            }}
                          >
                            <Trash2 className="h-4 w-4 text-red-500" />
                          </Button>
                        </div>
                      </div>
                    </CardHeader>
                    
                    <CardContent className="p-4 pt-3">
                      <div className="flex flex-wrap gap-x-4 gap-y-2 text-xs">
                        <div className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-slate-300 dark:bg-slate-600"></span>
                          <span>{getMediaTypeName(media.type)}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-slate-300 dark:bg-slate-600"></span>
                          <span>{formatFileSize(media.size)}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-slate-300 dark:bg-slate-600"></span>
                          <span>{formatDate(media.uploadTime)}</span>
                        </div>
                        <div className="mt-1 w-full">
                          {getStatusBadge(media.status)}
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
              
              {totalItems > (pagination.size || 10) && (
                <div className="mt-4">
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious 
                          href="#" 
                          onClick={(e) => {
                            e.preventDefault();
                            if ((pagination.page || 0) > 0) {
                              handlePageChange((pagination.page || 0) - 1);
                            }
                          }}
                          aria-disabled={(pagination.page || 0) === 0}
                          className={(pagination.page || 0) === 0 ? 'pointer-events-none opacity-50' : ''}
                        />
                      </PaginationItem>
                      
                      {Array.from({ 
                        length: Math.ceil(totalItems / (pagination.size || 10)) 
                      }).map((_, i) => (
                        <PaginationItem key={i}>
                          <PaginationLink
                            href="#"
                            onClick={(e) => {
                              e.preventDefault();
                              handlePageChange(i);
                            }}
                            isActive={i === (pagination.page || 0)}
                          >
                            {i + 1}
                          </PaginationLink>
                        </PaginationItem>
                      ))}
                      
                      <PaginationItem>
                        <PaginationNext 
                          href="#" 
                          onClick={(e) => {
                            e.preventDefault();
                            const maxPage = Math.ceil(totalItems / (pagination.size || 10)) - 1;
                            if ((pagination.page || 0) < maxPage) {
                              handlePageChange((pagination.page || 0) + 1);
                            }
                          }}
                          aria-disabled={(pagination.page || 0) >= Math.ceil(totalItems / (pagination.size || 10)) - 1}
                          className={
                            (pagination.page || 0) >= Math.ceil(totalItems / (pagination.size || 10)) - 1 
                              ? 'pointer-events-none opacity-50' 
                              : ''
                          }
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              )}
            </>
          )}
        </div>
      </Tabs>
    </div>
  );
} 