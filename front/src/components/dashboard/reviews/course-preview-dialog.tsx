'use client';

import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion';
import { CourseStructureVO, ChapterVO, SectionVO } from '@/types/course';
import { Loader2, AlertCircle, CheckCircle, XCircle, BookOpen, Video, FileText, Film, FileQuestion } from 'lucide-react';
import { reviewService } from '@/services/review-service';
import { toast } from 'sonner';

// 定义预览对话框组件属性
interface CoursePreviewDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  courseId: number;
  onReviewComplete: () => void;
}

export function CoursePreviewDialog({
  open,
  onOpenChange,
  courseId,
  onReviewComplete
}: CoursePreviewDialogProps) {
  // 状态管理
  const [courseStructure, setCourseStructure] = useState<CourseStructureVO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [reviewComment, setReviewComment] = useState<string>('');
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [sectionContent, setSectionContent] = useState<any | null>(null);
  const [isLoadingSection, setIsLoadingSection] = useState<boolean>(false);

  // 加载课程结构
  useEffect(() => {
    if (open && courseId) {
      loadCourseStructure();
    }
  }, [open, courseId]);

  const loadCourseStructure = async () => {
    try {
      setIsLoading(true);
      setError(null);
      console.log('加载课程结构, ID:', courseId);
      const structure = await reviewService.getCourseStructure(courseId);
      setCourseStructure(structure);
    } catch (err: any) {
      console.error('加载课程结构失败:', err);
      setError('无法加载课程结构: ' + (err.message || '未知错误'));
    } finally {
      setIsLoading(false);
    }
  };

  // 加载小节内容
  const loadSectionContent = async (sectionId: number) => {
    if (selectedSectionId === sectionId && sectionContent) {
      return; // 已经加载过该小节
    }

    try {
      setIsLoadingSection(true);
      setSelectedSectionId(sectionId);
      setSectionContent(null);

      // 获取小节信息
      const section = findSectionById(sectionId);
      if (!section) {
        throw new Error('找不到小节信息');
      }

      if (section.resourceTypeDiscriminator === 'MEDIA' && section.mediaId) {
        // 加载媒体资源
        const mediaResponse = await fetch(`/api/preview/resources/sections/${sectionId}/media`);
        if (!mediaResponse.ok) {
          throw new Error('获取媒体资源失败');
        }
        const mediaData = await mediaResponse.json();
        setSectionContent(mediaData.data);
      } else if (section.resourceTypeDiscriminator === 'QUESTION_GROUP' && section.questionGroupId) {
        // 加载题组
        const questionResponse = await fetch(`/api/preview/resources/sections/${sectionId}/question-group`);
        if (!questionResponse.ok) {
          throw new Error('获取题组失败');
        }
        const questionData = await questionResponse.json();
        setSectionContent(questionData.data);
      } else {
        setSectionContent({ message: '此小节没有可预览的内容' });
      }
    } catch (err: any) {
      console.error('加载小节内容失败:', err);
      toast.error('加载失败', {
        description: err.message || '无法加载小节内容',
      });
    } finally {
      setIsLoadingSection(false);
    }
  };

  // 查找指定ID的小节
  const findSectionById = (sectionId: number): SectionVO | null => {
    if (!courseStructure) return null;

    for (const chapter of courseStructure.chapters) {
      const section = chapter.sections.find(s => s.id === sectionId);
      if (section) return section;
    }

    return null;
  };

  // 获取资源类型图标
  const getResourceIcon = (resourceType: string) => {
    switch (resourceType) {
      case 'MEDIA':
        return <Film className="h-4 w-4 mr-2" />;
      case 'QUESTION_GROUP':
        return <FileQuestion className="h-4 w-4 mr-2" />;
      default:
        return <FileText className="h-4 w-4 mr-2" />;
    }
  };

  // 通过审核
  const handleApprove = async () => {
    try {
      setIsSubmitting(true);
      await reviewService.approveCourse(String(courseId), reviewComment);
      toast.success('审核通过', {
        description: '课程已成功通过审核',
      });
      onOpenChange(false); // 关闭对话框
      onReviewComplete(); // 通知父组件刷新数据
    } catch (err: any) {
      console.error('通过审核失败:', err);
      toast.error('操作失败', {
        description: err.message || '无法完成审核操作',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  // 拒绝审核
  const handleReject = async () => {
    if (!reviewComment.trim()) {
      toast.error('请填写拒绝原因', {
        description: '拒绝时必须提供审核意见',
      });
      return;
    }

    try {
      setIsSubmitting(true);
      await reviewService.rejectCourse(String(courseId), reviewComment);
      toast.success('已拒绝审核', {
        description: '课程审核已被拒绝',
      });
      onOpenChange(false); // 关闭对话框
      onReviewComplete(); // 通知父组件刷新数据
    } catch (err: any) {
      console.error('拒绝审核失败:', err);
      toast.error('操作失败', {
        description: err.message || '无法完成审核操作',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  // 渲染媒体资源预览
  const renderMediaPreview = (media: any) => {
    if (!media) return <div>无法加载媒体资源</div>;

    return (
      <div className="space-y-4">
        <div className="font-medium text-lg">{media.title}</div>
        {media.description && <p className="text-sm text-muted-foreground">{media.description}</p>}

        {media.type && media.type.startsWith('video') && media.accessUrl && (
          <div className="relative aspect-video rounded-md overflow-hidden bg-muted">
            <video
              src={media.accessUrl}
              controls
              className="w-full h-full object-cover"
              poster={media.thumbnailUrl}
            >
              您的浏览器不支持视频播放
            </video>
          </div>
        )}

        {media.type && media.type.startsWith('audio') && media.accessUrl && (
          <div className="p-4 bg-muted rounded-md">
            <audio src={media.accessUrl} controls className="w-full">
              您的浏览器不支持音频播放
            </audio>
          </div>
        )}

        {media.type && media.type.startsWith('image') && media.accessUrl && (
          <div className="relative aspect-video rounded-md overflow-hidden bg-muted">
            <img
              src={media.accessUrl}
              alt={media.title}
              className="w-full h-full object-contain"
            />
          </div>
        )}

        {(!media.type || (!media.type.startsWith('video') && !media.type.startsWith('audio') && !media.type.startsWith('image'))) && media.accessUrl && (
          <div className="p-4 border rounded-md flex items-center">
            <FileText className="h-5 w-5 mr-2 text-primary" />
            <a
              href={media.accessUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary underline"
            >
              预览/下载文件
            </a>
          </div>
        )}
      </div>
    );
  };

  // 渲染题组预览
  const renderQuestionGroupPreview = (questionGroup: any) => {
    if (!questionGroup) return <div>无法加载题组资源</div>;

    return (
      <div className="space-y-4">
        <div className="font-medium text-lg">{questionGroup.name}</div>
        {questionGroup.description && <p className="text-sm text-muted-foreground">{questionGroup.description}</p>}

        <div className="text-sm text-muted-foreground">
          共 {questionGroup.questions?.length || 0} 道题目
        </div>

        {questionGroup.questions && questionGroup.questions.length > 0 && (
          <div className="space-y-4 mt-4">
            {questionGroup.questions.map((question: any, index: number) => (
              <div key={question.id} className="border rounded-md p-4">
                <div className="font-medium">
                  题目 {index + 1}: {question.content}
                </div>

                {(question.type === 'MULTIPLE_CHOICE' || question.type === 'SINGLE_CHOICE' || question.type === 0 || question.type === 1) && (
                  <div className="mt-2 space-y-2">
                    {question.options && question.options.map((option: any, index: number) => {
                      const optionLabel = String.fromCharCode(65 + index); // A, B, C, D...
                      return (
                        <div key={option.id || index} className={`flex p-3 rounded-md border ${
                          option.isCorrect ? 'bg-green-50 border-green-200' : 'bg-white'
                        }`}>
                          <div className="flex-shrink-0 mr-2">
                            <span className={`inline-flex items-center justify-center h-6 w-6 rounded-full text-xs font-medium ${
                              option.isCorrect ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100'
                            }`}>
                              {option.label || optionLabel}
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
                )}

                {(question.type === 'TRUE_FALSE' || question.type === 2) && (
                  <div className="mt-2 space-y-2">
                    <div className={`flex p-3 rounded-md border ${question.answer === 'TRUE' ? 'bg-green-50 border-green-200' : 'bg-white'}`}>
                      <div className="flex-shrink-0 mr-2">
                        <span className={`inline-flex items-center justify-center h-6 w-6 rounded-full text-xs font-medium ${
                          question.answer === 'TRUE' ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100'
                        }`}>
                          T
                        </span>
                      </div>
                      <div className="flex-grow">
                        <div className={`text-sm ${question.answer === 'TRUE' ? 'font-bold text-green-600 dark:text-green-400' : ''}`}>
                          正确
                        </div>
                        {question.answer === 'TRUE' && (
                          <div className="text-xs text-green-600 dark:text-green-400 mt-1">
                            (正确答案)
                          </div>
                        )}
                      </div>
                    </div>
                    <div className={`flex p-3 rounded-md border ${question.answer === 'FALSE' ? 'bg-green-50 border-green-200' : 'bg-white'}`}>
                      <div className="flex-shrink-0 mr-2">
                        <span className={`inline-flex items-center justify-center h-6 w-6 rounded-full text-xs font-medium ${
                          question.answer === 'FALSE' ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100'
                        }`}>
                          F
                        </span>
                      </div>
                      <div className="flex-grow">
                        <div className={`text-sm ${question.answer === 'FALSE' ? 'font-bold text-green-600 dark:text-green-400' : ''}`}>
                          错误
                        </div>
                        {question.answer === 'FALSE' && (
                          <div className="text-xs text-green-600 dark:text-green-400 mt-1">
                            (正确答案)
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {question.analysis && (
                  <div className="mt-3 pt-3 border-t text-sm">
                    <div className="font-medium">题目解析:</div>
                    <div className="text-muted-foreground">{question.analysis}</div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>
            课程预览与审核
            {courseStructure && ` - ${courseStructure.course.title}`}
          </DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className="flex flex-col items-center justify-center p-12">
            <Loader2 className="h-8 w-8 animate-spin text-primary mb-4" />
            <div>加载课程内容...</div>
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center p-12 text-destructive">
            <AlertCircle className="h-8 w-8 mb-4" />
            <div>{error}</div>
          </div>
        ) : courseStructure ? (
          <div className="flex flex-1 overflow-hidden">
            {/* 左侧章节目录 */}
            <div className="w-1/3 border-r pr-4 overflow-y-auto">
              <div className="mb-4 pb-2 border-b">
                <h3 className="font-semibold text-sm">课程信息</h3>
                <div className="mt-2 text-sm space-y-1">
                  <div><span className="text-muted-foreground">标题：</span>{courseStructure.course.title}</div>
                  <div><span className="text-muted-foreground">机构：</span>{courseStructure.course.institution?.name || '未知'}</div>
                  <div><span className="text-muted-foreground">章节数：</span>{courseStructure.chapters.length}</div>
                  <div><span className="text-muted-foreground">创建者：</span>{courseStructure.course.creatorName || '未知'}</div>
                </div>
              </div>

              <div className="text-sm font-medium mb-2">章节目录</div>

              <Accordion type="multiple" defaultValue={['chapter-0']} className="w-full">
                {courseStructure.chapters.map((chapter, index) => (
                  <AccordionItem
                    key={chapter.id}
                    value={`chapter-${index}`}
                    className="border-b"
                  >
                    <AccordionTrigger className="py-2">
                      <div className="flex items-center gap-2">
                        <BookOpen className="h-4 w-4 text-primary flex-shrink-0" />
                        <span className="text-sm">{chapter.title}</span>
                      </div>
                    </AccordionTrigger>
                    <AccordionContent>
                      <div className="space-y-1 ml-6">
                        {chapter.sections.map((section) => (
                          <button
                            key={section.id}
                            className={`flex items-center w-full text-left py-1.5 px-2 text-sm rounded ${selectedSectionId === section.id ? 'bg-muted' : 'hover:bg-muted/50'}`}
                            onClick={() => loadSectionContent(section.id)}
                          >
                            {getResourceIcon(section.resourceTypeDiscriminator)}
                            <span>{section.title}</span>
                          </button>
                        ))}
                      </div>
                    </AccordionContent>
                  </AccordionItem>
                ))}
              </Accordion>
            </div>

            {/* 右侧内容预览 */}
            <div className="flex-1 pl-4 overflow-y-auto">
              {!selectedSectionId ? (
                <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
                  <FileText className="h-12 w-12 mb-4" />
                  <div>请从左侧章节目录中选择要预览的小节内容</div>
                </div>
              ) : isLoadingSection ? (
                <div className="flex flex-col items-center justify-center h-full">
                  <Loader2 className="h-8 w-8 animate-spin text-primary mb-4" />
                  <div>加载内容中...</div>
                </div>
              ) : (
                <div className="py-2">
                  {sectionContent ? (
                    <div>
                      {/* 渲染不同类型的内容 */}
                      {sectionContent.type && (
                        renderMediaPreview(sectionContent)
                      )}

                      {sectionContent.questions && (
                        renderQuestionGroupPreview(sectionContent)
                      )}

                      {sectionContent.message && (
                        <div className="text-center text-muted-foreground py-8">
                          {sectionContent.message}
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="text-center text-muted-foreground py-8">
                      无法加载内容
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="text-center text-muted-foreground py-12">
            无法加载课程内容
          </div>
        )}

        {/* 审核评价表单 */}
        <div className="border-t pt-4 mt-auto">
          <div className="space-y-4">
            <div>
              <Label htmlFor="review-comment">审核意见</Label>
              <Textarea
                id="review-comment"
                placeholder="请输入审核意见或拒绝原因..."
                value={reviewComment}
                onChange={(e) => setReviewComment(e.target.value)}
                className="mt-1.5"
              />
            </div>

            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => onOpenChange(false)}
                disabled={isSubmitting}
              >
                取消
              </Button>
              <Button
                variant="destructive"
                onClick={handleReject}
                disabled={isSubmitting || isLoading}
                className="gap-2"
              >
                {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <XCircle className="h-4 w-4" />}
                拒绝审核
              </Button>
              <Button
                onClick={handleApprove}
                disabled={isSubmitting || isLoading}
                className="gap-2"
              >
                {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle className="h-4 w-4" />}
                通过审核
              </Button>
            </DialogFooter>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}