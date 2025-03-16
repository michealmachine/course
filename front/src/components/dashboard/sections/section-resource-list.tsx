'use client';

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { 
  Plus, 
  Trash2, 
  Video, 
  FileText, 
  Music, 
  Image as ImageIcon, 
  File, 
  Loader2,
  ExternalLink,
  ClipboardList
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Empty } from '@/components/ui/empty';

import { sectionService } from '@/services';
import { Section } from '@/types/course';

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
  const [section, setSection] = useState<Section | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteType, setDeleteType] = useState<'media' | 'questionGroup' | null>(null);
  
  // 加载小节详情
  const loadSection = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const data = await sectionService.getSectionById(sectionId);
      setSection(data); 
    } catch (err: any) {
      console.error('加载小节详情失败:', err);
      setError(err.message || '无法加载小节详情');
      toast.error('加载小节详情失败', {
        description: err.message || '请稍后重试'
      });
      setSection(null);
    } finally {
      setIsLoading(false);
    }
  };
  
  // 初始加载
  useEffect(() => {
    if (sectionId) {
      loadSection();
    }
  }, [sectionId]);
  
  // 打开删除确认对话框
  const handleDeleteClick = (type: 'media' | 'questionGroup') => {
    setDeleteType(type);
    setIsDeleteDialogOpen(true);
  };
  
  // 确认删除资源
  const handleDeleteConfirm = async () => {
    if (!deleteType) return;
    
    try {
      setIsDeleting(true);
      
      if (deleteType === 'media') {
        // 删除媒体资源
        await sectionService.removeMediaResource(sectionId);
        toast.success('媒体资源已删除');
      } else {
        // 删除题目组
        await sectionService.removeQuestionGroup(sectionId);
        toast.success('题目组已删除');
      }
      
      // 重新加载小节数据
      await loadSection();
      
      // 关闭对话框
      setIsDeleteDialogOpen(false);
      setDeleteType(null);
    } catch (err: any) {
      console.error('删除失败:', err);
      toast.error('删除失败', {
        description: err.message || '请稍后重试'
      });
    } finally {
      setIsDeleting(false);
    }
  };
  
  // 获取资源类型信息
  const getResourceTypeInfo = (type: string) => {
    return resourceTypes.find(t => t.value === type) || { value: type, label: type, description: '' };
  };
  
  // 获取媒体类型图标
  const getMediaTypeIcon = (type: string) => {
    return mediaTypeIcons[type as keyof typeof mediaTypeIcons] || <File className="h-5 w-5" />;
  };
  
  // 格式化文件大小
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    
    return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${sizes[i]}`;
  };
  
  // 渲染加载状态
  if (isLoading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-8 w-full" />
        <Skeleton className="h-24 w-full" />
      </div>
    );
  }
  
  // 渲染错误状态
  if (error) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50 text-red-800">
        <p className="font-medium">加载资源失败</p>
        <p className="text-sm">{error}</p>
        <Button variant="outline" size="sm" className="mt-2" onClick={() => loadSection()}>
          重试
        </Button>
      </div>
    );
  }
  
  // 渲染媒体资源
  const renderMediaResource = () => {
    if (!section || !section.media) return null;
    
    const media = section.media;
    const resourceTypeInfo = getResourceTypeInfo(section.mediaResourceType || 'primary');
    
    return (
      <Card key={media.id} className="group relative hover:border-primary/50 transition-colors">
        <CardHeader className="pb-2">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2">
              <div className="p-2 rounded-md bg-primary/10">
                {getMediaTypeIcon(media.type || 'OTHER')}
              </div>
              <div>
                <CardTitle className="text-base">{media.title}</CardTitle>
                <CardDescription className="text-xs">
                  {media.type} • {media.size ? formatFileSize(media.size) : '未知大小'}
                </CardDescription>
              </div>
            </div>
            
            <Badge className={resourceTypeColors[section.mediaResourceType || 'primary']}>
              {resourceTypeInfo.label}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="pb-2">
          {media.description && (
            <p className="text-sm text-muted-foreground">{media.description}</p>
          )}
        </CardContent>
        <CardFooter className="pt-2 flex justify-between">
          <div>
            {/* 访问URL */}
            {media.accessUrl && (
              <Button variant="outline" size="sm" className="gap-1"
                onClick={() => window.open(media.accessUrl, '_blank')}>
                <ExternalLink className="h-3.5 w-3.5" />
                访问
              </Button>
            )}
          </div>
          
          <Button 
            variant="ghost" 
            size="sm" 
            className="text-destructive hover:text-destructive gap-1 opacity-0 group-hover:opacity-100"
            onClick={() => handleDeleteClick('media')}
          >
            <Trash2 className="h-3.5 w-3.5" />
            移除
          </Button>
        </CardFooter>
      </Card>
    );
  };
  
  // 渲染题目组
  const renderQuestionGroup = () => {
    if (!section || !section.questionGroup) return null;
    
    const questionGroup = section.questionGroup;
    
    return (
      <Card key={questionGroup.id} className="group relative hover:border-primary/50 transition-colors">
        <CardHeader className="pb-2">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2">
              <div className="p-2 rounded-md bg-primary/10">
                <ClipboardList className="h-5 w-5" />
              </div>
              <div>
                <CardTitle className="text-base">{questionGroup.name}</CardTitle>
                <CardDescription className="text-xs">
                  {questionGroup.questionCount || 0} 题
                </CardDescription>
              </div>
            </div>
            
            <Badge className="bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400">
              题目组
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="pb-2">
          {questionGroup.description && (
            <p className="text-sm text-muted-foreground">{questionGroup.description}</p>
          )}
          
          <div className="flex flex-wrap gap-2 mt-2">
            {section.randomOrder && (
              <Badge variant="outline" className="text-xs">随机顺序</Badge>
            )}
            {section.orderByDifficulty && (
              <Badge variant="outline" className="text-xs">按难度排序</Badge>
            )}
            {section.showAnalysis && (
              <Badge variant="outline" className="text-xs">显示解析</Badge>
            )}
          </div>
        </CardContent>
        <CardFooter className="pt-2 flex justify-end">
          <Button 
            variant="ghost" 
            size="sm" 
            className="text-destructive hover:text-destructive gap-1 opacity-0 group-hover:opacity-100"
            onClick={() => handleDeleteClick('questionGroup')}
          >
            <Trash2 className="h-3.5 w-3.5" />
            移除
          </Button>
        </CardFooter>
      </Card>
    );
  };
  
  // 渲染空状态
  const renderEmptyState = () => {
    return (
      <Empty 
        title="暂无资源"
        description="点击添加资源按钮添加媒体资源或题目组到这个小节"
        action={
          <Button onClick={onAddResource}>
            <Plus className="h-4 w-4 mr-2" />
            添加资源
          </Button>
        }
      />
    );
  };
  
  // 渲染资源内容
  const renderContent = () => {
    if (!section) return renderEmptyState();
    
    // 检查是否有直接关联的媒体资源
    if (section.resourceTypeDiscriminator === 'MEDIA' && section.media) {
      return renderMediaResource();
    }
    
    // 检查是否有直接关联的题目组
    if (section.resourceTypeDiscriminator === 'QUESTION_GROUP' && section.questionGroup) {
      return renderQuestionGroup();
    }
    
    // 如果没有任何资源，显示空状态
    return renderEmptyState();
  };
  
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-medium">小节资源</h3>
        <Button onClick={onAddResource} size="sm">
          <Plus className="h-4 w-4 mr-2" />
          添加资源
        </Button>
      </div>
      
      {renderContent()}
      
      {/* 删除确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              {deleteType === 'media' ? 
                '确定要移除这个媒体资源吗？此操作无法撤销。' : 
                '确定要移除这个题目组吗？此操作无法撤销。'}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              取消
            </Button>
            <Button 
              variant="destructive" 
              onClick={handleDeleteConfirm}
              disabled={isDeleting}
            >
              {isDeleting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              确认删除
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 