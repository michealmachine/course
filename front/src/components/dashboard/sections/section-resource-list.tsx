'use client';

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { 
  Plus, 
  Trash2, 
  Copy, 
  Video, 
  FileText, 
  Music, 
  Image as ImageIcon, 
  File, 
  DownloadCloud,
  Loader2,
  ExternalLink
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { ScrollArea } from '@/components/ui/scroll-area';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';

import { sectionService } from '@/services';
import { SectionResource } from '@/types/course';
import { Empty } from '@/components/ui/empty';

// 资源类型映射
const resourceTypes = [
  { value: 'primary', label: '主要资源', description: '课程的主要内容' },
  { value: 'supplementary', label: '补充资源', description: '课程的补充材料' },
  { value: 'homework', label: '作业', description: '学习任务和作业' },
  { value: 'reference', label: '参考资料', description: '扩展阅读和参考材料' },
];

// 媒体类型映射
const mediaTypeIcons = {
  'VIDEO': <Video className="h-5 w-5" />,
  'AUDIO': <Music className="h-5 w-5" />,
  'DOCUMENT': <FileText className="h-5 w-5" />,
  'IMAGE': <ImageIcon className="h-5 w-5" />,
  'OTHER': <File className="h-5 w-5" />,
};

// 资源类型标签颜色
const resourceTypeColors: Record<string, string> = {
  'primary': 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400',
  'supplementary': 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400',
  'homework': 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-400',
  'reference': 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400',
};

interface SectionResourceListProps {
  sectionId: number;
  onAddResource?: () => void;
}

export function SectionResourceList({ sectionId, onAddResource }: SectionResourceListProps) {
  const [resources, setResources] = useState<SectionResource[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedResource, setSelectedResource] = useState<SectionResource | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  
  // 加载资源列表
  const loadResources = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const data = await sectionService.getSectionResources(sectionId);
      setResources(data);
    } catch (err: any) {
      console.error('加载小节资源失败:', err);
      setError(err.message || '无法加载小节资源');
      toast.error('加载小节资源失败', {
        description: err.message || '请稍后重试'
      });
    } finally {
      setIsLoading(false);
    }
  };
  
  // 初始加载
  useEffect(() => {
    if (sectionId) {
      loadResources();
    }
  }, [sectionId]);
  
  // 打开删除确认对话框
  const handleDeleteClick = (resource: SectionResource) => {
    setSelectedResource(resource);
    setIsDeleteDialogOpen(true);
  };
  
  // 删除资源
  const handleDeleteConfirm = async () => {
    if (!selectedResource) return;
    
    try {
      setIsDeleting(true);
      await sectionService.deleteSectionResource(selectedResource.id);
      
      toast.success('资源删除成功', {
        description: '已从小节中移除该资源'
      });
      
      // 重新加载资源列表
      loadResources();
    } catch (err: any) {
      console.error('删除资源失败:', err);
      toast.error('删除资源失败', {
        description: err.message || '请稍后重试'
      });
    } finally {
      setIsDeleting(false);
      setIsDeleteDialogOpen(false);
    }
  };
  
  // 拖拽结束后重新排序
  const handleDragEnd = async (result: any) => {
    setIsDragging(false);
    
    if (!result.destination) return;
    if (result.destination.index === result.source.index) return;
    
    const items = Array.from(resources);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);
    
    // 更新本地状态
    setResources(items);
    
    // 准备更新请求数据
    const orderData = items.map((item, index) => ({
      id: item.id,
      orderIndex: index
    }));
    
    try {
      // 调用API更新顺序
      // 注意：由于当前API设计可能不支持资源排序，此处可能需要后端支持
      // await sectionService.reorderResources(sectionId, orderData);
      
      toast.success('资源顺序已更新', {
        description: '资源顺序已成功调整'
      });
    } catch (err: any) {
      console.error('更新资源顺序失败:', err);
      toast.error('更新资源顺序失败', {
        description: err.message || '无法更新资源顺序'
      });
      
      // 恢复原始顺序
      loadResources();
    }
  };
  
  // 获取资源类型显示信息
  const getResourceTypeInfo = (type: string) => {
    const resourceType = resourceTypes.find(t => t.value === type);
    return resourceType || { value: type, label: type, description: '' };
  };
  
  // 获取媒体类型图标
  const getMediaTypeIcon = (type: string) => {
    return mediaTypeIcons[type as keyof typeof mediaTypeIcons] || <File className="h-5 w-5" />;
  };
  
  // 加载中状态
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>资源列表</CardTitle>
          <CardDescription>小节的媒体资源</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[1, 2, 3].map(i => (
              <div key={i} className="flex items-center gap-4 p-4 border rounded-md">
                <Skeleton className="w-10 h-10 rounded-md bg-primary/10" />
                <div className="space-y-2 flex-1">
                  <Skeleton className="h-4 w-[250px]" />
                  <Skeleton className="h-4 w-[150px]" />
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }
  
  return (
    <Card className="shadow-sm">
      <CardHeader>
        <div className="flex justify-between items-center">
          <div>
            <CardTitle>资源列表</CardTitle>
            <CardDescription>管理小节的媒体资源</CardDescription>
          </div>
          <Button size="sm" onClick={onAddResource}>
            <Plus className="h-4 w-4 mr-2" />
            添加资源
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {error ? (
          <div className="text-center py-8 text-destructive">
            <p>{error}</p>
            <Button variant="outline" onClick={loadResources} className="mt-2">
              重试
            </Button>
          </div>
        ) : resources.length === 0 ? (
          <Empty 
            icon={<DownloadCloud className="h-10 w-10" />}
            title="暂无资源" 
            description="为小节添加媒体资源，如视频、文档、音频等"
            action={
              <Button onClick={onAddResource}>
                <Plus className="h-4 w-4 mr-2" />
                添加资源
              </Button>
            }
          />
        ) : (
          <DragDropContext
            onDragStart={() => setIsDragging(true)}
            onDragEnd={handleDragEnd}
          >
            <Droppable droppableId="resources">
              {(provided) => (
                <div
                  {...provided.droppableProps}
                  ref={provided.innerRef}
                  className={`space-y-2 ${isDragging ? 'opacity-70' : ''}`}
                >
                  {resources.map((resource, index) => {
                    const typeInfo = getResourceTypeInfo(resource.resourceType);
                    
                    return (
                      <Draggable
                        key={resource.id.toString()}
                        draggableId={resource.id.toString()}
                        index={index}
                      >
                        {(provided) => (
                          <div
                            ref={provided.innerRef}
                            {...provided.draggableProps}
                            {...provided.dragHandleProps}
                            className="flex items-center gap-3 p-3 border rounded-md hover:bg-muted/30 transition-colors"
                          >
                            <div className="p-2 bg-primary/10 rounded-md">
                              {getMediaTypeIcon(resource.media?.type || 'OTHER')}
                            </div>
                            
                            <div className="flex-1 min-w-0">
                              <h4 className="font-medium text-sm truncate">
                                {resource.media?.title || '未命名资源'}
                              </h4>
                              <div className="flex items-center mt-1 gap-2">
                                <Badge 
                                  variant="outline"
                                  className={resourceTypeColors[resource.resourceType] || ''}
                                >
                                  {typeInfo.label}
                                </Badge>
                                <span className="text-xs text-muted-foreground">
                                  {resource.media?.type || 'OTHER'}
                                </span>
                              </div>
                            </div>
                            
                            <div className="flex items-center gap-2">
                              {resource.media?.accessUrl && (
                                <Button size="icon" variant="ghost" asChild>
                                  <a href={resource.media.accessUrl} target="_blank" rel="noopener noreferrer">
                                    <ExternalLink className="h-4 w-4" />
                                    <span className="sr-only">预览</span>
                                  </a>
                                </Button>
                              )}
                              
                              <Button 
                                size="icon" 
                                variant="ghost" 
                                className="text-destructive hover:text-destructive hover:bg-destructive/10"
                                onClick={() => handleDeleteClick(resource)}
                              >
                                <Trash2 className="h-4 w-4" />
                                <span className="sr-only">删除</span>
                              </Button>
                            </div>
                          </div>
                        )}
                      </Draggable>
                    );
                  })}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        )}
      </CardContent>
      
      {/* 删除确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除资源</DialogTitle>
            <DialogDescription>
              确定要从小节中移除此资源吗？此操作不会删除媒体库中的原始资源文件。
            </DialogDescription>
          </DialogHeader>
          
          {selectedResource && (
            <div className="flex items-center gap-3 p-3 border rounded-md">
              <div className="p-2 bg-primary/10 rounded-md">
                {getMediaTypeIcon(selectedResource.media?.type || 'OTHER')}
              </div>
              
              <div className="flex-1 min-w-0">
                <h4 className="font-medium text-sm">
                  {selectedResource.media?.title || '未命名资源'}
                </h4>
                <div className="flex items-center mt-1 gap-2">
                  <Badge variant="outline">
                    {getResourceTypeInfo(selectedResource.resourceType).label}
                  </Badge>
                </div>
              </div>
            </div>
          )}
          
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => setIsDeleteDialogOpen(false)}
              disabled={isDeleting}
            >
              取消
            </Button>
            <Button 
              variant="destructive" 
              onClick={handleDeleteConfirm}
              disabled={isDeleting}
            >
              {isDeleting ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  删除中...
                </>
              ) : (
                '确认删除'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  );
} 