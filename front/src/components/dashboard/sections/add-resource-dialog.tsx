'use client';

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import {
  Search,
  FileText,
  Video,
  Music,
  Image as ImageIcon,
  File,
  Loader2,
  Plus,
  Upload,
  ArrowRight,
  X
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogFooter, 
  DialogHeader, 
  DialogTitle, 
  DialogTrigger 
} from '@/components/ui/dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Select, 
  SelectContent, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { cn } from '@/lib/utils';

import { mediaService } from '@/services/media-service';
import sectionService from '@/services/section';
import { Empty } from '@/components/ui/empty';

// 媒体类型映射
const mediaTypeIcons = {
  'VIDEO': <Video className="h-5 w-5" />,
  'AUDIO': <Music className="h-5 w-5" />,
  'DOCUMENT': <FileText className="h-5 w-5" />,
  'IMAGE': <ImageIcon className="h-5 w-5" />,
  'OTHER': <File className="h-5 w-5" />,
};

// 资源类型选项
const resourceTypes = [
  { value: 'primary', label: '主要资源', description: '课程的主要内容' },
  { value: 'supplementary', label: '补充资源', description: '课程的补充材料' },
  { value: 'homework', label: '作业', description: '学习任务和作业' },
  { value: 'reference', label: '参考资料', description: '扩展阅读和参考材料' },
];

// 转换文件大小为可读格式
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  
  return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${sizes[i]}`;
};

interface AddResourceDialogProps {
  sectionId: number;
  onResourceAdded?: () => void;
  trigger?: React.ReactNode;
}

export function AddResourceDialog({ sectionId, onResourceAdded, trigger }: AddResourceDialogProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [mediaList, setMediaList] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTab, setSelectedTab] = useState('all');
  const [selectedMedia, setSelectedMedia] = useState<any | null>(null);
  const [selectedResourceType, setSelectedResourceType] = useState('primary');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // 加载媒体列表
  const loadMediaList = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      // 构建查询参数
      const params: any = {
        page: currentPage,
        size: 12
      };
      
      // 添加搜索条件
      if (searchQuery) {
        params.search = searchQuery;
      }
      
      // 添加媒体类型过滤
      if (selectedTab !== 'all') {
        params.type = selectedTab;
      }
      
      // 调用API获取媒体列表
      const response = await mediaService.getMediaList(params);
      
      if (response && response.data) {
        setMediaList(response.data.content || []);
        setTotalPages(response.data.totalPages || 0);
      } else {
        setMediaList([]);
        setTotalPages(0);
      }
    } catch (err: any) {
      console.error('加载媒体列表失败:', err);
      setError(err.message || '无法加载媒体列表');
    } finally {
      setIsLoading(false);
    }
  };
  
  // 选择媒体资源
  const handleSelectMedia = (media: any) => {
    setSelectedMedia(media.id === selectedMedia?.id ? null : media);
  };
  
  // 添加资源到小节
  const handleAddResource = async () => {
    if (!selectedMedia || !selectedResourceType) {
      toast.error('请选择媒体资源和资源类型');
      return;
    }
    
    try {
      setIsSubmitting(true);
      setError(null);
      
      // 构建资源对象
      const resourceData = {
        sectionId,
        mediaId: selectedMedia.id,
        resourceType: selectedResourceType
      };
      
      // 调用API添加资源
      await sectionService.addSectionResource(resourceData);
      
      toast.success('资源添加成功', {
        description: '媒体资源已成功添加到小节'
      });
      
      // 重置表单
      setSelectedMedia(null);
      
      // 通知父组件刷新
      if (onResourceAdded) {
        onResourceAdded();
      }
      
      // 关闭对话框
      setIsOpen(false);
    } catch (err: any) {
      console.error('添加资源失败:', err);
      setError(err.message || '添加资源失败');
      toast.error('添加资源失败', {
        description: err.message || '请稍后重试'
      });
    } finally {
      setIsSubmitting(false);
    }
  };
  
  // 当对话框打开时加载媒体列表
  useEffect(() => {
    if (isOpen) {
      loadMediaList();
    }
  }, [isOpen, currentPage, selectedTab, searchQuery]);
  
  // 媒体项目组件
  const MediaItem = ({ media }: { media: any }) => {
    const isSelected = selectedMedia?.id === media.id;
    
    return (
      <div 
        className={cn(
          'border rounded-md p-3 cursor-pointer transition-all hover:border-primary',
          isSelected ? 'border-primary ring-1 ring-primary' : ''
        )}
        onClick={() => handleSelectMedia(media)}
      >
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-md">
            {mediaTypeIcons[media.type as keyof typeof mediaTypeIcons] || <File className="h-5 w-5" />}
          </div>
          
          <div className="flex-1 min-w-0">
            <h4 className="font-medium text-sm truncate">{media.title}</h4>
            <div className="flex flex-col mt-1">
              <span className="text-xs text-muted-foreground">
                {media.type} • {formatFileSize(media.size)}
              </span>
            </div>
          </div>
          
          {isSelected && (
            <div className="w-5 h-5 rounded-full bg-primary flex items-center justify-center text-primary-foreground">
              <Plus className="h-3 w-3" />
            </div>
          )}
        </div>
      </div>
    );
  };
  
  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      {trigger ? (
        <DialogTrigger asChild>
          {trigger}
        </DialogTrigger>
      ) : (
        <DialogTrigger asChild>
          <Button>
            <Plus className="h-4 w-4 mr-2" />
            添加资源
          </Button>
        </DialogTrigger>
      )}
      
      <DialogContent className="sm:max-w-[700px] max-h-[85vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>添加媒体资源</DialogTitle>
          <DialogDescription>
            从媒体库中选择资源添加到小节
          </DialogDescription>
        </DialogHeader>
        
        {/* 标签页和搜索框 */}
        <div className="flex flex-col gap-4 my-4">
          <div className="flex items-center gap-4">
            <Tabs defaultValue="all" value={selectedTab} onValueChange={setSelectedTab} className="flex-1">
              <TabsList className="grid grid-cols-5">
                <TabsTrigger value="all">全部</TabsTrigger>
                <TabsTrigger value="VIDEO">视频</TabsTrigger>
                <TabsTrigger value="AUDIO">音频</TabsTrigger>
                <TabsTrigger value="DOCUMENT">文档</TabsTrigger>
                <TabsTrigger value="IMAGE">图片</TabsTrigger>
              </TabsList>
            </Tabs>
            
            <div className="relative">
              <Search className="absolute left-2 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索资源..."
                className="pl-8 w-60"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    loadMediaList();
                  }
                }}
              />
            </div>
          </div>
        </div>
        
        {/* 媒体列表 */}
        <div className="flex-1 overflow-hidden">
          <ScrollArea className="h-[300px] pr-4">
            {isLoading ? (
              <div className="flex items-center justify-center h-full">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <span className="ml-2 text-lg">加载中...</span>
              </div>
            ) : error ? (
              <div className="text-center py-8 text-destructive">
                <p>{error}</p>
                <Button variant="outline" onClick={loadMediaList} className="mt-2">
                  重试
                </Button>
              </div>
            ) : mediaList.length === 0 ? (
              <Empty
                icon={<Upload className="h-10 w-10" />}
                title="没有找到资源"
                description="尝试使用其他搜索条件，或前往媒体库上传新资源"
                action={
                  <Button variant="outline" asChild>
                    <a href="/dashboard/media" target="_blank" rel="noopener noreferrer">
                      转到媒体库
                      <ArrowRight className="ml-2 h-4 w-4" />
                    </a>
                  </Button>
                }
              />
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {mediaList.map((media) => (
                  <MediaItem key={media.id} media={media} />
                ))}
              </div>
            )}
          </ScrollArea>
        </div>
        
        {/* 分页按钮 */}
        {mediaList.length > 0 && (
          <div className="flex justify-between items-center py-2">
            <div className="text-sm text-muted-foreground">
              显示 {mediaList.length} 个结果
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0 || isLoading}
              >
                上一页
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={currentPage >= totalPages - 1 || isLoading}
              >
                下一页
              </Button>
            </div>
          </div>
        )}
        
        {/* 资源类型选择器 */}
        {selectedMedia && (
          <div className="border-t pt-4 mt-4">
            <div className="flex items-center justify-between">
              <div className="flex flex-col">
                <span className="text-sm font-medium">已选择资源</span>
                <span className="text-xs text-muted-foreground">
                  {selectedMedia.title}
                </span>
              </div>
              
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setSelectedMedia(null)}
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
            
            <div className="mt-4">
              <Label htmlFor="resource-type">资源类型</Label>
              <Select
                value={selectedResourceType}
                onValueChange={setSelectedResourceType}
              >
                <SelectTrigger id="resource-type">
                  <SelectValue placeholder="选择资源类型" />
                </SelectTrigger>
                <SelectContent>
                  {resourceTypes.map((type) => (
                    <SelectItem key={type.value} value={type.value}>
                      <div className="flex flex-col">
                        <span>{type.label}</span>
                        <span className="text-xs text-muted-foreground">
                          {type.description}
                        </span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        )}
        
        <DialogFooter className="pt-4">
          <Button variant="outline" onClick={() => setIsOpen(false)}>
            取消
          </Button>
          <Button 
            onClick={handleAddResource} 
            disabled={!selectedMedia || !selectedResourceType || isSubmitting}
          >
            {isSubmitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                添加中...
              </>
            ) : (
              <>
                <Plus className="mr-2 h-4 w-4" />
                添加资源
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
} 