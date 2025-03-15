import React, { useEffect, useState } from 'react';
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import * as z from "zod";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from '@/components/ui/textarea';
import { MultiSelect } from '@/components/ui/multi-select';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Checkbox } from '@/components/ui/checkbox';
import { QuestionType } from './QuestionList';

// 表单验证Schema
const QuestionFormSchema = z.object({
  content: z.string().min(1, "问题内容不能为空"),
  level: z.enum(["EASY", "MEDIUM", "HARD"]),
  type: z.enum(["SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_BLANK", "SHORT_ANSWER"]),
  answer: z.string(),
  options: z.string().optional(),
  tagIds: z.array(z.number()).optional(),
});

// 表单字段的描述文本
const questionLevelNames = {
  EASY: "简单",
  MEDIUM: "中等",
  HARD: "困难",
};

const questionTypeNames = {
  SINGLE_CHOICE: "单选题",
  MULTIPLE_CHOICE: "多选题",
  TRUE_FALSE: "判断题",
  FILL_BLANK: "填空题",
  SHORT_ANSWER: "简答题",
};

// 属性类型定义
export interface Question {
  id: string;
  content: string;
  level: "EASY" | "MEDIUM" | "HARD";
  type: QuestionType;
  answer: string;
  options?: string;
  tagIds?: number[];
  institutionId?: string;
  createdAt?: string;
  updatedAt?: string;
}

type QuestionDetailFormProps = {
  question?: Question; // 现有问题数据，用于编辑
  onSubmit: (data: any) => void; // 提交回调
  isLoading?: boolean; // 加载状态
  tags?: Array<{ id: number; name: string }>; // 可选标签
  setDebugInfo?: (info: any) => void; // 用于调试
  institutionId?: string; // 机构ID
  backToCourseDetail?: boolean; // 是否返回课程详情页
  isExpanded?: boolean; // 是否展开编辑区域
};

export function QuestionDetailForm({ 
  question,
  onSubmit,
  isLoading = false,
  tags = [],
  setDebugInfo,
  institutionId,
  backToCourseDetail = false,
  isExpanded = true,
}: QuestionDetailFormProps) {
  // 将标签数据转换为MultiSelect所需格式
  const tagOptions = tags.map(tag => ({
    label: tag.name,
    value: tag.id,
  }));

  // 为options字段添加一个本地状态以处理JSON转换
  const [optionsArray, setOptionsArray] = useState<string[]>([]);
  
  // 添加选项选择状态（用于单选题）
  const [selectedOptionIndex, setSelectedOptionIndex] = useState<number>(-1);
  
  // 添加选项多选状态（用于多选题）
  const [selectedOptionIndices, setSelectedOptionIndices] = useState<number[]>([]);
  
  // 准备默认值，确保tagIds是数组类型
  const defaultValues = {
    content: question?.content || "",
    level: question?.level || "MEDIUM",
    type: question?.type || "SINGLE_CHOICE",
    answer: question?.answer || "",
    options: question?.options || "",
    tagIds: question?.tagIds || [],
  };

  // 创建表单实例
  const form = useForm<z.infer<typeof QuestionFormSchema>>({
    resolver: zodResolver(QuestionFormSchema),
    defaultValues,
  });

  // 将调试信息移至useEffect中，避免在渲染期间调用setState
  useEffect(() => {
    if (setDebugInfo) {
      setDebugInfo({
        formValues: form.getValues(),
        question,
        watchedValues: {
          content: form.watch("content"),
          level: form.watch("level"),
          type: form.watch("type"),
          answer: form.watch("answer"),
          options: form.watch("options"),
          tagIds: form.watch("tagIds"),
        }
      });
    }
  }, [form, question, setDebugInfo]);

  // 监听问题类型变化，处理选项格式
  useEffect(() => {
    const type = form.watch("type");
    const currentOptions = form.getValues("options");
    
    try {
      // 尝试解析选项JSON
      if (currentOptions) {
        const parsedOptions = JSON.parse(currentOptions);
        setOptionsArray(parsedOptions);
        
        // 对于单选题，设置已选择的选项
        if (type === "SINGLE_CHOICE" && question?.answer) {
          const answerIndex = parseInt(question.answer, 10);
          if (!isNaN(answerIndex)) {
            setSelectedOptionIndex(answerIndex);
          }
        }
        
        // 对于多选题，设置已选择的选项
        if (type === "MULTIPLE_CHOICE" && question?.answer) {
          try {
            const selectedIndices = question.answer.split(',').map(index => parseInt(index.trim(), 10));
            if (selectedIndices.every(index => !isNaN(index))) {
              setSelectedOptionIndices(selectedIndices);
            }
          } catch (e) {
            setSelectedOptionIndices([]);
          }
        }
      } else {
        setOptionsArray([]);
        setSelectedOptionIndex(-1);
        setSelectedOptionIndices([]);
      }
    } catch (e) {
      // 如果解析失败，设置为空数组
      setOptionsArray([]);
      setSelectedOptionIndex(-1);
      setSelectedOptionIndices([]);
    }
  }, [form.watch("type"), form.getValues("options"), question?.answer]);
  
  // 当初始问题数据更新时，更新表单默认值
  useEffect(() => {
    if (question) {
      form.reset({
        content: question.content || "",
        level: question.level || "MEDIUM",
        type: question.type || "SINGLE_CHOICE",
        answer: question.answer || "",
        options: question.options || "",
        tagIds: question.tagIds || [],
      });
      
      try {
        if (question.options) {
          const parsedOptions = JSON.parse(question.options);
          setOptionsArray(parsedOptions);
          
          // 对于单选题，设置已选择的选项
          if (question.type === "SINGLE_CHOICE" && question.answer) {
            const answerIndex = parseInt(question.answer, 10);
            if (!isNaN(answerIndex)) {
              setSelectedOptionIndex(answerIndex);
            }
          }
          
          // 对于多选题，设置已选择的选项
          if (question.type === "MULTIPLE_CHOICE" && question.answer) {
            try {
              const selectedIndices = question.answer.split(',').map(index => parseInt(index.trim(), 10));
              if (selectedIndices.every(index => !isNaN(index))) {
                setSelectedOptionIndices(selectedIndices);
              }
            } catch (e) {
              setSelectedOptionIndices([]);
            }
          }
        }
      } catch (e) {
        setOptionsArray([]);
      }
    }
  }, [question, form]);

  // 处理单选题选项变更
  const handleSingleOptionChange = (index: number) => {
    setSelectedOptionIndex(index);
    form.setValue("answer", index.toString());
  };
  
  // 处理多选题选项变更
  const handleMultipleOptionChange = (index: number, checked: boolean) => {
    let newIndices = [...selectedOptionIndices];
    
    if (checked) {
      // 添加索引，如果不存在
      if (!newIndices.includes(index)) {
        newIndices.push(index);
      }
    } else {
      // 移除索引
      newIndices = newIndices.filter(i => i !== index);
    }
    
    // 排序以保持一致顺序
    newIndices.sort((a, b) => a - b);
    setSelectedOptionIndices(newIndices);
    form.setValue("answer", newIndices.join(','));
  };

  // 处理选项变更
  const handleOptionChange = (index: number, value: string) => {
    const newOptions = [...optionsArray];
    newOptions[index] = value;
    setOptionsArray(newOptions);
    form.setValue("options", JSON.stringify(newOptions));
  };

  // 添加新选项
  const addOption = () => {
    const newOptions = [...optionsArray, ""];
    setOptionsArray(newOptions);
    form.setValue("options", JSON.stringify(newOptions));
  };

  // 删除选项
  const removeOption = (index: number) => {
    const newOptions = optionsArray.filter((_, i) => i !== index);
    setOptionsArray(newOptions);
    form.setValue("options", JSON.stringify(newOptions));
    
    // 更新选择的选项索引
    if (form.watch("type") === "SINGLE_CHOICE") {
      if (selectedOptionIndex === index) {
        setSelectedOptionIndex(-1);
        form.setValue("answer", "");
      } else if (selectedOptionIndex > index) {
        // 调整索引，因为前面的选项被删除了
        setSelectedOptionIndex(selectedOptionIndex - 1);
        form.setValue("answer", (selectedOptionIndex - 1).toString());
      }
    } else if (form.watch("type") === "MULTIPLE_CHOICE") {
      let newIndices = selectedOptionIndices.filter(i => i !== index)
        .map(i => i > index ? i - 1 : i);
      setSelectedOptionIndices(newIndices);
      form.setValue("answer", newIndices.join(','));
    }
  };
  
  // 处理标签选择变更，避免无限循环渲染
  const handleTagChange = (selected: (number | string)[]) => {
    // 确保所有值都是数字类型
    const numericValues = selected.map(val => typeof val === 'string' ? parseInt(val, 10) : val);
    form.setValue("tagIds", numericValues);
  };

  // 表单提交处理
  const onSubmitForm = (values: z.infer<typeof QuestionFormSchema>) => {
    try {
      console.log("提交表单", values);
      
      // 检查选项格式
      if ((values.type === "SINGLE_CHOICE" || values.type === "MULTIPLE_CHOICE") 
           && (!values.options || values.options === "[]")) {
        toast.error("请添加选项");
        return;
      }
      
      // 检查答案是否已选择
      if (values.type === "SINGLE_CHOICE" && selectedOptionIndex === -1) {
        toast.error("请选择正确答案");
        return;
      }
      
      if (values.type === "MULTIPLE_CHOICE" && selectedOptionIndices.length === 0) {
        toast.error("请至少选择一个正确答案");
        return;
      }
      
      // 如果问题存在，保留其ID和创建时间
      const finalData = {
        ...values,
        id: question?.id,
        createdAt: question?.createdAt,
        // 确保提交时包含机构ID
        institutionId: institutionId || question?.institutionId,
        // 保持type不变，编辑时不允许修改问题类型
        type: question?.id ? question.type : values.type,
      };

      onSubmit(finalData);
    } catch (error) {
      console.error("提交表单时出错:", error);
      toast.error("提交表单时出错");
    }
  };

  // 渲染选项列表（单选题和多选题）
  const renderOptions = () => {
    const questionType = form.watch("type");
    
    if (questionType === "SINGLE_CHOICE") {
      return (
        <FormItem className="space-y-4">
          <FormLabel>选项与答案</FormLabel>
          <RadioGroup 
            value={selectedOptionIndex.toString()} 
            onValueChange={(value) => handleSingleOptionChange(parseInt(value, 10))}
          >
            {optionsArray.map((option, index) => (
              <div key={index} className="flex items-center space-x-2 mb-2">
                <RadioGroupItem value={index.toString()} id={`option-${index}`} />
                <Input
                  value={option}
                  onChange={(e) => handleOptionChange(index, e.target.value)}
                  placeholder={`选项 ${index + 1}`}
                  className="flex-1"
                />
                <Button 
                  type="button"
                  variant="destructive"
                  size="sm"
                  onClick={() => removeOption(index)}
                >
                  删除
                </Button>
              </div>
            ))}
          </RadioGroup>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={addOption}
            className="mt-2"
          >
            添加选项
          </Button>
          <FormDescription>
            选择单选钮表示该选项为正确答案
          </FormDescription>
        </FormItem>
      );
    } else if (questionType === "MULTIPLE_CHOICE") {
      return (
        <FormItem className="space-y-4">
          <FormLabel>选项与答案</FormLabel>
          <div className="space-y-2">
            {optionsArray.map((option, index) => (
              <div key={index} className="flex items-center space-x-2 mb-2">
                <Checkbox 
                  id={`option-${index}`}
                  checked={selectedOptionIndices.includes(index)}
                  onCheckedChange={(checked) => 
                    handleMultipleOptionChange(index, checked === true)
                  }
                />
                <Input
                  value={option}
                  onChange={(e) => handleOptionChange(index, e.target.value)}
                  placeholder={`选项 ${index + 1}`}
                  className="flex-1"
                />
                <Button 
                  type="button"
                  variant="destructive"
                  size="sm"
                  onClick={() => removeOption(index)}
                >
                  删除
                </Button>
              </div>
            ))}
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={addOption}
              className="mt-2"
            >
              添加选项
            </Button>
          </div>
          <FormDescription>
            勾选复选框表示该选项为正确答案，可多选
          </FormDescription>
        </FormItem>
      );
    }
    
    return null;
  };

  // 渲染不同类型问题的答案输入区域
  const renderAnswerField = () => {
    const questionType = form.watch("type");
    
    // 单选题和多选题的答案在选项中处理
    if (questionType === "SINGLE_CHOICE" || questionType === "MULTIPLE_CHOICE") {
      return null;
    }
    
    switch (questionType) {
      case "TRUE_FALSE":
        return (
          <FormField
            control={form.control}
            name="answer"
            render={({ field }) => (
              <FormItem>
                <FormLabel>正确答案</FormLabel>
                <Select
                  onValueChange={field.onChange}
                  defaultValue={field.value}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="选择正确答案" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value="true">正确</SelectItem>
                    <SelectItem value="false">错误</SelectItem>
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
        );
      
      case "FILL_BLANK":
      case "SHORT_ANSWER":
      default:
        return (
          <FormField
            control={form.control}
            name="answer"
            render={({ field }) => (
              <FormItem>
                <FormLabel>参考答案</FormLabel>
                <FormControl>
                  <Textarea 
                    placeholder="输入参考答案" 
                    {...field} 
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        );
    }
  };

  // 返回表单UI
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmitForm)} className="space-y-4">
        {/* 如果是已展开状态，显示更紧凑的表单 */}
        {isExpanded ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-4 md:col-span-2">
              <FormField
                control={form.control}
                name="content"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>问题内容</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="输入问题内容"
                        {...field}
                        className="min-h-20"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="level"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>难度级别</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="选择难度级别" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {Object.entries(questionLevelNames).map(([value, label]) => (
                        <SelectItem key={value} value={value}>
                          {label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="type"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>问题类型</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                    disabled={!!question?.id} // 编辑模式下不允许修改问题类型
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="选择问题类型" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {Object.entries(questionTypeNames).map(([value, label]) => (
                        <SelectItem key={value} value={value}>
                          {label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormDescription>
                    {question?.id && "编辑模式下不能修改问题类型"}
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="md:col-span-2">
              {renderOptions()}
              {renderAnswerField()}
            </div>

            <div className="md:col-span-2">
              <FormField
                control={form.control}
                name="tagIds"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>标签</FormLabel>
                    <FormControl>
                      <MultiSelect
                        options={tagOptions}
                        selected={field.value as number[]}
                        onValueChange={handleTagChange}
                        placeholder="选择标签"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </div>
        ) : (
          // 简洁版表单，仅包含基本字段
          <div className="space-y-2">
            <FormField
              control={form.control}
              name="content"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>问题内容</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="输入问题内容"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            <div className="grid grid-cols-2 gap-2">
              <FormField
                control={form.control}
                name="level"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>难度</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="选择难度" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Object.entries(questionLevelNames).map(([value, label]) => (
                          <SelectItem key={value} value={value}>
                            {label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="type"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>类型</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      disabled={!!question?.id} // 编辑模式下不允许修改
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="选择类型" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Object.entries(questionTypeNames).map(([value, label]) => (
                          <SelectItem key={value} value={value}>
                            {label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </div>
        )}
        
        <div className="flex justify-end space-x-2">
          <Button
            type="submit"
            disabled={isLoading}
            className="bg-primary hover:bg-primary/90"
          >
            {isLoading ? "保存中..." : (question?.id ? "更新问题" : "创建问题")}
          </Button>
        </div>
      </form>
    </Form>
  );
} 