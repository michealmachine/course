'use client';

import { useState, useEffect } from 'react';
import { AlertCircle, Check, ChevronRight, CircleCheck, CircleX, Clock, Loader2, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Separator } from '@/components/ui/separator';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { formatTime } from '@/utils/format';
import { useToast } from '@/components/ui/use-toast';
import { toast as sonnerToast } from 'sonner';
import { QuestionGroupVO, QuestionVO, QuestionType, UserQuestionAnswerDTO } from '@/types/learning';
import { learningService } from '@/services';

interface CourseQuestionGroupProps {
  questionGroup: QuestionGroupVO;
  onComplete: (isAllCorrect: boolean) => void;
  onError: (error: string) => void;
}

export function CourseQuestionGroup({
  questionGroup,
  onComplete,
  onError
}: CourseQuestionGroupProps) {
  const { toast } = useToast();
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string[]>>({});
  const [submitted, setSubmitted] = useState<Record<number, boolean>>({});
  const [results, setResults] = useState<Record<number, boolean>>({});
  const [elapsedTime, setElapsedTime] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [allCompleted, setAllCompleted] = useState(false);
  
  const questions = questionGroup.questions || [];
  const currentQuestion = questions[currentQuestionIndex];
  
  // 计时器
  useEffect(() => {
    const timer = setInterval(() => {
      setElapsedTime(prev => prev + 1);
    }, 1000);
    
    return () => clearInterval(timer);
  }, []);
  
  // 获取问题类型
  const getQuestionType = (type: string): QuestionType => {
    switch (type.toUpperCase()) {
      case 'SINGLE_CHOICE': return QuestionType.SINGLE_CHOICE;
      case 'MULTIPLE_CHOICE': return QuestionType.MULTIPLE_CHOICE;
      case 'TRUE_FALSE': return QuestionType.TRUE_FALSE;
      case 'FILL_BLANK': return QuestionType.FILL_BLANK;
      case 'SHORT_ANSWER': return QuestionType.SHORT_ANSWER;
      default: return QuestionType.SINGLE_CHOICE;
    }
  };
  
  // 提交问题答案
  const submitAnswer = async (questionId: number) => {
    if (!currentQuestion || isSubmitting) return;
    
    const userAnswers = answers[questionId] || [];
    if (userAnswers.length === 0) {
      sonnerToast.error("请至少选择一个选项再提交");
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      const answerDto: UserQuestionAnswerDTO = {
        questionId,
        answers: userAnswers,
        correctAnswers: [], // 这个字段会由后端填充
        questionType: getQuestionType(currentQuestion.type),
        questionTitle: currentQuestion.title,
        duration: elapsedTime,
        isWrong: false // 这个字段会由后端判断
      };
      
      const result = await learningService.submitQuestionAnswer(questionGroup.sectionId, answerDto);
      
      // 更新状态
      setSubmitted(prev => ({ ...prev, [questionId]: true }));
      setResults(prev => ({ ...prev, [questionId]: result.correct }));
      
      // 如果回答错误且类型是选择题，则记录到错题本
      if (!result.correct && 
          (currentQuestion.type === 'SINGLE_CHOICE' || 
           currentQuestion.type === 'MULTIPLE_CHOICE' || 
           currentQuestion.type === 'TRUE_FALSE')) {
        // 后端会自动记录错题
        sonnerToast.success("此题已添加到您的错题本中，可以稍后复习");
      }
      
      // 重置计时器
      setElapsedTime(0);
      
      // 检查是否所有题目都已完成
      const newSubmittedState = { ...submitted, [questionId]: true };
      const isAllSubmitted = questions.every(q => newSubmittedState[q.id]);
      
      if (isAllSubmitted) {
        setAllCompleted(true);
        // 检查是否全部正确
        const isAllCorrect = questions.every(q => results[q.id] || (q.id === questionId && result.correct));
        onComplete(isAllCorrect);
      }
      
    } catch (err) {
      console.error('提交答案失败', err);
      onError('提交答案失败，请稍后重试');
    } finally {
      setIsSubmitting(false);
    }
  };
  
  // 处理单选题答案
  const handleSingleChoiceChange = (value: string) => {
    if (!currentQuestion || submitted[currentQuestion.id]) return;
    setAnswers(prev => ({
      ...prev,
      [currentQuestion.id]: [value]
    }));
  };
  
  // 处理多选题答案
  const handleMultipleChoiceChange = (checked: boolean, value: string) => {
    if (!currentQuestion || submitted[currentQuestion.id]) return;
    
    setAnswers(prev => {
      const currentAnswers = prev[currentQuestion.id] || [];
      if (checked) {
        return {
          ...prev,
          [currentQuestion.id]: [...currentAnswers, value]
        };
      } else {
        return {
          ...prev,
          [currentQuestion.id]: currentAnswers.filter(a => a !== value)
        };
      }
    });
  };
  
  // 处理判断题答案
  const handleTrueFalseChange = (value: string) => {
    if (!currentQuestion || submitted[currentQuestion.id]) return;
    setAnswers(prev => ({
      ...prev,
      [currentQuestion.id]: [value]
    }));
  };
  
  // 处理填空题答案
  const handleFillBlankChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (!currentQuestion || submitted[currentQuestion.id]) return;
    const value = event.target.value.trim();
    setAnswers(prev => ({
      ...prev,
      [currentQuestion.id]: value ? [value] : []
    }));
  };
  
  // 处理简答题答案
  const handleShortAnswerChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (!currentQuestion || submitted[currentQuestion.id]) return;
    const value = event.target.value.trim();
    setAnswers(prev => ({
      ...prev,
      [currentQuestion.id]: value ? [value] : []
    }));
  };
  
  // 进入下一题
  const goToNextQuestion = () => {
    if (currentQuestionIndex < questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
      setElapsedTime(0);
    }
  };
  
  // 进入上一题
  const goToPreviousQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1);
    }
  };
  
  // 渲染选项
  const renderOptions = (question: QuestionVO) => {
    const questionId = question.id;
    const isSubmittedQuestion = submitted[questionId];
    const isCorrect = results[questionId];
    const userAnswers = answers[questionId] || [];
    
    switch (question.type.toUpperCase()) {
      case 'SINGLE_CHOICE':
        return (
          <RadioGroup
            value={userAnswers[0] || ''}
            onValueChange={handleSingleChoiceChange}
            disabled={isSubmittedQuestion}
            className="space-y-3 mt-4"
          >
            {question.options?.map((option, index) => {
              const optionValue = String.fromCharCode(65 + index); // A, B, C, D...
              const isSelected = userAnswers.includes(optionValue);
              const isCorrectOption = isSubmittedQuestion && question.correctOptions?.includes(optionValue);
              
              return (
                <div 
                  key={index} 
                  className={`flex items-start space-x-2 p-3 rounded-md ${
                    isSubmittedQuestion && isSelected && isCorrectOption 
                      ? 'bg-green-50 dark:bg-green-950/20' 
                      : isSubmittedQuestion && isSelected && !isCorrectOption 
                        ? 'bg-red-50 dark:bg-red-950/20' 
                        : isSubmittedQuestion && !isSelected && isCorrectOption 
                          ? 'bg-blue-50 dark:bg-blue-950/20' 
                          : ''
                  }`}
                >
                  <RadioGroupItem 
                    value={optionValue} 
                    id={`option-${questionId}-${index}`} 
                    disabled={isSubmittedQuestion}
                  />
                  <div className="grid gap-1.5 leading-none w-full">
                    <div className="flex items-center justify-between">
                      <Label 
                        htmlFor={`option-${questionId}-${index}`}
                        className={`text-sm font-medium leading-none peer-disabled:cursor-not-allowed 
                          peer-disabled:opacity-70 flex-1 ${
                            isSubmittedQuestion && isCorrectOption ? 'text-green-600 dark:text-green-400' : ''
                          }`}
                      >
                        {optionValue}. {option}
                      </Label>
                      {isSubmittedQuestion && isSelected && isCorrectOption && (
                        <CircleCheck className="h-5 w-5 text-green-600 dark:text-green-400" />
                      )}
                      {isSubmittedQuestion && isSelected && !isCorrectOption && (
                        <CircleX className="h-5 w-5 text-red-600 dark:text-red-400" />
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </RadioGroup>
        );
      
      case 'MULTIPLE_CHOICE':
        return (
          <div className="space-y-3 mt-4">
            {question.options?.map((option, index) => {
              const optionValue = String.fromCharCode(65 + index); // A, B, C, D...
              const isSelected = userAnswers.includes(optionValue);
              const isCorrectOption = question.correctOptions?.includes(optionValue);
              
              return (
                <div 
                  key={index} 
                  className={`flex items-start space-x-2 p-3 rounded-md ${
                    isSubmittedQuestion && isSelected && isCorrectOption 
                      ? 'bg-green-50 dark:bg-green-950/20' 
                      : isSubmittedQuestion && isSelected && !isCorrectOption 
                        ? 'bg-red-50 dark:bg-red-950/20' 
                        : isSubmittedQuestion && !isSelected && isCorrectOption 
                          ? 'bg-blue-50 dark:bg-blue-950/20' 
                          : ''
                  }`}
                >
                  <Checkbox 
                    id={`option-${questionId}-${index}`}
                    checked={isSelected}
                    onCheckedChange={(checked) => 
                      handleMultipleChoiceChange(checked as boolean, optionValue)
                    }
                    disabled={isSubmittedQuestion}
                  />
                  <div className="grid gap-1.5 leading-none w-full">
                    <div className="flex items-center justify-between">
                      <Label 
                        htmlFor={`option-${questionId}-${index}`}
                        className={`text-sm font-medium leading-none peer-disabled:cursor-not-allowed 
                          peer-disabled:opacity-70 flex-1 ${
                            isSubmittedQuestion && isCorrectOption ? 'text-green-600 dark:text-green-400' : ''
                          }`}
                      >
                        {optionValue}. {option}
                      </Label>
                      {isSubmittedQuestion && isCorrectOption && (
                        <CircleCheck className="h-5 w-5 text-green-600 dark:text-green-400" />
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        );
      
      case 'TRUE_FALSE':
        return (
          <RadioGroup
            value={userAnswers[0] || ''}
            onValueChange={handleTrueFalseChange}
            disabled={isSubmittedQuestion}
            className="space-y-3 mt-4"
          >
            {[
              { value: 'T', label: '正确' },
              { value: 'F', label: '错误' }
            ].map((option) => {
              const isSelected = userAnswers.includes(option.value);
              const isCorrectOption = question.correctOptions?.includes(option.value);
              
              return (
                <div 
                  key={option.value} 
                  className={`flex items-start space-x-2 p-3 rounded-md ${
                    isSubmittedQuestion && isSelected && isCorrectOption 
                      ? 'bg-green-50 dark:bg-green-950/20' 
                      : isSubmittedQuestion && isSelected && !isCorrectOption 
                        ? 'bg-red-50 dark:bg-red-950/20' 
                        : isSubmittedQuestion && !isSelected && isCorrectOption 
                          ? 'bg-blue-50 dark:bg-blue-950/20' 
                          : ''
                  }`}
                >
                  <RadioGroupItem 
                    value={option.value} 
                    id={`option-${questionId}-${option.value}`} 
                    disabled={isSubmittedQuestion}
                  />
                  <div className="grid gap-1.5 leading-none w-full">
                    <div className="flex items-center justify-between">
                      <Label 
                        htmlFor={`option-${questionId}-${option.value}`}
                        className={`text-sm font-medium leading-none peer-disabled:cursor-not-allowed 
                          peer-disabled:opacity-70 flex-1 ${
                            isSubmittedQuestion && isCorrectOption ? 'text-green-600 dark:text-green-400' : ''
                          }`}
                      >
                        {option.label}
                      </Label>
                      {isSubmittedQuestion && isSelected && isCorrectOption && (
                        <CircleCheck className="h-5 w-5 text-green-600 dark:text-green-400" />
                      )}
                      {isSubmittedQuestion && isSelected && !isCorrectOption && (
                        <CircleX className="h-5 w-5 text-red-600 dark:text-red-400" />
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </RadioGroup>
        );
      
      case 'FILL_BLANK':
      case 'SHORT_ANSWER':
        const handleChange = question.type.toUpperCase() === 'FILL_BLANK' 
          ? handleFillBlankChange 
          : handleShortAnswerChange;
        
        return (
          <div className="space-y-4 mt-4">
            <Textarea
              placeholder={`请输入${question.type.toUpperCase() === 'FILL_BLANK' ? '填空' : '简答'}内容...`}
              value={userAnswers[0] || ''}
              onChange={handleChange}
              disabled={isSubmittedQuestion}
              rows={question.type.toUpperCase() === 'FILL_BLANK' ? 3 : 6}
            />
            
            {isSubmittedQuestion && (
              <div className="mt-4">
                <div className="font-medium mb-2">参考答案:</div>
                <div className="p-3 bg-muted rounded-md">
                  {question.correctOptions?.map((answer, index) => (
                    <div key={index}>{answer}</div>
                  ))}
                </div>
                
                <div className="font-medium mt-4 mb-2">您的答案:</div>
                <div className={`p-3 rounded-md ${isCorrect ? 'bg-green-50 dark:bg-green-950/20' : 'bg-red-50 dark:bg-red-950/20'}`}>
                  {userAnswers[0] || '(未作答)'}
                </div>
                
                <div className="flex items-center mt-4">
                  <Badge variant={isCorrect ? "success" : "destructive"}>
                    {isCorrect ? '回答正确' : '回答错误'}
                  </Badge>
                  {!isCorrect && question.explanation && (
                    <div className="ml-4 text-sm text-muted-foreground">{question.explanation}</div>
                  )}
                </div>
              </div>
            )}
          </div>
        );
      
      default:
        return <div className="text-red-500">不支持的题目类型: {question.type}</div>;
    }
  };
  
  // 如果没有问题，显示错误信息
  if (questions.length === 0) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>无法加载题目</AlertTitle>
        <AlertDescription>当前章节没有可用的题目或测验</AlertDescription>
      </Alert>
    );
  }
  
  return (
    <div className="space-y-6">
      {/* 问题组标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">{questionGroup.title}</h2>
          <p className="text-muted-foreground">{questionGroup.description}</p>
        </div>
        <div className="flex items-center space-x-2">
          <Clock className="h-5 w-5 text-muted-foreground" />
          <span className="text-sm font-medium">{formatTime(elapsedTime)}</span>
        </div>
      </div>
      
      <Separator />
      
      {/* 进度指示器 */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-muted-foreground">
          题目 {currentQuestionIndex + 1} / {questions.length}
        </div>
        <div className="flex space-x-1">
          {questions.map((q, index) => (
            <div
              key={q.id}
              className={`h-2 w-8 rounded-full cursor-pointer transition-colors ${
                index === currentQuestionIndex
                  ? 'bg-primary'
                  : submitted[q.id]
                    ? results[q.id] 
                      ? 'bg-green-500' 
                      : 'bg-red-500'
                    : 'bg-muted'
              }`}
              onClick={() => setCurrentQuestionIndex(index)}
            />
          ))}
        </div>
      </div>
      
      {/* 当前问题 */}
      {currentQuestion && (
        <Card>
          <CardHeader>
            <div className="flex items-start justify-between">
              <div>
                <Badge variant="outline" className="mb-2">
                  {currentQuestion.type === 'SINGLE_CHOICE' && '单选题'}
                  {currentQuestion.type === 'MULTIPLE_CHOICE' && '多选题'}
                  {currentQuestion.type === 'TRUE_FALSE' && '判断题'}
                  {currentQuestion.type === 'FILL_BLANK' && '填空题'}
                  {currentQuestion.type === 'SHORT_ANSWER' && '简答题'}
                </Badge>
                <CardTitle className="text-lg">
                  {currentQuestionIndex + 1}. {currentQuestion.title}
                </CardTitle>
                {currentQuestion.content && (
                  <CardDescription className="mt-2 whitespace-pre-line">
                    {currentQuestion.content}
                  </CardDescription>
                )}
              </div>
              {submitted[currentQuestion.id] && (
                <Badge variant={results[currentQuestion.id] ? "success" : "destructive"}>
                  {results[currentQuestion.id] ? '答对了' : '答错了'}
                </Badge>
              )}
            </div>
          </CardHeader>
          <CardContent>
            {renderOptions(currentQuestion)}
            
            {submitted[currentQuestion.id] && currentQuestion.explanation && !results[currentQuestion.id] && (
              <Alert className="mt-4">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>解析</AlertTitle>
                <AlertDescription>{currentQuestion.explanation}</AlertDescription>
              </Alert>
            )}
          </CardContent>
          <CardFooter className="flex justify-between">
            <Button
              variant="outline"
              onClick={goToPreviousQuestion}
              disabled={currentQuestionIndex === 0}
            >
              上一题
            </Button>
            
            <div>
              {!submitted[currentQuestion.id] ? (
                <Button
                  onClick={() => submitAnswer(currentQuestion.id)}
                  disabled={isSubmitting || !answers[currentQuestion.id]?.length}
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      提交中...
                    </>
                  ) : '提交答案'}
                </Button>
              ) : (
                <Button
                  onClick={goToNextQuestion}
                  disabled={currentQuestionIndex === questions.length - 1}
                >
                  下一题 <ChevronRight className="ml-1 h-4 w-4" />
                </Button>
              )}
            </div>
          </CardFooter>
        </Card>
      )}
      
      {/* 完成所有题目的提示 */}
      {allCompleted && (
        <Alert variant={questions.every(q => results[q.id]) ? "default" : "warning"}>
          {questions.every(q => results[q.id]) ? (
            <Check className="h-4 w-4" />
          ) : (
            <AlertCircle className="h-4 w-4" />
          )}
          <AlertTitle>
            {questions.every(q => results[q.id]) ? '全部答对！' : '练习完成'}
          </AlertTitle>
          <AlertDescription>
            {questions.every(q => results[q.id]) 
              ? '恭喜你，回答正确所有问题！你可以继续下一节课程。' 
              : '你已完成所有题目，但有些题目回答错误。错误的题目已添加到错题本，你可以稍后复习。'}
          </AlertDescription>
        </Alert>
      )}
    </div>
  );
} 