'use client';

import { useState, useEffect } from 'react';
import { Section } from '@/types/course';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { sectionService, mediaService, questionGroupService } from '@/services';
import { MediaVO } from '@/services/media-service';
import { QuestionGroup } from '@/types/question';
import { QuestionGroupItemVO, Question, QuestionType, QuestionDifficulty } from '@/types/question';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { toast } from 'sonner';
import {
  Video,
  FileText,
  Headphones,
  AlertCircle,
  Loader2,
  BrainCircuit,
  Clock,
  ExternalLink,
  RefreshCw,
  ChevronUp,
  ChevronDown,
  Download
} from 'lucide-react';

interface CourseContentPlayerProps {
  section: Section;
}

export function CourseContentPlayer({ section }: CourseContentPlayerProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [media, setMedia] = useState<MediaVO | null>(null);
  const [questionGroup, setQuestionGroup] = useState<QuestionGroup | null>(null);
  const [activeTab, setActiveTab] = useState<string>('content');
  const [mediaUrlLoading, setMediaUrlLoading] = useState(false);

  // 题组相关状态 - 提升到组件顶层
  const [expandedQuestionId, setExpandedQuestionId] = useState<number | null>(null);
  const [groupItems, setGroupItems] = useState<QuestionGroupItemVO[]>([]);
  const [loadingItems, setLoadingItems] = useState<boolean>(false);
  const [itemsError, setItemsError] = useState<string | null>(null);

  // 加载小节资源
  useEffect(() => {
    async function loadSectionResources() {
      try {
        setLoading(true);
        setError(null);

        // 重置资源状态
        setMedia(null);
        setQuestionGroup(null);

        // 根据资源类型加载对应资源
        if (section.resourceTypeDiscriminator === 'MEDIA' && section.mediaId) {
          const mediaData = await mediaService.getMediaInfo(section.mediaId);
          if (mediaData && mediaData.data) {
            setMedia(mediaData.data);
            // 获取媒体访问URL
            await fetchMediaAccessUrl(mediaData.data.id);
          }
        } else if (section.resourceTypeDiscriminator === 'QUESTION_GROUP' && section.questionGroupId) {
          const groupData = await questionGroupService.getQuestionGroupById(section.questionGroupId);
          if (groupData) {
            setQuestionGroup(groupData);
          }
        }
      } catch (err: any) {
        console.error('加载小节资源失败:', err);
        setError(err.message || '无法加载小节资源');
      } finally {
        setLoading(false);
      }
    }

    if (section) {
      loadSectionResources();
    }
  }, [section]);

  // 加载题组中的题目 - 提升到组件顶层
  useEffect(() => {
    async function loadGroupItems() {
      if (!questionGroup) return;

      try {
        setLoadingItems(true);
        setItemsError(null);
        const items = await questionGroupService.getGroupItems(questionGroup.id);
        setGroupItems(items);
      } catch (err: any) {
        console.error('加载题组题目失败:', err);
        setItemsError(err.message || '加载题目失败');
      } finally {
        setLoadingItems(false);
      }
    }

    if (questionGroup) {
      loadGroupItems();
    }
  }, [questionGroup]);

  // 获取媒体访问URL
  const fetchMediaAccessUrl = async (mediaId: number) => {
    try {
      setMediaUrlLoading(true);
      console.log('获取媒体访问URL, mediaId:', mediaId);
      const response = await mediaService.getMediaAccessUrl(mediaId);

      if (response && response.data && response.data.accessUrl) {
        console.log('获取到媒体访问URL');
        // 更新媒体对象的访问URL
        setMedia(prev => {
          if (!prev) return null;
          return {
            ...prev,
            accessUrl: response.data.accessUrl
          };
        });
      } else {
        console.error('获取媒体访问URL失败：未返回有效URL');
        toast.error('无法加载媒体预览');
      }
    } catch (err) {
      console.error('获取媒体访问URL失败:', err);
      toast.error('无法加载媒体预览');
    } finally {
      setMediaUrlLoading(false);
    }
  };

  // 处理展开/折叠题目 - 提升到组件顶层
  const handleToggleQuestion = (questionId: number) => {
    if (expandedQuestionId === questionId) {
      setExpandedQuestionId(null);
    } else {
      setExpandedQuestionId(questionId);
    }
  };

  // 获取问题类型文本
  const getQuestionTypeText = (type: QuestionType) => {
    switch (type) {
      case QuestionType.SINGLE_CHOICE: return '单选题';
      case QuestionType.MULTIPLE_CHOICE: return '多选题';
      case QuestionType.TRUE_FALSE: return '判断题';
      case QuestionType.FILL_BLANK: return '填空题';
      case QuestionType.SHORT_ANSWER: return '简答题';
      default: return '未知题型';
    }
  };

  // 获取问题难度文本
  const getQuestionDifficultyText = (difficulty: QuestionDifficulty) => {
    switch (difficulty) {
      case QuestionDifficulty.EASY: return '简单';
      case QuestionDifficulty.MEDIUM: return '中等';
      case QuestionDifficulty.HARD: return '困难';
      default: return '未知难度';
    }
  };

  // 获取问题类型对应的样式
  const getTypeStyle = (type: QuestionType) => {
    switch (type) {
      case QuestionType.SINGLE_CHOICE: return 'bg-blue-100 text-blue-800';
      case QuestionType.MULTIPLE_CHOICE: return 'bg-purple-100 text-purple-800';
      case QuestionType.TRUE_FALSE: return 'bg-green-100 text-green-800';
      case QuestionType.FILL_BLANK: return 'bg-yellow-100 text-yellow-800';
      case QuestionType.SHORT_ANSWER: return 'bg-orange-100 text-orange-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // 获取问题难度对应的样式
  const getDifficultyStyle = (difficulty: QuestionDifficulty) => {
    switch (difficulty) {
      case QuestionDifficulty.EASY: return 'bg-emerald-100 text-emerald-800';
      case QuestionDifficulty.MEDIUM: return 'bg-amber-100 text-amber-800';
      case QuestionDifficulty.HARD: return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // 渲染加载状态
  if (loading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-1/3" />
          <Skeleton className="h-4 w-1/2 mt-2" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-[400px] w-full rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  // 渲染错误状态
  if (error) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>加载失败</AlertTitle>
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }

  // 渲染媒体内容
  const renderMediaContent = () => {
    if (!media) return <p className="text-center text-muted-foreground py-6">该小节没有关联媒体资源</p>;

    // 根据媒体类型渲染不同的播放器
    const mediaType = media.type?.toLowerCase() || '';

    // 判断是否已经获取到访问URL
    if (!media.accessUrl) {
      return (
        <div className="text-center p-6 bg-muted rounded-lg">
          <div className="flex flex-col items-center justify-center py-10">
            {mediaUrlLoading ? (
              <>
                <Loader2 className="h-8 w-8 animate-spin text-primary mb-4" />
                <p className="text-muted-foreground">正在加载媒体资源...</p>
              </>
            ) : (
              <>
                {mediaType.includes('video')
                  ? <Video className="h-12 w-12 text-muted-foreground mb-4" />
                  : mediaType.includes('audio')
                    ? <Headphones className="h-12 w-12 text-muted-foreground mb-4" />
                    : <FileText className="h-12 w-12 text-muted-foreground mb-4" />
                }
                <p className="text-muted-foreground mb-4">媒体资源链接加载失败</p>
                <Button
                  variant="outline"
                  onClick={() => media && fetchMediaAccessUrl(media.id)}
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  重新加载
                </Button>
              </>
            )}
          </div>
        </div>
      );
    }

    // 视频
    if (mediaType.includes('video')) {
      return (
        <div className="rounded-lg overflow-hidden border shadow-sm">
          <div className="bg-muted p-2 flex justify-between items-center">
            <span className="text-sm font-medium">{media.title}</span>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => window.open(media.accessUrl, '_blank')}
            >
              <ExternalLink className="h-4 w-4 mr-1" />
              新窗口打开
            </Button>
          </div>

          <div className="aspect-video bg-black">
            <video
              key={media.accessUrl}
              src={media.accessUrl}
              controls
              controlsList="nodownload"
              playsInline
              preload="metadata"
              className="w-full h-full"
              onError={(e) => {
                console.error('视频加载错误:', e);
                toast.error('视频加载失败，请重试');
              }}
            >
              您的浏览器不支持HTML5视频播放，请更新浏览器版本。
            </video>
          </div>

          {media.description && (
            <div className="p-3 bg-muted/50 border-t">
              <p className="text-sm text-muted-foreground">{media.description}</p>
            </div>
          )}

          <div className="p-2 bg-muted flex justify-between items-center text-xs text-muted-foreground">
            <div>
              提示：可使用空格键暂停/播放，左右方向键快退/快进
            </div>
            <div className="flex items-center">
              <Button
                variant="ghost"
                size="sm"
                className="h-7 px-2"
                onClick={() => {
                  const videoElement = document.querySelector('video');
                  if (videoElement) {
                    if (videoElement.requestFullscreen) {
                      videoElement.requestFullscreen();
                    }
                  }
                }}
              >
                <span className="text-xs">全屏播放</span>
              </Button>
            </div>
          </div>
        </div>
      );
    }

    // 文档 (PDF或其他文档，使用iframe)
    if (mediaType.includes('pdf') ||
        mediaType.includes('document') ||
        mediaType.includes('msword') ||
        mediaType.includes('excel') ||
        mediaType.includes('powerpoint')) {
      return (
        <div className="relative rounded-lg overflow-hidden border">
          <div className="flex justify-between items-center bg-muted p-2">
            <span className="text-sm font-medium">{media.title}</span>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => window.open(media.accessUrl, '_blank')}
            >
              <ExternalLink className="h-4 w-4 mr-1" />
              新窗口打开
            </Button>
          </div>
          <iframe
            src={media.accessUrl}
            className="w-full h-[500px] border-0"
            title={media.title || "文档预览"}
            sandbox="allow-scripts allow-same-origin allow-forms"
            referrerPolicy="no-referrer"
            loading="lazy"
            onError={(e) => {
              console.error('文档加载错误:', e);
              toast.error('文档加载失败，请重试');
            }}
          />
        </div>
      );
    }

    // 音频
    if (mediaType.includes('audio')) {
      return (
        <div className="p-6 bg-muted rounded-lg">
          <div className="flex flex-col items-center space-y-4">
            <div className="w-40 h-40 bg-primary/10 rounded-full flex items-center justify-center mb-2">
              <Headphones className="h-16 w-16 text-primary" />
            </div>

            <div className="text-center mb-2">
              <h3 className="text-lg font-medium">{media.title}</h3>
              {media.description && (
                <p className="text-sm text-muted-foreground mt-1">{media.description}</p>
              )}
            </div>

            <div className="w-full max-w-md bg-card p-4 rounded-lg border shadow-sm">
              <audio
                key={media.accessUrl}
                src={media.accessUrl}
                controls
                className="w-full"
                controlsList="nodownload"
                preload="metadata"
                onError={(e) => {
                  console.error('音频加载错误:', e);
                  toast.error('音频加载失败，请重试');
                }}
              >
                您的浏览器不支持HTML5音频播放，请更新浏览器版本。
              </audio>

              <div className="flex justify-between items-center mt-3 text-xs text-muted-foreground">
                <div>
                  提示：可使用空格键暂停/播放
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => window.open(media.accessUrl, '_blank')}
                >
                  <ExternalLink className="h-3 w-3 mr-1" />
                  新窗口打开
                </Button>
              </div>
            </div>
          </div>
        </div>
      );
    }

    // 默认：提供下载链接
    return (
      <div className="p-6 bg-card rounded-lg border shadow-sm">
        <div className="flex flex-col items-center justify-center">
          <div className="w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center mb-4">
            <FileText className="h-10 w-10 text-primary" />
          </div>

          <h3 className="text-lg font-medium mb-1">{media.title}</h3>

          {media.description && (
            <p className="text-sm text-muted-foreground text-center mb-4 max-w-md">{media.description}</p>
          )}

          <div className="text-center mb-6">
            <p className="text-sm text-muted-foreground">
              当前文件类型 ({media.type || '未知类型'}) 不支持在线预览
            </p>
          </div>

          <div className="flex gap-3">
            <Button
              variant="outline"
              onClick={() => window.open(media.accessUrl, '_blank')}
            >
              <FileText className="h-4 w-4 mr-2" />
              在浏览器中打开
            </Button>

            <Button
              onClick={() => {
                // 创建一个临时链接元素来触发下载
                const a = document.createElement('a');
                a.href = media.accessUrl || '';
                // 确保设置一个有效的文件名
                const fileName = (media.title && media.title.trim()) ? media.title : '资源下载';
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
              }}
            >
              <Download className="h-4 w-4 mr-2" />
              下载文件
            </Button>
          </div>
        </div>
      </div>
    );
  };

  // 渲染选项列表
  const renderOptions = (question: Question) => {
    if (!question.options || question.options.length === 0) {
      return <p className="text-muted-foreground text-sm">该题目没有选项</p>;
    }

    return (
      <div className="space-y-2 mt-3">
        {question.options.map((option, index) => {
          const optionLabel = String.fromCharCode(65 + index); // A, B, C, D...

          return (
            <div
              key={`${question.id}-option-${index}`}
              className={`flex p-3 rounded-md border ${
                option.isCorrect ? 'bg-green-50 border-green-200' : 'bg-white'
              }`}
            >
              <div className="flex-shrink-0 mr-2">
                <span className={`inline-flex items-center justify-center h-6 w-6 rounded-full text-xs font-medium ${
                  option.isCorrect ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100'
                }`}>
                  {optionLabel}
                </span>
              </div>
              <div className="flex-grow">
                <div className={`text-sm ${option.isCorrect ? 'font-bold text-green-600 dark:text-green-400' : ''}`}>
                  {option.content}
                </div>
                {option.isCorrect && (
                  <div className="text-xs text-green-600 dark:text-green-400 mt-1">
                    (正确答案)
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  // 渲染判断题选项
  const renderTrueFalseOptions = (question: Question) => {
    const correctAnswer = question.answer?.toLowerCase() === 'true';

    return (
      <div className="space-y-2 mt-3">
        <div
          key={`${question.id}-true-option`}
          className={`flex p-3 rounded-md border ${
            correctAnswer ? 'bg-green-50 border-green-200' : 'bg-white'
          }`}
        >
          <div className="flex-shrink-0 mr-2">
            <span className={`inline-flex items-center justify-center h-6 w-6 rounded-full text-xs font-medium ${
              correctAnswer ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100'
            }`}>
              T
            </span>
          </div>
          <div className="flex-grow">
            <div className={`text-sm ${correctAnswer ? 'font-bold text-green-600 dark:text-green-400' : ''}`}>正确</div>
            {correctAnswer && (
              <div className="text-xs text-green-600 dark:text-green-400 mt-1">
                (正确答案)
              </div>
            )}
          </div>
        </div>

        <div
          key={`${question.id}-false-option`}
          className={`flex p-3 rounded-md border ${
            !correctAnswer ? 'bg-green-50 border-green-200' : 'bg-white'
          }`}
        >
          <div className="flex-shrink-0 mr-2">
            <span className={`inline-flex items-center justify-center h-6 w-6 rounded-full text-xs font-medium ${
              !correctAnswer ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100'
            }`}>
              F
            </span>
          </div>
          <div className="flex-grow">
            <div className={`text-sm ${!correctAnswer ? 'font-bold text-green-600 dark:text-green-400' : ''}`}>错误</div>
            {!correctAnswer && (
              <div className="text-xs text-green-600 dark:text-green-400 mt-1">
                (正确答案)
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // 渲染填空题答案
  const renderFillBlankAnswer = (question: Question) => {
    if (!question.answer) {
      return <p className="text-muted-foreground text-sm">该题目没有设置答案</p>;
    }

    // 填空题可能有多个空，答案以分号分隔
    const answers = question.answer.split(';').map(ans => ans.trim());

    return (
      <div className="mt-3 space-y-2">
        <div className="text-sm font-medium">参考答案:</div>
        {answers.map((ans, index) => (
          <div key={`${question.id}-fill-blank-${index}`} className="p-2 bg-green-50 border border-green-200 rounded-md text-sm">
            空 {index + 1}: {ans}
          </div>
        ))}
      </div>
    );
  };

  // 渲染简答题答案
  const renderShortAnswerAnswer = (question: Question) => {
    if (!question.answer) {
      return <p className="text-muted-foreground text-sm">该题目没有设置答案</p>;
    }

    return (
      <div className="mt-3">
        <div className="text-sm font-medium">参考答案:</div>
        <div className="p-3 mt-1 bg-green-50 border border-green-200 rounded-md text-sm whitespace-pre-wrap">
          {question.answer}
        </div>
      </div>
    );
  };

  // 根据题目类型渲染不同的选项/答案
  const renderQuestionAnswer = (question: Question) => {
    switch (question.type) {
      case QuestionType.SINGLE_CHOICE:
      case QuestionType.MULTIPLE_CHOICE:
        return renderOptions(question);
      case QuestionType.TRUE_FALSE:
        return renderTrueFalseOptions(question);
      case QuestionType.FILL_BLANK:
        return renderFillBlankAnswer(question);
      case QuestionType.SHORT_ANSWER:
        return renderShortAnswerAnswer(question);
      default:
        return <p className="text-muted-foreground text-sm">未知题型</p>;
    }
  };

  // 渲染题目组内容
  const renderQuestionGroupContent = () => {
    if (!questionGroup) return <p className="text-center text-muted-foreground py-6">该小节没有关联题目组</p>;

    if (loadingItems) {
      return (
        <div className="p-6 bg-muted rounded-lg">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium flex items-center">
              <BrainCircuit className="h-5 w-5 mr-2" />
              {questionGroup.name}
            </h3>
            <div className="text-sm text-muted-foreground">
              共 {questionGroup.questionCount || 0} 题
            </div>
          </div>

          <div className="bg-card rounded-lg p-6 shadow-sm border">
            <div className="flex justify-center items-center py-10">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2 text-muted-foreground">加载题目中...</span>
            </div>
          </div>
        </div>
      );
    }

    if (itemsError) {
      return (
        <div className="p-6 bg-muted rounded-lg">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium flex items-center">
              <BrainCircuit className="h-5 w-5 mr-2" />
              {questionGroup.name}
            </h3>
          </div>

          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>加载失败</AlertTitle>
            <AlertDescription>{itemsError}</AlertDescription>
          </Alert>
        </div>
      );
    }

    if (groupItems.length === 0) {
      return (
        <div className="p-6 bg-muted rounded-lg">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium flex items-center">
              <BrainCircuit className="h-5 w-5 mr-2" />
              {questionGroup.name}
            </h3>
          </div>

          <div className="bg-card rounded-lg p-6 shadow-sm border text-center">
            <p className="text-muted-foreground">该题组暂无题目</p>
          </div>
        </div>
      );
    }

    return (
      <div className="p-6 bg-muted rounded-lg">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-medium flex items-center">
            <BrainCircuit className="h-5 w-5 mr-2" />
            {questionGroup.name}
          </h3>
          <div className="text-sm text-muted-foreground">
            共 {groupItems.length} 题
          </div>
        </div>

        <div className="space-y-4">
          {groupItems.map((item, index) => {
            const { question } = item;
            const isExpanded = expandedQuestionId === question.id;

            return (
              <div key={item.id} className="bg-card rounded-lg shadow-sm border overflow-hidden">
                <div
                  className="flex items-start p-4 cursor-pointer"
                  onClick={() => handleToggleQuestion(question.id)}
                >
                  <div className="mr-3 font-bold text-muted-foreground">
                    {index + 1}.
                  </div>
                  <div className="flex-grow">
                    <div className="flex flex-wrap items-center gap-2 mb-2">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getTypeStyle(question.type)}`}>
                        {getQuestionTypeText(question.type)}
                      </span>
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getDifficultyStyle(question.difficulty)}`}>
                        {getQuestionDifficultyText(question.difficulty)}
                      </span>
                      {item.score && (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                          {item.score} 分
                        </span>
                      )}
                    </div>
                    <div className="text-sm font-medium">
                      {question.title}
                    </div>
                    {question.content && (
                      <div className="mt-1 text-sm">
                        {question.content}
                      </div>
                    )}
                    {question.description && (
                      <div className="mt-1 text-sm text-muted-foreground">
                        {question.description}
                      </div>
                    )}
                  </div>
                  <div className="flex-shrink-0 ml-2">
                    {isExpanded ? (
                      <ChevronUp className="h-5 w-5 text-muted-foreground" />
                    ) : (
                      <ChevronDown className="h-5 w-5 text-muted-foreground" />
                    )}
                  </div>
                </div>

                {isExpanded && (
                  <div className="border-t px-4 py-3 bg-gray-50">
                    {renderQuestionAnswer(question)}

                    {question.analysis && (
                      <div className="mt-4 pt-3 border-t">
                        <div className="text-sm font-medium">题目解析:</div>
                        <div className="p-3 mt-1 bg-blue-50 border border-blue-100 rounded-md text-sm whitespace-pre-wrap">
                          {question.analysis}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  // 没有资源
  if (!media && !questionGroup && section.resourceTypeDiscriminator !== 'MEDIA' && section.resourceTypeDiscriminator !== 'QUESTION_GROUP') {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{section.title}</CardTitle>
          {section.description && (
            <CardDescription>{section.description}</CardDescription>
          )}
        </CardHeader>
        <CardContent>
          <div className="p-6 bg-muted rounded-lg text-center">
            <p className="text-muted-foreground">该小节暂无内容资源</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{section.title}</CardTitle>
        {section.description && (
          <CardDescription>{section.description}</CardDescription>
        )}
        {section.estimatedMinutes && (
          <div className="flex items-center mt-2 text-sm text-muted-foreground">
            <Clock className="h-4 w-4 mr-1" />
            <span>预计学习时间: {section.estimatedMinutes} 分钟</span>
          </div>
        )}
      </CardHeader>
      <CardContent>
        {section.resourceTypeDiscriminator === 'MEDIA' && renderMediaContent()}
        {section.resourceTypeDiscriminator === 'QUESTION_GROUP' && renderQuestionGroupContent()}
      </CardContent>
    </Card>
  );
}