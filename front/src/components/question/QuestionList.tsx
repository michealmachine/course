'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Card, CardContent } from '@/components/ui/card';
import {
  MoreHorizontal,
  Search,
  Filter,
  Trash2,
  Edit,
  Eye,
  Copy,
  ChevronDown,
  ChevronUp,
  AlertCircle,
  Save,
  Plus,
} from 'lucide-react';

import { questionService, questionTagService } from '@/services';
import { Question, QuestionDifficulty, QuestionType, QuestionTag, QuestionOptionDTO } from '@/types/question';
import useQuestionStore from '@/stores/question-store';
import { getQuestionTypeText, getQuestionDifficultyText } from '@/utils/questionUtils';
import { executeQuestionSearch } from '@/services/question-helper';
import { QuestionDetailForm } from './QuestionDetailForm';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Textarea } from '@/components/ui/textarea';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Label } from '@/components/ui/label';
import React from 'react';

interface QuestionListProps {
  institutionId: number;
}

export function QuestionList({ institutionId }: QuestionListProps) {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [totalQuestions, setTotalQuestions] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);

  // 筛选状态
  const [searchTitle, setSearchTitle] = useState('');
  const [filterTypeLocal, setFilterTypeLocal] = useState<QuestionType | null>(null);
  const [filterDifficulty, setFilterDifficulty] = useState<QuestionDifficulty | null>(null);
  const [filterTagIds, setFilterTagIds] = useState<number[]>([]);
  const [selectedTagId, setSelectedTagId] = useState<string>('');
  const [tags, setTags] = useState<QuestionTag[]>([]);

  // 界面交互状态
  const [expandedQuestionId, setExpandedQuestionId] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [questionToDelete, setQuestionToDelete] = useState<number | null>(null);

  // 编辑状态
  const [editingAnalysis, setEditingAnalysis] = useState<{[key: number]: string}>({});
  const [editingOptions, setEditingOptions] = useState<{[key: number]: QuestionOptionDTO[]}>({});

  // 从问题状态存储获取状态
  const {
    selectedQuestionIds,
    isSelectAll,
    setPage,
    setFilter,
    selectQuestion,
    selectAll,
    setFilterType: setGlobalFilterType,
  } = useQuestionStore();

  // 加载标签列表
  useEffect(() => {
    if (institutionId) {
      loadTags();
    }
  }, [institutionId]);

  // 获取所有标签
  const loadTags = async () => {
    try {
      const response = await questionTagService.getAllQuestionTags(institutionId);
      setTags(response);
    } catch (error) {
      console.error('获取标签列表失败:', error);
      toast.error('获取标签失败');
    }
  };

  // 加载问题列表
  useEffect(() => {
    fetchQuestions();
  }, [currentPage, pageSize, filterTypeLocal, filterDifficulty, filterTagIds, institutionId]);

  // 获取问题列表
  const fetchQuestions = async () => {
    if (!institutionId) return;

    setIsLoading(true);
    try {
      const result = await questionService.getQuestionList({
        institutionId,
        page: currentPage - 1,
        pageSize,
        type: filterTypeLocal === null ? undefined : filterTypeLocal,
        difficulty: filterDifficulty === null ? undefined : filterDifficulty,
        tagIds: filterTagIds,
        search: searchTitle
      });

      setQuestions(result.content);
      setTotalQuestions(result.totalElements);
    } catch (error) {
      console.error('获取问题列表失败:', error);
      toast.error('获取题目列表失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 处理搜索
  const handleSearch = () => {
    setCurrentPage(1);
    fetchQuestions();
  };

  // 处理类型筛选变化
  const handleTypeChange = (value: string) => {
    const typeNumber = value === 'all' ? null : parseInt(value) as QuestionType;
    setFilterTypeLocal(typeNumber);
    setGlobalFilterType(typeNumber);
    setCurrentPage(1);
  };

  // 处理难度筛选变化
  const handleDifficultyChange = (value: string) => {
    const difficultyNumber = value === 'all' ? null : parseInt(value) as QuestionDifficulty;
    setFilterDifficulty(difficultyNumber);
    setCurrentPage(1);
  };

  // 处理标签筛选变化
  const handleTagChange = (value: string) => {
    const tagId = value === 'all' ? null : parseInt(value);
    const tagIds = tagId ? [tagId] : [];

    setSelectedTagId(value);
    setFilterTagIds(tagIds);
    setCurrentPage(1);
  };

  // 处理展开/折叠
  const handleToggleExpand = (questionId: number) => {
    // 如果之前已经展开并且要关闭，先清除编辑状态
    if (expandedQuestionId === questionId) {
      setEditingAnalysis({});
      setEditingOptions({});
      setExpandedQuestionId(null);
    } else {
      // 如果要打开，初始化编辑状态
      const question = questions.find(q => q.id === questionId);
      if (question) {
        setEditingAnalysis({ [questionId]: question.analysis || '' });

        // 确保选项只包含必要的字段，避免后端反序列化错误
        const cleanOptions = (question.options || []).map((option, index) => ({
          content: option.content,
          isCorrect: option.isCorrect,
          orderIndex: option.optionOrder || option.orderIndex || index
          // 不包含其他字段，如questionId等
        }));

        setEditingOptions({ [questionId]: cleanOptions });
      }
      setExpandedQuestionId(questionId);
    }
  };

  // 打开删除确认对话框
  const openDeleteDialog = (questionId: number) => {
    setQuestionToDelete(questionId);
    setDeleteDialogOpen(true);
  };

  // 处理删除
  const handleDelete = async () => {
    if (!questionToDelete) return;

    try {
      // 先检查题目是否被任何题组引用
      const checkResult = await questionService.checkQuestionReferences(questionToDelete, institutionId);

      if (checkResult.isReferenced) {
        // 如果题目被引用，显示更友好的错误信息
        toast.error('无法删除题目：该题目正在被一个或多个题组使用。请先从题组中移除此题目，然后再尝试删除。');
        setDeleteDialogOpen(false);
        setQuestionToDelete(null);
        return;
      }

      // 如果没有被引用，正常执行删除
      await questionService.deleteQuestion(questionToDelete);
      toast.success('删除成功');
      await fetchQuestions();
      setDeleteDialogOpen(false);
      setQuestionToDelete(null);
    } catch (error: any) {
      console.error('删除题目失败:', error);

      // 处理外键约束错误
      if (error.response?.data?.message?.includes('foreign key constraint fails') ||
          error.message?.includes('DataIntegrityViolationException')) {
        toast.error('无法删除题目：该题目正在被一个或多个题组使用。请先从题组中移除此题目，然后再尝试删除。');
      } else {
        toast.error('删除失败: ' + (error.message || '未知错误'));
      }
    }
  };

  // 处理解析编辑
  const handleAnalysisChange = (questionId: number, analysis: string) => {
    setEditingAnalysis({
      ...editingAnalysis,
      [questionId]: analysis
    });
  };

  // 处理选项编辑 - 内容
  const handleOptionContentChange = (questionId: number, optionIndex: number, content: string) => {
    const options = [...(editingOptions[questionId] || [])];
    options[optionIndex] = { ...options[optionIndex], content };
    setEditingOptions({
      ...editingOptions,
      [questionId]: options
    });
  };

  // 处理选项编辑 - 正确性
  const handleOptionCorrectChange = (questionId: number, optionIndex: number, isCorrect: boolean, questionType: QuestionType) => {
    const options = [...(editingOptions[questionId] || [])];

    // 如果是单选题，先重置所有选项为不正确
    if (questionType === QuestionType.SINGLE_CHOICE && isCorrect) {
      options.forEach(option => option.isCorrect = false);
    }

    options[optionIndex] = { ...options[optionIndex], isCorrect };
    setEditingOptions({
      ...editingOptions,
      [questionId]: options
    });
  };

  // 添加选项
  const handleAddOption = (questionId: number) => {
    const options = [...(editingOptions[questionId] || [])];
    options.push({
      content: '',
      isCorrect: false,
      orderIndex: options.length
    });
    setEditingOptions({
      ...editingOptions,
      [questionId]: options
    });
  };

  // 删除选项
  const handleRemoveOption = (questionId: number, optionIndex: number) => {
    const options = [...(editingOptions[questionId] || [])];
    if (options.length <= 2) {
      toast.error('选择题至少需要两个选项');
      return;
    }

    const updatedOptions = options.filter((_, i) => i !== optionIndex)
      .map((opt, i) => ({ ...opt, orderIndex: i }));

    setEditingOptions({
      ...editingOptions,
      [questionId]: updatedOptions
    });
  };

  // 保存修改
  const handleSaveChanges = async (questionId: number) => {
    const question = questions.find(q => q.id === questionId);
    if (!question) return;

    const options = editingOptions[questionId] || [];
    const analysis = editingAnalysis[questionId] || '';

    // 验证选项
    if ((question.type === QuestionType.SINGLE_CHOICE || question.type === QuestionType.MULTIPLE_CHOICE) && options.length < 2) {
      toast.error('选项数量不足，请至少添加两个选项');
      return;
    }

    // 验证单选题必须有一个正确答案
    if (question.type === QuestionType.SINGLE_CHOICE && !options.some(opt => opt.isCorrect)) {
      toast.error('单选题必须有一个正确答案');
      return;
    }

    // 验证多选题必须有至少一个正确答案
    if (question.type === QuestionType.MULTIPLE_CHOICE && !options.some(opt => opt.isCorrect)) {
      toast.error('多选题必须至少有一个正确答案');
      return;
    }

    // 验证所有选项都有内容
    if (options.some(opt => !opt.content.trim())) {
      toast.error('所有选项都必须填写内容');
      return;
    }

    setIsSubmitting(true);

    try {
      // 从原始题目中获取分值，如果不存在则使用默认值
      // @ts-ignore - 处理可能在Question类型中不存在score字段的问题
      const score = question.score !== undefined ? question.score : 1;

      // 处理选项，确保只包含后端需要的字段
      const cleanOptions = options.map((option, index) => ({
        content: option.content,
        isCorrect: option.isCorrect,
        orderIndex: option.optionOrder || index,
        // 不包含其他字段，如questionId、optionOrder等
      }));

      // 只包含后端需要的字段
      const data = {
        id: question.id,
        title: question.title,
        content: question.description || '',
        type: question.type,
        difficulty: question.difficulty,
        options: cleanOptions,
        answer: question.answer,
        analysis: analysis,
        institutionId: institutionId,
        score: score
      };

      // 确保移除description字段，避免后端反序列化错误
      delete (data as any).description;

      // 确保不包含tagIds字段，后端DTO中没有这个字段
      delete (data as any).tagIds;

      console.log('提交更新数据:', data);
      const response = await questionService.updateQuestion(questionId, data);
      console.log('更新成功:', response);
      toast.success('题目更新成功');

      // 刷新题目列表
      await fetchQuestions();
      // 关闭编辑状态
      setExpandedQuestionId(null);
      setEditingAnalysis({});
      setEditingOptions({});
    } catch (error: any) {
      console.error('更新题目失败:', error);
      if (error.response?.data?.message) {
        toast.error(`更新失败: ${error.response.data.message}`);
      } else {
        toast.error('更新失败，请检查数据格式');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // 渲染选项编辑器
  const renderOptionEditor = (questionId: number, questionType: QuestionType) => {
    const options = editingOptions[questionId] || [];

    return (
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-medium">选项</h3>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => handleAddOption(questionId)}
          >
            <Plus className="h-3 w-3 mr-1" />
            添加选项
          </Button>
        </div>

        <div className="space-y-2">
          {questionType === QuestionType.SINGLE_CHOICE ? (
            <RadioGroup
              value={options.findIndex(opt => opt.isCorrect) >= 0 ?
                options.findIndex(opt => opt.isCorrect).toString() : ""}
              onValueChange={(value) => {
                // 直接更新所有选项，而不是调用handleOptionCorrectChange
                const updatedOptions = options.map((option, idx) => ({
                  ...option,
                  isCorrect: idx === parseInt(value)
                }));

                setEditingOptions({
                  ...editingOptions,
                  [questionId]: updatedOptions
                });
              }}
            >
              {options.map((option, index) => (
                <div key={index} className="flex items-center gap-2">
                  <Input
                    value={option.content}
                    onChange={(e) => handleOptionContentChange(questionId, index, e.target.value)}
                    placeholder={`选项 ${index + 1}`}
                    className="flex-1"
                  />

                  <div className="flex items-center">
                    <RadioGroupItem value={index.toString()} id={`option-${questionId}-${index}`} />
                    <Label htmlFor={`option-${questionId}-${index}`} className="ml-2">
                      {option.isCorrect ? '正确答案' : ''}
                    </Label>
                  </div>

                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleRemoveOption(questionId, index);
                    }}
                    title="删除选项"
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </RadioGroup>
          ) : (
            // 多选题保持原样
            options.map((option, index) => (
              <div key={index} className="flex items-center gap-2">
                <Input
                  value={option.content}
                  onChange={(e) => handleOptionContentChange(questionId, index, e.target.value)}
                  placeholder={`选项 ${index + 1}`}
                  className="flex-1"
                />

                <Checkbox
                  checked={option.isCorrect}
                  onCheckedChange={(checked) => {
                    handleOptionCorrectChange(
                      questionId,
                      index,
                      !!checked,
                      QuestionType.MULTIPLE_CHOICE
                    );
                  }}
                />

                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleRemoveOption(questionId, index);
                  }}
                  title="删除选项"
                >
                  <Trash2 className="h-4 w-4 text-destructive" />
                </Button>
              </div>
            ))
          )}
        </div>
      </div>
    );
  };

  // 判断题渲染
  const renderTrueFalseEditor = (questionId: number) => {
    const options = editingOptions[questionId] || [];

    if (options.length !== 2) {
      // 初始化选项
      const updatedOptions = [
        { content: '正确', isCorrect: options.some(o => o.content === '正确' && o.isCorrect), orderIndex: 0 },
        { content: '错误', isCorrect: options.some(o => o.content === '错误' && o.isCorrect), orderIndex: 1 }
      ];
      setEditingOptions({
        ...editingOptions,
        [questionId]: updatedOptions
      });
    }

    return (
      <div className="space-y-2">
        <h3 className="text-sm font-medium">答案</h3>

        <RadioGroup
          value={options.findIndex(opt => opt.isCorrect).toString()}
          onValueChange={(value) => {
            const updatedOptions = [
              { content: '正确', isCorrect: value === '0', orderIndex: 0 },
              { content: '错误', isCorrect: value === '1', orderIndex: 1 }
            ];
            setEditingOptions({
              ...editingOptions,
              [questionId]: updatedOptions
            });
          }}
        >
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="0" id={`true-${questionId}`} />
              <Label htmlFor={`true-${questionId}`}>正确</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="1" id={`false-${questionId}`} />
              <Label htmlFor={`false-${questionId}`}>错误</Label>
            </div>
          </div>
        </RadioGroup>
      </div>
    );
  };

  return (
    <div className="space-y-3">
      {/* 搜索和筛选区域 */}
      <div className="flex flex-wrap items-center gap-2 mb-4">
        <div className="flex-1 flex flex-wrap items-center gap-2">
          <div className="min-w-[180px] w-full sm:w-auto">
            <Input
              placeholder="搜索题目..."
              value={searchTitle}
              onChange={(e) => setSearchTitle(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>
          <Select value={filterTypeLocal?.toString() || 'all'} onValueChange={handleTypeChange}>
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="题目类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部类型</SelectItem>
              <SelectItem value={QuestionType.SINGLE_CHOICE.toString()}>
                {getQuestionTypeText(QuestionType.SINGLE_CHOICE)}
              </SelectItem>
              <SelectItem value={QuestionType.MULTIPLE_CHOICE.toString()}>
                {getQuestionTypeText(QuestionType.MULTIPLE_CHOICE)}
              </SelectItem>
              <SelectItem value={QuestionType.TRUE_FALSE.toString()}>
                {getQuestionTypeText(QuestionType.TRUE_FALSE)}
              </SelectItem>
              <SelectItem value={QuestionType.FILL_BLANK.toString()}>
                {getQuestionTypeText(QuestionType.FILL_BLANK)}
              </SelectItem>
              <SelectItem value={QuestionType.SHORT_ANSWER.toString()}>
                {getQuestionTypeText(QuestionType.SHORT_ANSWER)}
              </SelectItem>
            </SelectContent>
          </Select>
          <Select value={filterDifficulty?.toString() || 'all'} onValueChange={handleDifficultyChange}>
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="难度级别" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部难度</SelectItem>
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
          <Select value={selectedTagId || 'all'} onValueChange={handleTagChange}>
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="标签筛选" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部标签</SelectItem>
              {tags.map(tag => (
                <SelectItem key={tag.id} value={tag.id.toString()}>
                  {tag.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button onClick={handleSearch} size="sm">
            <Search className="h-4 w-4 mr-1" />
            搜索
          </Button>
        </div>
      </div>

      {/* 问题列表 */}
      <div className="space-y-1">
        {isLoading ? (
          // 加载骨架屏
          <div className="border rounded-md overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="bg-muted">
                  <th className="px-4 py-2 text-left font-medium text-sm">题目</th>
                  <th className="px-4 py-2 text-left font-medium text-sm w-24">类型</th>
                  <th className="px-4 py-2 text-left font-medium text-sm w-24">难度</th>
                  <th className="px-4 py-2 text-right font-medium text-sm w-20">操作</th>
                </tr>
              </thead>
              <tbody>
                {Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i} className="border-t">
                    <td className="px-4 py-3">
                      <Skeleton className="h-4 w-3/4" />
                    </td>
                    <td className="px-4 py-3">
                      <Skeleton className="h-4 w-full" />
                    </td>
                    <td className="px-4 py-3">
                      <Skeleton className="h-4 w-full" />
                    </td>
                    <td className="px-4 py-3">
                      <Skeleton className="h-4 w-full" />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : questions.length === 0 ? (
          // 空状态
          <div className="p-8 flex flex-col items-center justify-center border rounded-md bg-gray-50">
            <AlertCircle className="h-6 w-6 text-muted-foreground mb-1" />
            <p className="text-center text-muted-foreground text-sm">未找到匹配的题目</p>
          </div>
        ) : (
          // 使用表格布局展示问题列表
          <div className="border rounded-md overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="bg-muted">
                  <th className="px-4 py-2 text-left font-medium text-sm">题目</th>
                  <th className="px-4 py-2 text-left font-medium text-sm w-24">类型</th>
                  <th className="px-4 py-2 text-left font-medium text-sm w-24">难度</th>
                  <th className="px-4 py-2 text-right font-medium text-sm w-20">操作</th>
                </tr>
              </thead>
              <tbody>
                {questions.map((question, index) => {
                  const isExpanded = expandedQuestionId === question.id;

                  // 为不同题型设置不同颜色样式
                  const getTypeStyle = (type: QuestionType) => {
                    switch(type) {
                      case QuestionType.SINGLE_CHOICE: return "bg-blue-50 text-blue-700";
                      case QuestionType.MULTIPLE_CHOICE: return "bg-purple-50 text-purple-700";
                      case QuestionType.TRUE_FALSE: return "bg-green-50 text-green-700";
                      case QuestionType.FILL_BLANK: return "bg-orange-50 text-orange-700";
                      case QuestionType.SHORT_ANSWER: return "bg-pink-50 text-pink-700";
                      default: return "bg-gray-50 text-gray-700";
                    }
                  };

                  // 为不同难度设置不同样式
                  const getDifficultyStyle = (difficulty: QuestionDifficulty) => {
                    switch(difficulty) {
                      case QuestionDifficulty.EASY: return "bg-green-500 text-white";
                      case QuestionDifficulty.MEDIUM: return "bg-black text-white";
                      case QuestionDifficulty.HARD: return "bg-red-500 text-white";
                      default: return "bg-gray-500 text-white";
                    }
                  };

                  return (
                    <React.Fragment key={question.id}>
                      <tr
                        className={`border-t hover:bg-muted/50 cursor-pointer ${isExpanded ? 'bg-muted/40' : ''}`}
                        onClick={() => handleToggleExpand(question.id)}
                      >
                        <td className="px-4 py-3">
                          <div className="flex flex-col">
                            <div className="flex items-center gap-2">
                              <span className="font-medium text-sm">{question.title}</span>
                              {/* 分值标签 */}
                              <span className="text-xs px-1.5 py-0.5 rounded bg-blue-100 text-blue-800">
                                {question.score || 1}分
                              </span>
                            </div>
                            {question.content && (
                              <p className="text-sm mt-1">
                                {question.content}
                              </p>
                            )}
                            {question.description && (
                              <p className="text-xs text-muted-foreground line-clamp-1 mt-0.5">
                                {question.description}
                              </p>
                            )}
                            {/* 标签显示 */}
                            {question.tags && question.tags.length > 0 && (
                              <div className="flex flex-wrap gap-1 mt-1">
                                {question.tags.slice(0, 3).map(tag => (
                                  <Badge key={tag.id} variant="outline" className="text-xs px-1 py-0">
                                    {tag.name}
                                  </Badge>
                                ))}
                                {question.tags.length > 3 && (
                                  <span className="text-xs text-muted-foreground">+{question.tags.length - 3}</span>
                                )}
                              </div>
                            )}
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex text-xs px-2 py-1 rounded-md ${getTypeStyle(question.type)}`}>
                            {getQuestionTypeText(question.type)}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex text-xs px-2 py-1 rounded-md ${getDifficultyStyle(question.difficulty)}`}>
                            {getQuestionDifficultyText(question.difficulty)}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <div className="flex justify-end items-center">
                            {isExpanded ? (
                              <ChevronUp className="h-4 w-4 text-muted-foreground" />
                            ) : (
                              <ChevronDown className="h-4 w-4 text-muted-foreground" />
                            )}
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                                <Button variant="ghost" size="sm" className="h-8 w-8 p-0 ml-1">
                                  <MoreHorizontal className="h-4 w-4" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="end">
                                <DropdownMenuItem onClick={() => handleToggleExpand(question.id)}>
                                  <Eye className="h-4 w-4 mr-2" />
                                  展开详情
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => router.push(`/dashboard/questions/${question.id}`)}>
                                  <Edit className="h-4 w-4 mr-2" />
                                  编辑全部
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => openDeleteDialog(question.id)}>
                                  <Trash2 className="h-4 w-4 mr-2" />
                                  删除
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          </div>
                        </td>
                      </tr>

                      {/* 展开的编辑区域 */}
                      {isExpanded && (
                        <tr>
                          <td colSpan={4} className="bg-gray-50 border-t border-dashed">
                            <div className="p-4 space-y-3">
                              {/* 选项编辑区域 */}
                              {(question.type === QuestionType.SINGLE_CHOICE ||
                                question.type === QuestionType.MULTIPLE_CHOICE) && (
                                renderOptionEditor(question.id, question.type)
                              )}

                              {/* 判断题编辑区域 */}
                              {question.type === QuestionType.TRUE_FALSE && (
                                renderTrueFalseEditor(question.id)
                              )}

                              {/* 解析编辑区域 */}
                              <div className="space-y-1">
                                <Label className="text-xs">题目解析</Label>
                                <Textarea
                                  value={
                                    editingAnalysis[question.id] !== undefined
                                      ? editingAnalysis[question.id]
                                      : question.analysis || ''
                                  }
                                  onChange={(e) => handleAnalysisChange(question.id, e.target.value)}
                                  placeholder="输入题目解析"
                                  className="min-h-[80px] text-sm"
                                />
                              </div>

                              {/* 保存按钮 */}
                              <div className="flex justify-end pt-2">
                                <Button
                                  variant="default"
                                  size="sm"
                                  onClick={() => handleSaveChanges(question.id)}
                                  disabled={isSubmitting}
                                >
                                  {isSubmitting ? (
                                    <>保存中...</>
                                  ) : (
                                    <>
                                      <Save className="h-3.5 w-3.5 mr-1" />
                                      保存修改
                                    </>
                                  )}
                                </Button>
                              </div>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* 分页控件 */}
      {!isLoading && questions.length > 0 && (
        <div className="flex justify-between items-center mt-4">
          <div className="text-xs text-muted-foreground">
            共 {totalQuestions} 条记录，当前第 {currentPage} 页，每页 {pageSize} 条
          </div>
          <div className="flex items-center space-x-1">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
              disabled={currentPage <= 1}
              className="h-7 px-2 text-xs"
            >
              上一页
            </Button>
            <div className="flex items-center">
              {Array.from({ length: Math.min(5, Math.ceil(totalQuestions / pageSize)) }, (_, i) => {
                // 计算要显示的页码
                let pageNum = 1;
                const totalPages = Math.ceil(totalQuestions / pageSize);

                if (totalPages <= 5) {
                  // 如果总页数少于5，显示所有页
                  pageNum = i + 1;
                } else if (currentPage <= 3) {
                  // 当前页接近开始
                  pageNum = i + 1;
                } else if (currentPage >= totalPages - 2) {
                  // 当前页接近结束
                  pageNum = totalPages - 4 + i;
                } else {
                  // 当前页在中间
                  pageNum = currentPage - 2 + i;
                }

                return (
                  <Button
                    key={i}
                    variant={pageNum === currentPage ? "default" : "outline"}
                    size="sm"
                    className="h-7 w-7 p-0 mx-0.5 text-xs"
                    onClick={() => setCurrentPage(pageNum)}
                  >
                    {pageNum}
                  </Button>
                );
              })}
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage(Math.min(Math.ceil(totalQuestions / pageSize), currentPage + 1))}
              disabled={currentPage >= Math.ceil(totalQuestions / pageSize)}
              className="h-7 px-2 text-xs"
            >
              下一页
            </Button>
          </div>
        </div>
      )}

      {/* 删除确认对话框 */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              您确定要删除这道题目吗？此操作不可撤销。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete}>
              确认删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}