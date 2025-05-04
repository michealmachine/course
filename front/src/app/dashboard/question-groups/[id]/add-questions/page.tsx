'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ArrowLeft, Plus, Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Skeleton } from '@/components/ui/skeleton';

import { questionService, questionGroupService } from '@/services';
import { Question, QuestionDifficulty, QuestionType } from '@/types/question';
import { getQuestionTypeText, getQuestionDifficultyText, getQuestionDifficultyColor } from '@/utils/questionUtils';

interface PageProps {
  params: {
    id: string;
  };
}

export default function AddQuestionsPage({ params }: PageProps) {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [selectedQuestions, setSelectedQuestions] = useState<number[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedType, setSelectedType] = useState<string>('');
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 加载题目列表
  useEffect(() => {
    fetchQuestions();
  }, [searchKeyword, selectedType, selectedDifficulty]);

  // 获取题目列表
  const fetchQuestions = async () => {
    setIsLoading(true);
    try {
      const response = await questionService.getQuestionList({
        keyword: searchKeyword,
        type: selectedType && selectedType !== 'all' ? parseInt(selectedType) : undefined,
        difficulty: selectedDifficulty && selectedDifficulty !== 'all' ? parseInt(selectedDifficulty) : undefined,
        page: 0,
        pageSize: 100
      });
      setQuestions(response.content);
    } catch (error) {
      console.error('获取题目列表失败:', error);
      toast.error('获取题目列表失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 处理选择题目
  const handleSelectQuestion = (questionId: number, checked: boolean) => {
    if (checked) {
      setSelectedQuestions(prev => [...prev, questionId]);
    } else {
      setSelectedQuestions(prev => prev.filter(id => id !== questionId));
    }
  };

  // 处理全选
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedQuestions(questions.map(q => q.id));
    } else {
      setSelectedQuestions([]);
    }
  };

  // 返回题组详情
  const handleBack = () => {
    router.back();
  };

  // 添加选中的题目到题组
  const handleAddQuestions = async () => {
    if (selectedQuestions.length === 0) {
      toast.error('请选择要添加的题目');
      return;
    }

    setIsSubmitting(true);
    try {
      await Promise.all(
        selectedQuestions.map(questionId =>
          questionGroupService.addQuestionToGroup(parseInt(params.id), questionId)
        )
      );
      toast.success('添加成功');
      router.back();
    } catch (error) {
      console.error('添加题目失败:', error);
      toast.error('添加题目失败');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button variant="ghost" onClick={handleBack}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          返回
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>添加题目</CardTitle>
          <CardDescription>
            选择要添加到题组的题目
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* 搜索和筛选 */}
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex flex-1 items-center space-x-2">
              <Input
                placeholder="搜索题目..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    fetchQuestions();
                  }
                }}
                className="max-w-sm"
              />
              <Button
                variant="outline"
                size="icon"
                onClick={fetchQuestions}
              >
                <Search className="h-4 w-4" />
              </Button>
            </div>

            <div className="flex items-center space-x-2">
              <Select value={selectedType} onValueChange={setSelectedType}>
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

              <Select value={selectedDifficulty} onValueChange={setSelectedDifficulty}>
                <SelectTrigger className="w-[120px]">
                  <SelectValue placeholder="难度" />
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
            </div>
          </div>

          {/* 题目列表 */}
          {isLoading ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, index) => (
                <Skeleton key={index} className="h-16 w-full" />
              ))}
            </div>
          ) : (
            <div>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-[50px]">
                        <Checkbox
                          checked={
                            questions.length > 0 &&
                            selectedQuestions.length === questions.length
                          }
                          onCheckedChange={handleSelectAll}
                        />
                      </TableHead>
                      <TableHead>题目</TableHead>
                      <TableHead>类型</TableHead>
                      <TableHead>难度</TableHead>
                      <TableHead>标签</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {questions.map((question) => (
                      <TableRow key={question.id}>
                        <TableCell>
                          <Checkbox
                            checked={selectedQuestions.includes(question.id)}
                            onCheckedChange={(checked) =>
                              handleSelectQuestion(question.id, !!checked)
                            }
                          />
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col">
                            <span className="font-medium">{question.title}</span>
                            {question.content && (
                              <p className="text-sm mt-1">
                                {question.content}
                              </p>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>{getQuestionTypeText(question.type)}</TableCell>
                        <TableCell>
                          <Badge variant={getQuestionDifficultyColor(question.difficulty)}>
                            {getQuestionDifficultyText(question.difficulty)}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {question.tags?.map((tag) => (
                            <Badge key={tag.id} variant="outline" className="mr-1">
                              {tag.name}
                            </Badge>
                          ))}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {questions.length === 0 && (
                <div className="text-center py-8 text-muted-foreground">
                  暂无题目
                </div>
              )}

              {questions.length > 0 && (
                <div className="mt-4 flex justify-between items-center">
                  <div className="text-sm text-muted-foreground">
                    已选择 {selectedQuestions.length} 个题目
                  </div>
                  <Button
                    onClick={handleAddQuestions}
                    disabled={selectedQuestions.length === 0 || isSubmitting}
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    {isSubmitting ? '添加中...' : '添加到题组'}
                  </Button>
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}