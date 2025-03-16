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
  X,
  ListChecks,
  ClipboardList
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
import { Badge } from '@/components/ui/badge';

import { mediaService } from '@/services/media-service';
import questionGroupService from '@/services/question-group';
import { sectionService } from '@/services/section';
import { Empty } from '@/components/ui/empty';
import { QuestionGroup } from '@/types/question';

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
  // 共享状态
  const [isOpen, setIsOpen] = useState(false);
  const [activeTab, setActiveTab] = useState('media'); // 'media' or 'questionGroup'
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 媒体资源状态
  const [mediaList, setMediaList] = useState<any[]>([]);
  const [isLoadingMedia, setIsLoadingMedia] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [mediaSearchQuery, setMediaSearchQuery] = useState('');
  const [selectedTab, setSelectedTab] = useState('all');
  const [selectedMedia, setSelectedMedia] = useState<any | null>(null);
  const [selectedResourceType, setSelectedResourceType] = useState('primary');
  
  // 题组状态
  const [questionGroups, setQuestionGroups] = useState<QuestionGroup[]>([]);
  const [isLoadingGroups, setIsLoadingGroups] = useState(false);
  const [groupSearchQuery, setGroupSearchQuery] = useState('');
  const [selectedQuestionGroup, setSelectedQuestionGroup] = useState<QuestionGroup | null>(null);
  
  // 加载媒体列表
  const loadMediaList = async () => {
    try {
      setIsLoadingMedia(true);
      setError(null);
      
      // 构建查询参数
      const params: any = {
        page: currentPage,
        size: 12
      };
      
      // 添加搜索条件
      if (mediaSearchQuery) {
        params.search = mediaSearchQuery;
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
      setIsLoadingMedia(false);
    }
  };
  
  // 加载题组列表
  const loadQuestionGroups = async () => {
    try {
      setIsLoadingGroups(true);
      setError(null);
      
      // 构建查询参数
      const params: any = {
        page: 0,
        pageSize: 20
      };
      
      // 添加搜索条件
      if (groupSearchQuery) {
        params.name = groupSearchQuery;
      }
      
      // 调用API获取题组列表
      const response = await questionGroupService.getQuestionGroupList(params);
      
      if (response && response.content) {
        setQuestionGroups(response.content || []);
      } else {
        setQuestionGroups([]);
      }
    } catch (err: any) {
      console.error('加载题组列表失败:', err);
      setError(err.message || '无法加载题组列表');
    } finally {
      setIsLoadingGroups(false);
    }
  };
  
  // 选择媒体资源
  const handleSelectMedia = (media: any) => {
    setSelectedMedia(media.id === selectedMedia?.id ? null : media);
    // 选择媒体时清除题组选择
    setSelectedQuestionGroup(null);
  };
  
  // 选择题组
  const handleSelectQuestionGroup = (group: QuestionGroup) => {
    setSelectedQuestionGroup(group.id === selectedQuestionGroup?.id ? null : group);
    // 选择题组时清除媒体选择
    setSelectedMedia(null);
  };
  
  // 添加资源到小节
  const handleAddResource = async () => {
    // 选择了媒体资源
    if (activeTab === 'media' && selectedMedia) {
      if (!selectedResourceType) {
        toast.error('请选择资源类型');
        return;
      }
      
      try {
        setIsSubmitting(true);
        setError(null);
        
        // 使用新的API添加资源
        await sectionService.setMediaResource(sectionId, selectedMedia.id, selectedResourceType);
        
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
    } 
    // 选择了题组
    else if (activeTab === 'questionGroup' && selectedQuestionGroup) {
      try {
        setIsSubmitting(true);
        setError(null);
        
        // 使用新的API添加题组
        await sectionService.setQuestionGroup(sectionId, selectedQuestionGroup.id);
        
        toast.success('题组添加成功', {
          description: '题组已成功添加到小节'
        });
        
        // 重置表单
        setSelectedQuestionGroup(null);
        
        // 通知父组件刷新
        if (onResourceAdded) {
          onResourceAdded();
        }
        
        // 关闭对话框
        setIsOpen(false);
      } catch (err: any) {
        console.error('添加题组失败:', err);
        setError(err.message || '添加题组失败');
        toast.error('添加题组失败', {
          description: err.message || '请稍后重试'
        });
      } finally {
        setIsSubmitting(false);
      }
    } else {
      toast.error('请选择要添加的内容', {
        description: activeTab === 'media' ? '请选择一个媒体资源' : '请选择一个题组'
      });
    }
  };
  
  // 当对话框打开或标签切换时加载相应数据
  useEffect(() => {
    if (isOpen) {
      if (activeTab === 'media') {
        loadMediaList();
      } else if (activeTab === 'questionGroup') {
        loadQuestionGroups();
      }
    }
  }, [isOpen, activeTab, currentPage, selectedTab, mediaSearchQuery]);
  
  // 当题组搜索查询变更时加载题组
  useEffect(() => {
    if (isOpen && activeTab === 'questionGroup') {
      const timer = setTimeout(() => {
        loadQuestionGroups();
      }, 500);
      
      return () => clearTimeout(timer);
    }
  }, [groupSearchQuery]);
  
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
  
  // 题组项目组件
  const QuestionGroupItem = ({ group }: { group: QuestionGroup }) => {
    const isSelected = selectedQuestionGroup?.id === group.id;
    
    return (
      <div 
        className={cn(
          'border rounded-md p-3 cursor-pointer transition-all hover:border-primary',
          isSelected ? 'border-primary ring-1 ring-primary' : ''
        )}
        onClick={() => handleSelectQuestionGroup(group)}
      >
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-md">
            <ClipboardList className="h-5 w-5" />
          </div>
          
          <div className="flex-1 min-w-0">
            <h4 className="font-medium text-sm truncate">{group.name}</h4>
            <div className="flex flex-col mt-1">
              <span className="text-xs text-muted-foreground">
                {group.questionCount || 0} 题 • {group.description ? group.description.substring(0, 20) + (group.description.length > 20 ? '...' : '') : '无描述'}
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
          <DialogTitle>添加内容到小节</DialogTitle>
          <DialogDescription>
            选择媒体资源或题组添加到小节
          </DialogDescription>
        </DialogHeader>
        
        {/* 主标签页：媒体 vs 题组 */}
        <Tabs 
          defaultValue="media"
          value={activeTab}
          onValueChange={setActiveTab}
          className="mt-2"
        >
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="media" className="flex items-center">
              <File className="mr-2 h-4 w-4" />
              媒体资源
            </TabsTrigger>
            <TabsTrigger value="questionGroup" className="flex items-center">
              <ListChecks className="mr-2 h-4 w-4" />
              题组
            </TabsTrigger>
          </TabsList>
          
          {/* 媒体标签内容 */}
          <TabsContent value="media">
            {/* 媒体类型选择和搜索框 */}
            <div className="flex flex-col gap-4 my-4">
              <Tabs defaultValue="all" value={selectedTab} onValueChange={setSelectedTab}>
                <TabsList className="flex w-full overflow-x-auto">
                  <TabsTrigger value="all">全部</TabsTrigger>
                  <TabsTrigger value="VIDEO">视频</TabsTrigger>
                  <TabsTrigger value="AUDIO">音频</TabsTrigger>
                  <TabsTrigger value="DOCUMENT">文档</TabsTrigger>
                  <TabsTrigger value="IMAGE">图片</TabsTrigger>
                  <TabsTrigger value="OTHER">其他</TabsTrigger>
                </TabsList>
              </Tabs>
              
              <div className="flex items-center gap-2">
                <Input
                  placeholder="搜索媒体资源..."
                  value={mediaSearchQuery}
                  onChange={(e) => setMediaSearchQuery(e.target.value)}
                  className="flex-1"
                />
                <Button variant="outline" onClick={() => loadMediaList()}>
                  <Search className="h-4 w-4" />
                </Button>
              </div>
            </div>
            
            {/* 媒体列表 */}
            <div className="relative">
              {isLoadingMedia ? (
                <div className="flex flex-col items-center justify-center p-8">
                  <Loader2 className="h-8 w-8 animate-spin text-primary mb-2" />
                  <p className="text-sm text-muted-foreground">加载媒体资源...</p>
                </div>
              ) : error ? (
                <div className="text-center py-8">
                  <p className="text-destructive">{error}</p>
                  <Button variant="outline" onClick={loadMediaList} className="mt-4">
                    <ArrowRight className="h-4 w-4 mr-2" />
                    重试
                  </Button>
                </div>
              ) : mediaList.length === 0 ? (
                <Empty 
                  icon={<Upload className="h-10 w-10" />}
                  title="未找到媒体资源" 
                  description="尝试其他搜索条件或上传新的媒体资源"
                />
              ) : (
                <ScrollArea className="h-[300px] rounded-md border p-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {mediaList.map((media) => (
                      <MediaItem key={media.id} media={media} />
                    ))}
                  </div>
                </ScrollArea>
              )}
            </div>
            
            {/* 媒体资源类型选择 */}
            {selectedMedia && (
              <div className="mt-4 p-4 border rounded-md bg-muted/20">
                <h4 className="font-medium mb-2">资源类型</h4>
                <Select value={selectedResourceType} onValueChange={setSelectedResourceType}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择资源类型" />
                  </SelectTrigger>
                  <SelectContent>
                    {resourceTypes.map(type => (
                      <SelectItem key={type.value} value={type.value}>
                        <div className="flex flex-col">
                          <span>{type.label}</span>
                          <span className="text-xs text-muted-foreground">{type.description}</span>
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className="text-xs text-muted-foreground mt-2">
                  选择资源类型以分类课程内容
                </p>
              </div>
            )}
          </TabsContent>
          
          {/* 题组标签内容 */}
          <TabsContent value="questionGroup">
            {/* 题组搜索框 */}
            <div className="flex items-center gap-2 my-4">
              <Input
                placeholder="搜索题组..."
                value={groupSearchQuery}
                onChange={(e) => setGroupSearchQuery(e.target.value)}
                className="flex-1"
              />
              <Button variant="outline" onClick={() => loadQuestionGroups()}>
                <Search className="h-4 w-4" />
              </Button>
            </div>
            
            {/* 题组列表 */}
            <div className="relative">
              {isLoadingGroups ? (
                <div className="flex flex-col items-center justify-center p-8">
                  <Loader2 className="h-8 w-8 animate-spin text-primary mb-2" />
                  <p className="text-sm text-muted-foreground">加载题组...</p>
                </div>
              ) : error ? (
                <div className="text-center py-8">
                  <p className="text-destructive">{error}</p>
                  <Button variant="outline" onClick={loadQuestionGroups} className="mt-4">
                    <ArrowRight className="h-4 w-4 mr-2" />
                    重试
                  </Button>
                </div>
              ) : questionGroups.length === 0 ? (
                <Empty 
                  icon={<ListChecks className="h-10 w-10" />}
                  title="未找到题组" 
                  description="尝试其他搜索条件或创建新的题组"
                />
              ) : (
                <ScrollArea className="h-[350px] rounded-md border p-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {questionGroups.map((group) => (
                      <QuestionGroupItem key={group.id} group={group} />
                    ))}
                  </div>
                </ScrollArea>
              )}
            </div>
          </TabsContent>
        </Tabs>
        
        {/* 错误提示和操作按钮 */}
        <DialogFooter className="mt-6">
          {error && (
            <div className="text-destructive text-sm mb-2 w-full">{error}</div>
          )}
          <Button variant="outline" onClick={() => setIsOpen(false)} disabled={isSubmitting}>
            取消
          </Button>
          <Button onClick={handleAddResource} disabled={isSubmitting}>
            {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            {activeTab === 'media' ? '添加媒体资源' : '添加题组'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
} 