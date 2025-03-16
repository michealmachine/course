'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import { 
  Save, 
  Loader2, 
  Video, 
  FileText, 
  Headphones,
  Image as ImageIcon,
  FileCode,
  Layers,
  Info,
  Lock,
  Clock,
  Settings,
  File,
  HelpCircle,
  Plus
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Separator } from '@/components/ui/separator';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Empty } from '@/components/ui/empty';

import { Section, ChapterAccessType } from '@/types/course';
import sectionService from '@/services/section';

// 简化版本：定义本地类型
interface MediaResource {
  id: number;
  filename: string;
  fileType: string;
  fileSize: number;
}

interface QuestionGroup {
  id: number;
  title: string;
  questionCount?: number;
}

// 扩展 Section 类型，确保它包含我们需要的属性
interface ExtendedSection extends Section {
  // 媒体资源相关
  mediaResource?: MediaResource | null;
  mediaResourceName?: string;
  mediaResourceType?: string;
  
  // 题组相关
  questionGroup?: QuestionGroup | null;
  questionGroupName?: string;
  questionCount?: number;
}

// 小节表单验证Schema
const formSchema = z.object({
  title: z
    .string()
    .min(1, { message: '标题不能为空' })
    .max(200, { message: '标题不能超过200个字符' }),
  description: z
    .string()
    .max(1000, { message: '描述不能超过1000个字符' })
    .optional(),
  contentType: z
    .string()
    .min(1, { message: '请选择内容类型' }),
  accessType: z
    .number()
    .optional(),
  estimatedMinutes: z
    .number()
    .optional()
    .nullable()
    .transform(val => val === null ? undefined : val),
  resourceType: z
    .enum(['media', 'questionGroup', 'none'])
    .default('none'),
  resourceId: z
    .number()
    .optional()
    .nullable()
});

// 内容类型选项
export const contentTypes = [
  { value: 'video', label: '视频', icon: <Video className="h-5 w-5" />, description: '视频课程内容' },
  { value: 'document', label: '文档', icon: <FileText className="h-5 w-5" />, description: '文档课程内容' },
  { value: 'audio', label: '音频', icon: <Headphones className="h-5 w-5" />, description: '音频课程内容' },
  { value: 'text', label: '文本', icon: <FileCode className="h-5 w-5" />, description: '纯文本课程内容' },
  { value: 'image', label: '图片', icon: <ImageIcon className="h-5 w-5" />, description: '图片课程内容' },
  { value: 'mixed', label: '混合', icon: <Layers className="h-5 w-5" />, description: '多种类型的混合内容' },
];

// 访问类型选项
export const accessTypes = [
  { value: ChapterAccessType.FREE_TRIAL, label: '免费试看', description: '学习者可以免费访问' },
  { value: ChapterAccessType.PAID_ONLY, label: '付费访问', description: '学习者需购买课程后才能访问' }
];

export type SectionFormValues = z.infer<typeof formSchema>;

interface SectionFormProps {
  defaultValues?: Partial<SectionFormValues>;
  section?: ExtendedSection;
  chapterId?: number;
  isSubmitting?: boolean;
  onSubmit: (values: SectionFormValues) => void;
  onCancel: () => void;
  submitLabel?: string;
  submitLoadingLabel?: string;
  error?: string | null;
  mode?: 'create' | 'edit';
}

export function SectionForm({
  defaultValues = {
    title: '',
    description: '',
    contentType: 'video',
    resourceType: 'none',
    resourceId: null
  },
  section,
  chapterId,
  isSubmitting = false,
  onSubmit,
  onCancel,
  submitLabel = '保存',
  submitLoadingLabel = '保存中...',
  error = null,
  mode = 'create'
}: SectionFormProps) {
  const form = useForm<SectionFormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: section ? {
      ...defaultValues,
      title: section.title,
      description: section.description || '',
      contentType: section.contentType || 'video',
      accessType: section.accessType,
      estimatedMinutes: section.estimatedMinutes,
      resourceType: section.mediaResource ? 'media' : 
                   section.questionGroup ? 'questionGroup' : 'none',
      resourceId: section.mediaResource?.id || section.questionGroup?.id || null
    } : defaultValues
  });

  const [selectedMedia, setSelectedMedia] = useState<MediaResource | null>(null);
  const [selectedQuestionGroup, setSelectedQuestionGroup] = useState<QuestionGroup | null>(null);
  const [isLoadingResource, setIsLoadingResource] = useState(false);
  
  // 资源类型
  const resourceType = form.watch('resourceType');
  
  // 在编辑模式下，加载已有的资源数据
  useEffect(() => {
    if (mode === 'edit' && section) {
      const loadExistingResource = async () => {
        setIsLoadingResource(true);
        try {
          // 加载媒体资源
          if (section.mediaResource) {
            setSelectedMedia(section.mediaResource);
            form.setValue('resourceType', 'media');
            form.setValue('resourceId', section.mediaResource.id);
          } 
          // 如果只有基本信息
          else if (section.mediaResourceName) {
            // 简化版：直接使用基本信息
            setSelectedMedia({
              id: section.id, // 使用小节ID作为临时ID
              filename: section.mediaResourceName || '未命名媒体',
              fileType: section.mediaResourceType || '未知',
              fileSize: 0 // 默认值
            });
            form.setValue('resourceType', 'media');
            form.setValue('resourceId', section.id);
          } 
          // 加载题组资源
          else if (section.questionGroup) {
            setSelectedQuestionGroup(section.questionGroup);
            form.setValue('resourceType', 'questionGroup');
            form.setValue('resourceId', section.questionGroup.id);
          }
          // 如果只有基本信息
          else if (section.questionGroupName) {
            // 简化版：直接使用基本信息
            setSelectedQuestionGroup({
              id: section.id, // 使用小节ID作为临时ID
              title: section.questionGroupName || '未命名题组',
              questionCount: section.questionCount || 0
            });
            form.setValue('resourceType', 'questionGroup');
            form.setValue('resourceId', section.id);
          }
        } catch (error) {
          console.error("加载资源失败:", error);
          toast.error("加载资源失败", {
            description: "无法获取小节相关资源"
          });
        } finally {
          setIsLoadingResource(false);
        }
      };
      
      loadExistingResource();
    }
  }, [section, mode, form]);
  
  const handleSubmit = async (values: SectionFormValues) => {
    try {
      await onSubmit(values);
    } catch (err: any) {
      console.error('表单提交错误:', err);
      toast.error('保存失败', {
        description: err.message || '请稍后重试'
      });
    }
  };
  
  // 处理媒体选择
  const handleMediaSelected = (media: MediaResource) => {
    setSelectedMedia(media);
    form.setValue('resourceId', media.id);
  };
  
  // 处理题组选择
  const handleQuestionGroupSelected = (questionGroup: QuestionGroup) => {
    setSelectedQuestionGroup(questionGroup);
    form.setValue('resourceId', questionGroup.id);
  };
  
  // 移除资源
  const handleRemoveResource = () => {
    if (resourceType === 'media') {
      setSelectedMedia(null);
    } else if (resourceType === 'questionGroup') {
      setSelectedQuestionGroup(null);
    }
    
    form.setValue('resourceId', null);
  };
  
  // 简化版的媒体选择器组件
  const MediaSelector = ({ onSelect }: { onSelect: (media: MediaResource) => void }) => {
    return (
      <div className="border rounded-md p-6 text-center">
        <Empty
          title="选择媒体资源"
          description="请从媒体库中选择一个资源"
          action={
            <Button onClick={() => {
              // 示例：选择一个模拟媒体资源
              onSelect({
                id: Math.floor(Math.random() * 1000) + 1,
                filename: '示例文件.mp4',
                fileType: 'video/mp4',
                fileSize: 1024 * 1024 * 5 // 5 MB
              });
            }}>
              <Plus className="h-4 w-4 mr-2" />
              选择媒体
            </Button>
          }
        />
      </div>
    );
  };
  
  // 简化版的题组选择器组件
  const QuestionGroupSelector = ({ onSelect }: { onSelect: (questionGroup: QuestionGroup) => void }) => {
    return (
      <div className="border rounded-md p-6 text-center">
        <Empty
          title="选择题目组"
          description="请从题库中选择一个题目组"
          action={
            <Button onClick={() => {
              // 示例：选择一个模拟题目组
              onSelect({
                id: Math.floor(Math.random() * 1000) + 1,
                title: '示例题目组',
                questionCount: 5
              });
            }}>
              <Plus className="h-4 w-4 mr-2" />
              选择题目组
            </Button>
          }
        />
      </div>
    );
  };
  
  // 渲染资源选择器
  const renderResourceSelector = () => {
    if (resourceType === 'none') {
      return (
        <div className="border rounded-md p-6 flex flex-col items-center justify-center text-center space-y-2">
          <HelpCircle className="h-8 w-8 text-muted-foreground" />
          <p className="text-sm text-muted-foreground">请先选择资源类型</p>
        </div>
      );
    }
    
    if (resourceType === 'media') {
      return (
        <div className="space-y-4">
          {selectedMedia ? (
            <div className="border rounded-md p-4 flex justify-between items-center">
              <div className="flex items-center space-x-4">
                <File className="h-8 w-8 text-primary" />
                <div>
                  <p className="font-medium">{selectedMedia.filename}</p>
                  <p className="text-sm text-muted-foreground">
                    {selectedMedia.fileType} · {(selectedMedia.fileSize / 1024 / 1024).toFixed(2)} MB
                  </p>
                </div>
              </div>
              <Button variant="ghost" size="sm" onClick={handleRemoveResource}>
                更换
              </Button>
            </div>
          ) : (
            <MediaSelector onSelect={handleMediaSelected} />
          )}
        </div>
      );
    }
    
    if (resourceType === 'questionGroup') {
      return (
        <div className="space-y-4">
          {selectedQuestionGroup ? (
            <div className="border rounded-md p-4 flex justify-between items-center">
              <div className="flex items-center space-x-4">
                <HelpCircle className="h-8 w-8 text-primary" />
                <div>
                  <p className="font-medium">{selectedQuestionGroup.title}</p>
                  <p className="text-sm text-muted-foreground">
                    题目数量: {selectedQuestionGroup.questionCount || '未知'}
                  </p>
                </div>
              </div>
              <Button variant="ghost" size="sm" onClick={handleRemoveResource}>
                更换
              </Button>
            </div>
          ) : (
            <QuestionGroupSelector onSelect={handleQuestionGroupSelected} />
          )}
        </div>
      );
    }
    
    return null;
  };
  
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)}>
        <Card className="w-full">
          <CardHeader>
            <CardTitle>{mode === 'create' ? '创建新小节' : '编辑小节'}</CardTitle>
            <CardDescription>
              {mode === 'create' 
                ? '添加新的小节到课程章节中' 
                : '修改现有小节的信息和内容'}
            </CardDescription>
          </CardHeader>
          
          <CardContent className="space-y-6">
            {/* 错误提示 */}
            {error && (
              <Alert variant="destructive">
                <Info className="h-4 w-4" />
                <AlertTitle>提交错误</AlertTitle>
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}
            
            {/* 基本信息部分 */}
            <div className="space-y-6">
              <h3 className="text-lg font-medium flex items-center gap-2">
                <Settings className="h-5 w-5" />
                基本信息
              </h3>
              
              {/* 标题 */}
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>小节标题 *</FormLabel>
                    <FormControl>
                      <Input placeholder="输入小节标题" {...field} />
                    </FormControl>
                    <FormDescription>
                      简洁清晰的标题能帮助学习者理解小节内容
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              {/* 描述 */}
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>小节描述</FormLabel>
                    <FormControl>
                      <Textarea 
                        placeholder="简要描述小节内容（可选）" 
                        className="resize-none min-h-[100px]"
                        {...field} 
                      />
                    </FormControl>
                    <FormDescription>
                      简短描述本小节的内容和学习目标
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              {/* 内容类型 */}
              <FormField
                control={form.control}
                name="contentType"
                render={({ field }) => (
                  <FormItem className="space-y-4">
                    <FormLabel>内容类型 *</FormLabel>
                    <FormControl>
                      <RadioGroup
                        onValueChange={field.onChange}
                        defaultValue={field.value}
                        className="grid grid-cols-2 md:grid-cols-3 gap-4"
                      >
                        {contentTypes.map((type) => (
                          <div key={type.value}>
                            <RadioGroupItem
                              value={type.value}
                              id={`content-type-${type.value}`}
                              className="peer sr-only"
                              checked={field.value === type.value}
                            />
                            <Label
                              htmlFor={`content-type-${type.value}`}
                              className="flex flex-col items-center justify-between rounded-md border-2 border-muted bg-popover p-4 hover:bg-accent hover:text-accent-foreground peer-data-[state=checked]:border-primary [&:has([data-state=checked])]:border-primary cursor-pointer"
                            >
                              <div className="flex flex-col items-center space-y-2">
                                <div className="p-2 bg-primary/10 rounded-md">
                                  {type.icon}
                                </div>
                                <div className="text-center space-y-1">
                                  <p className="text-sm font-medium">{type.label}</p>
                                  <p className="text-xs text-muted-foreground">{type.description}</p>
                                </div>
                              </div>
                            </Label>
                          </div>
                        ))}
                      </RadioGroup>
                    </FormControl>
                    <FormDescription>
                      选择小节的主要内容类型
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* 访问类型 */}
                <FormField
                  control={form.control}
                  name="accessType"
                  render={({ field }) => (
                    <FormItem className="space-y-4">
                      <FormLabel className="flex items-center gap-2">
                        <Lock className="h-4 w-4" />
                        访问权限
                      </FormLabel>
                      <FormControl>
                        <RadioGroup
                          onValueChange={(value) => field.onChange(Number(value))}
                          defaultValue={field.value?.toString()}
                          className="grid grid-cols-1 gap-4"
                        >
                          {accessTypes.map((type) => (
                            <div key={type.value}>
                              <RadioGroupItem
                                value={type.value.toString()}
                                id={`access-type-${type.value}`}
                                className="peer sr-only"
                                checked={field.value === type.value}
                              />
                              <Label
                                htmlFor={`access-type-${type.value}`}
                                className="flex items-center space-x-3 rounded-md border-2 border-muted bg-popover p-4 hover:bg-accent hover:text-accent-foreground peer-data-[state=checked]:border-primary [&:has([data-state=checked])]:border-primary cursor-pointer"
                              >
                                <div className="flex-1">
                                  <div className="text-sm font-medium">{type.label}</div>
                                  <div className="text-xs text-muted-foreground">{type.description}</div>
                                </div>
                              </Label>
                            </div>
                          ))}
                        </RadioGroup>
                      </FormControl>
                      <FormDescription>
                        设置学习者访问此小节的权限要求
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                {/* 学习时长 */}
                <FormField
                  control={form.control}
                  name="estimatedMinutes"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="flex items-center gap-2">
                        <Clock className="h-4 w-4" />
                        预计学习时间（分钟）
                      </FormLabel>
                      <FormControl>
                        <Input 
                          type="number" 
                          placeholder="例如：30" 
                          min={1}
                          max={999}
                          {...field}
                          onChange={(e) => {
                            const value = e.target.value === '' ? undefined : Number(e.target.value);
                            field.onChange(value);
                          }}
                          value={field.value === undefined ? '' : field.value}
                        />
                      </FormControl>
                      <FormDescription>
                        预计完成本小节学习所需的时间（分钟）
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            </div>
            
            {/* 分隔符 */}
            <Separator />
            
            {/* 资源管理部分 */}
            <div className="space-y-6">
              <h3 className="text-lg font-medium flex items-center gap-2">
                <File className="h-5 w-5" />
                小节资源
              </h3>
              
              <p className="text-sm text-muted-foreground">
                每个小节可以关联一个资源，可以是媒体文件（视频、音频、文档等）或题目组。
              </p>
              
              {/* 资源类型选择 */}
              <FormField
                control={form.control}
                name="resourceType"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>资源类型</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="选择资源类型" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="none">不设置资源</SelectItem>
                        <SelectItem value="media">媒体资源</SelectItem>
                        <SelectItem value="questionGroup">题目组</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormDescription>
                      选择要添加到小节的资源类型
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              {/* 资源选择器 */}
              {isLoadingResource ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin text-primary" />
                  <span className="ml-2">加载资源中...</span>
                </div>
              ) : (
                renderResourceSelector()
              )}
            </div>
          </CardContent>
          
          <CardFooter className="flex justify-between">
            <Button 
              type="button" 
              variant="outline" 
              onClick={onCancel}
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
                  {submitLoadingLabel}
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  {submitLabel}
                </>
              )}
            </Button>
          </CardFooter>
        </Card>
      </form>
    </Form>
  );
} 