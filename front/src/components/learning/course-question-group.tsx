'use client';

import { useState, useEffect, useRef } from 'react';
import { AlertCircle, Check, ChevronRight, CircleCheck, CircleX, Clock, Loader2, X, BookmarkPlus, ThumbsDown, Info } from 'lucide-react';
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
import { Switch } from '@/components/ui/switch';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { QuestionGroupVO, QuestionVO, QuestionType, UserQuestionAnswerDTO } from '@/types/learning';
import { learningService } from '@/services';

// 导入学习活动类型常量
const QUIZ_ATTEMPT = 'QUIZ_ATTEMPT';
const QUIZ_COMPLETED = 'QUIZ_COMPLETED';

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
  // 新增状态：累计学习时间
  const [learningTime, setLearningTime] = useState(0);
  const learningTimeRef = useRef<number>(0);
  const lastRecordTimeRef = useRef<number>(Date.now());
  const recordIntervalId = useRef<NodeJS.Timeout | null>(null);
  // 新增状态：手动添加到错题本的题目
  const [manualWrongQuestions, setManualWrongQuestions] = useState<Record<number, boolean>>({});
  // 新增状态：收藏的题目
  const [bookmarkedQuestions, setBookmarkedQuestions] = useState<Record<number, boolean>>({});
  // 显示题组标题的状态
  const [showTitle, setShowTitle] = useState(true);
  
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
  
  // 学习时间计时器
  useEffect(() => {
    // 当组件挂载时启动计时器
    const timer = setInterval(() => {
      setElapsedTime(prev => prev + 1000);
      learningTimeRef.current += 1;
      
      // 每10秒打印一次当前学习时间（用于调试）
      if (learningTimeRef.current % 10 === 0) {
        console.log(`当前试题学习时间: ${learningTimeRef.current}秒`);
      }
    }, 1000);
    
    console.log('题目组件学习计时器已启动');
    
    // 每15秒记录一次学习活动（从30秒改为15秒）
    recordIntervalId.current = setInterval(() => {
      const currentLearningTime = learningTimeRef.current;
      
      if (currentLearningTime > 0) {
        console.log(`定期记录试题页面学习时间: ${currentLearningTime}秒`);
        recordLearningActivity(currentLearningTime);
        // 只更新记录时间点，不重置学习时间（保持累计）
        lastRecordTimeRef.current = Date.now();
      }
    }, 15000); // 从30秒改为15秒
    
    return () => {
      clearInterval(timer);
      
      // 组件卸载时记录最后的学习时间
      if (recordIntervalId.current) {
        clearInterval(recordIntervalId.current);
        recordIntervalId.current = null;
      }
      
      const finalLearningTime = learningTimeRef.current;
      if (finalLearningTime > 0 && questionGroup?.courseId) {
        console.log(`组件卸载，记录最后学习时间: ${finalLearningTime}秒`);
        recordLearningActivity(finalLearningTime, 'UNMOUNTED');
      }
    };
  }, []);
  
  // 记录学习活动的函数
  const recordLearningActivity = (duration: number, status: string = 'LEARNING') => {
    if (!questionGroup?.courseId || !questionGroup?.sectionId) {
      console.warn("无法记录学习活动：缺少课程ID或小节ID", {
        courseId: questionGroup?.courseId,
        sectionId: questionGroup?.sectionId
      });
      return;
    }
    
    try {
      // 确保时长至少为1秒（降低阈值从原本需要多秒）
      const actualDuration = Math.max(Math.round(duration), 1);
      
      console.log(`记录试题学习活动: 状态=${status}, 时长=${actualDuration}秒, 课程ID=${questionGroup.courseId}, 小节ID=${questionGroup.sectionId}`);
      
      learningService.recordCompletedActivity({
        courseId: Number(questionGroup.courseId),
        chapterId: questionGroup.chapterId ? Number(questionGroup.chapterId) : undefined,
        sectionId: Number(questionGroup.sectionId),
        activityType: 'QUIZ_ATTEMPT',
        durationSeconds: actualDuration,
        contextData: JSON.stringify({
          status: status,
          questionIndex: currentQuestionIndex,
          totalQuestions: questions.length,
          duration: duration
        })
      }).then(() => {
        console.log(`记录试题学习活动成功: ${status}, 时长: ${actualDuration}秒`);
        if (status !== 'LEARNING') {
          // 如果不是常规学习状态（如组件卸载、完成等），则重置学习时间
          learningTimeRef.current = 0;
        }
      }).catch(err => {
        console.error("记录试题学习活动失败:", err);
      });
    } catch (error) {
      console.error("记录试题学习活动失败:", error);
    }
  };
  
  // 获取问题类型
  const getQuestionType = (type: string | number): QuestionType => {
    // 将类型转换为字符串进行比较
    const typeStr = String(type).toUpperCase();
    
    // 根据数值或字符串判断题目类型 - 与后端保持一致(0-4)
    if (typeStr === '0' || typeStr === 'SINGLE_CHOICE') return QuestionType.SINGLE_CHOICE;
    if (typeStr === '1' || typeStr === 'MULTIPLE_CHOICE') return QuestionType.MULTIPLE_CHOICE;
    if (typeStr === '2' || typeStr === 'TRUE_FALSE') return QuestionType.TRUE_FALSE;
    if (typeStr === '3' || typeStr === 'FILL_BLANK') return QuestionType.FILL_BLANK;
    if (typeStr === '4' || typeStr === 'SHORT_ANSWER') return QuestionType.SHORT_ANSWER;
    
    // 默认返回单选题
    console.warn(`未知题目类型 ${type}，默认当作单选题`);
    return QuestionType.SINGLE_CHOICE;
  };
  
  // 提交答案 - 修改为前端判断逻辑
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
      
      // 当前问题对象
      const currentQuestion = questions.find(q => q.id === questionId);
      if (!currentQuestion) {
        sonnerToast.error("提交失败", {
          description: "找不到当前题目信息"
        });
        setIsSubmitting(false);
        return;
      }
      
      console.log(`准备提交答案 - 题目ID: ${questionId}, 用户答案:`, userAnswers);
      
      // 确定questionType类型 - 支持数字和字符串类型
      let questionTypeValue: QuestionType;
      
      if (typeof questionType === 'number') {
        switch (questionType) {
          case 0: questionTypeValue = QuestionType.SINGLE_CHOICE; break;
          case 1: questionTypeValue = QuestionType.MULTIPLE_CHOICE; break;
          case 2: questionTypeValue = QuestionType.TRUE_FALSE; break;
          case 3: questionTypeValue = QuestionType.FILL_BLANK; break;
          case 4: questionTypeValue = QuestionType.SHORT_ANSWER; break;
          default: questionTypeValue = QuestionType.SINGLE_CHOICE; // 默认为单选题
        }
      } else if (typeof questionType === 'string' && !Object.values(QuestionType).includes(questionType as QuestionType)) {
        // 处理字符串数字，如 "0", "1" 等
        switch (questionType) {
          case '0': questionTypeValue = QuestionType.SINGLE_CHOICE; break;
          case '1': questionTypeValue = QuestionType.MULTIPLE_CHOICE; break;
          case '2': questionTypeValue = QuestionType.TRUE_FALSE; break;
          case '3': questionTypeValue = QuestionType.FILL_BLANK; break;
          case '4': questionTypeValue = QuestionType.SHORT_ANSWER; break;
          default: questionTypeValue = QuestionType.SINGLE_CHOICE; // 默认为单选题
        }
      } else {
        // 已经是QuestionType枚举值
        questionTypeValue = questionType as QuestionType;
      }
      
      // 在前端判断答案是否正确
      let isCorrect = false;
      
      // 对于简答题，不自动判断对错，直接显示参考答案
      const isShortAnswer = questionTypeValue === QuestionType.SHORT_ANSWER;
      
      if (isShortAnswer) {
        // 简答题默认为"正确"，由用户自己判断
        isCorrect = true;
      } 
      else if (currentQuestion.correctOptions && currentQuestion.correctOptions.length > 0) {
        // 单选题/判断题：用户的唯一答案必须在正确答案中
        if (questionTypeValue === QuestionType.SINGLE_CHOICE || 
            questionTypeValue === QuestionType.TRUE_FALSE ||
            questionTypeValue === QuestionType.FILL_BLANK) {
          isCorrect = currentQuestion.correctOptions.includes(userAnswers[0]);
        }
        // 多选题：用户答案和正确答案必须完全匹配（数量和内容都一致）
        else if (questionTypeValue === QuestionType.MULTIPLE_CHOICE) {
          // 排序后比较数组
          const sortedUserAnswers = [...userAnswers].sort();
          const sortedCorrectAnswers = [...currentQuestion.correctOptions].sort();
          
          isCorrect = sortedUserAnswers.length === sortedCorrectAnswers.length &&
                      sortedUserAnswers.every((value, index) => value === sortedCorrectAnswers[index]);
        }
      }
      
      console.log(`前端判断答案结果 - 题目ID: ${questionId}, 正确: ${isCorrect}`);
      
      // 记录答案已提交
      setSubmitted(prev => ({ ...prev, [questionId]: true }));
      
      // 记录答题结果
      setResults(prev => ({ ...prev, [questionId]: isCorrect }));
      
      // 如果答错了，记录到错题本（可选是否调用后端API）
      if (!isCorrect && !isShortAnswer) {
        try {
          // 获取题目类型的枚举值
          const questionTypeValue = getQuestionType(questionType);

          // 确保duration至少为1000毫秒(1秒)
          const answerDuration = Math.max(1000, elapsedTime);
          
          const answerDto: UserQuestionAnswerDTO = {
            questionId,
            answers: userAnswers,
            correctAnswers: currentQuestion.correctOptions || [],
            questionType: questionTypeValue,
            questionTitle: currentQuestion.title,
            duration: answerDuration, // 确保至少为1000毫秒(1秒)
            isWrong: true // 标记为错题
          };
          
          // 调用API将错题添加到错题本
          if (questionGroup.sectionId) {
            learningService.submitQuestionAnswer(questionGroup.sectionId, answerDto)
              .then(() => console.log('已添加到错题本'))
              .catch(err => console.error('添加到错题本失败:', err));
          }
        } catch (err) {
          console.error('添加到错题本失败:', err);
        }
      }
      
      // 记录答题活动
      try {
        // 获取课程ID和章节ID
        const courseId = questionGroup.courseId;
        const chapterId = questionGroup.chapterId;
        const sectionId = questionGroup.sectionId;
        
        if (courseId) {
          // 确保时长至少为1000毫秒(1秒)
          const activityDuration = Math.max(1000, elapsedTime);
          
          learningService.recordCompletedActivity({
            courseId,
            chapterId: chapterId || undefined,
            sectionId,
            activityType: QUIZ_ATTEMPT,
            durationSeconds: Math.floor(activityDuration / 1000), // 转换为秒，并确保至少为1秒
            contextData: JSON.stringify({
              questionId,
              questionType: questionTypeValue,
              isCorrect,
              answers: userAnswers,
              correctAnswers: currentQuestion.correctOptions
            })
          }).then(() => {
            console.log(`记录答题活动成功 - 题目ID: ${questionId}, 用时: ${activityDuration}秒`);
          }).catch(err => {
            console.error("记录答题活动失败:", err);
          });
        } else {
          console.warn("无法记录答题活动: 缺少课程ID");
        }
      } catch (err) {
        console.error("记录答题活动失败:", err);
      }
      
      // 检查是否所有题目都已完成
      const allSubmitted = questions.every(q => submitted[q.id] || q.id === questionId);
      const allCorrect = questions.every(q => {
        if (q.id === questionId) return isCorrect;
        return results[q.id] === true;
      });
      
      console.log(`答题进度检查 - 全部提交: ${allSubmitted}, 全部正确: ${allCorrect}`);
      
      if (allSubmitted) {
        setAllCompleted(true);
        
        // 记录测验完成活动
        try {
          if (questionGroup.courseId) {
            learningService.recordCompletedActivity({
              courseId: questionGroup.courseId,
              chapterId: questionGroup.chapterId || undefined,
              sectionId: questionGroup.sectionId,
              activityType: QUIZ_COMPLETED,
              durationSeconds: Math.max(1, Math.floor(elapsedTime / 1000)), // 确保持续时间至少为1秒
              contextData: JSON.stringify({
                totalQuestions: questions.length,
                correctCount: questions.filter(q => results[q.id] === true || (q.id === questionId && isCorrect)).length,
                allCorrect
              })
            }).then(() => {
              console.log(`记录测验完成活动成功 - 是否全部正确: ${allCorrect}`);
            }).catch(err => {
              console.error("记录测验完成活动失败:", err);
            });
          }
        } catch (err) {
          console.error("记录测验完成活动失败:", err);
        }
        
        // 如果提供了onComplete回调，则调用
        if (onComplete) {
          console.log(`所有题目已完成，调用onComplete回调`);
          onComplete(allCorrect);
        }
      }
      
      // 如果正确或提交了最后一题，自动前进到下一题
      if (isCorrect || currentQuestionIndex === questions.length - 1) {
        // 最后一题且正确，留在最后一题以显示完成状态
        // 否则前进到下一题
        if (currentQuestionIndex < questions.length - 1) {
          console.log(`自动前进到下一题，索引: ${currentQuestionIndex + 1}`);
          setCurrentQuestionIndex(currentQuestionIndex + 1);
          // 重置计时器
          setElapsedTime(0);
        }
      }
      
      // 显示正确/错误的提示
      if (isCorrect && !isShortAnswer) {
        sonnerToast.success("回答正确", {
          description: "做得好! 继续前进吧。"
        });
      } else if (isShortAnswer) {
        sonnerToast.success("已提交回答", {
          description: "您的答案已提交，请继续作答。"
        });
      } else {
        sonnerToast.error("回答错误", {
          description: `答案不正确，请查看解析${currentQuestion.explanation ? `：${currentQuestion.explanation}` : ""}`
        });
      }
      
    } catch (error) {
      console.error(`提交答案失败 - 题目ID: ${questionId}:`, error);
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
  
  // 新增：手动将题目添加到错题本
  const addToWrongQuestions = async (question: QuestionVO) => {
    if (!questionGroup || !questionGroup.sectionId) {
      sonnerToast.error("操作失败", {
        description: "无法添加到错题本，缺少必要信息"
      });
      return;
    }
    
    try {
      const questionId = question.id;
      const userAnswers = answers[questionId] || [];
      
      // 如果没有回答，直接提示
      if (userAnswers.length === 0 && !submitted[questionId]) {
        sonnerToast.error("请先回答问题", {
          description: "提交答案后才能添加到错题本"
        });
        return;
      }
      
      // 确保持续时间至少为1000毫秒(1秒)
      const answerDuration = Math.max(1000, elapsedTime);
      
      // 创建答案DTO，明确标记为错误
      const answerDto: UserQuestionAnswerDTO = {
        questionId,
        answers: userAnswers,
        correctAnswers: question.correctOptions || [],
        questionType: getQuestionType(question.type),
        questionTitle: question.title,
        duration: answerDuration, // 确保持续时间至少为1000毫秒(1秒)
        isWrong: true // 明确标记为错误
      };
      
      // 尝试提交到错题本
      await learningService.submitQuestionAnswer(questionGroup.sectionId, answerDto);
      
      // 更新本地状态
      setManualWrongQuestions(prev => ({ ...prev, [questionId]: true }));
      
      sonnerToast.success("已添加到错题本", {
        description: "您可以在错题本中找到此题"
      });
    } catch (error) {
      console.error("添加到错题本失败:", error);
      sonnerToast.error("添加失败", {
        description: "添加到错题本时出错，请稍后重试"
      });
    }
  };
  
  // 新增：收藏题目
  const toggleBookmark = (questionId: number) => {
    setBookmarkedQuestions(prev => {
      const newState = { ...prev, [questionId]: !prev[questionId] };
      // 可以在这里添加存储到localStorage的逻辑，暂时只在当前会话有效
      return newState;
    });
    
    const isBookmarked = !bookmarkedQuestions[questionId];
    sonnerToast.success(
      isBookmarked ? "已收藏题目" : "已取消收藏", 
      { description: isBookmarked ? "您可以在收藏夹中找到此题" : "此题已从收藏夹中移除" }
    );
  };
  
  // 渲染选项
  const renderOptions = (question: QuestionVO) => {
    const questionId = question.id;
    const isSubmittedQuestion = submitted[questionId];
    const isCorrect = results[questionId];
    const userAnswers = answers[questionId] || [];
    const questionType = getQuestionType(question.type);
    
    // 对于填空题和简答题，使用特殊处理
    if (questionType === QuestionType.FILL_BLANK || questionType === QuestionType.SHORT_ANSWER) {
      return (
        <div className="space-y-4 mt-4">
          <Textarea
            placeholder={`请输入${questionType === QuestionType.FILL_BLANK ? '填空' : '简答'}内容...`}
            value={userAnswers[0] || ''}
            onChange={questionType === QuestionType.FILL_BLANK ? handleFillBlankChange : handleShortAnswerChange}
            disabled={isSubmittedQuestion}
            rows={questionType === QuestionType.FILL_BLANK ? 3 : 6}
            className={`${isSubmittedQuestion ? 'opacity-90 border-muted' : ''}`}
          />
          
          {isSubmittedQuestion && (
            <div className="mt-4 space-y-4">
              {question.correctOptions && question.correctOptions.length > 0 && (
                <div className="bg-muted p-4 rounded-md border border-muted">
                  <h4 className="font-medium text-sm mb-2 text-muted-foreground">参考答案:</h4>
                  <div className="text-sm">
                    {question.correctOptions?.map((answer, index) => (
                      <div key={index} className="mb-1 last:mb-0">{answer}</div>
                    ))}
                  </div>
                </div>
              )}
              
              <div className={`p-4 rounded-md border ${
                questionType === QuestionType.SHORT_ANSWER 
                  ? 'bg-blue-50 dark:bg-blue-950/20 border-blue-200 dark:border-blue-800' 
                  : isCorrect 
                    ? 'bg-green-50 dark:bg-green-950/20 border-green-200 dark:border-green-800' 
                    : 'bg-red-50 dark:bg-red-950/20 border-red-200 dark:border-red-800'
              }`}>
                <h4 className="font-medium text-sm mb-2 text-muted-foreground">您的答案:</h4>
                <div className="text-sm whitespace-pre-wrap">{userAnswers[0] || '(未作答)'}</div>
              </div>
              
              {questionType === QuestionType.SHORT_ANSWER && !manualWrongQuestions[questionId] && (
                <div className="flex items-center justify-between mt-4">
                  <div className="flex items-center gap-2">
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => addToWrongQuestions(question)}
                      className="flex items-center gap-1"
                    >
                      <ThumbsDown className="h-4 w-4" />
                      <span>标记为错题</span>
                    </Button>
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <Info className="h-4 w-4" />
                          </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                          <p className="text-sm max-w-xs">简答题由于答案多样，系统无法自动判断正确性。如果您对照参考答案后认为答错了，可以手动添加到错题本。</p>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                  </div>
                </div>
              )}
              
              {question.explanation && (
                <Alert className="mt-2">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>解析</AlertTitle>
                  <AlertDescription>{question.explanation}</AlertDescription>
                </Alert>
              )}
            </div>
          )}
        </div>
      );
    }
    
    // 对于选择题等其他类型，确保有选项
    if (!question.options || !Array.isArray(question.options) || question.options.length === 0) {
      console.warn(`题目ID=${question.id}没有选项`);
      return (
        <Alert variant="destructive" className="mt-4">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>选项缺失</AlertTitle>
          <AlertDescription>该题目没有可用的选项，请联系管理员。</AlertDescription>
        </Alert>
      );
    }
    
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
                      {option.value}. {option.label}
                    </Label>
                    {isSubmittedQuestion && isSelected && isCorrectOption && (
                      <CircleCheck className="h-5 w-5 text-green-600 dark:text-green-400" />
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </RadioGroup>
      );
    }
  };
  
  // 添加组件的返回语句
  return (
    <div className="space-y-6">
      {/* 题组标题与描述 */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold">
            {questionGroup?.title || '测验'}
          </h3>
          {questionGroup?.description && (
            <p className="text-sm text-muted-foreground mt-1">{questionGroup.description}</p>
          )}
        </div>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => setShowTitle(!showTitle)}
          className="text-muted-foreground"
        >
          {showTitle ? '隐藏标题' : '显示标题'}
        </Button>
      </div>
      
      {/* 进度显示 */}
      <div className="flex items-center justify-between text-sm">
        <div>
          <span className="font-medium">{currentQuestionIndex + 1}</span>
          <span className="text-muted-foreground"> / {questions.length}</span>
        </div>
        <div className="flex items-center gap-2">
          <Clock className="h-4 w-4 text-muted-foreground" />
          <span>{formatTime(elapsedTime)}</span>
        </div>
      </div>
      
      {/* 有题目时显示题目内容 */}
      {currentQuestion ? (
        <Card>
          <CardHeader>
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <CardTitle className="flex items-start gap-2 text-base">
                  <Badge variant="outline" className="mt-0.5">
                    {getQuestionType(currentQuestion.type) === QuestionType.SINGLE_CHOICE && '单选题'}
                    {getQuestionType(currentQuestion.type) === QuestionType.MULTIPLE_CHOICE && '多选题'}
                    {getQuestionType(currentQuestion.type) === QuestionType.TRUE_FALSE && '判断题'}
                    {getQuestionType(currentQuestion.type) === QuestionType.FILL_BLANK && '填空题'}
                    {getQuestionType(currentQuestion.type) === QuestionType.SHORT_ANSWER && '简答题'}
                  </Badge>
                  <span className="flex-1">{currentQuestion.title}</span>
                </CardTitle>
                {currentQuestion.score && (
                  <CardDescription className="mt-1">
                    分值: {currentQuestion.score}分
                  </CardDescription>
                )}
              </div>
              
              {/* 收藏按钮 */}
              <Button
                variant="ghost"
                size="icon"
                onClick={() => toggleBookmark(currentQuestion.id)}
                className={bookmarkedQuestions[currentQuestion.id] ? 'text-yellow-500' : 'text-muted-foreground'}
              >
                <BookmarkPlus className="h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          
          <CardContent>
            {renderOptions(currentQuestion)}
          </CardContent>
          
          <CardFooter className="flex justify-between pt-2">
            <Button
              variant="outline"
              size="sm"
              onClick={goToPreviousQuestion}
              disabled={currentQuestionIndex === 0}
            >
              上一题
            </Button>
            
            <div className="flex items-center gap-2">
              {!submitted[currentQuestion.id] && (
                <Button
                  onClick={() => {
                    const userAnswers = answers[currentQuestion.id] || [];
                    // 对于填空题和简答题，至少有一个非空答案
                    // 对于选择题，必须有选择的选项
                    const isAnswered = userAnswers.length > 0 && (
                      getQuestionType(currentQuestion.type) === QuestionType.FILL_BLANK ||
                      getQuestionType(currentQuestion.type) === QuestionType.SHORT_ANSWER ||
                      userAnswers[0]?.trim() !== ''
                    );
                    
                    if (!isAnswered) {
                      sonnerToast.error("提交失败", {
                        description: "请先回答问题再提交"
                      });
                      return;
                    }
                    
                    submitAnswer(
                      currentQuestion.id,
                      currentQuestion.type,
                      userAnswers
                    );
                  }}
                  disabled={isSubmitting || submitted[currentQuestion.id]}
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      提交中
                    </>
                  ) : (
                    '提交答案'
                  )}
                </Button>
              )}
              
              {submitted[currentQuestion.id] && currentQuestionIndex < questions.length - 1 && (
                <Button onClick={goToNextQuestion} className="ml-auto">
                  下一题
                  <ChevronRight className="ml-1 h-4 w-4" />
                </Button>
              )}
              
              {allCompleted && currentQuestionIndex === questions.length - 1 && (
                <Button onClick={() => onComplete && onComplete(questions.every(q => results[q.id] === true))}>
                  完成答题
                  <Check className="ml-1 h-4 w-4" />
                </Button>
              )}
            </div>
          </CardFooter>
        </Card>
      ) : (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>没有题目</AlertTitle>
          <AlertDescription>
            该测验中没有题目，或题目数据加载失败。
          </AlertDescription>
        </Alert>
      )}
    </div>
  );
}