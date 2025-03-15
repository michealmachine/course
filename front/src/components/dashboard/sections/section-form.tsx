'use client';

import { useState } from 'react';
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
  Clock
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Switch } from '@/components/ui/switch';

import { Section, ChapterAccessType } from '@/types/course';

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
    .transform(val => val === null ? undefined : val)
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
  section?: Section;
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
    defaultValues
  });
  
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
                      className="grid grid-cols-2 gap-4"
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