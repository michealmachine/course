'use client';

import { useState, useEffect } from 'react';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription, SheetFooter } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
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
  RefreshCw,
  X,
  CheckCircle,
  AlertCircle,
  Lock,
  File
} from 'lucide-react';

import { Section, ChapterAccessType } from '@/types/course';
import { sectionService, mediaService, questionGroupService } from '@/services';
import { MediaVO, Page, Result } from '@/services/media-service';
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

// 扩展题目组类型，增加需要的属性
interface EnhancedQuestionGroup {
  id: number;
  name: string;
  title?: string; // 用于兼容UI显示
  description?: string;
  institutionId?: number;
  questionCount?: number;
  avgDifficulty?: number;
  creatorId?: number;
  createdAt?: string;
  updatedAt?: string;
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

interface SectionDrawerProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  courseId: number;
  chapterId: number;
  onSuccess?: () => void;
}

export function SectionDrawer({
  open,
  onOpenChange,
  courseId,
  chapterId,
  onSuccess
}: SectionDrawerProps) {
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

  // 重置表单
  const resetForm = () => {
    setFormData({
      title: '',
      description: '',
      contentType: 'video',
      resourceType: 'NONE'
    });
    setSelectedMedia(null);
    setSelectedQuestionGroup(null);
    setActiveTab('media');
    setError(null);
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
    
    // 根据资源类型自动设置内容类型
    if (name === 'resourceType') {
      if (value === 'MEDIA' && selectedMedia) {
        // 根据媒体类型设置内容类型
        const mediaType = selectedMedia.type.toLowerCase();
        if (mediaType.includes('video')) {
          setFormData(prev => ({ ...prev, contentType: 'video' }));
        } else if (mediaType.includes('audio')) {
          setFormData(prev => ({ ...prev, contentType: 'audio' }));
        } else if (mediaType.includes('document') || mediaType.includes('pdf')) {
          setFormData(prev => ({ ...prev, contentType: 'document' }));
        } else {
          setFormData(prev => ({ ...prev, contentType: 'text' }));
        }
      } else if (value === 'QUESTION_GROUP') {
        setFormData(prev => ({ ...prev, contentType: 'text' }));
      }
    }
  };

  // 选择媒体资源
  const handleSelectMedia = (media: MediaVO) => {
    setSelectedMedia(media);
    setFormData(prev => ({
      ...prev,
      resourceType: 'MEDIA',
      resourceId: media.id,
      // 根据媒体类型设置内容类型
      contentType: media.type.toLowerCase().includes('video') 
        ? 'video' 
        : media.type.toLowerCase().includes('audio')
          ? 'audio'
          : media.type.toLowerCase().includes('document') || media.type.toLowerCase().includes('pdf')
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
      
      if (formData.resourceType !== 'NONE' && !formData.resourceId) {
        setError(formData.resourceType === 'MEDIA' ? '请选择媒体资源' : '请选择题目组');
        return;
      }
      
      // 创建小节数据
      const sectionData = {
        title: formData.title,
        description: formData.description,
        contentType: formData.contentType,
        chapterId: chapterId
      };
      
      // 创建小节
      const createdSection = await sectionService.createSection(sectionData);
      
      // 如果有选择资源，绑定资源
      if (formData.resourceType === 'MEDIA' && formData.resourceId && createdSection) {
        await sectionService.setMediaResource(
          createdSection.id, 
          formData.resourceId,
          'primary' // 默认为主要资源类型
        );
      } else if (formData.resourceType === 'QUESTION_GROUP' && formData.resourceId && createdSection) {
        await sectionService.setQuestionGroup(
          createdSection.id,
          formData.resourceId,
          { 
            randomOrder: false,
            orderByDifficulty: false,
            showAnalysis: true
          }
        );
      }
      
      // 成功提示
      toast.success('小节创建成功', {
        description: '小节已成功添加到章节中'
      });
      
      // 关闭抽屉并重置表单
      onOpenChange(false);
      resetForm();
      
      // 调用成功回调
      if (onSuccess) {
        onSuccess();
      }
    } catch (error: any) {
      console.error('创建小节失败:', error);
      setError(error.message || '创建小节失败，请稍后重试');
      toast.error('创建小节失败', {
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

  return (
    <Sheet open={open} onOpenChange={(isOpen) => {
      onOpenChange(isOpen);
      if (!isOpen) {
        resetForm();
      }
    }}>
      <SheetContent className="w-full sm:max-w-md md:max-w-xl lg:max-w-2xl overflow-y-auto p-0">
        <div className="p-6 space-y-6">
          <SheetHeader className="mb-2 text-left">
            <SheetTitle className="text-xl">添加新小节</SheetTitle>
            <SheetDescription>
              填写小节信息并选择关联资源，一步完成小节创建
            </SheetDescription>
          </SheetHeader>
          
          {error && (
            <Alert variant="destructive" className="mb-5">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
          
          <div className="space-y-8">
            {/* 基本信息 */}
            <div className="space-y-5">
              <h3 className="text-lg font-medium">基本信息</h3>
              
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
                              <span>{media.type.toLowerCase().split('/')[0] || '未知类型'}</span>
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
                          类型: {selectedMedia.type.toLowerCase().split('/')[0] || '未知'}
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
                创建中...
              </>
            ) : "创建小节"}
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  );
} 