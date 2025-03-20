'use client';

import { useState, useEffect } from 'react';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription, SheetFooter } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { toast } from 'sonner';
import { 
  ChevronRight, 
  Info, 
  Loader2, 
  Save, 
  Search, 
  FileText, 
  Video, 
  Headphones, 
  FileCode, 
  BrainCircuit,
  Clock,
  X,
  CheckCircle,
  AlertCircle,
  Lock,
  File,
  Trash2
} from 'lucide-react';

import { Section, ChapterAccessType } from '@/types/course';
import { sectionService, mediaService, questionGroupService } from '@/services';
import { MediaVO } from '@/services/media-service';
import { QuestionGroup } from '@/types/question';

// 访问类型选项
const accessTypes = [
  { value: ChapterAccessType.FREE_TRIAL, label: '免费试看', description: '学习者可以免费访问' },
  { value: ChapterAccessType.PAID_ONLY, label: '付费访问', description: '学习者需购买课程后才能访问' }
];

// 内容类型
interface ContentType {
  value: string;
  label: string;
  icon: React.ReactNode;
}

// 扩展题目组类型
interface EnhancedQuestionGroup extends QuestionGroup {
  title?: string; // 用于兼容UI显示
}

// 内容类型选项
const contentTypes: ContentType[] = [
  { value: 'video', label: '视频', icon: <Video className="h-5 w-5" /> },
  { value: 'document', label: '文档', icon: <FileText className="h-5 w-5" /> },
  { value: 'audio', label: '音频', icon: <Headphones className="h-5 w-5" /> },
  { value: 'text', label: '文本', icon: <FileCode className="h-5 w-5" /> }
];

// 小节表单数据类型
interface SectionFormData {
  title: string;
  description: string;
  contentType: string;
  resourceType: 'MEDIA' | 'QUESTION_GROUP' | 'NONE';
  resourceId?: number;
}

interface SectionEditDrawerProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  courseId: number;
  chapterId: number;
  section: Section;
  onSuccess?: () => void;
  onDelete?: () => void;
}

export function SectionEditDrawer({
  open,
  onOpenChange,
  courseId,
  chapterId,
  section,
  onSuccess,
  onDelete
}: SectionEditDrawerProps) {
  // 表单状态
  const [formData, setFormData] = useState<SectionFormData>({
    title: '',
    description: '',
    contentType: 'video',
    resourceType: 'NONE'
  });

  // 资源选择和加载状态
  const [selectedMedia, setSelectedMedia] = useState<MediaVO | null>(null);
  const [selectedQuestionGroup, setSelectedQuestionGroup] = useState<EnhancedQuestionGroup | null>(null);
  const [activeTab, setActiveTab] = useState<string>('media');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 媒体资源状态
  const [mediaList, setMediaList] = useState<MediaVO[]>([]);
  const [isLoadingMedia, setIsLoadingMedia] = useState(false);
  const [mediaSearchTerm, setMediaSearchTerm] = useState('');
  const [mediaPage, setMediaPage] = useState(0);
  const [mediaTotalPages, setMediaTotalPages] = useState(0);
  const [mediaType, setMediaType] = useState<string>('all');

  // 题目组状态
  const [questionGroups, setQuestionGroups] = useState<EnhancedQuestionGroup[]>([]);
  const [isLoadingQuestionGroups, setIsLoadingQuestionGroups] = useState(false);
  const [questionGroupSearchTerm, setQuestionGroupSearchTerm] = useState('');
  const [questionGroupPage, setQuestionGroupPage] = useState(0);
  const [questionGroupTotalPages, setQuestionGroupTotalPages] = useState(0);

  // 初始化表单数据
  useEffect(() => {
    if (section && open) {
      // 设置基本表单数据
      setFormData({
        title: section.title,
        description: section.description || '',
        contentType: section.contentType,
        resourceType: section.resourceTypeDiscriminator === 'MEDIA' 
          ? 'MEDIA' 
          : section.resourceTypeDiscriminator === 'QUESTION_GROUP' 
            ? 'QUESTION_GROUP' 
            : 'NONE',
        resourceId: section.mediaId || section.questionGroupId
      });

      // 加载已绑定资源
      loadCurrentResources();
    }
  }, [section, open]);

  // 加载当前小节的资源
  const loadCurrentResources = async () => {
    if (!section) return;

    // 如果有媒体资源
    if (section.resourceTypeDiscriminator === 'MEDIA' && section.mediaId) {
      try {
        setIsLoadingMedia(true);
        const result = await mediaService.getMediaInfo(section.mediaId);
        if (result && result.data) {
          setSelectedMedia(result.data);
          setActiveTab('media');
        }
      } catch (error) {
        console.error('加载媒体资源失败:', error);
      } finally {
        setIsLoadingMedia(false);
      }
    }
    // 如果有题目组
    else if (section.resourceTypeDiscriminator === 'QUESTION_GROUP' && section.questionGroupId) {
      try {
        setIsLoadingQuestionGroups(true);
        const group = await questionGroupService.getQuestionGroupById(section.questionGroupId);
        if (group) {
          // 转换为EnhancedQuestionGroup
          const enhancedGroup: EnhancedQuestionGroup = {
            ...group,
            title: group.name // 添加title属性以兼容UI
          };
          setSelectedQuestionGroup(enhancedGroup);
          setActiveTab('question-group');
        }
      } catch (error) {
        console.error('加载题目组失败:', error);
      } finally {
        setIsLoadingQuestionGroups(false);
      }
    }
  };

  // 加载媒体资源
  const loadMediaResources = async (page = 0, searchTerm = '', type = 'all') => {
    try {
      setIsLoadingMedia(true);
      
      const params: any = {
        page,
        size: 10,
      };
      
      if (searchTerm) {
        params.keyword = searchTerm;
      }
      
      if (type !== 'all') {
        params.type = type.toUpperCase();
      }
      
      const response = await mediaService.getMediaList(params);
      
      if (response && response.data) {
        setMediaList(response.data.content);
        setMediaTotalPages(response.data.totalPages);
      }
    } catch (error) {
      console.error('加载媒体资源失败:', error);
      toast.error('加载媒体资源失败');
    } finally {
      setIsLoadingMedia(false);
    }
  };

  // 加载题目组
  const loadQuestionGroups = async (page = 0, searchTerm = '') => {
    try {
      setIsLoadingQuestionGroups(true);
      
      const params: any = {
        page,
        pageSize: 10,
      };
      
      if (searchTerm) {
        params.name = searchTerm;
      }
      
      const response = await questionGroupService.getQuestionGroupList(params);
      
      if (response) {
        // 转换后端返回的QuestionGroup为EnhancedQuestionGroup
        const enhancedGroups: EnhancedQuestionGroup[] = response.content.map(group => ({
          ...group,
          title: group.name // 添加title属性以兼容UI
        }));
        
        setQuestionGroups(enhancedGroups);
        setQuestionGroupTotalPages(response.totalPages);
      }
    } catch (error) {
      console.error('加载题目组失败:', error);
      toast.error('加载题目组失败');
    } finally {
      setIsLoadingQuestionGroups(false);
    }
  };

  // 处理表单字段变更
  const handleChange = (name: keyof SectionFormData, value: any) => {
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // 选择媒体资源
  const handleSelectMedia = (media: MediaVO) => {
    setSelectedMedia(media);
    setFormData(prev => ({
      ...prev,
      resourceType: 'MEDIA',
      resourceId: media.id,
      // 根据媒体类型设置内容类型
      contentType: media.type?.toLowerCase().includes('video') 
        ? 'video' 
        : media.type?.toLowerCase().includes('audio')
          ? 'audio'
          : media.type?.toLowerCase().includes('document') || media.type?.toLowerCase().includes('pdf')
            ? 'document'
            : 'text'
    }));
  };

  // 选择题目组
  const handleSelectQuestionGroup = (group: EnhancedQuestionGroup) => {
    setSelectedQuestionGroup(group);
    setFormData(prev => ({
      ...prev,
      resourceType: 'QUESTION_GROUP',
      resourceId: group.id,
      contentType: 'text'
    }));
  };

  // 处理表单提交
  const handleSubmit = async () => {
    try {
      setIsSubmitting(true);
      setError(null);
      
      // 验证表单
      if (!formData.title) {
        setError('小节标题不能为空');
        return;
      }
      
      // 创建小节更新数据
      const sectionData = {
        title: formData.title,
        description: formData.description,
        contentType: formData.contentType,
        chapterId: chapterId
      };
      
      // 更新小节基本信息
      await sectionService.updateSection(section.id, sectionData);
      
      // 处理资源绑定
      if (formData.resourceType === 'MEDIA' && formData.resourceId) {
        // 先解除已有绑定，再绑定新资源
        if (section.resourceTypeDiscriminator && section.resourceTypeDiscriminator !== 'MEDIA') {
          // 如果原来有题目组，先移除
          if (section.resourceTypeDiscriminator === 'QUESTION_GROUP') {
            await sectionService.removeQuestionGroup(section.id);
          }
        }
        await sectionService.setMediaResource(
          section.id, 
          formData.resourceId,
          'primary'
        );
      } else if (formData.resourceType === 'QUESTION_GROUP' && formData.resourceId) {
        // 先解除已有绑定，再绑定新资源
        if (section.resourceTypeDiscriminator && section.resourceTypeDiscriminator !== 'QUESTION_GROUP') {
          // 如果原来有媒体资源，先移除
          if (section.resourceTypeDiscriminator === 'MEDIA') {
            await sectionService.removeMediaResource(section.id);
          }
        }
        await sectionService.setQuestionGroup(
          section.id,
          formData.resourceId,
          { 
            randomOrder: false,
            orderByDifficulty: false,
            showAnalysis: true
          }
        );
      } else if (formData.resourceType === 'NONE' && section.resourceTypeDiscriminator) {
        // 如果选择无资源，但原来有资源，则清除资源
        if (section.resourceTypeDiscriminator === 'MEDIA') {
          await sectionService.removeMediaResource(section.id);
        } else if (section.resourceTypeDiscriminator === 'QUESTION_GROUP') {
          await sectionService.removeQuestionGroup(section.id);
        }
      }
      
      // 成功提示
      toast.success('小节更新成功', {
        description: '小节信息已成功保存'
      });
      
      // 关闭抽屉
      onOpenChange(false);
      
      // 调用成功回调
      if (onSuccess) {
        onSuccess();
      }
    } catch (error: any) {
      console.error('更新小节失败:', error);
      setError(error.message || '更新小节失败，请稍后重试');
      toast.error('更新小节失败', {
        description: error.message || '请稍后重试'
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  // 根据激活的标签初始加载资源
  useEffect(() => {
    if (open) {
      if (activeTab === 'media') {
        loadMediaResources(0, mediaSearchTerm, mediaType);
      } else if (activeTab === 'question-group') {
        loadQuestionGroups(0, questionGroupSearchTerm);
      }
    }
  }, [open, activeTab]);

  // 处理删除小节
  const handleDelete = async () => {
    try {
      setIsSubmitting(true);
      setError(null);
      
      const confirmed = window.confirm(`确定要删除小节"${section.title}"吗？此操作不可恢复。`);
      
      if (confirmed) {
        await sectionService.deleteSection(section.id);
        
        toast.success('小节已删除', {
          description: '小节已成功从章节中移除'
        });
        
        // 关闭抽屉
        onOpenChange(false);
        
        // 调用删除成功回调
        if (onDelete) {
          onDelete();
        } else if (onSuccess) {
          onSuccess();
        }
      }
    } catch (error: any) {
      console.error('删除小节失败:', error);
      setError(error.message || '删除小节失败，请稍后重试');
      toast.error('删除小节失败', {
        description: error.message || '请稍后重试'
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full sm:max-w-md md:max-w-xl lg:max-w-2xl overflow-y-auto p-0">
        <div className="p-6 space-y-6">
          <div className="flex justify-between items-start">
            <SheetHeader className="mb-2 text-left">
              <SheetTitle className="text-xl">编辑小节</SheetTitle>
              <SheetDescription>
                修改小节信息和关联资源
              </SheetDescription>
            </SheetHeader>
            
            <Button
              variant="destructive"
              size="sm"
              onClick={handleDelete}
              disabled={isSubmitting}
              className="mt-1"
            >
              删除小节
            </Button>
          </div>
          
          {error && (
            <Alert variant="destructive" className="mb-5">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
          
          <div className="space-y-8">
            {/* 基本信息 */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">基本信息</h3>
              
              <div className="space-y-5">
                <div className="space-y-2">
                  <Label htmlFor="title">小节标题 *</Label>
                  <Input 
                    id="title" 
                    value={formData.title} 
                    onChange={e => handleChange('title', e.target.value)}
                    placeholder="输入小节标题"
                    className="w-full"
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="description">小节描述</Label>
                  <Textarea 
                    id="description" 
                    value={formData.description} 
                    onChange={e => handleChange('description', e.target.value)}
                    placeholder="描述小节内容（可选）"
                    rows={3}
                    className="w-full resize-none"
                  />
                </div>
              </div>
            </div>
            
            <Separator />
            
            {/* 资源选择 */}
            <div className="space-y-5">
              <h3 className="text-lg font-medium">关联资源</h3>
              
              <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
                <TabsList className="grid w-full grid-cols-2 mb-4">
                  <TabsTrigger value="media">媒体资源</TabsTrigger>
                  <TabsTrigger value="question-group">题目组</TabsTrigger>
                </TabsList>
                
                {/* 媒体资源选择 */}
                <TabsContent value="media" className="space-y-4">
                  <div className="flex items-center gap-2">
                    <Input
                      placeholder="搜索媒体资源..."
                      value={mediaSearchTerm}
                      onChange={e => setMediaSearchTerm(e.target.value)}
                      className="flex-1"
                    />
                    <Button 
                      variant="secondary"
                      size="sm"
                      onClick={() => loadMediaResources(0, mediaSearchTerm, mediaType)}
                    >
                      搜索
                    </Button>
                  </div>
                  
                  {isLoadingMedia ? (
                    <div className="flex justify-center items-center py-6">
                      <Loader2 className="h-6 w-6 animate-spin text-primary" />
                      <span className="ml-2">加载媒体资源中...</span>
                    </div>
                  ) : mediaList.length > 0 ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-[320px] overflow-y-auto p-1">
                      {mediaList.map(media => (
                        <div
                          key={media.id}
                          className={`rounded-md p-3 cursor-pointer transition-colors ${
                            selectedMedia?.id === media.id 
                              ? 'bg-primary/10 border-primary border' 
                              : 'border hover:bg-accent/50'
                          }`}
                          onClick={() => handleSelectMedia(media)}
                        >
                          <div className="flex flex-col">
                            <div className="font-medium truncate">{media.title}</div>
                            <div className="text-xs text-muted-foreground mt-1 flex justify-between">
                              <span>{media.type?.split('/')[0] || '未知类型'}</span>
                              {media.size ? <span>{(media.size / 1024 / 1024).toFixed(1)} MB</span> : null}
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-10 text-muted-foreground">
                      <p>未找到媒体资源</p>
                    </div>
                  )}
                </TabsContent>
                
                {/* 题目组选择 */}
                <TabsContent value="question-group" className="space-y-4">
                  <div className="flex items-center gap-2">
                    <Input
                      placeholder="搜索题目组..."
                      value={questionGroupSearchTerm}
                      onChange={e => setQuestionGroupSearchTerm(e.target.value)}
                      className="flex-1"
                    />
                    <Button 
                      variant="secondary"
                      size="sm"
                      onClick={() => loadQuestionGroups(0, questionGroupSearchTerm)}
                    >
                      搜索
                    </Button>
                  </div>
                  
                  {isLoadingQuestionGroups ? (
                    <div className="flex justify-center items-center py-6">
                      <Loader2 className="h-6 w-6 animate-spin text-primary" />
                      <span className="ml-2">加载题目组中...</span>
                    </div>
                  ) : questionGroups.length > 0 ? (
                    <div className="grid grid-cols-1 gap-2 max-h-[320px] overflow-y-auto p-1">
                      {questionGroups.map(group => (
                        <div
                          key={group.id}
                          className={`rounded-md p-3 cursor-pointer transition-colors ${
                            selectedQuestionGroup?.id === group.id 
                              ? 'bg-primary/10 border-primary border' 
                              : 'border hover:bg-accent/50'
                          }`}
                          onClick={() => handleSelectQuestionGroup(group)}
                        >
                          <div className="flex flex-col">
                            <div className="font-medium">{group.name}</div>
                            {group.questionCount ? (
                              <div className="text-xs text-muted-foreground mt-1">
                                {group.questionCount} 题
                              </div>
                            ) : null}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-10 text-muted-foreground">
                      <p>未找到题目组</p>
                    </div>
                  )}
                </TabsContent>
              </Tabs>
              
              {/* 已选择资源显示 */}
              {(selectedMedia || selectedQuestionGroup) && (
                <div className="bg-muted/50 p-4 rounded-md border">
                  <div className="flex justify-between items-center">
                    <h4 className="font-medium">已选择资源</h4>
                    <Button 
                      variant="ghost" 
                      size="sm" 
                      className="h-8 px-2 text-muted-foreground hover:text-foreground"
                      onClick={() => {
                        setSelectedMedia(null);
                        setSelectedQuestionGroup(null);
                        setFormData(prev => ({ ...prev, resourceType: 'NONE', resourceId: undefined }));
                      }}
                    >
                      清除选择
                    </Button>
                  </div>
                  
                  <div className="mt-2">
                    {selectedMedia && (
                      <div className="text-sm">
                        <span className="font-medium">{selectedMedia.title}</span>
                        <p className="text-xs text-muted-foreground">
                          类型: {selectedMedia.type?.split('/')[0] || '未知'}
                        </p>
                      </div>
                    )}
                    
                    {selectedQuestionGroup && (
                      <div className="text-sm">
                        <span className="font-medium">{selectedQuestionGroup.name}</span>
                        <p className="text-xs text-muted-foreground">
                          {selectedQuestionGroup.questionCount || 0}题
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
        
        <div className="flex flex-col-reverse sm:flex-row sm:justify-end gap-2 px-6 py-4 border-t">
          <Button 
            variant="outline" 
            onClick={() => onOpenChange(false)}
            disabled={isSubmitting}
          >
            取消
          </Button>
          <Button 
            onClick={handleSubmit}
            disabled={isSubmitting}
          >
            {isSubmitting ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                保存中...
              </>
            ) : "保存修改"}
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  );
} 