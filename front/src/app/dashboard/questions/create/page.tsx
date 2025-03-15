'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ArrowLeft, Save, Plus, Trash } from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { 
  Select, 
  SelectContent, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Switch } from '@/components/ui/switch';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';

import { questionService, questionTagService } from '@/services';
import { 
  Question, 
  QuestionDTO, 
  QuestionOption, 
  QuestionOptionDTO, 
  QuestionType, 
  QuestionDifficulty,
  QuestionTag
} from '@/types/question';

// 创建问题页面
export default function CreateQuestionPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [tags, setTags] = useState<QuestionTag[]>([]);
  const [isTagsLoading, setIsTagsLoading] = useState(false);
  
  // 问题表单数据
  const [question, setQuestion] = useState<Partial<QuestionDTO>>({
    title: '',
    description: '',
    type: QuestionType.SINGLE_CHOICE,
    difficulty: QuestionDifficulty.MEDIUM,
    options: [],
    answer: '',
    analysis: '',
    institutionId: 1, // 这里应该根据实际情况获取机构ID
    tagIds: [],
    score: 1 // 默认分值为1
  });
  
  // 加载标签列表
  useEffect(() => {
    fetchTags();
  }, []);
  
  // 当问题类型改变时，初始化选项
  useEffect(() => {
    if (question.type === QuestionType.SINGLE_CHOICE || question.type === QuestionType.MULTIPLE_CHOICE) {
      // 如果没有选项，初始化4个选项
      if (!question.options || question.options.length === 0) {
        setQuestion(prev => ({
          ...prev,
          options: [
            { content: '', isCorrect: false, optionOrder: 0 },
            { content: '', isCorrect: false, optionOrder: 1 },
            { content: '', isCorrect: false, optionOrder: 2 },
            { content: '', isCorrect: false, optionOrder: 3 }
          ]
        }));
      }
    } else if (question.type === QuestionType.TRUE_FALSE) {
      // 判断题只有两个选项：对/错
      setQuestion(prev => ({
        ...prev,
        options: [
          { content: '正确', isCorrect: false, optionOrder: 0 },
          { content: '错误', isCorrect: false, optionOrder: 1 }
        ]
      }));
    } else {
      // 填空题和简答题没有选项
      setQuestion(prev => ({
        ...prev,
        options: []
      }));
    }
  }, [question.type]);
  
  // 获取标签列表
  const fetchTags = async () => {
    setIsTagsLoading(true);
    try {
      // 使用getAllQuestionTags获取所有标签，不使用分页
      const result = await questionTagService.getAllQuestionTags(1); // 这里应该根据实际情况获取机构ID
      setTags(result);
      console.log('标签加载成功:', result);
    } catch (error) {
      console.error('获取标签列表失败:', error);
      toast.error('获取标签列表失败');
    } finally {
      setIsTagsLoading(false);
    }
  };
  
  // 返回列表
  const handleBack = () => {
    router.push('/dashboard/questions');
  };
  
  // 处理通用输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setQuestion(prev => ({ ...prev, [name]: value }));
  };
  
  // 处理选择框变化
  const handleSelectChange = (name: string, value: string) => {
    if (name === 'type') {
      const typeValue = parseInt(value);
      if (!isNaN(typeValue) && Object.values(QuestionType).includes(typeValue)) {
        setQuestion(prev => ({
          ...prev,
          type: typeValue as QuestionType
        }));
      }
    } else if (name === 'difficulty') {
      const difficultyValue = parseInt(value);
      if (!isNaN(difficultyValue) && Object.values(QuestionDifficulty).includes(difficultyValue)) {
        setQuestion(prev => ({
          ...prev,
          difficulty: difficultyValue as QuestionDifficulty
        }));
      }
    } else {
      setQuestion(prev => ({ ...prev, [name]: value }));
    }
  };
  
  // 处理选项内容变化
  const handleOptionContentChange = (index: number, value: string) => {
    if (!question.options) return;
    
    const updatedOptions = [...question.options];
    updatedOptions[index] = { ...updatedOptions[index], content: value };
    
    setQuestion(prev => ({ ...prev, options: updatedOptions }));
  };
  
  // 处理选项正确性变化
  const handleOptionCorrectChange = (index: number, isCorrect: boolean) => {
    if (!question.options) return;
    
    const updatedOptions = [...question.options];
    
    // 单选题需要确保只有一个选项被选中
    if (question.type === QuestionType.SINGLE_CHOICE && isCorrect) {
      updatedOptions.forEach((option, i) => {
        updatedOptions[i] = { ...option, isCorrect: i === index };
      });
    } else {
      updatedOptions[index] = { ...updatedOptions[index], isCorrect };
    }
    
    setQuestion(prev => ({ ...prev, options: updatedOptions }));
  };
  
  // 添加选项
  const handleAddOption = () => {
    if (!question.options) return;
    
    const newOption: QuestionOptionDTO = {
      content: '',
      isCorrect: false,
      optionOrder: question.options.length
    };
    
    setQuestion(prev => ({
      ...prev,
      options: [...(prev.options || []), newOption]
    }));
  };
  
  // 删除选项
  const handleRemoveOption = (index: number) => {
    if (!question.options) return;
    
    const updatedOptions = question.options.filter((_, i) => i !== index)
      .map((option, i) => ({ ...option, optionOrder: i }));
    
    setQuestion(prev => ({ ...prev, options: updatedOptions }));
  };
  
  // 处理标签选择变化
  const handleTagChange = (tagId: number, checked: boolean) => {
    const currentTagIds = question.tagIds || [];
    
    if (checked) {
      setQuestion(prev => ({
        ...prev,
        tagIds: [...currentTagIds, tagId]
      }));
    } else {
      setQuestion(prev => ({
        ...prev,
        tagIds: currentTagIds.filter(id => id !== tagId)
      }));
    }
  };
  
  // 处理分数变更
  const handleScoreChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(e.target.value);
    setQuestion(prev => ({
      ...prev,
      score: isNaN(value) ? 1 : Math.max(1, Math.min(100, value))
    }));
  };
  
  // 创建问题
  const handleSubmit = async () => {
    // 表单验证
    if (!question.title) {
      toast.error('请输入问题标题');
      return;
    }
    
    // 验证分值
    if (!question.score || question.score < 1 || question.score > 100) {
      toast.error('分值必须在1-100之间');
      return;
    }
    
    // 选择题需要至少有两个选项
    if ((question.type === QuestionType.SINGLE_CHOICE || 
         question.type === QuestionType.MULTIPLE_CHOICE) && 
        (!question.options || question.options.length < 2)) {
      toast.error('选择题至少需要两个选项');
      return;
    }
    
    // 选择题至少需要一个正确答案
    if ((question.type === QuestionType.SINGLE_CHOICE || 
         question.type === QuestionType.MULTIPLE_CHOICE) && 
        (!question.options || !question.options.some(option => option.isCorrect))) {
      toast.error('请标记至少一个正确答案');
      return;
    }
    
    // 判断题必须有一个正确答案
    if (question.type === QuestionType.TRUE_FALSE && 
        (!question.options || !question.options.some(option => option.isCorrect))) {
      toast.error('请选择正确答案');
      return;
    }
    
    // 填空题和简答题需要有参考答案
    if ((question.type === QuestionType.FILL_BLANK || 
         question.type === QuestionType.SHORT_ANSWER) && 
        !question.answer) {
      toast.error('请输入参考答案');
      return;
    }
    
    setIsSubmitting(true);
    try {
      // 准备提交数据，创建深拷贝避免修改原始状态
      const submitData: QuestionDTO = {
        ...question as QuestionDTO,
        content: question.description // 确保content字段设置为description的值
      };
      
      // 如果有选项，将optionOrder映射为orderIndex字段以匹配后端API期望
      if (submitData.options && submitData.options.length > 0) {
        submitData.options = submitData.options.map(option => ({
          ...option,
          // 添加orderIndex字段以匹配后端API期望
          orderIndex: option.optionOrder
        })) as any;
      }
      
      console.log('提交的问题数据:', submitData);
      
      await questionService.createQuestion(submitData);
      toast.success('创建问题成功');
      router.push('/dashboard/questions');
    } catch (error) {
      console.error('创建问题失败:', error);
      toast.error('创建问题失败');
    } finally {
      setIsSubmitting(false);
    }
  };
  
  // 渲染选项输入
  const renderOptionsInput = () => {
    if (!question.options || question.options.length === 0) return null;
    
    if (question.type === QuestionType.TRUE_FALSE) {
      const correctIndex = question.options.findIndex(o => o.isCorrect);
      const currentValue = correctIndex >= 0 ? correctIndex.toString() : "";
      
      return (
        <div className="space-y-3">
          <Label>正确答案</Label>
          <RadioGroup
            value={currentValue}
            onValueChange={(value) => {
              const index = parseInt(value);
              if (!isNaN(index) && question.options) {
                const updatedOptions = question.options.map((opt, i) => ({
                  ...opt,
                  isCorrect: i === index
                }));
                setQuestion(prev => ({ ...prev, options: updatedOptions }));
              }
            }}
          >
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="0" id="true" />
              <Label htmlFor="true" className="font-normal">正确</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="1" id="false" />
              <Label htmlFor="false" className="font-normal">错误</Label>
            </div>
          </RadioGroup>
        </div>
      );
    }
    
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <Label>选项</Label>
          {(question.type === QuestionType.SINGLE_CHOICE || 
            question.type === QuestionType.MULTIPLE_CHOICE) && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleAddOption}
            >
              <Plus className="h-4 w-4 mr-2" />
              添加选项
            </Button>
          )}
        </div>
        
        {/* 单选题 */}
        {question.type === QuestionType.SINGLE_CHOICE && question.options && (
          <div className="space-y-2">
            <div className="grid grid-cols-[1fr_auto_auto] gap-3">
              <div className="text-sm font-medium text-muted-foreground">选项内容</div>
              <div className="text-sm font-medium text-muted-foreground text-center">正确答案</div>
              <div></div>
            </div>
            
            <RadioGroup
              value={(question.options?.findIndex(o => o.isCorrect) ?? -1) >= 0 ? 
                (question.options?.findIndex(o => o.isCorrect) ?? -1).toString() : ""}
              onValueChange={(value) => {
                const selectedIndex = parseInt(value);
                if (!isNaN(selectedIndex) && question.options) {
                  const updatedOptions = [...question.options].map((opt, i) => ({
                    ...opt,
                    isCorrect: i === selectedIndex
                  }));
                  setQuestion(prev => ({ ...prev, options: updatedOptions }));
                }
              }}
              className="space-y-2"
            >
              {(question.options || []).map((option, index) => (
                <div key={index} className="grid grid-cols-[1fr_auto_auto] gap-3 items-center">
                  <Input
                    value={option.content}
                    onChange={(e) => handleOptionContentChange(index, e.target.value)}
                    placeholder={`选项 ${index + 1}`}
                  />
                  <div className="flex justify-center">
                    <RadioGroupItem value={index.toString()} id={`option-${index}`} />
                  </div>
                  {(question.options?.length || 0) > 2 && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => handleRemoveOption(index)}
                    >
                      <Trash className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              ))}
            </RadioGroup>
          </div>
        )}
        
        {/* 多选题 */}
        {question.type === QuestionType.MULTIPLE_CHOICE && question.options && (
          <div className="space-y-2">
            <div className="grid grid-cols-[1fr_auto_auto] gap-3">
              <div className="text-sm font-medium text-muted-foreground">选项内容</div>
              <div className="text-sm font-medium text-muted-foreground text-center">正确答案</div>
              <div></div>
            </div>
            
            {(question.options || []).map((option, index) => (
              <div key={index} className="grid grid-cols-[1fr_auto_auto] gap-3 items-center">
                <Input
                  value={option.content}
                  onChange={(e) => handleOptionContentChange(index, e.target.value)}
                  placeholder={`选项 ${index + 1}`}
                />
                <div className="flex justify-center">
                  <Checkbox
                    id={`option-correct-${index}`}
                    checked={option.isCorrect}
                    onCheckedChange={(checked) => 
                      handleOptionCorrectChange(index, !!checked)
                    }
                  />
                </div>
                {(question.options?.length || 0) > 2 && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => handleRemoveOption(index)}
                  >
                    <Trash className="h-4 w-4" />
                  </Button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    );
  };
  
  // 渲染答案输入
  const renderAnswerInput = () => {
    if (question.type === QuestionType.SINGLE_CHOICE || 
        question.type === QuestionType.MULTIPLE_CHOICE || 
        question.type === QuestionType.TRUE_FALSE) {
      return null;
    }
    
    return (
      <div className="space-y-2">
        <Label htmlFor="answer">参考答案</Label>
        <Textarea
          id="answer"
          name="answer"
          value={question.answer || ''}
          onChange={handleInputChange}
          placeholder="输入参考答案"
          rows={3}
        />
      </div>
    );
  };
  
  // 渲染标签选择
  const renderTagsSelection = () => {
    if (isTagsLoading) {
      return <div className="text-sm text-muted-foreground">加载标签列表...</div>;
    }
    
    if (!tags || tags.length === 0) {
      return (
        <div className="space-y-3">
          <div className="text-sm text-muted-foreground">暂无标签</div>
          <Button 
            variant="outline" 
            size="sm" 
            onClick={() => router.push('/dashboard/questions?tab=tags')}
          >
            <Plus className="h-3.5 w-3.5 mr-1" />
            创建标签
          </Button>
        </div>
      );
    }
    
    return (
      <div className="space-y-4">
        <div className="grid grid-cols-2 gap-2">
          {tags.map(tag => (
            <div key={tag.id} className="flex items-center space-x-2">
              <Checkbox
                id={`tag-${tag.id}`}
                checked={(question.tagIds || []).includes(tag.id)}
                onCheckedChange={(checked) => 
                  handleTagChange(tag.id, checked as boolean)
                }
              />
              <Label htmlFor={`tag-${tag.id}`} className="text-sm font-normal cursor-pointer">
                {tag.name}
              </Label>
            </div>
          ))}
        </div>
        
        <div className="pt-2 flex justify-between items-center">
          <div className="text-xs text-muted-foreground">
            已选择 {(question.tagIds || []).length} 个标签
          </div>
          <Button 
            variant="outline" 
            size="sm" 
            onClick={() => router.push('/dashboard/questions?tab=tags')}
          >
            管理标签
          </Button>
        </div>
      </div>
    );
  };
  
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button
          variant="outline"
          size="sm"
          onClick={handleBack}
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          返回列表
        </Button>
        
        <Button
          size="sm"
          onClick={handleSubmit}
          disabled={isSubmitting}
        >
          <Save className="h-4 w-4 mr-2" />
          保存问题
        </Button>
      </div>
      
      <Separator />
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>问题基本信息</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="title">问题标题</Label>
                <Input
                  id="title"
                  name="title"
                  value={question.title}
                  onChange={handleInputChange}
                  placeholder="输入问题标题"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="description">问题描述</Label>
                <Textarea
                  id="description"
                  name="description"
                  value={question.description || ''}
                  onChange={handleInputChange}
                  placeholder="输入问题描述（可选）"
                  rows={3}
                />
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="type">问题类型</Label>
                  <Select
                    value={question.type?.toString()}
                    onValueChange={(value) => 
                      handleSelectChange('type', value)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="选择问题类型" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="0">单选题</SelectItem>
                      <SelectItem value="1">多选题</SelectItem>
                      <SelectItem value="2">判断题</SelectItem>
                      <SelectItem value="3">填空题</SelectItem>
                      <SelectItem value="4">简答题</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="difficulty">难度级别</Label>
                  <Select
                    value={question.difficulty?.toString()}
                    onValueChange={(value) => 
                      handleSelectChange('difficulty', value)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="选择难度级别" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="1">简单</SelectItem>
                      <SelectItem value="2">中等</SelectItem>
                      <SelectItem value="3">困难</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="score">分值</Label>
                  <Input
                    id="score"
                    name="score"
                    type="number"
                    min="1"
                    max="100"
                    value={question.score || 1}
                    onChange={handleScoreChange}
                    placeholder="设置题目分值"
                  />
                  <p className="text-xs text-muted-foreground">
                    设置题目分值 (1-100)
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
          
          <Card>
            <CardHeader>
              <CardTitle>选项与答案</CardTitle>
              <CardDescription>
                根据问题类型设置选项和答案
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {renderOptionsInput()}
              {renderAnswerInput()}
              
              <div className="space-y-2">
                <Label htmlFor="analysis">解析</Label>
                <Textarea
                  id="analysis"
                  name="analysis"
                  value={question.analysis || ''}
                  onChange={handleInputChange}
                  placeholder="输入问题解析（可选）"
                  rows={3}
                />
              </div>
            </CardContent>
          </Card>
        </div>
        
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>标签</CardTitle>
              <CardDescription>
                为问题添加标签以便分类和检索
              </CardDescription>
            </CardHeader>
            <CardContent>
              {renderTagsSelection()}
            </CardContent>
          </Card>
          
          <Card>
            <CardHeader>
              <CardTitle>预览</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="border rounded-md p-4 space-y-4">
                <div>
                  <h3 className="font-medium">{question.title || '问题标题'}</h3>
                  {question.description && (
                    <p className="text-sm text-muted-foreground mt-1">{question.description}</p>
                  )}
                </div>
                
                {/* 显示分值 */}
                <div className="text-sm">
                  <span className="inline-flex items-center px-2 py-1 rounded-md bg-blue-100 text-blue-800 text-xs font-medium">
                    {question.score || 1}分
                  </span>
                </div>
                
                {question.options && question.options.length > 0 && (
                  <div className="space-y-2">
                    {question.options.map((option, index) => (
                      <div key={index} className="flex items-center space-x-2">
                        {question.type === QuestionType.SINGLE_CHOICE ? (
                          <div className={`w-4 h-4 rounded-full border ${option.isCorrect ? 'bg-primary border-primary' : 'border-input'}`} />
                        ) : (
                          <div className={`w-4 h-4 rounded-sm border ${option.isCorrect ? 'bg-primary border-primary' : 'border-input'}`} />
                        )}
                        <span className={option.isCorrect ? 'font-medium' : ''}>{option.content || `选项 ${index + 1}`}</span>
                      </div>
                    ))}
                  </div>
                )}
                
                {(question.type === QuestionType.FILL_BLANK || 
                  question.type === QuestionType.SHORT_ANSWER) && 
                  question.answer && (
                  <div>
                    <h4 className="text-sm font-medium">参考答案</h4>
                    <p className="text-sm mt-1">{question.answer}</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
} 