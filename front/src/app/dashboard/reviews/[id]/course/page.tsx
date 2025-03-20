'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Image from 'next/image';
import { 
  Card, 
  CardContent, 
  CardDescription, 
  CardFooter, 
  CardHeader, 
  CardTitle 
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogFooter, 
  DialogHeader, 
  DialogTitle 
} from '@/components/ui/dialog';
import { 
  Tabs, 
  TabsContent, 
  TabsList, 
  TabsTrigger 
} from '@/components/ui/tabs';
import { 
  Accordion, 
  AccordionContent, 
  AccordionItem, 
  AccordionTrigger 
} from '@/components/ui/accordion';
import { 
  ArrowLeft, 
  CheckCircle, 
  XCircle, 
  Info, 
  AlertTriangle, 
  Loader2, 
  BookOpen, 
  FileText, 
  Clock, 
  ChevronRight,
  Eye
} from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { formatDate } from '@/utils/date';
import { ReviewTask, ReviewStatus, ReviewType, ReviewResponseDTO } from '@/types/review';
import { CourseStructureVO, CourseStatus } from '@/types/course';
import { reviewService } from '@/services/review-service';
import { CourseContentPlayer } from '@/components/preview/course-content-player';
import { toast } from 'sonner';
import { CoursePreviewDialog } from '@/components/dashboard/reviews/course-preview-dialog';

export default function CourseReviewPage() {
  const params = useParams();
  const router = useRouter();
  const reviewId = Number(params.id);
  
  const [reviewTask, setReviewTask] = useState<ReviewTask | null>(null);
  const [courseStructure, setCourseStructure] = useState<CourseStructureVO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<string>('preview');
  const [comment, setComment] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [showApproveDialog, setShowApproveDialog] = useState<boolean>(false);
  const [showRejectDialog, setShowRejectDialog] = useState<boolean>(false);
  
  // 预览相关状态
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(null);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [activeSection, setActiveSection] = useState<any | null>(null);
  
  // 加载审核任务和课程结构
  useEffect(() => {
    const loadReviewData = async () => {
      try {
        setIsLoading(true);
        setError(null);
        
        // 获取审核任务详情
        const reviewTask = await reviewService.getReviewTask(String(reviewId));
        setReviewTask(reviewTask);
        
        // 获取课程结构详情（包含章节、小节）
        const courseData = await reviewService.getCourseStructure(Number(reviewTask.targetId));
        setCourseStructure(courseData);
        
        // 如果有章节，默认选中第一个
        if (courseData.chapters && courseData.chapters.length > 0) {
          setSelectedChapterId(courseData.chapters[0].id);
          
          // 如果第一个章节有小节，默认选中第一个小节
          if (courseData.chapters[0].sections && courseData.chapters[0].sections.length > 0) {
            const firstSection = courseData.chapters[0].sections[0];
            setSelectedSectionId(firstSection.id);
            setActiveSection(firstSection);
          }
        }
      } catch (err: any) {
        setError(err.message || '加载审核数据失败');
        console.error('加载审核数据失败:', err);
      } finally {
        setIsLoading(false);
      }
    };
    
    if (reviewId) {
      loadReviewData();
    }
  }, [reviewId]);
  
  // 处理小节选择
  const handleSectionSelect = (section: any) => {
    setSelectedSectionId(section.id);
    setActiveSection(section);
  };
  
  // 处理审核通过
  const handleApprove = async () => {
    try {
      setIsSubmitting(true);
      
      const data: ReviewResponseDTO = {
        comment: comment.trim() || undefined
      };
      
      await reviewService.approveCourse(String(reviewId), comment.trim() || '');
      
      // 跳转回审核列表页
      router.push('/dashboard/reviews');
    } catch (err: any) {
      console.error('审核通过失败:', err);
      setError(err.message || '审核通过失败');
    } finally {
      setIsSubmitting(false);
      setShowApproveDialog(false);
    }
  };
  
  // 处理审核拒绝
  const handleReject = async () => {
    try {
      setIsSubmitting(true);
      
      // 拒绝必须提供原因
      if (!comment.trim()) {
        setError('请提供拒绝原因');
        setIsSubmitting(false);
        return;
      }
      
      await reviewService.rejectCourse(String(reviewId), comment.trim());
      
      // 跳转回审核列表页
      router.push('/dashboard/reviews');
    } catch (err: any) {
      console.error('审核拒绝失败:', err);
      setError(err.message || '审核拒绝失败');
    } finally {
      setIsSubmitting(false);
      setShowRejectDialog(false);
    }
  };
  
  // 返回审核列表
  const handleBack = () => {
    router.push('/dashboard/reviews');
  };
  
  // 渲染课程状态标签
  const renderCourseStatusBadge = (status: number) => {
    switch (status) {
      case CourseStatus.DRAFT:
        return <Badge variant="outline" className="bg-gray-50 text-gray-800 border-gray-300">草稿</Badge>;
      case CourseStatus.REVIEWING:
        return <Badge variant="outline" className="bg-blue-50 text-blue-800 border-blue-300">审核中</Badge>;
      case CourseStatus.PUBLISHED:
        return <Badge variant="outline" className="bg-green-50 text-green-800 border-green-300">已发布</Badge>;
      case CourseStatus.REJECTED:
        return <Badge variant="outline" className="bg-red-50 text-red-800 border-red-300">已拒绝</Badge>;
      default:
        return <Badge variant="outline">未知</Badge>;
    }
  };
  
  // 跳转到全屏预览页面
  const handlePreview = () => {
    router.push(`/dashboard/reviews/${params.id}/preview`);
  };
  
  if (isLoading) {
    return (
      <div className="container py-10">
        <div className="flex flex-col items-center justify-center h-[60vh]">
          <Loader2 className="h-10 w-10 animate-spin text-primary mb-4" />
          <p className="text-muted-foreground">加载审核数据...</p>
        </div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="container py-10">
        <Alert variant="destructive" className="mb-6">
          <AlertTriangle className="h-4 w-4" />
          <AlertTitle>错误</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
        <Button 
          variant="outline" 
          onClick={handleBack}
          className="mt-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          返回审核列表
        </Button>
      </div>
    );
  }
  
  if (!reviewTask || !courseStructure) {
    return (
      <div className="container py-10">
        <Alert variant="destructive" className="mb-6">
          <AlertTriangle className="h-4 w-4" />
          <AlertTitle>未找到审核任务</AlertTitle>
          <AlertDescription>找不到指定ID的审核任务或课程结构</AlertDescription>
        </Alert>
        <Button 
          variant="outline" 
          onClick={handleBack}
          className="mt-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          返回审核列表
        </Button>
      </div>
    );
  }
  
  const course = courseStructure.course;
  
  return (
    <div className="container py-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <Button 
            variant="ghost" 
            onClick={handleBack}
            className="mb-2 pl-0 hover:pl-0"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            返回审核列表
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">课程审核</h1>
          <div className="flex items-center space-x-2 mt-2">
            <span className="text-sm text-muted-foreground">提交于 {formatDate(reviewTask.submittedAt)}</span>
            <span className="text-sm text-muted-foreground">•</span>
            <span className="text-sm text-muted-foreground">
              {course.status === 2 ? '审核中' : 
               course.status === 3 ? '已发布' : 
               course.status === 4 ? '已拒绝' : 
               course.status === 5 ? '已下线' : '草稿'}
            </span>
          </div>
        </div>
        
        <Button
          variant="outline"
          onClick={handlePreview}
        >
          <Eye className="mr-2 h-4 w-4" />
          全屏预览
        </Button>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* 左侧课程信息 */}
        <div className="md:col-span-1">
          <Card>
            <CardHeader>
              <CardTitle>课程信息</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                {course.coverUrl ? (
                  <div className="relative aspect-video rounded-md overflow-hidden mb-4">
                    <img
                      src={course.coverUrl}
                      alt={course.title}
                      className="w-full h-full object-cover"
                    />
                  </div>
                ) : (
                  <div className="aspect-video rounded-md bg-muted flex items-center justify-center mb-4">
                    <BookOpen className="h-12 w-12 text-muted-foreground" />
                  </div>
                )}
              </div>
              
              <div>
                <h3 className="text-lg font-semibold">{course.title}</h3>
                <p className="text-sm text-muted-foreground mt-1">{course.description || '无描述'}</p>
              </div>
              
              <Separator />
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium">状态</p>
                  <div className="mt-1">{renderCourseStatusBadge(course.status)}</div>
                </div>
                <div>
                  <p className="text-sm font-medium">机构</p>
                  <p className="text-sm mt-1">{course.institution?.name || '未知'}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">创建时间</p>
                  <p className="text-sm mt-1">{course.createdAt ? formatDate(course.createdAt) : '未知'}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">章节数</p>
                  <p className="text-sm mt-1">{courseStructure.chapters.length}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">小节数</p>
                  <p className="text-sm mt-1">
                    {courseStructure.chapters.reduce((total, chapter) => total + chapter.sections.length, 0)}
                  </p>
                </div>
                {course.submittedAt && (
                  <div>
                    <p className="text-sm font-medium">提交审核时间</p>
                    <p className="text-sm mt-1">{formatDate(course.submittedAt)}</p>
                  </div>
                )}
                {course.reviewStartedAt && (
                  <div>
                    <p className="text-sm font-medium">开始审核时间</p>
                    <p className="text-sm mt-1">{formatDate(course.reviewStartedAt)}</p>
                  </div>
                )}
                {course.reviewedAt && (
                  <div>
                    <p className="text-sm font-medium">完成审核时间</p>
                    <p className="text-sm mt-1">{formatDate(course.reviewedAt)}</p>
                  </div>
                )}
              </div>
              
              <Separator />
              
              <div>
                <h4 className="text-sm font-medium mb-2">课程章节</h4>
                <Accordion type="single" collapsible className="w-full">
                  {courseStructure.chapters.map((chapter) => (
                    <AccordionItem key={chapter.id} value={chapter.id.toString()}>
                      <AccordionTrigger className="text-sm">
                        {chapter.title}
                      </AccordionTrigger>
                      <AccordionContent>
                        <div className="pl-4 space-y-2">
                          {chapter.sections.map((section) => (
                            <div 
                              key={section.id} 
                              className="text-sm flex items-center justify-between py-1 px-2 rounded-md hover:bg-muted cursor-pointer"
                              onClick={() => {
                                setSelectedChapterId(chapter.id);
                                handleSectionSelect(section);
                                setActiveTab('preview');
                              }}
                            >
                              <span>{section.title}</span>
                              <ChevronRight className="h-4 w-4 text-muted-foreground" />
                            </div>
                          ))}
                        </div>
                      </AccordionContent>
                    </AccordionItem>
                  ))}
                </Accordion>
              </div>
            </CardContent>
          </Card>
        </div>
        
        {/* 右侧内容预览和审核表单 */}
        <div className="md:col-span-2">
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="mb-4">
              <TabsTrigger value="preview">内容预览</TabsTrigger>
            </TabsList>
            
            <TabsContent value="preview">
              <Card>
                <CardHeader>
                  <CardTitle>内容预览</CardTitle>
                  <CardDescription>
                    预览课程内容以进行审核
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {!selectedSectionId ? (
                    <div className="text-center py-12 border-2 border-dashed rounded-md">
                      <FileText className="h-10 w-10 mx-auto text-muted-foreground mb-4" />
                      <h3 className="text-lg font-medium mb-2">请选择小节</h3>
                      <p className="text-muted-foreground">从左侧章节列表中选择一个小节进行预览</p>
                    </div>
                  ) : (
                    <div>
                      {activeSection && (
                        <CourseContentPlayer section={activeSection} />
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
          
          <Card className="mt-6">
            <CardHeader>
              <CardTitle>审核表单</CardTitle>
              <CardDescription>
                填写审核意见并提交审核结果
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div>
                  <h3 className="text-sm font-medium mb-2">审核意见</h3>
                  <Textarea
                    placeholder="请输入审核意见或拒绝原因..."
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    rows={6}
                  />
                  <p className="text-xs text-muted-foreground mt-2">
                    如果拒绝审核，必须提供拒绝原因。
                  </p>
                </div>
                
                <div className="flex justify-end space-x-2 mt-6">
                  <Button 
                    variant="outline" 
                    onClick={() => setShowRejectDialog(true)}
                  >
                    <XCircle className="mr-2 h-4 w-4" />
                    拒绝
                  </Button>
                  <Button 
                    onClick={() => setShowApproveDialog(true)}
                  >
                    <CheckCircle className="mr-2 h-4 w-4" />
                    通过
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
      
      {/* 通过确认对话框 */}
      <Dialog open={showApproveDialog} onOpenChange={setShowApproveDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认通过审核</DialogTitle>
            <DialogDescription>
              通过审核后，该课程将被发布并对学生可见。
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <Textarea
              placeholder="审核意见（可选）..."
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              rows={4}
            />
          </div>
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => setShowApproveDialog(false)}
              disabled={isSubmitting}
            >
              取消
            </Button>
            <Button 
              onClick={handleApprove}
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  处理中...
                </>
              ) : (
                '确认通过'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      {/* 拒绝确认对话框 */}
      <Dialog open={showRejectDialog} onOpenChange={setShowRejectDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认拒绝审核</DialogTitle>
            <DialogDescription>
              拒绝审核时必须提供理由，请详细说明拒绝原因以帮助机构改进课程。
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <Textarea
              placeholder="请输入拒绝原因（必填）..."
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              rows={4}
            />
            {!comment.trim() && (
              <p className="text-sm text-destructive mt-2">
                拒绝审核必须提供原因
              </p>
            )}
          </div>
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => setShowRejectDialog(false)}
              disabled={isSubmitting}
            >
              取消
            </Button>
            <Button 
              variant="destructive"
              onClick={handleReject}
              disabled={isSubmitting || !comment.trim()}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  处理中...
                </>
              ) : (
                '确认拒绝'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 