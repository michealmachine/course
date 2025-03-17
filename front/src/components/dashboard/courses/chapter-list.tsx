'use client';

import { useState, useEffect } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Chapter, ChapterAccessType, ChapterOrderDTO } from '@/types/course';
import { chapterService } from '@/services';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import {
  Edit,
  Trash2,
  GripVertical,
  Plus,
  X,
  ExternalLink,
  Clock,
  Lock,
  Unlock,
  BookOpen,
  ArrowUp,
  ArrowDown,
  Loader2
} from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from "sonner";
import { ChapterSections } from '@/components/dashboard/courses/chapter-sections';

// 定义与后端枚举匹配的章节访问类型
const ChapterAccessTypeMap = {
  'FREE': ChapterAccessType.FREE_TRIAL,
  'PREMIUM': ChapterAccessType.PAID_ONLY,
  'REGISTERED': ChapterAccessType.FREE_TRIAL // 暂时映射到FREE_TRIAL
};

// 修改为直接使用数值，确保与后端匹配
const ChapterAccessTypeValueMap = {
  'FREE': 0, // FREE_TRIAL
  'PREMIUM': 1, // PAID_ONLY
  'REGISTERED': 0 // 暂时映射到FREE_TRIAL
};

const chapterFormSchema = z.object({
  title: z.string().min(1, { message: '标题不能为空' }),
  description: z.string().optional(),
  accessType: z.enum(['FREE', 'PREMIUM', 'REGISTERED'], {
    required_error: '请选择访问类型',
  }),
  estimatedMinutes: z.coerce.number().int().min(1, { message: '学习时长不能小于1分钟' }).max(300, { message: '学习时长不能超过300分钟' }),
});

type ChapterFormValues = z.infer<typeof chapterFormSchema>;

interface SortableChapterItemProps {
  chapter: Chapter;
  index: number;
  onEdit: (chapter: Chapter) => void;
  onDelete: (chapterId: number) => void;
  onClick: (chapter: Chapter) => void;
  expanded?: boolean;
  onSectionUpdated?: () => void;
}

const SortableChapterItem = ({ chapter, index, onEdit, onDelete, onClick, expanded = false, onSectionUpdated }: SortableChapterItemProps) => {
  const [isExpanded, setIsExpanded] = useState(expanded);
  
  // 辅助函数：获取访问类型对应的显示文本
  const getAccessTypeDisplay = (accessType: ChapterAccessType) => {
    switch(accessType) {
      case ChapterAccessType.FREE_TRIAL:
        return { text: '免费', variant: 'secondary', icon: <Unlock className="h-3 w-3 mr-1" /> };
      case ChapterAccessType.PAID_ONLY:
        return { text: '付费', variant: 'destructive', icon: <Lock className="h-3 w-3 mr-1" /> };
      default:
        return { text: '注册用户', variant: 'outline', icon: <BookOpen className="h-3 w-3 mr-1" /> };
    }
  };

  const accessTypeDisplay = getAccessTypeDisplay(chapter.accessType);
  
  // 处理章节点击，切换展开/折叠状态
  const handleChapterClick = () => {
    setIsExpanded(!isExpanded);
    onClick(chapter);
  };

  return (
    <Draggable draggableId={chapter.id.toString()} index={index}>
      {(provided) => (
        <div
          ref={provided.innerRef}
          {...provided.draggableProps}
          className="mb-4"
        >
          <div className="relative">
            {/* 拖拽手柄 */}
            <div
              {...provided.dragHandleProps}
              className="absolute left-2 top-4 z-10 flex items-center justify-center p-2 text-muted-foreground transition hover:text-foreground cursor-grab"
            >
              <GripVertical className="h-5 w-5" />
            </div>
            
            {/* 编辑和删除按钮 */}
            <div className="absolute right-2 top-4 z-10 flex items-center space-x-2">
              <Button 
                onClick={() => onEdit(chapter)}
                size="sm" 
                variant="ghost"
              >
                <Edit className="h-4 w-4" />
              </Button>
              <Button 
                onClick={() => onDelete(chapter.id)}
                size="sm" 
                variant="ghost"
                className="text-destructive hover:text-destructive hover:bg-destructive/10"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
            
            <ChapterSections
              chapter={chapter}
              courseId={chapter.courseId}
              expanded={isExpanded}
              onChapterClick={handleChapterClick}
              onSectionUpdated={onSectionUpdated}
            />
          </div>
        </div>
      )}
    </Draggable>
  );
};

interface ChapterListProps {
  courseId: number;
  onChapterClick: (chapter: Chapter) => void;
  isDialogOpen?: boolean;
  setIsDialogOpen?: React.Dispatch<React.SetStateAction<boolean>>;
  onChapterCreated?: () => void;
  onChapterUpdated?: () => void;
  readOnly?: boolean;
}

export function ChapterList({ 
  courseId, 
  onChapterClick, 
  isDialogOpen: externalIsDialogOpen, 
  setIsDialogOpen: externalSetIsDialogOpen,
  onChapterCreated,
  onChapterUpdated,
  readOnly = false
}: ChapterListProps) {
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingChapter, setEditingChapter] = useState<Chapter | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const form = useForm<ChapterFormValues>({
    resolver: zodResolver(chapterFormSchema),
    defaultValues: {
      title: '',
      description: '',
      accessType: 'FREE',
      estimatedMinutes: 30,
    },
  });
  
  // 使用外部或内部对话框状态
  const dialogOpen = externalIsDialogOpen !== undefined ? externalIsDialogOpen : isDialogOpen;
  const setDialogOpen = externalSetIsDialogOpen || setIsDialogOpen;
  
  const loadChapters = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await chapterService.getChaptersByCourse(courseId);
      setChapters(data);
    } catch (err: any) {
      setError(err.message || '获取章节列表失败');
      console.error('获取章节列表失败:', err);
    } finally {
      setIsLoading(false);
    }
  };
  
  useEffect(() => {
    if (courseId) {
      loadChapters();
    }
  }, [courseId]);
  
  const openCreateDialog = () => {
    if (readOnly) {
      toast.warning('课程审核中，不能修改内容', {
        description: '课程正在审核中，不能添加或修改章节内容'
      });
      return;
    }
    form.reset({
      title: '',
      description: '',
      accessType: 'FREE',
      estimatedMinutes: 30,
    });
    setEditingChapter(null);
    setDialogOpen(true);
  };
  
  const openEditDialog = (chapter: Chapter) => {
    if (readOnly) {
      toast.warning('课程审核中，不能修改内容', {
        description: '课程正在审核中，不能编辑章节内容'
      });
      return;
    }
    form.reset({
      title: chapter.title,
      description: chapter.description || '',
      accessType: chapter.accessType === 0 ? 'FREE' : 'PREMIUM',
      estimatedMinutes: 30, // 使用默认值，因为Chapter类型中没有estimatedMinutes字段
    });
    setEditingChapter(chapter);
    setDialogOpen(true);
  };
  
  const onDragEnd = async (result: any) => {
    if (readOnly) {
      toast.warning('课程审核中，不能修改内容', {
        description: '课程正在审核中，不能调整章节顺序'
      });
      return;
    }
    setIsDragging(false);
    
    if (!result.destination) {
      return;
    }
    
    if (result.destination.index === result.source.index) {
      return;
    }
    
    const items = Array.from(chapters);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);
    
    // 更新本地状态以立即反映
    setChapters(items);
    
    try {
      // 创建符合ChapterOrderDTO类型的数组
      const reorderedChapters: ChapterOrderDTO[] = items.map((item, index) => ({
        id: item.id,
        orderIndex: index
      }));
      
      await chapterService.reorderChapters(courseId, reorderedChapters);
      
      // 使用正确的sonner toast格式
      toast.success('章节顺序已更新', {
        description: '章节顺序已成功调整'
      });
    } catch (err: any) {
      console.error('更新章节顺序失败:', err);
      // 使用正确的sonner toast格式
      toast.error('更新章节顺序失败', {
        description: err.message || '无法更新章节顺序'
      });
      
      // 重新加载以确保UI与服务器状态同步
      loadChapters();
    }
  };
  
  const onSubmit = async (values: ChapterFormValues) => {
    setIsSubmitting(true);
    
    try {
      // 直接使用数值而不是枚举类型
      const chapterData = {
        ...values,
        accessType: ChapterAccessTypeValueMap[values.accessType],
        courseId,
      };
      
      console.log('提交章节数据:', chapterData);
      
      if (editingChapter) {
        // 更新章节
        const updatedChapter = await chapterService.updateChapter(editingChapter.id, chapterData);
        console.log('更新章节成功:', updatedChapter);
        
        // 使用正确的sonner toast格式
        toast.success('章节已更新', {
          description: `章节 "${values.title}" 已成功更新`
        });
        
        // 重新加载数据
        loadChapters();
        
        // 通知父组件
        if (onChapterUpdated) {
          onChapterUpdated();
        }
      } else {
        // 创建新章节
        const newChapter = await chapterService.createChapter(chapterData);
        console.log('创建章节成功:', newChapter);
        
        // 使用正确的sonner toast格式
        toast.success('章节已创建', {
          description: `章节 "${values.title}" 已成功创建`
        });
        
        // 重新加载数据
        loadChapters();
        
        // 通知父组件
        if (onChapterCreated) {
          onChapterCreated();
        }
      }
      
      // 关闭对话框
      setDialogOpen(false);
    } catch (err: any) {
      console.error('章节操作失败:', err);
      // 输出更详细的错误信息
      if(err.response) {
        console.error('错误响应:', err.response.data);
        console.error('错误状态:', err.response.status);
      }
      
      // 使用正确的sonner toast格式
      toast.error(editingChapter ? '更新章节失败' : '创建章节失败', {
        description: err.message || '操作失败，请稍后重试'
      });
    } finally {
      setIsSubmitting(false);
    }
  };
  
  const handleDeleteChapter = async (chapterId: number) => {
    if (readOnly) {
      toast.warning('课程审核中，不能修改内容', {
        description: '课程正在审核中，不能删除章节'
      });
      return;
    }
    if (!confirm('确定要删除这个章节吗？删除后无法恢复。')) {
      return;
    }
    
    try {
      await chapterService.deleteChapter(chapterId);
      
      // 使用正确的sonner toast格式
      toast.success('章节已删除', {
        description: '章节已成功删除'
      });
      
      loadChapters(); // 重新加载章节列表
    } catch (err: any) {
      console.error('删除章节失败:', err);
      // 输出更详细的错误信息
      if(err.response) {
        console.error('错误响应:', err.response.data);
        console.error('错误状态:', err.response.status);
      }
      
      // 使用正确的sonner toast格式
      toast.error('删除章节失败', {
        description: err.message || '无法删除章节'
      });
    }
  };
  
  return (
    <>
      <Card className="mt-4">
        <CardHeader className="px-6 pt-6 pb-2">
          <div className="flex justify-between items-center">
            <CardTitle>章节管理</CardTitle>
            {!readOnly && (
              <Button onClick={openCreateDialog}>
                <Plus className="mr-2 h-4 w-4" />
                添加章节
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="p-6">
          {isLoading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-center space-x-4">
                  <Skeleton className="h-12 w-12 rounded-md" />
                  <div className="space-y-2 flex-1">
                    <Skeleton className="h-4 w-[250px]" />
                    <Skeleton className="h-4 w-[400px]" />
                  </div>
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="text-center text-destructive py-8">
              <p>{error}</p>
              <Button 
                onClick={loadChapters} 
                variant="outline" 
                className="mt-2"
              >
                重试
              </Button>
            </div>
          ) : chapters.length === 0 ? (
            <div className="text-center py-12 border-2 border-dashed rounded-md">
              <BookOpen className="h-10 w-10 mx-auto text-muted-foreground mb-4" />
              <h3 className="text-lg font-medium mb-2">尚未添加章节</h3>
              <p className="text-muted-foreground mb-4">点击下方按钮创建第一个章节</p>
              
              {!readOnly && (
                <Button onClick={openCreateDialog}>
                  <Plus className="mr-2 h-4 w-4" />
                  添加章节
                </Button>
              )}
            </div>
          ) : (
            <DragDropContext
              onDragStart={() => setIsDragging(true)}
              onDragEnd={onDragEnd}
            >
              <Droppable droppableId="chapters">
                {(provided) => (
                  <div
                    {...provided.droppableProps}
                    ref={provided.innerRef}
                    className={isDragging ? 'opacity-70' : ''}
                  >
                    {chapters.map((chapter, index) => (
                      <SortableChapterItem
                        key={chapter.id}
                        chapter={chapter}
                        index={index}
                        onEdit={openEditDialog}
                        onDelete={handleDeleteChapter}
                        onClick={onChapterClick}
                        onSectionUpdated={loadChapters}
                      />
                    ))}
                    {provided.placeholder}
                  </div>
                )}
              </Droppable>
            </DragDropContext>
          )}
        </CardContent>
        <CardFooter className="border-t bg-muted/10 px-6 py-4">
          <p className="text-sm text-muted-foreground">
            提示: 拖拽调整章节顺序 • 点击章节管理小节
          </p>
        </CardFooter>
      </Card>
      
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-[525px]">
          <DialogHeader>
            <DialogTitle>
              {editingChapter ? '编辑章节' : '创建新章节'}
            </DialogTitle>
            <DialogDescription>
              {editingChapter 
                ? '修改章节的信息和设置' 
                : '为课程添加新的章节'}
            </DialogDescription>
          </DialogHeader>
          
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>章节标题</FormLabel>
                    <FormControl>
                      <Input placeholder="输入章节标题" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>章节描述</FormLabel>
                    <FormControl>
                      <Textarea 
                        placeholder="简短描述章节内容（可选）" 
                        {...field} 
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormDescription>
                      简要介绍本章节的内容和学习目标
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="accessType"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>访问权限</FormLabel>
                      <Select 
                        onValueChange={field.onChange} 
                        defaultValue={field.value}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择访问权限" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="FREE">
                            <div className="flex items-center">
                              <Unlock className="h-4 w-4 mr-2" />
                              <span>免费</span>
                            </div>
                          </SelectItem>
                          <SelectItem value="REGISTERED">
                            <div className="flex items-center">
                              <BookOpen className="h-4 w-4 mr-2" />
                              <span>注册用户</span>
                            </div>
                          </SelectItem>
                          <SelectItem value="PREMIUM">
                            <div className="flex items-center">
                              <Lock className="h-4 w-4 mr-2" />
                              <span>付费</span>
                            </div>
                          </SelectItem>
                        </SelectContent>
                      </Select>
                      <FormDescription>
                        设置谁可以访问此章节内容
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="estimatedMinutes"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>学习时长（分钟）</FormLabel>
                      <FormControl>
                        <Input 
                          type="number" 
                          min={1} 
                          max={300} 
                          {...field} 
                        />
                      </FormControl>
                      <FormDescription>
                        完成本章节预计所需时间
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              
              <DialogFooter>
                <Button 
                  type="button" 
                  variant="outline" 
                  onClick={() => setDialogOpen(false)}
                  disabled={isSubmitting}
                >
                  取消
                </Button>
                <Button 
                  type="submit" 
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      {editingChapter ? '保存中...' : '创建中...'}
                    </>
                  ) : (
                    editingChapter ? '保存章节' : '创建章节'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </>
  );
} 