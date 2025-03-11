'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import React from 'react';
import { 
  ArrowLeft,
  FileVideo,
  FileImage,
  FileAudio,
  File,
  FileText,
  Trash2,
  Download,
  Share2,
  Edit,
  Save,
  X,
  RefreshCw
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { mediaService, MediaVO } from '@/services/media-service';
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
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED'
};

// 使用MediaVO类型作为我们的媒体详情类型，避免类型不匹配
type MediaDetail = MediaVO & {
  format?: string; // 服务端返回的MediaVO可能没有这些字段，所以标记为可选
  // 以下字段可能不在原始MediaVO中，我们在客户端扩展它们
  customResolution?: string;
  customDuration?: number;
  customThumbnail?: string;
};

// 媒体资源详情页面
export default function MediaDetailPage({ params }: { params: { id: string } }) {
  // 使用React.use()解包params - 按照Next.js建议处理
  const unwrappedParams = React.use(params as any) as { id: string };
  const id = unwrappedParams.id;
  
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [media, setMedia] = useState<MediaDetail | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  
  // 加载媒体详情
  useEffect(() => {
    fetchMediaDetail();
  }, [id]); // 使用解包后的id
  
  // 获取媒体详情
  const fetchMediaDetail = async () => {
    setIsLoading(true);
    try {
      // 使用解包后的id参数
      const response = await mediaService.getMediaInfo(parseInt(id));
      
      if (response && response.data) {
        // 扩展服务器返回的MediaVO，添加我们需要的其他字段
        const mediaData: MediaDetail = {
          ...response.data,
          format: getFileFormat(response.data.originalFilename),
          // 后端可能会提供以下信息，如果没有则保持为undefined
          customDuration: response.data.type === MEDIA_TYPES.VIDEO ? 0 : undefined,
          customResolution: response.data.type === MEDIA_TYPES.VIDEO || response.data.type === MEDIA_TYPES.IMAGE 
            ? '未知' : undefined,
          customThumbnail: undefined
        };
        
        setMedia(mediaData);
        setEditTitle(mediaData.title);
        setEditDescription(mediaData.description || '');
        
        // 如果是视频或图片，尝试获取访问URL
        if (mediaData.type === MEDIA_TYPES.VIDEO || mediaData.type === MEDIA_TYPES.IMAGE) {
          fetchMediaAccessUrl(mediaData.id);
        }
      } else {
        toast.error('获取媒体详情失败：没有返回数据');
      }
      
      setIsLoading(false);
    } catch (error) {
      console.error('获取媒体详情失败:', error);
      toast.error('获取媒体详情失败');
      setIsLoading(false);
    }
  };
  
  // 获取媒体访问URL
  const fetchMediaAccessUrl = async (mediaId: number) => {
    try {
      console.log('尝试获取媒体访问URL，mediaId:', mediaId);
      const response = await mediaService.getMediaAccessUrl(mediaId);
      console.log('获取媒体访问URL响应:', response);
      
      if (response && response.data && response.data.accessUrl) {
        console.log('获取到媒体访问URL:', response.data.accessUrl);
        // 更新媒体对象的访问URL
        setMedia(prev => {
          if (!prev) return null;
          const updated = {
            ...prev,
            accessUrl: response.data.accessUrl
          };
          console.log('更新后的media对象:', updated);
          return updated;
        });
      } else {
        console.error('获取媒体访问URL失败：没有返回有效的URL', response);
        toast.error('无法加载媒体预览：获取访问URL失败');
      }
    } catch (error) {
      console.error('获取媒体访问URL失败:', error);
      toast.error('无法加载媒体预览：API错误');
    }
  };
  
  // 获取文件格式
  const getFileFormat = (filename: string): string => {
    const ext = filename.split('.').pop()?.toUpperCase() || 'UNKNOWN';
    return ext;
  };
  
  // 获取媒体图标
  const getMediaIcon = (type: string) => {
    switch (type) {
      case MEDIA_TYPES.VIDEO: return <FileVideo className="h-10 w-10 text-blue-500" />;
      case MEDIA_TYPES.AUDIO: return <FileAudio className="h-10 w-10 text-yellow-500" />;
      case MEDIA_TYPES.IMAGE: return <FileImage className="h-10 w-10 text-green-500" />;
      case MEDIA_TYPES.DOCUMENT: return <FileText className="h-10 w-10 text-red-500" />;
      default: return <File className="h-10 w-10 text-gray-500" />;
    }
  };
  
  // 格式化文件大小
  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  };
  
  // 格式化时长
  const formatDuration = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const remainingSeconds = seconds % 60;
    
    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };
  
  // 获取状态文本
  const getStatusText = (status: string): string => {
    switch (status) {
      case MEDIA_STATUS.UPLOADING: return '上传中';
      case MEDIA_STATUS.PROCESSING: return '处理中';
      case MEDIA_STATUS.COMPLETED: return '已完成';
      case MEDIA_STATUS.FAILED: return '失败';
      default: return '未知';
    }
  };
  
  // 获取状态样式
  const getStatusStyle = (status: string): string => {
    switch (status) {
      case MEDIA_STATUS.UPLOADING: return 'text-blue-500';
      case MEDIA_STATUS.PROCESSING: return 'text-yellow-500';
      case MEDIA_STATUS.COMPLETED: return 'text-green-500';
      case MEDIA_STATUS.FAILED: return 'text-red-500';
      default: return 'text-gray-500';
    }
  };
  
  // 获取状态徽章
  const getStatusBadge = (status: string) => {
    let variant: 'default' | 'secondary' | 'destructive' | 'outline' = 'outline';
    let label = '未知';
    
    switch (status) {
      case MEDIA_STATUS.UPLOADING:
        variant = 'secondary';
        label = '上传中';
        break;
      case MEDIA_STATUS.PROCESSING:
        variant = 'secondary';
        label = '处理中';
        break;
      case MEDIA_STATUS.COMPLETED:
        variant = 'default';
        label = '已完成';
        break;
      case MEDIA_STATUS.FAILED:
        variant = 'destructive';
        label = '失败';
        break;
    }
    
    return <Badge variant={variant}>{label}</Badge>;
  };
  
  // 处理保存编辑
  const handleSaveEdit = async () => {
    try {
      // TODO: 替换为实际API调用
      // await mediaService.updateMedia(media.id, {
      //   title: editTitle,
      //   description: editDescription
      // });
      
      // 模拟更新
      setMedia(prev => prev ? {
        ...prev,
        title: editTitle,
        description: editDescription
      } : null);
      
      setIsEditing(false);
      toast.success('更新成功');
    } catch (error) {
      toast.error('更新失败');
    }
  };
  
  // 处理删除
  const handleDelete = async () => {
    if (!media) return;
    
    try {
      await mediaService.deleteMedia(media.id);
      setDeleteDialogOpen(false);
      toast.success('删除成功');
      router.push('/dashboard/media');
    } catch (error) {
      console.error('删除失败:', error);
      toast.error('删除失败');
    }
  };
  
  if (isLoading) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.back()}
          >
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <Skeleton className="h-8 w-48" />
        </div>
        
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-32" />
            <Skeleton className="h-4 w-24" />
          </CardHeader>
          <CardContent className="space-y-4">
            <Skeleton className="h-64 w-full" />
            <div className="space-y-2">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-2/3" />
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }
  
  if (!media) {
    return (
      <div className="p-6">
        <Card>
          <CardHeader>
            <CardTitle>资源不存在</CardTitle>
            <CardDescription>
              该媒体资源可能已被删除或您没有访问权限
            </CardDescription>
          </CardHeader>
          <CardFooter>
            <Button
              variant="outline"
              onClick={() => router.push('/dashboard/media')}
            >
              返回列表
            </Button>
          </CardFooter>
        </Card>
      </div>
    );
  }
  
  return (
    <div className="p-6 space-y-6">
      {/* 顶部导航 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.back()}
          >
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">媒体资源详情</h1>
            <p className="text-muted-foreground">查看和管理媒体资源</p>
          </div>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => window.location.reload()}
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            刷新
          </Button>
          <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
            <DialogTrigger asChild>
              <Button variant="destructive" size="sm">
                <Trash2 className="h-4 w-4 mr-2" />
                删除资源
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>确认删除</DialogTitle>
                <DialogDescription>
                  您确定要删除这个资源吗？此操作无法撤销。
                </DialogDescription>
              </DialogHeader>
              <DialogFooter>
                <Button
                  variant="outline"
                  onClick={() => setDeleteDialogOpen(false)}
                >
                  取消
                </Button>
                <Button
                  variant="destructive"
                  onClick={handleDelete}
                >
                  确认删除
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </div>
      
      {/* 资源信息 */}
      <div className="grid gap-6 md:grid-cols-3">
        {/* 主要信息 */}
        <Card className="md:col-span-2">
          <CardHeader>
            <div className="flex items-start justify-between">
              <div className="space-y-1">
                {isEditing ? (
                  <div className="space-y-2">
                    <Input
                      value={editTitle}
                      onChange={(e) => setEditTitle(e.target.value)}
                      placeholder="输入资源标题"
                    />
                    <Textarea
                      value={editDescription}
                      onChange={(e) => setEditDescription(e.target.value)}
                      placeholder="输入资源描述"
                      rows={3}
                    />
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        onClick={handleSaveEdit}
                      >
                        <Save className="h-4 w-4 mr-2" />
                        保存
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setIsEditing(false);
                          setEditTitle(media.title);
                          setEditDescription(media.description || '');
                        }}
                      >
                        <X className="h-4 w-4 mr-2" />
                        取消
                      </Button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="flex items-center gap-2">
                      <h2 className="text-xl font-semibold">{media.title}</h2>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6"
                        onClick={() => setIsEditing(true)}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                    </div>
                    <p className="text-muted-foreground">
                      {media.description || '无描述'}
                    </p>
                  </>
                )}
              </div>
            </div>
          </CardHeader>
          
          <CardContent>
            {/* 预览区域 */}
            <div className="aspect-video bg-slate-100 dark:bg-slate-900 rounded-lg mb-4 flex items-center justify-center">
              {media.type === MEDIA_TYPES.VIDEO && media.accessUrl ? (
                <video
                  key={media.accessUrl}
                  src={media.accessUrl}
                  controls
                  autoPlay={false}
                  className="w-full h-full rounded-lg"
                  poster={media.customThumbnail}
                  onError={(e) => {
                    console.error('视频加载错误:', e);
                    toast.error('视频加载失败，请刷新页面重试');
                  }}
                >
                  您的浏览器不支持视频播放
                </video>
              ) : media.type === MEDIA_TYPES.AUDIO && media.accessUrl ? (
                <audio
                  key={media.accessUrl}
                  src={media.accessUrl}
                  controls
                  autoPlay={false}
                  className="w-full max-w-md"
                  onError={(e) => {
                    console.error('音频加载错误:', e);
                    toast.error('音频加载失败，请刷新页面重试');
                  }}
                >
                  您的浏览器不支持音频播放
                </audio>
              ) : media.type === MEDIA_TYPES.IMAGE && media.accessUrl ? (
                <img
                  key={media.accessUrl}
                  src={media.accessUrl}
                  alt={media.title}
                  className="max-h-full rounded-lg"
                  onError={(e) => {
                    console.error('图片加载错误:', e);
                    toast.error('图片加载失败，请刷新页面重试');
                  }}
                />
              ) : (
                <div className="text-center">
                  {getMediaIcon(media.type)}
                  <p className="text-sm text-muted-foreground mt-2">
                    {media.type === MEDIA_TYPES.DOCUMENT 
                      ? '文档预览暂不可用' 
                      : media.type === MEDIA_TYPES.VIDEO || media.type === MEDIA_TYPES.AUDIO || media.type === MEDIA_TYPES.IMAGE
                        ? '正在加载预览...' 
                        : '预览不可用'}
                  </p>
                  {(media.type === MEDIA_TYPES.VIDEO || media.type === MEDIA_TYPES.AUDIO || media.type === MEDIA_TYPES.IMAGE) && !media.accessUrl && (
                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="mt-2"
                      onClick={() => fetchMediaAccessUrl(media.id)}
                    >
                      <RefreshCw className="h-4 w-4 mr-2" />
                      重新加载
                    </Button>
                  )}
                </div>
              )}
            </div>
            
            {/* 操作按钮 */}
            <div className="flex gap-2">
              <Button variant="outline" className="flex-1">
                <Download className="h-4 w-4 mr-2" />
                下载
              </Button>
              <Button variant="outline" className="flex-1">
                <Share2 className="h-4 w-4 mr-2" />
                分享
              </Button>
            </div>
          </CardContent>
        </Card>
        
        {/* 详细信息 */}
        <Card>
          <CardHeader>
            <CardTitle>资源信息</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">状态</span>
                <span className={getStatusStyle(media.status)}>
                  {getStatusText(media.status)}
                </span>
              </div>
              <Separator />
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">文件大小</span>
                <span>{formatFileSize(media.size)}</span>
              </div>
              <Separator />
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">文件格式</span>
                <span>{media.format || getFileFormat(media.originalFilename)}</span>
              </div>
              <Separator />
              {media.customDuration !== undefined && (
                <>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">时长</span>
                    <span>{media.customDuration > 0 ? formatDuration(media.customDuration) : '未知'}</span>
                  </div>
                  <Separator />
                </>
              )}
              {media.customResolution && (
                <>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">分辨率</span>
                    <span>{media.customResolution}</span>
                  </div>
                  <Separator />
                </>
              )}
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">原始文件名</span>
                <span className="truncate max-w-[200px]" title={media.originalFilename}>
                  {media.originalFilename}
                </span>
              </div>
              <Separator />
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">上传时间</span>
                <span>{new Date(media.uploadTime).toLocaleString()}</span>
              </div>
              {media.lastAccessTime && (
                <>
                  <Separator />
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">最后访问</span>
                    <span>{new Date(media.lastAccessTime).toLocaleString()}</span>
                  </div>
                </>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
} 