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
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string[]>>({});
  const [submitted, setSubmitted] = useState<Record<number, boolean>>({});
  const [results, setResults] = useState<Record<number, boolean>>({});
  const [elapsedTime, setElapsedTime] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [allCompleted, setAllCompleted] = useState(false);
  
  // 本地处理后的题目列表
  const [processedQuestions, setProcessedQuestions] = useState<QuestionVO[]>([]);
  
  // 检查题组数据完整性并进行处理
  useEffect(() => {
    console.log("CourseQuestionGroup组件接收到题组数据:", questionGroup);
    
    if (!questionGroup) {
      console.error("题组数据为空");
      onError && onError("题组数据加载失败，请刷新页面重试");
      return;
    }
    
    // 判断使用哪个字段作为题目列表
    let questionsToUse: QuestionVO[] = [];
    
    if (questionGroup.questions && Array.isArray(questionGroup.questions)) {
      console.log("使用questions字段中的题目:", questionGroup.questions.length);
      questionsToUse = questionGroup.questions;
    } else if (questionGroup.items && Array.isArray(questionGroup.items)) {
      console.log("从items字段提取题目:", questionGroup.items.length);
      // 从items中提取questions
      questionsToUse = questionGroup.items
        .filter(item => item.question)
        .map(item => ({
          ...item.question!,
          id: item.questionId || item.question!.id,
          score: item.score || item.question!.score
        }));
    }
    
    if (questionsToUse.length === 0) {
      console.error("题组中没有题目:", questionGroup);
      onError && onError("题组中没有题目，请检查题组配置");
      return;
    }
    
    console.log("处理后的题目列表:", questionsToUse);
    setProcessedQuestions(questionsToUse);
    
    // 检查sectionId是否存在
    if (!questionGroup.sectionId) {
      console.warn("题组缺少sectionId字段，这可能导致提交答案时出错");
    }
  }, [questionGroup, onError]);
  
  // 使用processedQuestions替代直接从questionGroup获取questions
  const questions = processedQuestions;
  const currentQuestion = questions[currentQuestionIndex];
  
  // 计时器
  useEffect(() => {
    const timer = setInterval(() => {
      setElapsedTime(prev => prev + 1);
    }, 1000);
    
    return () => clearInterval(timer);
  }, []);
  
  // 获取问题类型
  const getQuestionType = (type: string | number): QuestionType => {
    // 将类型转换为字符串进行比较
    const typeStr = String(type).toUpperCase();
    
    // 根据数值或字符串判断题目类型
    if (typeStr === '1' || typeStr === 'SINGLE_CHOICE') return QuestionType.SINGLE_CHOICE;
    if (typeStr === '2' || typeStr === 'MULTIPLE_CHOICE') return QuestionType.MULTIPLE_CHOICE;
    if (typeStr === '3' || typeStr === 'TRUE_FALSE') return QuestionType.TRUE_FALSE;
    if (typeStr === '4' || typeStr === 'FILL_BLANK') return QuestionType.FILL_BLANK;
    if (typeStr === '5' || typeStr === 'SHORT_ANSWER') return QuestionType.SHORT_ANSWER;
    
    // 默认返回单选题
    console.warn(`未知题目类型 ${type}，默认当作单选题`);
    return QuestionType.SINGLE_CHOICE;
  };
  
  // 提交答案
  const submitAnswer = async (questionId: number, questionType: QuestionType | string | number, userAnswers: string[]) => {
    try {
      setIsSubmitting(true);
      
      if (!questionGroup || !questionGroup.sectionId) {
        const error = "无法提交答案：题组中没有sectionId";
        console.error(error, questionGroup);
        sonnerToast.error("提交失败", {
          description: error
        });
        return;
      }
      
      console.log(`准备提交答案 - 章节ID: ${questionGroup.sectionId}, 题目ID: ${questionId}, 用户答案:`, userAnswers);
      
      // 确定questionType类型 - 支持数字和字符串类型
      let questionTypeValue: QuestionType;
      
      if (typeof questionType === 'number') {
        switch (questionType) {
          case 1: questionTypeValue = QuestionType.SINGLE_CHOICE; break;
          case 2: questionTypeValue = QuestionType.MULTIPLE_CHOICE; break;
          case 3: questionTypeValue = QuestionType.TRUE_FALSE; break;
          case 4: questionTypeValue = QuestionType.FILL_BLANK; break;
          case 5: questionTypeValue = QuestionType.SHORT_ANSWER; break;
          default: questionTypeValue = QuestionType.SINGLE_CHOICE; // 默认为单选题
        }
      } else if (typeof questionType === 'string' && !Object.values(QuestionType).includes(questionType as QuestionType)) {
        // 处理字符串数字，如 "1", "2" 等
        switch (questionType) {
          case '1': questionTypeValue = QuestionType.SINGLE_CHOICE; break;
          case '2': questionTypeValue = QuestionType.MULTIPLE_CHOICE; break;
          case '3': questionTypeValue = QuestionType.TRUE_FALSE; break;
          case '4': questionTypeValue = QuestionType.FILL_BLANK; break;
          case '5': questionTypeValue = QuestionType.SHORT_ANSWER; break;
          default: questionTypeValue = QuestionType.SINGLE_CHOICE; // 默认为单选题
        }
      } else {
        // 已经是QuestionType枚举值
        questionTypeValue = questionType as QuestionType;
      }
      
      console.log(`提交的题目类型: ${questionTypeValue}`);
      
      // 创建答案DTO
      const answerDto: UserQuestionAnswerDTO = {
        questionId,
        answers: userAnswers,
        correctAnswers: [], // 这个字段会由后端填充
        questionType: questionTypeValue,
        questionTitle: currentQuestion.title,
        duration: elapsedTime,
        isWrong: false // 这个字段会由后端判断
      };
      
      const result = await learningService.submitQuestionAnswer(questionGroup.sectionId, answerDto);
      
      console.log(`答案提交结果:`, result);
      
      // 记录答案已提交
      setSubmitted(prev => ({ ...prev, [questionId]: true }));
      
      // 记录答题结果
      setResults(prev => ({ ...prev, [questionId]: result.correct }));
      
      // 检查是否所有题目都已完成
      const allSubmitted = questions.every(q => submitted[q.id] || q.id === questionId);
      const allCorrect = questions.every(q => {
        if (q.id === questionId) return result.correct;
        return results[q.id] === true;
      });
      
      console.log(`答题进度检查 - 全部提交: ${allSubmitted}, 全部正确: ${allCorrect}`);
      
      if (allSubmitted) {
        setAllCompleted(true);
        
        // 如果提供了onComplete回调，则调用
        if (onComplete) {
          console.log(`所有题目已完成，调用onComplete回调`);
          onComplete(allCorrect);
        }
      }
      
      // 如果正确或提交了最后一题，自动前进到下一题
      if (result.correct || currentQuestionIndex === questions.length - 1) {
        // 最后一题且正确，留在最后一题以显示完成状态
        // 否则前进到下一题
        if (currentQuestionIndex < questions.length - 1) {
          console.log(`自动前进到下一题，索引: ${currentQuestionIndex + 1}`);
          setCurrentQuestionIndex(currentQuestionIndex + 1);
        }
      }
      
      // 显示正确/错误的提示
      if (result.correct) {
        sonnerToast.success("回答正确", {
          description: "做得好! 继续前进吧。"
        });
      } else {
        sonnerToast.error("回答错误", {
          description: `答案不正确，请查看解析${result.explanation ? `：${result.explanation}` : ""}`
        });
      }
      
    } catch (error) {
      console.error(`提交答案失败 - 章节ID: ${questionGroup?.sectionId}, 题目ID: ${questionId}:`, error);
      const errorMessage = error instanceof Error 
        ? error.message 
        : "提交失败，请稍后重试";
        
      sonnerToast.error("提交失败", {
        description: errorMessage
      });
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
    // 确保题目有选项
    if (!question.options || !Array.isArray(question.options) || question.options.length === 0) {
      console.warn(`题目ID=${question.id}没有选项`);
      return null;
    }
    
    // 检查options的类型
    const firstOption = question.options[0];
    const isObjectOptions = typeof firstOption === 'object' && firstOption !== null;
    
    const questionId = question.id;
    const isSubmittedQuestion = submitted[questionId];
    const isCorrect = results[questionId];
    const userAnswers = answers[questionId] || [];
    
    // 根据题目类型渲染不同的选项组件
    if (question.type === 1 || getQuestionType(question.type) === QuestionType.SINGLE_CHOICE) {
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
    }
    
    if (question.type === 2 || getQuestionType(question.type) === QuestionType.MULTIPLE_CHOICE) {
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
    }
    
    if (question.type === 3 || getQuestionType(question.type) === QuestionType.TRUE_FALSE) {
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
    }
    
    if (question.type === 4 || getQuestionType(question.type) === QuestionType.FILL_BLANK) {
      const handleChange = question.type === 4 ? handleFillBlankChange : handleShortAnswerChange;
      
      return (
        <div className="space-y-4 mt-4">
          <Textarea
            placeholder={`请输入${question.type === 4 ? '填空' : '简答'}内容...`}
            value={userAnswers[0] || ''}
            onChange={handleChange}
            disabled={isSubmittedQuestion}
            rows={question.type === 4 ? 3 : 6}
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
    }
    
    // 如果无法识别的类型，返回null
    console.warn(`不支持的题目类型: ${question.type}`);
    return null;
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
                  onClick={() => {
                    const questionId = currentQuestion.id;
                    const userAnswers = answers[questionId] || [];
                    if (userAnswers.length === 0) {
                      sonnerToast.error("请选择至少一个选项再提交");
                      return;
                    }
                    submitAnswer(
                      questionId, 
                      currentQuestion.type, 
                      userAnswers
                    );
                  }}
                  disabled={isSubmitting || !answers[currentQuestion.id]?.length}
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      提交中...
                    </>
                  ) : (
                    "提交答案"
                  )}
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