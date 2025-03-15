'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage, Form } from '@/components/ui/form';
import { Save, Plus, Trash2, ChevronDown, ArrowRight, Info } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { toast } from 'sonner';
import { Question, QuestionDTO, QuestionType, QuestionDifficulty, QuestionOptionDTO, QuestionTag } from '@/types/question';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Badge } from '@/components/ui/badge';
import { MultiSelect } from '@/components/ui/multi-select';
import { getQuestionTypeText, getQuestionDifficultyText } from '@/utils/questionUtils';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';

// 创建与QuestionDTO匹配的表单验证模式
const questionFormSchema = z.object({
  title: z.string().min(1, { message: '题目标题不能为空' }),
  description: z.string().optional(),
  type: z.nativeEnum(QuestionType),
  difficulty: z.nativeEnum(QuestionDifficulty),
  analysis: z.string().optional(),
  options: z.array(
    z.object({
      content: z.string().min(1, { message: '选项内容不能为空' }),
      isCorrect: z.boolean(),
      optionOrder: z.number()
    })
  ).optional(),
  answer: z.string().optional(),
  tagIds: z.array(z.number()).optional(),
  institutionId: z.number(),
  score: z.number().min(1, { message: '分值必须大于0' }).max(100, { message: '分值不能超过100' })
});

// 使用类型别名确保表单类型与DTO类型兼容
type FormValues = z.infer<typeof questionFormSchema>;

interface QuestionDetailFormProps {
  question: Question;
  tags: QuestionTag[];
  readOnly?: boolean;
  onSubmit: (data: QuestionDTO) => void;
  isSubmitting?: boolean;
}

export function QuestionDetailForm({ 
  question, 
  tags, 
  readOnly = false, 
  onSubmit, 
  isSubmitting = false 
}: QuestionDetailFormProps) {
  // 将题目数据转换为表单数据
  const toFormValues = (question: Question) => {
    // 确保 tagIds 是数组类型
    const tagIds = question.tagIds || 
                  (question.tags ? question.tags.map(tag => tag.id) : []);
    
    // 单选题和判断题确保只有一个正确答案
    let options = question.options || [];
    if (question.type === QuestionType.SINGLE_CHOICE || question.type === QuestionType.TRUE_FALSE) {
      const correctIndex = options.findIndex(opt => opt.isCorrect);
      if (correctIndex >= 0) {
        options = options.map((opt, index) => ({
          ...opt,
          isCorrect: index === correctIndex
        }));
      }
    }
    
    // 判断题内容确保为"正确"和"错误"
    if (question.type === QuestionType.TRUE_FALSE && (!options || options.length !== 2)) {
      // 创建判断题的默认选项
      options = [
        { content: '正确', isCorrect: true, optionOrder: 0 } as any,
        { content: '错误', isCorrect: false, optionOrder: 1 } as any
      ];
    }
    
    return {
      title: question.title,
      description: question.description || '',
      type: question.type,
      difficulty: question.difficulty,
      analysis: question.analysis || '',
      options: options.map(opt => ({
        content: opt.content,
        isCorrect: opt.isCorrect,
        optionOrder: opt.optionOrder
      })),
      answer: question.answer || '',
      tagIds: tagIds,
      institutionId: question.institutionId,
      // @ts-ignore - 处理可能在Question类型中不存在score字段的问题
      score: question.score || 1 // 默认分值为1
    };
  };
  
  // 设置表单
  const form = useForm<FormValues>({
    resolver: zodResolver(questionFormSchema),
    defaultValues: toFormValues(question)
  });
  
  // 当外部问题数据更新时，重置表单
  useEffect(() => {
    form.reset(toFormValues(question));
  }, [question, form]);
  
  // 处理表单提交
  const handleSubmit = (data: FormValues) => {
    try {
      // 确保不修改题目类型
      data.type = question.type;
      
      // 基础表单验证
      if (!data.title.trim()) {
        toast.error("题目标题不能为空");
        return;
      }
      
      // 验证分值
      if (!data.score || data.score < 1 || data.score > 100) {
        toast.error("分值必须在1-100之间");
        return;
      }
      
      // 根据题目类型进行特定验证
      if (data.type === QuestionType.SINGLE_CHOICE || data.type === QuestionType.MULTIPLE_CHOICE) {
        // 选项验证
        if (!data.options || data.options.length < 2) {
          toast.error(`${data.type === QuestionType.SINGLE_CHOICE ? "单选题" : "多选题"}至少需要两个选项`);
          return;
        }
        
        // 确保所有选项都有内容
        const emptyOptions = data.options.filter(option => !option.content.trim());
        if (emptyOptions.length > 0) {
          toast.error("所有选项都必须填写内容");
          return;
        }
        
        // 确保选择题至少有一个正确答案
        const hasCorrectOption = data.options.some(option => option.isCorrect);
        if (!hasCorrectOption) {
          toast.error("请至少选择一个正确答案");
          return;
        }
        
        // 单选题确保只有一个正确答案
        if (data.type === QuestionType.SINGLE_CHOICE) {
          const correctCount = data.options.filter(option => option.isCorrect).length;
          if (correctCount > 1) {
            toast.error("单选题只能有一个正确答案");
            return;
          }
        }
      }
      
      // 创建提交数据对象
      const submitData: QuestionDTO = {
        ...data,
        // 确保其他必要字段存在
        institutionId: data.institutionId || question.institutionId,
        // 确保content字段正确设置 - 用description的值
        content: data.description,
        // 确保score字段正确设置
        score: data.score
      };
      
      console.log('最终提交数据:', submitData);
      
      // 直接调用提交回调
      onSubmit(submitData);
    } catch (error) {
      console.error('表单提交错误:', error);
      toast.error('表单提交失败: ' + (error instanceof Error ? error.message : '未知错误'));
    }
  };
  
  // 添加选项
  const addOption = () => {
    const currentOptions = form.getValues('options') || [];
    form.setValue('options', [
      ...currentOptions, 
      { content: '', isCorrect: false, optionOrder: currentOptions.length }
    ]);
  };
  
  // 删除选项
  const removeOption = (index: number) => {
    const currentOptions = form.getValues('options') || [];
    if (currentOptions.length <= 2) {
      toast.error('选择题至少需要两个选项');
      return;
    }
    
    const newOptions = currentOptions.filter((_, i) => i !== index)
      .map((opt, i) => ({ ...opt, optionOrder: i }));
    
    form.setValue('options', newOptions);
  };
  
  // 获取标签选项
  const tagOptions = tags.map(tag => ({
    label: tag.name,
    value: tag.id
  }));
  
  // 处理标签选择变更
  const handleTagChange = (values: (number | string)[]) => {
    // 将字符串值转换为数字
    const numericValues = values.map(val => 
      typeof val === 'string' ? parseInt(val, 10) : val
    ) as number[];
    
    // 直接设置表单值并触发表单验证
    form.setValue('tagIds', numericValues, { 
      shouldValidate: true,
      shouldDirty: true,
      shouldTouch: true
    });
  };
  
  // 处理单选题选项选择
  const handleSingleChoiceChange = (value: string) => {
    const index = parseInt(value);
    const options = form.getValues('options') || [];
    
    // 创建一个新的选项数组，确保只有一个正确答案
    const updatedOptions = options.map((opt, idx) => ({
      ...opt,
      isCorrect: idx === index
    }));
    
    // 更新表单值
    form.setValue('options', updatedOptions, {
      shouldValidate: true,
      shouldDirty: true,
      shouldTouch: true
    });
  };
  
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>题目基本信息</CardTitle>
            <CardDescription>设置题目的标题、类型、难度和分值</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* 题目标题 */}
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>题目标题</FormLabel>
                    <FormControl>
                      <Input {...field} className="w-full" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* 题目类型 - 只读 */}
                <FormField
                  control={form.control}
                  name="type"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>题目类型</FormLabel>
                      <FormControl>
                        <div className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background">
                          {getQuestionTypeText(field.value)}
                        </div>
                      </FormControl>
                    </FormItem>
                  )}
                />

                {/* 难度级别 */}
                <FormField
                  control={form.control}
                  name="difficulty"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>难度级别</FormLabel>
                      <Select
                        onValueChange={(value) => field.onChange(parseInt(value))}
                        defaultValue={field.value.toString()}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择难度级别" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value={QuestionDifficulty.EASY.toString()}>
                            {getQuestionDifficultyText(QuestionDifficulty.EASY)}
                          </SelectItem>
                          <SelectItem value={QuestionDifficulty.MEDIUM.toString()}>
                            {getQuestionDifficultyText(QuestionDifficulty.MEDIUM)}
                          </SelectItem>
                          <SelectItem value={QuestionDifficulty.HARD.toString()}>
                            {getQuestionDifficultyText(QuestionDifficulty.HARD)}
                          </SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* 分值设置 */}
                <FormField
                  control={form.control}
                  name="score"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>分值</FormLabel>
                      <FormControl>
                        <Input 
                          type="number" 
                          min="1" 
                          max="100" 
                          {...field}
                          onChange={(e) => field.onChange(parseInt(e.target.value) || 1)}
                          value={field.value}
                        />
                      </FormControl>
                      <FormDescription className="text-xs">
                        设置题目分值 (1-100)
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              {/* 题目描述 */}
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>题目描述</FormLabel>
                    <FormControl>
                      <Textarea {...field} placeholder="请输入题目描述" className="min-h-[100px]" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>
              {form.watch('type') === QuestionType.SINGLE_CHOICE && "单选题选项"}
              {form.watch('type') === QuestionType.MULTIPLE_CHOICE && "多选题选项"}
              {form.watch('type') === QuestionType.TRUE_FALSE && "判断题选项"}
              {form.watch('type') === QuestionType.FILL_BLANK && "填空题答案"}
              {form.watch('type') === QuestionType.SHORT_ANSWER && "简答题答案"}
            </CardTitle>
            <CardDescription>
              {form.watch('type') === QuestionType.SINGLE_CHOICE && "设置单选题的选项和正确答案（单选）"}
              {form.watch('type') === QuestionType.MULTIPLE_CHOICE && "设置多选题的选项和正确答案（可多选）"}
              {form.watch('type') === QuestionType.TRUE_FALSE && "设置判断题的正确答案"}
              {form.watch('type') === QuestionType.FILL_BLANK && "填写填空题的参考答案"}
              {form.watch('type') === QuestionType.SHORT_ANSWER && "填写简答题的参考答案"}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {/* 选择题选项编辑区域 */}
            {(form.watch('type') === QuestionType.SINGLE_CHOICE || 
              form.watch('type') === QuestionType.MULTIPLE_CHOICE) && (
              <div className="space-y-3">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center">
                    <div className="mr-2 text-sm font-medium">选项列表</div>
                    <Badge variant="outline" className="text-xs">
                      {form.watch('options')?.length || 0} 个选项
                    </Badge>
                  </div>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={addOption}
                  >
                    <Plus className="h-4 w-4 mr-1" />
                    添加选项
                  </Button>
                </div>
                
                <div className="space-y-2 rounded-md border p-3">
                  {form.watch('type') === QuestionType.SINGLE_CHOICE && (
                    <div className="grid grid-cols-[1fr_auto_auto] gap-3">
                      <div className="text-sm font-medium text-muted-foreground">选项内容</div>
                      <div className="text-sm font-medium text-muted-foreground text-center">正确答案</div>
                      <div></div>
                    </div>
                  )}
                  
                  {form.watch('type') === QuestionType.SINGLE_CHOICE && (
                    <RadioGroup
                      value={form.watch('options')?.findIndex(opt => opt.isCorrect)?.toString() || ''}
                      onValueChange={handleSingleChoiceChange}
                      className="space-y-2"
                    >
                      {form.watch('options')?.map((option, index) => (
                        <div key={index} className="grid grid-cols-[1fr_auto_auto] gap-3 items-center">
                          <FormField
                            control={form.control}
                            name={`options.${index}.content`}
                            render={({ field }) => (
                              <FormItem className="m-0">
                                <FormControl>
                                  <Input {...field} placeholder={`选项 ${index + 1}`} />
                                </FormControl>
                              </FormItem>
                            )}
                          />
                          <div className="flex justify-center">
                            <RadioGroupItem value={index.toString()} id={`option-${index}`} />
                          </div>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => removeOption(index)}
                          >
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        </div>
                      ))}
                    </RadioGroup>
                  )}
                  
                  {form.watch('type') === QuestionType.MULTIPLE_CHOICE && (
                    <div className="space-y-2">
                      <div className="grid grid-cols-[1fr_auto_auto] gap-3">
                        <div className="text-sm font-medium text-muted-foreground">选项内容</div>
                        <div className="text-sm font-medium text-muted-foreground text-center">正确答案</div>
                        <div></div>
                      </div>
                      
                      {form.watch('options')?.map((option, index) => (
                        <div key={index} className="grid grid-cols-[1fr_auto_auto] gap-3 items-center">
                          <FormField
                            control={form.control}
                            name={`options.${index}.content`}
                            render={({ field }) => (
                              <FormItem className="m-0">
                                <FormControl>
                                  <Input {...field} placeholder={`选项 ${index + 1}`} />
                                </FormControl>
                              </FormItem>
                            )}
                          />
                          <div className="flex justify-center">
                            <FormField
                              control={form.control}
                              name={`options.${index}.isCorrect`}
                              render={({ field }) => (
                                <FormItem className="m-0">
                                  <FormControl>
                                    <Checkbox
                                      checked={field.value}
                                      onCheckedChange={field.onChange}
                                    />
                                  </FormControl>
                                </FormItem>
                              )}
                            />
                          </div>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => removeOption(index)}
                          >
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* 判断题选项 */}
            {form.watch('type') === QuestionType.TRUE_FALSE && (
              <div className="space-y-3">
                <div className="text-sm font-medium">选择正确答案</div>
                <div className="rounded-md border p-4">
                  <RadioGroup
                    value={form.watch('options')?.findIndex(opt => opt.isCorrect)?.toString() || '0'}
                    onValueChange={(value) => {
                      const updatedOptions = [
                        { content: '正确', isCorrect: value === '0', optionOrder: 0 },
                        { content: '错误', isCorrect: value === '1', optionOrder: 1 }
                      ];
                      form.setValue('options', updatedOptions);
                    }}
                    className="flex flex-col space-y-3"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="0" id="true-option" />
                      <Label htmlFor="true-option" className="text-base">正确</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="1" id="false-option" />
                      <Label htmlFor="false-option" className="text-base">错误</Label>
                    </div>
                  </RadioGroup>
                </div>
              </div>
            )}

            {/* 填空题和简答题的答案 */}
            {(form.watch('type') === QuestionType.FILL_BLANK || 
              form.watch('type') === QuestionType.SHORT_ANSWER) && (
              <FormField
                control={form.control}
                name="answer"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>参考答案</FormLabel>
                    <FormControl>
                      <Textarea {...field} placeholder="请输入参考答案" className="min-h-[150px]" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>题目解析与标签</CardTitle>
            <CardDescription>添加题目解析和相关标签</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* 解析编辑区域 */}
            <FormField
              control={form.control}
              name="analysis"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>题目解析</FormLabel>
                  <FormControl>
                    <Textarea {...field} placeholder="请输入题目解析" className="min-h-[150px]" />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* 标签选择区域 */}
            <FormField
              control={form.control}
              name="tagIds"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>题目标签</FormLabel>
                  <FormControl>
                    <MultiSelect
                      options={tagOptions}
                      selected={field.value || []}
                      onSelectChange={handleTagChange}
                      placeholder="选择标签"
                      disabled={false}
                      className="cursor-pointer hover:border-primary focus:border-primary"
                    />
                  </FormControl>
                  <FormDescription className="text-xs">
                    选择适合题目的标签，可多选
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
          </CardContent>
          <CardFooter className="flex justify-end pt-2">
            <Button 
              type="button" 
              disabled={isSubmitting}
              onClick={form.handleSubmit(handleSubmit)}
              className="relative"
            >
              {isSubmitting && (
                <span className="absolute inset-0 flex items-center justify-center bg-primary/80 rounded-md">
                  <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                </span>
              )}
              <Save className="h-4 w-4 mr-1" />
              {isSubmitting ? '保存中...' : '保存修改'}
            </Button>
          </CardFooter>
        </Card>
      </form>
    </Form>
  );
} 