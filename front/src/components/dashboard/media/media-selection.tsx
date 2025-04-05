'use client';

import { useState, useEffect } from 'react';
import { HoverCard, HoverCardContent, HoverCardTrigger } from '@/components/ui/hover-card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { mediaService } from '@/services/media-service';
import { toast } from 'sonner';
import { 
  Loader2, 
  FileText, 
  FileVideo, 
  FileAudio, 
  FileImage,
  RefreshCw,
} from 'lucide-react';
import type { MediaVO } from '@/types/media';

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const dm = 2;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

// 根据媒体类型获取媒体类型名称
const getMediaTypeName = (type: string): string => {
  const typeStr = type.toUpperCase();
  if (typeStr.includes('VIDEO')) return '视频';
  if (typeStr.includes('AUDIO')) return '音频';
  if (typeStr.includes('IMAGE')) return '图片';
  if (typeStr.includes('DOCUMENT') || typeStr.includes('PDF')) return '文档';
  return '其他';
};

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

interface MediaSelectionProps {
  onSelectMedia: (media: MediaVO) => void;
  selectedMediaId?: number;
  preloadAccessUrl?: boolean;
}

export function MediaSelection({ onSelectMedia, selectedMediaId, preloadAccessUrl = true }: MediaSelectionProps) {
  // 媒体资源状态
  const [mediaList, setMediaList] = useState<MediaVO[]>([]);
  const [isLoadingMedia, setIsLoadingMedia] = useState(false);
  const [mediaSearchTerm, setMediaSearchTerm] = useState('');
  const [mediaPage, setMediaPage] = useState(0);
  const [mediaTotalPages, setMediaTotalPages] = useState(0);
  const [mediaType, setMediaType] = useState<string>('all');

  // 加载媒体资源
  const loadMediaResources = async (page = 0, searchTerm = '', type = 'all') => {
    try {
      setIsLoadingMedia(true);
      
      const params: any = {
        page,
        size: 12,
      };
      
      if (searchTerm) {
        params.filename = searchTerm;
      }
      
      if (type !== 'all') {
        params.type = type.toUpperCase();
      }
      
      const response = await mediaService.getMediaList(params);
      
      if (response && response.data) {
        const mediaItems = response.data.content;
        
        // 如果需要预加载访问URL
        if (preloadAccessUrl) {
          for (const item of mediaItems) {
            if (!item.accessUrl) {
              try {
                const urlResult = await mediaService.getMediaAccessUrl(item.id, 60);
                if (urlResult && urlResult.data && urlResult.data.accessUrl) {
                  item.accessUrl = urlResult.data.accessUrl;
                }
              } catch (err) {
                console.error(`无法加载媒体 ${item.id} 的预览URL:`, err);
              }
            }
          }
        }
        
        setMediaList(mediaItems);
        setMediaTotalPages(response.data.totalPages);
      }
    } catch (error) {
      console.error('加载媒体资源失败:', error);
      toast.error('加载媒体资源失败');
    } finally {
      setIsLoadingMedia(false);
    }
  };

  // 首次加载
  useEffect(() => {
    loadMediaResources(0, '', 'all');
  }, []);

  // 处理搜索
  const handleSearch = () => {
    loadMediaResources(0, mediaSearchTerm, mediaType);
  };

  // 处理重置
  const handleReset = () => {
    setMediaSearchTerm('');
    setMediaType('all');
    loadMediaResources(0, '', 'all');
  };

  // 处理键盘事件
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-2">
        <Input
          placeholder="搜索媒体资源..."
          value={mediaSearchTerm}
          onChange={e => setMediaSearchTerm(e.target.value)}
          onKeyDown={handleKeyDown}
          className="flex-1"
        />
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Select
            value={mediaType}
            onValueChange={setMediaType}
          >
            <SelectTrigger className="w-full sm:w-[150px]">
              <SelectValue placeholder="媒体类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部类型</SelectItem>
              <SelectItem value="video">视频</SelectItem>
              <SelectItem value="audio">音频</SelectItem>
              <SelectItem value="image">图片</SelectItem>
              <SelectItem value="document">文档</SelectItem>
            </SelectContent>
          </Select>
          
          <Button 
            variant="secondary"
            size="sm"
            onClick={handleSearch}
          >
            搜索
          </Button>
          
          <Button 
            variant="outline"
            size="sm"
            onClick={handleReset}
          >
            <RefreshCw className="h-4 w-4 mr-1" />
            重置
          </Button>
        </div>
      </div>
      
      {isLoadingMedia ? (
        <div className="flex justify-center items-center py-6">
          <Loader2 className="h-6 w-6 animate-spin text-primary" />
          <span className="ml-2">加载媒体资源中...</span>
        </div>
      ) : mediaList.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3 max-h-[400px] overflow-y-auto p-1">
          {mediaList.map(media => (
            <HoverCard key={media.id} openDelay={200}>
              <HoverCardTrigger asChild>
                <div
                  className={`rounded-md p-3 cursor-pointer transition-colors ${
                    selectedMediaId === media.id 
                      ? 'bg-primary/10 border-primary border' 
                      : 'border hover:bg-accent/50'
                  }`}
                  onClick={() => onSelectMedia(media)}
                >
                  <div className="flex items-center">
                    <div className={cn(
                      "h-10 w-10 rounded-full flex items-center justify-center mr-3",
                      media.type.toLowerCase().includes('video') && "bg-blue-100 dark:bg-blue-950/50",
                      media.type.toLowerCase().includes('audio') && "bg-green-100 dark:bg-green-950/50",
                      media.type.toLowerCase().includes('image') && "bg-purple-100 dark:bg-purple-950/50",
                      (media.type.toLowerCase().includes('document') || media.type.toLowerCase().includes('pdf')) && "bg-orange-100 dark:bg-orange-950/50"
                    )}>
                      {media.type.toLowerCase().includes('video') && <FileVideo className="h-5 w-5 text-blue-600 dark:text-blue-400" />}
                      {media.type.toLowerCase().includes('audio') && <FileAudio className="h-5 w-5 text-green-600 dark:text-green-400" />}
                      {media.type.toLowerCase().includes('image') && <FileImage className="h-5 w-5 text-purple-600 dark:text-purple-400" />}
                      {(media.type.toLowerCase().includes('document') || media.type.toLowerCase().includes('pdf')) && <FileText className="h-5 w-5 text-orange-600 dark:text-orange-400" />}
                    </div>
                    <div className="flex flex-col flex-1 min-w-0">
                      <div className="font-medium truncate">{media.title}</div>
                      <div className="text-xs text-muted-foreground mt-1 flex justify-between">
                        <span>{getMediaTypeName(media.type)}</span>
                        {media.size ? <span>{formatFileSize(media.size)}</span> : null}
                      </div>
                    </div>
                  </div>
                </div>
              </HoverCardTrigger>
              <HoverCardContent className="w-80 p-0">
                {media.type.toLowerCase().includes('image') && media.accessUrl ? (
                  <div className="relative">
                    <img 
                      src={media.accessUrl} 
                      alt={media.title} 
                      className="w-full h-40 object-cover rounded-t-md"
                      onError={(e) => {
                        const target = e.target as HTMLImageElement;
                        target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 24 24' fill='none' stroke='%23888' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='3' y='3' width='18' height='18' rx='2' ry='2'%3E%3C/rect%3E%3Ccircle cx='8.5' cy='8.5' r='1.5'%3E%3C/circle%3E%3Cpolyline points='21 15 16 10 5 21'%3E%3C/polyline%3E%3C/svg%3E";
                        target.classList.add("p-8", "bg-muted");
                      }}
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex items-end">
                      <div className="p-3 text-white w-full">
                        <p className="font-medium truncate">{media.title}</p>
                        <p className="text-xs text-white/80 truncate">{media.originalFilename}</p>
                      </div>
                    </div>
                  </div>
                ) : media.type.toLowerCase().includes('video') && media.accessUrl ? (
                  <div className="relative">
                    <video
                      src={media.accessUrl}
                      controls
                      className="w-full h-40 object-cover rounded-t-md"
                      onError={(e) => {
                        const target = e.target as HTMLVideoElement;
                        target.style.display = "none";
                        const errorDiv = document.createElement('div');
                        errorDiv.className = "w-full h-40 flex items-center justify-center bg-blue-50 dark:bg-blue-950/20";
                        errorDiv.innerHTML = `<div class="flex flex-col items-center text-center">
                          <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-500 mb-2"><path d="m22 8-6 4 6 4V8Z"></path><rect width="14" height="12" x="2" y="6" rx="2" ry="2"></rect></svg>
                          <p class="text-sm text-muted-foreground">视频加载失败</p>
                        </div>`;
                        target.parentElement?.appendChild(errorDiv);
                      }}
                    >
                      您的浏览器不支持视频播放
                    </video>
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex items-end pointer-events-none">
                      <div className="p-3 text-white w-full">
                        <p className="font-medium truncate">{media.title}</p>
                        <p className="text-xs text-white/80 truncate">{media.originalFilename}</p>
                      </div>
                    </div>
                  </div>
                ) : media.type.toLowerCase().includes('audio') && media.accessUrl ? (
                  <div className="p-4">
                    <div className="w-full h-32 bg-green-50 dark:bg-green-950/20 rounded-md flex flex-col items-center justify-center mb-2">
                      <FileAudio className="h-10 w-10 text-green-500 mb-2" />
                      <p className="text-sm font-medium truncate">{media.title}</p>
                    </div>
                    <audio
                      src={media.accessUrl}
                      controls
                      className="w-full"
                      onError={(e) => {
                        const target = e.target as HTMLAudioElement;
                        target.style.display = "none";
                        const errorDiv = document.createElement('div');
                        errorDiv.className = "w-full text-center mt-2";
                        errorDiv.innerHTML = `<p class="text-sm text-red-500">音频加载失败</p>`;
                        target.parentElement?.appendChild(errorDiv);
                      }}
                    >
                      您的浏览器不支持音频播放
                    </audio>
                  </div>
                ) : (
                  <div className={cn(
                    "w-full h-40 rounded-t-md flex items-center justify-center",
                    media.type.toLowerCase().includes('video') && "bg-blue-50 dark:bg-blue-950/20",
                    media.type.toLowerCase().includes('audio') && "bg-green-50 dark:bg-green-950/20",
                    media.type.toLowerCase().includes('image') && "bg-purple-50 dark:bg-purple-950/20",
                    (media.type.toLowerCase().includes('document') || media.type.toLowerCase().includes('pdf')) && "bg-orange-50 dark:bg-orange-950/20"
                  )}>
                    {media.type.toLowerCase().includes('video') && <FileVideo className="h-16 w-16 text-blue-600 dark:text-blue-400" />}
                    {media.type.toLowerCase().includes('audio') && <FileAudio className="h-16 w-16 text-green-600 dark:text-green-400" />}
                    {media.type.toLowerCase().includes('image') && <FileImage className="h-16 w-16 text-purple-600 dark:text-purple-400" />}
                    {(media.type.toLowerCase().includes('document') || media.type.toLowerCase().includes('pdf')) && <FileText className="h-16 w-16 text-orange-600 dark:text-orange-400" />}
                  </div>
                )}
                <div className="p-3">
                  <div className="grid grid-cols-2 gap-2 mb-2">
                    <div>
                      <p className="text-xs text-muted-foreground">类型</p>
                      <p className="text-sm">{getMediaTypeName(media.type)}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">大小</p>
                      <p className="text-sm">{formatFileSize(media.size)}</p>
                    </div>
                    {media.uploadTime && (
                      <div className="col-span-2">
                        <p className="text-xs text-muted-foreground">上传时间</p>
                        <p className="text-sm">{formatDate(media.uploadTime)}</p>
                      </div>
                    )}
                  </div>
                  <Button 
                    className="w-full mt-2"
                    onClick={() => onSelectMedia(media)}
                  >
                    选择此媒体
                  </Button>
                </div>
              </HoverCardContent>
            </HoverCard>
          ))}
        </div>
      ) : (
        <div className="text-center py-10 text-muted-foreground">
          <p>未找到媒体资源</p>
        </div>
      )}
      
      {mediaList.length > 0 && mediaTotalPages > 1 && (
        <div className="flex justify-center pt-4">
          <div className="flex gap-1">
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                if (mediaPage > 0) {
                  const newPage = mediaPage - 1;
                  setMediaPage(newPage);
                  loadMediaResources(newPage, mediaSearchTerm, mediaType);
                }
              }}
              disabled={mediaPage === 0 || isLoadingMedia}
            >
              上一页
            </Button>
            <div className="flex items-center px-3">
              <span className="text-sm">{mediaPage + 1} / {mediaTotalPages}</span>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                if (mediaPage < mediaTotalPages - 1) {
                  const newPage = mediaPage + 1;
                  setMediaPage(newPage);
                  loadMediaResources(newPage, mediaSearchTerm, mediaType);
                }
              }}
              disabled={mediaPage >= mediaTotalPages - 1 || isLoadingMedia}
            >
              下一页
            </Button>
          </div>
        </div>
      )}
    </div>
  );
} 