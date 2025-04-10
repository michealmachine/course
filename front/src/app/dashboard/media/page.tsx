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
  Play,
  Calendar,
  BarChart,
  CalendarDays,
  ChevronsUpDown,
  X,
  Search
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
import { mediaService, MediaStatus, MediaQueryParams } from '@/services/media-service';
import { mediaActivityService } from '@/services/media-activity-service';
import { MediaActivityDTO, MediaActivityCalendarVO } from '@/types/media-activity';
import { Badge } from '@/components/ui/badge';
import { DatePicker } from '@/components/ui/date-picker';
import { HoverCard, HoverCardContent, HoverCardTrigger } from '@/components/ui/hover-card';
import { format, parseISO, isEqual, isWithinInterval, subDays, isSameDay } from 'date-fns';
import { cn } from '@/lib/utils';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Textarea } from '@/components/ui/textarea';
import { Progress } from '@/components/ui/progress';
import { MediaType } from '@/types/media';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';

// 媒体类型
const MEDIA_TYPES = {
  VIDEO: 'VIDEO',
  AUDIO: 'AUDIO',
  IMAGE: 'IMAGE',
  DOCUMENT: 'DOCUMENT'
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
  type: string; // 'VIDEO', 'AUDIO', 'IMAGE', 'DOCUMENT'
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

// 添加Media接口定义
interface Media {
  id: number;
  title: string;
  description?: string;
  originalFilename: string;
  type: string;
  size: number;
  status: string;
  uploadTime: string;
  accessUrl?: string;
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 获取媒体类型名称
const getMediaTypeName = (type: MediaType | string): string => {
  switch (type) {
    case MediaType.VIDEO:
      return '视频';
    case MediaType.AUDIO:
      return '音频';
    case MediaType.IMAGE:
      return '图片';
    case MediaType.DOCUMENT:
      return '文档';
    default:
      return '未知';
  }
};

// 格式化日期
const formatDate = (dateStr: string): string => {
  try {
    const date = new Date(dateStr);
    return format(date, 'yyyy-MM-dd HH:mm');
  } catch (error) {
    return dateStr;
  }
};

// 热图颜色计算
const getHeatColor = (count: number, maxCount: number): string => {
  if (count === 0) return 'bg-gray-100';
  
  const intensity = Math.min(Math.ceil((count / maxCount) * 5), 5);
  
  switch (intensity) {
    case 1: return 'bg-blue-100';
    case 2: return 'bg-blue-200';
    case 3: return 'bg-blue-300';
    case 4: return 'bg-blue-400';
    case 5: return 'bg-blue-500';
    default: return 'bg-gray-100';
  }
};

// 活动热图组件
const ActivityHeatmap = ({ 
  calendarData, 
  startDate, 
  endDate,
  selectedDate,
  onDateSelect
}: {
  calendarData: MediaActivityCalendarVO | null;
  startDate?: Date;
  endDate?: Date;
  selectedDate?: Date;
  onDateSelect: (date: Date | undefined) => void;
}) => {
  if (!calendarData || !calendarData.calendarData || calendarData.calendarData.length === 0) {
    return (
      <div className="p-4 text-center">
        <p className="text-muted-foreground">该时间段内没有媒体活动数据</p>
      </div>
    );
  }

  // 创建日期映射
  const dateMap = new Map<string, MediaActivityDTO>();
  calendarData.calendarData.forEach(item => {
    dateMap.set(item.date, item);
  });

  // 生成日期区间内的所有日期
  const dateRange: Date[] = [];
  if (startDate && endDate) {
    let currentDate = new Date(startDate);
    while (currentDate <= endDate) {
      dateRange.push(new Date(currentDate));
      currentDate.setDate(currentDate.getDate() + 1);
    }
  }

  // 计算列和行
  const daysPerRow = 7;
  const totalRows = Math.ceil(dateRange.length / daysPerRow);
  const rows = Array.from({ length: totalRows }, (_, rowIndex) => {
    return dateRange.slice(rowIndex * daysPerRow, (rowIndex + 1) * daysPerRow);
  });

  return (
    <div className="p-4">
      <div className="mb-4">
        <p className="text-sm text-muted-foreground mb-2">活跃程度</p>
        <div className="flex items-center gap-1">
          {[0, 1, 2, 3, 4, 5].map(level => (
            <div 
              key={level} 
              className={`w-4 h-4 rounded ${level === 0 ? 'bg-gray-100' : `bg-blue-${level * 100}`}`} 
              title={level === 0 ? '无活动' : `活动等级 ${level}`}
            />
          ))}
        </div>
      </div>
      
      <div className="grid gap-1">
        {rows.map((week, weekIndex) => (
          <div key={weekIndex} className="flex gap-1">
            {week.map(date => {
              const dateStr = format(date, 'yyyy-MM-dd');
              const activity = dateMap.get(dateStr);
              const count = activity?.count || 0;
              const isSelectedDate = selectedDate && isSameDay(date, selectedDate);
              const dayColor = getHeatColor(count, calendarData.peakCount);
              
              return (
                <HoverCard key={dateStr} openDelay={200}>
                  <HoverCardTrigger asChild>
                    <button
                      onClick={() => onDateSelect(date)}
                      className={cn(
                        'w-6 h-6 rounded-sm transition-colors',
                        dayColor,
                        isSelectedDate && 'ring-2 ring-primary'
                      )}
                      disabled={count === 0}
                    />
                  </HoverCardTrigger>
                  <HoverCardContent className="w-64 p-2">
                    <div className="space-y-1">
                      <p className="text-sm font-medium">{format(date, 'yyyy年MM月dd日')}</p>
                      {activity ? (
                        <>
                          <p className="text-xs">上传文件: {activity.count} 个</p>
                          <p className="text-xs">总大小: {formatFileSize(activity.totalSize)}</p>
                        </>
                      ) : (
                        <p className="text-xs text-muted-foreground">无活动</p>
                      )}
                    </div>
                  </HoverCardContent>
                </HoverCard>
              );
            })}
          </div>
        ))}
      </div>
      
      {/* 如果有最活跃日期，显示信息但不自动选择 */}
      {calendarData.mostActiveDate && (
        <div className="mt-4 text-sm text-muted-foreground">
          <p>最活跃日期: {format(parseISO(calendarData.mostActiveDate), 'yyyy年MM月dd日')}</p>
          <p>总上传数量: {calendarData.totalCount} 个文件</p>
          <p>总大小: {formatFileSize(calendarData.totalSize)}</p>
        </div>
      )}

      {/* 如果已经选择了日期，提供清除选择的按钮 */}
      {selectedDate && (
        <div className="mt-4">
          <Button 
            variant="outline" 
            size="sm" 
            className="w-full"
            onClick={() => onDateSelect(undefined)}
          >
            清除日期选择
          </Button>
        </div>
      )}
    </div>
  );
};

// 媒体卡片组件
const MediaCard = ({ item, onView, onDelete }: { 
  item: MediaItem, 
  onView: () => void, 
  onDelete: () => void
}) => {
  return (
    <Card className="overflow-hidden h-full flex flex-col">
      <div className="aspect-video bg-muted relative">
        {item.type === MediaType.VIDEO && (
          <div className="w-full h-full flex items-center justify-center">
            <FileVideo className="h-12 w-12 text-muted-foreground/50" />
          </div>
        )}
        {item.type === MediaType.AUDIO && (
          <div className="w-full h-full flex items-center justify-center">
            <FileAudio className="h-12 w-12 text-muted-foreground/50" />
          </div>
        )}
        {item.type === MediaType.IMAGE && (
          <div className="w-full h-full flex items-center justify-center">
            <FileImage className="h-12 w-12 text-muted-foreground/50" />
          </div>
        )}
        {item.type === MediaType.DOCUMENT && (
          <div className="w-full h-full flex items-center justify-center">
            <FileText className="h-12 w-12 text-muted-foreground/50" />
          </div>
        )}
        <Button 
          variant="secondary" 
          size="icon" 
          className="absolute top-2 right-2" 
          onClick={(e) => {
            e.stopPropagation();
            onView();
          }}
        >
          <Play className="h-4 w-4" />
        </Button>
      </div>
      <CardHeader className="p-4">
        <CardTitle className="text-base truncate">{item.title}</CardTitle>
        <CardDescription className="truncate text-xs">{item.originalFilename}</CardDescription>
      </CardHeader>
      <CardContent className="p-4 pt-0 flex-grow">
        <div className="flex flex-wrap gap-2 text-xs">
          <Badge variant="outline">{getMediaTypeName(item.type as MediaType)}</Badge>
          <Badge variant="outline">{formatFileSize(item.size)}</Badge>
        </div>
      </CardContent>
      <CardFooter className="p-4 pt-0 flex justify-between">
        <span className="text-xs text-muted-foreground">{formatDate(item.uploadTime)}</span>
        <Button 
          variant="ghost" 
          size="icon" 
          onClick={(e) => {
            e.stopPropagation();
            onDelete();
          }}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </CardFooter>
    </Card>
  );
};

// 添加状态徽章获取函数
const getStatusBadge = (status: string) => {
  switch (status) {
    case 'READY':
      return <Badge variant="outline" className="bg-green-50 text-green-700 hover:bg-green-100 dark:bg-green-950/20 dark:text-green-400">可用</Badge>;
    case 'PROCESSING':
      return <Badge variant="outline" className="bg-yellow-50 text-yellow-700 hover:bg-yellow-100 dark:bg-yellow-950/20 dark:text-yellow-400">处理中</Badge>;
    case 'ERROR':
      return <Badge variant="outline" className="bg-red-50 text-red-700 hover:bg-red-100 dark:bg-red-950/20 dark:text-red-400">错误</Badge>;
    case 'UPLOADING':
      return <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-100 dark:bg-blue-950/20 dark:text-blue-400">上传中</Badge>;
    default:
      return <Badge variant="outline">未知</Badge>;
  }
};

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
  
  // 活动热图相关状态
  const [startDate, setStartDate] = useState<Date | undefined>(subDays(new Date(), 30));
  const [endDate, setEndDate] = useState<Date | undefined>(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(undefined);
  const [calendarData, setCalendarData] = useState<MediaActivityCalendarVO | null>(null);
  const [isActivityLoading, setIsActivityLoading] = useState<boolean>(false);
  
  // 搜索相关状态
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [isSearching, setIsSearching] = useState(false);
  
  // 加载媒体列表
  const fetchMediaList = async () => {
    try {
      setIsLoading(true);
      // 构建查询参数
      const queryParams: MediaQueryParams = {
        page: pagination.page,
        size: pagination.size
      };
      
      // 如果不是"全部"，则添加类型筛选条件
      if (activeTab !== 'all') {
        // 确保类型参数正确设置
        queryParams.type = activeTab as MediaType;
        console.log(`筛选媒体类型: ${activeTab}`);
      }
      
      // 如果有搜索关键词，添加文件名筛选条件
      if (searchKeyword && searchKeyword.trim() !== '') {
        queryParams.filename = searchKeyword.trim();
        console.log(`搜索文件名: ${searchKeyword}`);
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
              setIsLoading(false);
              return;
            }
          }
        }
        
        // 更新UI
        setMediaList(items);
        setTotalItems(total);
        
        // 计算各类型的数量
        const stats: Record<string, number> = {
          'all': total,
          [MediaType.VIDEO]: 0,
          [MediaType.AUDIO]: 0,
          [MediaType.IMAGE]: 0,
          [MediaType.DOCUMENT]: 0
        };
        
        items.forEach(item => {
          const type = item.type as MediaType;
          if (stats[type] !== undefined) {
            stats[type]++;
          }
        });
        
        setTypeStats(stats);
        
        // 为媒体类型预加载访问URL
        items.forEach(item => {
          if (!item.accessUrl) {
            preloadMediaAccessUrl(item.id);
          }
        });
      } else {
        console.warn('获取媒体列表返回无数据');
        setMediaList([]);
        setTotalItems(0);
        
        // 重置类型统计
        setTypeStats({
          'all': 0,
          [MediaType.VIDEO]: 0,
          [MediaType.AUDIO]: 0,
          [MediaType.IMAGE]: 0,
          [MediaType.DOCUMENT]: 0
        });
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
  
  // 获取媒体活动数据
  const fetchActivityData = async () => {
    try {
      if (!startDate || !endDate) return;
      
      setIsActivityLoading(true);
      const formatDate = (date: Date) => {
        return format(date, 'yyyy-MM-dd');
      };
      
      const data = await mediaActivityService.getMediaActivityCalendar(
        formatDate(startDate),
        formatDate(endDate)
      );
      
      setCalendarData(data);
      
      // 移除自动选择最活跃日期的逻辑
      // 如果手动清除了选择，则不应该自动选择回来
      // 只有在用户明确点击热力图上的日期时才选择日期
      
    } catch (error) {
      console.error('加载媒体活动数据失败:', error);
      toast.error('加载媒体活动数据失败');
    } finally {
      setIsActivityLoading(false);
    }
  };
  
  // 根据日期加载媒体列表
  const loadMediaByDate = async (date: Date) => {
    try {
      setIsLoading(true);
      const formatDate = (date: Date) => {
        return format(date, 'yyyy-MM-dd');
      };
      
      const page = pagination.page !== undefined ? pagination.page + 1 : 1; // MediaActivity API 使用1-based索引
      const size = pagination.size || 10;
      
      const data = await mediaActivityService.getMediaListByDate(
        formatDate(date),
        page,
        size
      );
      
      setMediaList(data.content);
      setTotalItems(data.totalElements);
      
      // 添加：预加载媒体访问URL
      if (data.content && data.content.length > 0) {
        data.content.forEach(item => {
          if (!item.accessUrl) {
            preloadMediaAccessUrl(item.id);
          }
        });
      }
    } catch (error) {
      console.error('加载指定日期媒体列表失败:', error);
      toast.error('加载指定日期媒体列表失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 日期选择处理
  const handleDateSelect = (date: Date | undefined) => {
    // 更新选中的日期状态
    setSelectedDate(date);
    
    if (date) {
      // 如果选择了日期，则加载该日期的媒体列表
      loadMediaByDate(date);
    } else {
      // 如果清除了日期选择，则重新加载默认的媒体列表（不按日期筛选）
      fetchMediaList();
    }
  };

  // 初始加载
  useEffect(() => {
    // 首先获取一般的媒体列表，不按日期筛选
    fetchMediaList();
    
    // 然后再获取热力图数据，但不自动选择日期
    fetchActivityData();
  }, [pagination, activeTab]);

  // 日期范围变化时重新加载活动数据
  useEffect(() => {
    if (startDate && endDate) {
      fetchActivityData();
    }
  }, [startDate, endDate]);
  
  // 删除媒体
  const handleDeleteMedia = (id: number) => {
    if (confirm('确定要删除这个文件吗？此操作无法撤销。')) {
      mediaService.deleteMedia(id).then(response => {
        toast.success('删除成功');
        if (selectedDate) {
          loadMediaByDate(selectedDate);
        } else {
          fetchMediaList();
        }
      }).catch(error => {
        console.error('删除媒体失败:', error);
        toast.error('删除媒体失败');
      });
    }
  };
  
  // 处理搜索
  const handleSearch = () => {
    console.log(`执行搜索，关键词: ${searchKeyword}`);
    setIsSearching(true);
    // 重置分页并获取数据
    setPagination({...pagination, page: 0});
    fetchMediaList();
  };

  // 清除搜索
  const handleClearSearch = () => {
    setSearchKeyword('');
    setIsSearching(false);
    fetchMediaList();
  };

  // 处理搜索框按键事件（回车搜索）
  const handleSearchKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };
  
  // 处理标签切换
  const handleTabChange = (value: string) => {
    // 清除可能的搜索条件
    if (isSearching) {
      setSearchKeyword('');
      setIsSearching(false);
    }
    
    setActiveTab(value);
    setPagination({...pagination, page: 0});
    
    if (value !== activeTab) {
      console.log(`切换标签到: ${value}`);
    }
  };

  // 分页变更处理函数
  const handlePageChange = (newPage: number) => {
    const currentPage = pagination.page ?? 0;
    const totalPages = Math.ceil(totalItems / (pagination.size ?? 10));
    
    if (newPage >= 0 && newPage < totalPages) {
      setPagination({ ...pagination, page: newPage });
    }
  };

  // 查看媒体详情
  const viewMediaDetail = (media: MediaItem) => {
    if (!media || !media.id) return;
    
    router.push(`/dashboard/media/${media.id}`);
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
        return response.data.accessUrl;
      }
      return null;
    } catch (error) {
      console.error('预加载媒体访问URL失败:', error);
      return null;
    }
  };
  
  // 处理上传
  const handleUpload = async () => {
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
          initResponse.data.presignedUrls,
          selectedFile,
          initResponse.data.totalParts,
          initResponse.data.chunkSize
        );
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

  // 上传分片
  const uploadParts = async (
    mediaId: number,
    uploadId: string,
    presignedUrls: Array<{partNumber: number, url: string}>,
    file: File,
    totalParts: number,
    chunkSize: number
  ) => {
    try {
      console.log(`开始分片上传 - 总分片数: ${totalParts}, 文件大小: ${file.size}`);
      
      // 用于保存已完成分片的信息
      const completedParts: Array<{partNumber: number, etag: string}> = [];
      
      // 依次上传分片，使用for循环而非并发，避免并发上传可能导致的问题
      for (let i = 0; i < presignedUrls.length; i++) {
        const { partNumber, url } = presignedUrls[i];
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
    const fileInput = document.getElementById('mediaFile') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  };

  return (
    <div className="container mx-auto py-6 space-y-6">
      <div className="flex flex-col space-y-4">
        <div className="flex justify-between items-center">
          <div>
            <h2 className="text-3xl font-bold tracking-tight">媒体管理</h2>
            <p className="text-muted-foreground">
              管理和查看您的媒体文件
            </p>
          </div>
          
          <div className="flex items-center space-x-2">
            <div className="relative">
              <Input
                type="text"
                placeholder="搜索文件名..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyDown={handleSearchKeyDown}
                className="w-64 pr-8"
              />
              {isSearching && (
                <Button
                  variant="ghost"
                  size="icon"
                  className="absolute right-8 top-0 h-full"
                  onClick={handleClearSearch}
                >
                  <X className="h-4 w-4" />
                </Button>
              )}
              <Button 
                variant="ghost" 
                size="icon" 
                className="absolute right-0 top-0 h-full"
                onClick={handleSearch}
              >
                <Search className="h-4 w-4" />
              </Button>
            </div>
            
            <Dialog open={uploadDialogOpen} onOpenChange={setUploadDialogOpen}>
              <DialogTrigger asChild>
                <Button>
                  <Upload className="mr-2 h-4 w-4" />
                  上传媒体
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>上传媒体文件</DialogTitle>
                  <DialogDescription>
                    上传媒体文件到您的机构。支持视频、音频、图片和文档等多种格式。
                  </DialogDescription>
                </DialogHeader>
                
                <div className="grid gap-4 py-4">
                  <div className="grid gap-2">
                    <Label htmlFor="title">标题</Label>
                    <Input
                      id="title"
                      value={uploadTitle}
                      onChange={(e) => setUploadTitle(e.target.value)}
                      placeholder="请输入媒体标题"
                    />
                  </div>
                  
                  <div className="grid gap-2">
                    <Label htmlFor="description">描述</Label>
                    <Textarea
                      id="description"
                      value={uploadDescription}
                      onChange={(e) => setUploadDescription(e.target.value)}
                      placeholder="请输入媒体描述（可选）"
                    />
                  </div>
                  
                  <div className="grid gap-2">
                    <Label htmlFor="mediaFile">选择文件</Label>
                    <Input
                      id="mediaFile"
                      type="file"
                      onChange={(e) => {
                        if (e.target.files && e.target.files.length > 0) {
                          const file = e.target.files[0];
                          setSelectedFile(file);
                          if (!uploadTitle) {
                            setUploadTitle(file.name);
                          }
                        }
                      }}
                    />
                  </div>
                  
                  {isUploading && (
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-sm">上传进度</span>
                        <span className="text-sm">{uploadProgress}%</span>
                      </div>
                      <Progress value={uploadProgress} />
                    </div>
                  )}
                </div>
                
                <DialogFooter>
                  <Button 
                    variant="outline" 
                    onClick={() => {
                      if (isUploading && uploadingMediaId) {
                        cancelUpload();
                      } else {
                        setUploadDialogOpen(false);
                        setUploadTitle('');
                        setUploadDescription('');
                        setSelectedFile(null);
                        setUploadProgress(0);
                      }
                    }}
                    disabled={isUploading && !uploadingMediaId}
                  >
                    {isUploading ? '取消上传' : '取消'}
                  </Button>
                  <Button 
                    onClick={handleUpload} 
                    disabled={!selectedFile || isUploading}
                  >
                    {isUploading ? '上传中...' : '上传'}
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
        </div>
      </div>

      {/* 类型筛选标签 */}
      <Tabs defaultValue={activeTab} onValueChange={handleTabChange} className="w-full">
        <TabsList className="mb-4">
          <TabsTrigger value="all" className="flex items-center gap-2">
            <FileText className="h-4 w-4" /> 全部
          </TabsTrigger>
          <TabsTrigger value={MediaType.VIDEO} className="flex items-center gap-2">
            <Video className="h-4 w-4" /> 视频
          </TabsTrigger>
          <TabsTrigger value={MediaType.AUDIO} className="flex items-center gap-2">
            <Music className="h-4 w-4" /> 音频
          </TabsTrigger>
          <TabsTrigger value={MediaType.IMAGE} className="flex items-center gap-2">
            <Image className="h-4 w-4" /> 图片
          </TabsTrigger>
          <TabsTrigger value={MediaType.DOCUMENT} className="flex items-center gap-2">
            <FileText className="h-4 w-4" /> 文档
          </TabsTrigger>
        </TabsList>
      </Tabs>

      {/* 集成热力图和列表视图 */}
      <div className="grid grid-cols-1 lg:grid-cols-[350px_1fr] gap-6">
        {/* 左侧热力图区域 */}
        <Card>
          <CardHeader>
            <CardTitle>上传活动热图</CardTitle>
            <CardDescription>按日期查看媒体文件上传活动情况</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-col space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="flex flex-col space-y-2">
                  <Label htmlFor="startDate">开始日期</Label>
                  <DatePicker
                    date={startDate}
                    setDate={setStartDate}
                  />
                </div>
                <div className="flex flex-col space-y-2">
                  <Label htmlFor="endDate">结束日期</Label>
                  <DatePicker
                    date={endDate}
                    setDate={setEndDate}
                  />
                </div>
              </div>
              
              {isActivityLoading ? (
                <div className="p-4">
                  <Skeleton className="h-[200px] w-full" />
                </div>
              ) : (
                <ActivityHeatmap
                  calendarData={calendarData}
                  startDate={startDate}
                  endDate={endDate}
                  selectedDate={selectedDate}
                  onDateSelect={handleDateSelect}
                />
              )}
            </div>
          </CardContent>
        </Card>

        {/* 右侧媒体列表 */}
        <div className="space-y-4">
          <Card>
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>
                    {selectedDate 
                      ? `${format(selectedDate, 'yyyy年MM月dd日')}的上传文件` 
                      : activeTab === 'all' 
                        ? '全部媒体文件' 
                        : `${getMediaTypeName(activeTab as MediaType)}文件`}
                  </CardTitle>
                  <CardDescription>
                    {selectedDate 
                      ? `当日上传: ${totalItems > 0 ? `共 ${totalItems} 个文件` : '暂无文件'}`
                      : `${totalItems > 0 ? `共 ${totalItems} 个文件` : '暂无文件'}`}
                  </CardDescription>
                </div>
                {selectedDate && (
                  <Button variant="outline" size="sm" onClick={() => handleDateSelect(undefined)}>
                    <X className="h-4 w-4 mr-1" />
                    清除日期筛选
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-[40px]"></TableHead>
                      <TableHead>名称</TableHead>
                      <TableHead>类型</TableHead>
                      <TableHead>大小</TableHead>
                      <TableHead>上传时间</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {isLoading ? (
                      // 加载中骨架屏
                      Array.from({ length: 6 }).map((_, index) => (
                        <TableRow key={index}>
                          <TableCell><Skeleton className="h-10 w-10 rounded-full" /></TableCell>
                          <TableCell>
                            <div className="space-y-2">
                              <Skeleton className="h-4 w-[250px]" />
                              <Skeleton className="h-3 w-[200px]" />
                            </div>
                          </TableCell>
                          <TableCell><Skeleton className="h-4 w-[70px]" /></TableCell>
                          <TableCell><Skeleton className="h-4 w-[60px]" /></TableCell>
                          <TableCell><Skeleton className="h-4 w-[120px]" /></TableCell>
                          <TableCell className="text-right"><Skeleton className="h-8 w-[80px] ml-auto" /></TableCell>
                        </TableRow>
                      ))
                    ) : mediaList.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} className="h-24 text-center">
                          <div className="flex flex-col items-center justify-center">
                            <p className="text-muted-foreground">没有找到媒体文件</p>
                            <Button onClick={() => setUploadDialogOpen(true)} variant="outline" className="mt-2">
                              <Upload className="mr-2 h-4 w-4" /> 上传媒体
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ) : (
                      // 媒体列表
                      mediaList.map((item) => (
                        <TableRow 
                          key={item.id}
                          className="cursor-pointer hover:bg-accent/50"
                          onClick={() => viewMediaDetail(item)}
                        >
                          <TableCell>
                            <div className="relative h-10 w-10 rounded-full flex items-center justify-center">
                              <HoverCard>
                                <HoverCardTrigger asChild>
                                  <div className={cn(
                                    "h-10 w-10 rounded-full flex items-center justify-center",
                                    item.type === MediaType.VIDEO && "bg-blue-100 dark:bg-blue-950/50",
                                    item.type === MediaType.AUDIO && "bg-green-100 dark:bg-green-950/50",
                                    item.type === MediaType.IMAGE && "bg-purple-100 dark:bg-purple-950/50",
                                    item.type === MediaType.DOCUMENT && "bg-orange-100 dark:bg-orange-950/50"
                                  )}>
                                    {item.type === MediaType.VIDEO && <FileVideo className="h-5 w-5 text-blue-600 dark:text-blue-400" />}
                                    {item.type === MediaType.AUDIO && <FileAudio className="h-5 w-5 text-green-600 dark:text-green-400" />}
                                    {item.type === MediaType.IMAGE && <FileImage className="h-5 w-5 text-purple-600 dark:text-purple-400" />}
                                    {item.type === MediaType.DOCUMENT && <FileText className="h-5 w-5 text-orange-600 dark:text-orange-400" />}
                                  </div>
                                </HoverCardTrigger>
                                <HoverCardContent className="w-80 p-0">
                                  {item.type === MediaType.IMAGE && item.accessUrl ? (
                                    <div className="relative">
                                      <img 
                                        src={item.accessUrl} 
                                        alt={item.title} 
                                        className="w-full h-40 object-cover rounded-t-md"
                                        onError={(e) => {
                                          const target = e.target as HTMLImageElement;
                                          target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 24 24' fill='none' stroke='%23888' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='3' y='3' width='18' height='18' rx='2' ry='2'%3E%3C/rect%3E%3Ccircle cx='8.5' cy='8.5' r='1.5'%3E%3C/circle%3E%3Cpolyline points='21 15 16 10 5 21'%3E%3C/polyline%3E%3C/svg%3E";
                                          target.classList.add("p-8", "bg-muted");
                                        }}
                                      />
                                      <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex items-end">
                                        <div className="p-3 text-white w-full">
                                          <p className="font-medium truncate">{item.title}</p>
                                          <p className="text-xs text-white/80 truncate">{item.originalFilename}</p>
                                        </div>
                                      </div>
                                    </div>
                                  ) : item.type === MediaType.VIDEO && item.accessUrl ? (
                                    <div className="relative">
                                      <video
                                        src={item.accessUrl}
                                        controls
                                        className="w-full h-40 object-cover rounded-t-md"
                                        onError={(e) => {
                                          console.error('视频加载错误:', e);
                                          const target = e.target as HTMLVideoElement;
                                          target.parentElement?.classList.add("bg-blue-50", "dark:bg-blue-950/20");
                                          target.style.display = "none";
                                          // 显示错误信息
                                          const errorDiv = document.createElement('div');
                                          errorDiv.className = "w-full h-40 flex items-center justify-center";
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
                                          <p className="font-medium truncate">{item.title}</p>
                                          <p className="text-xs text-white/80 truncate">{item.originalFilename}</p>
                                        </div>
                                      </div>
                                    </div>
                                  ) : item.type === MediaType.AUDIO && item.accessUrl ? (
                                    <div className="p-4">
                                      <div className="w-full h-32 bg-green-50 dark:bg-green-950/20 rounded-md flex flex-col items-center justify-center mb-2">
                                        <FileAudio className="h-10 w-10 text-green-500 mb-2" />
                                        <p className="text-sm font-medium truncate">{item.title}</p>
                                      </div>
                                      <audio
                                        src={item.accessUrl}
                                        controls
                                        className="w-full"
                                        onError={(e) => {
                                          console.error('音频加载错误:', e);
                                          const target = e.target as HTMLAudioElement;
                                          target.style.display = "none";
                                          // 显示错误信息
                                          const errorDiv = document.createElement('div');
                                          errorDiv.className = "w-full text-center mt-2";
                                          errorDiv.innerHTML = `<p class="text-sm text-red-500">音频加载失败</p>`;
                                          target.parentElement?.appendChild(errorDiv);
                                        }}
                                      >
                                        您的浏览器不支持音频播放
                                      </audio>
                                    </div>
                                  ) : item.type === MediaType.DOCUMENT && item.accessUrl ? (
                                    <div className="relative">
                                      <div className="w-full h-40 bg-orange-50 dark:bg-orange-950/20 overflow-hidden rounded-t-md">
                                        <iframe 
                                          src={item.accessUrl} 
                                          className="w-full h-full border-0 scale-[0.6] origin-top-left"
                                          title={item.title}
                                          sandbox="allow-scripts allow-same-origin"
                                          onError={(e) => {
                                            console.error('文档加载错误:', e);
                                            // 显示错误信息
                                            const target = e.target as HTMLIFrameElement;
                                            if (target.parentElement) {
                                              target.style.display = "none";
                                              
                                              // 清空父元素内容
                                              while (target.parentElement.firstChild) {
                                                target.parentElement.removeChild(target.parentElement.firstChild);
                                              }
                                              
                                              // 创建错误提示元素
                                              const errorContainer = document.createElement('div');
                                              errorContainer.className = "w-full h-full flex items-center justify-center";
                                              
                                              const errorContent = document.createElement('div');
                                              errorContent.className = "flex flex-col items-center text-center";
                                              
                                              // 创建SVG图标
                                              const iconSvg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
                                              iconSvg.setAttribute("width", "40");
                                              iconSvg.setAttribute("height", "40");
                                              iconSvg.setAttribute("viewBox", "0 0 24 24");
                                              iconSvg.setAttribute("fill", "none");
                                              iconSvg.setAttribute("stroke", "currentColor");
                                              iconSvg.setAttribute("stroke-width", "2");
                                              iconSvg.setAttribute("stroke-linecap", "round");
                                              iconSvg.setAttribute("stroke-linejoin", "round");
                                              iconSvg.classList.add("text-orange-500", "mb-2");
                                              
                                              // 添加SVG路径
                                              const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
                                              path.setAttribute("d", "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z");
                                              iconSvg.appendChild(path);
                                              
                                              const polyline = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
                                              polyline.setAttribute("points", "14 2 14 8 20 8");
                                              iconSvg.appendChild(polyline);
                                              
                                              const line1 = document.createElementNS("http://www.w3.org/2000/svg", "line");
                                              line1.setAttribute("x1", "16");
                                              line1.setAttribute("y1", "13");
                                              line1.setAttribute("x2", "8");
                                              line1.setAttribute("y2", "13");
                                              iconSvg.appendChild(line1);
                                              
                                              const line2 = document.createElementNS("http://www.w3.org/2000/svg", "line");
                                              line2.setAttribute("x1", "16");
                                              line2.setAttribute("y1", "17");
                                              line2.setAttribute("x2", "8");
                                              line2.setAttribute("y2", "17");
                                              iconSvg.appendChild(line2);
                                              
                                              const line3 = document.createElementNS("http://www.w3.org/2000/svg", "line");
                                              line3.setAttribute("x1", "10");
                                              line3.setAttribute("y1", "9");
                                              line3.setAttribute("x2", "8");
                                              line3.setAttribute("y2", "9");
                                              iconSvg.appendChild(line3);
                                              
                                              errorContent.appendChild(iconSvg);
                                              
                                              // 创建错误文本
                                              const errorText = document.createElement('p');
                                              errorText.className = "text-sm text-muted-foreground";
                                              errorText.textContent = "文档加载失败";
                                              errorContent.appendChild(errorText);
                                              
                                              errorContainer.appendChild(errorContent);
                                              target.parentElement.appendChild(errorContainer);
                                            }
                                          }}
                                        />
                                        <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex items-end">
                                          <div className="p-3 text-white w-full">
                                            <p className="font-medium truncate">{item.title}</p>
                                            <p className="text-xs text-white/80 truncate">{item.originalFilename}</p>
                                          </div>
                                        </div>
                                      </div>
                                    </div>
                                  ) : (
                                    <div className={cn(
                                      "w-full h-40 rounded-t-md flex items-center justify-center",
                                      item.type === MediaType.VIDEO && "bg-blue-50 dark:bg-blue-950/20",
                                      item.type === MediaType.AUDIO && "bg-green-50 dark:bg-green-950/20",
                                      item.type === MediaType.IMAGE && "bg-purple-50 dark:bg-purple-950/20",
                                      item.type === MediaType.DOCUMENT && "bg-orange-50 dark:bg-orange-950/20"
                                    )}>
                                      {item.type === MediaType.VIDEO && <FileVideo className="h-16 w-16 text-blue-600 dark:text-blue-400" />}
                                      {item.type === MediaType.AUDIO && <FileAudio className="h-16 w-16 text-green-600 dark:text-green-400" />}
                                      {item.type === MediaType.IMAGE && <FileImage className="h-16 w-16 text-purple-600 dark:text-purple-400" />}
                                      {item.type === MediaType.DOCUMENT && <FileText className="h-16 w-16 text-orange-600 dark:text-orange-400" />}
                                    </div>
                                  )}
                                  <div className="p-3">
                                    <div className="grid grid-cols-2 gap-2 mb-2">
                                      <div>
                                        <p className="text-xs text-muted-foreground">类型</p>
                                        <p className="text-sm">{getMediaTypeName(item.type as MediaType)}</p>
                                      </div>
                                      <div>
                                        <p className="text-xs text-muted-foreground">大小</p>
                                        <p className="text-sm">{formatFileSize(item.size)}</p>
                                      </div>
                                      <div>
                                        <p className="text-xs text-muted-foreground">上传时间</p>
                                        <p className="text-sm">{formatDate(item.uploadTime)}</p>
                                      </div>
                                      <div>
                                        <p className="text-xs text-muted-foreground">状态</p>
                                        <div className="text-sm">{getStatusBadge(item.status)}</div>
                                      </div>
                                    </div>
                                    <Button 
                                      className="w-full mt-2"
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        viewMediaDetail(item);
                                      }}
                                    >
                                      查看详情
                                    </Button>
                                  </div>
                                </HoverCardContent>
                              </HoverCard>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div>
                              <p className="font-medium">{item.title}</p>
                              <p className="text-xs text-muted-foreground truncate max-w-56">{item.originalFilename}</p>
                            </div>
                          </TableCell>
                          <TableCell>
                            <Badge variant="outline" className={cn(
                              item.type === MediaType.VIDEO && "bg-blue-50 text-blue-700 hover:bg-blue-100 dark:bg-blue-950/20 dark:text-blue-400",
                              item.type === MediaType.AUDIO && "bg-green-50 text-green-700 hover:bg-green-100 dark:bg-green-950/20 dark:text-green-400",
                              item.type === MediaType.IMAGE && "bg-purple-50 text-purple-700 hover:bg-purple-100 dark:bg-purple-950/20 dark:text-purple-400",
                              item.type === MediaType.DOCUMENT && "bg-orange-50 text-orange-700 hover:bg-orange-100 dark:bg-orange-950/20 dark:text-orange-400"
                            )}>
                              {getMediaTypeName(item.type as MediaType)}
                            </Badge>
                          </TableCell>
                          <TableCell>{formatFileSize(item.size)}</TableCell>
                          <TableCell>{formatDate(item.uploadTime)}</TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end space-x-1">
                              <Button 
                                variant="ghost" 
                                size="icon"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  viewMediaDetail(item);
                                }}
                              >
                                <Play className="h-4 w-4" />
                              </Button>
                              <Button 
                                variant="ghost" 
                                size="icon"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleDeleteMedia(item.id);
                                }}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>
              
              {mediaList.length > 0 && (
                <div className="mt-4 flex items-center justify-center">
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious 
                          aria-disabled={(pagination.page ?? 0) === 0}
                          tabIndex={(pagination.page ?? 0) === 0 ? -1 : 0}
                          className={(pagination.page ?? 0) === 0 ? 'pointer-events-none opacity-50' : ''}
                          onClick={() => handlePageChange((pagination.page ?? 0) - 1)}
                        />
                      </PaginationItem>
                      {Array.from({ length: Math.min(5, Math.ceil(totalItems / (pagination.size ?? 10))) }).map((_, i) => {
                        const page = pagination.page ?? 0;
                        const size = pagination.size ?? 10;
                        let pageNum = i;
                        
                        if (Math.ceil(totalItems / size) > 5) {
                          if (page > 1) {
                            pageNum = page - 2 + i;
                          }
                          if (page > Math.ceil(totalItems / size) - 3) {
                            pageNum = Math.ceil(totalItems / size) - 5 + i;
                          }
                        }
                        
                        if (pageNum >= 0 && pageNum < Math.ceil(totalItems / size)) {
                          return (
                            <PaginationItem key={pageNum}>
                              <PaginationLink
                                isActive={pageNum === page}
                                onClick={() => handlePageChange(pageNum)}
                              >
                                {pageNum + 1}
                              </PaginationLink>
                            </PaginationItem>
                          );
                        }
                        return null;
                      })}
                      <PaginationItem>
                        <PaginationNext 
                          aria-disabled={(pagination.page ?? 0) >= Math.ceil(totalItems / (pagination.size ?? 10)) - 1}
                          tabIndex={(pagination.page ?? 0) >= Math.ceil(totalItems / (pagination.size ?? 10)) - 1 ? -1 : 0}
                          className={(pagination.page ?? 0) >= Math.ceil(totalItems / (pagination.size ?? 10)) - 1 ? 'pointer-events-none opacity-50' : ''}
                          onClick={() => handlePageChange((pagination.page ?? 0) + 1)}
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
} 