'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { 
  Tabs, 
  TabsContent, 
  TabsList, 
  TabsTrigger 
} from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import {
  ChevronLeft,
  Eye,
  CheckCircle,
  XCircle,
  Bookmark,
  Users,
  Target,
  FileText,
  AlertCircle,
  Loader2,
  BookOpen
} from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { ReviewContentPlayer } from '@/components/dashboard/reviews/review-content-player';
import { reviewService } from '@/services/review-service';
import { ReviewTask, ReviewResponseDTO, ReviewType, ReviewStatus } from '@/types/review';
import { SectionVO, ChapterVO } from '@/types/course';
import { Textarea } from '@/components/ui/textarea';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import Image from 'next/image';
import { formatDate } from '@/utils/date';

export default function ReviewPreviewPage() {
  const params = useParams();
  const router = useRouter();
  const reviewId = params.id as string;
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [courseData, setCourseData] = useState<any | null>(null);
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(null);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [selectedSection, setSelectedSection] = useState<SectionVO | null>(null);
  const [sectionLoading, setSectionLoading] = useState(false);
  const [sectionError, setSectionError] = useState<string | null>(null);
  const [reviewComment, setReviewComment] = useState('');
  const [activeTab, setActiveTab] = useState('overview');
  const [submitting, setSubmitting] = useState(false);
  
  // 加载课程结构数据
  useEffect(() => {
    let mounted = true;

    async function loadData() {
      if (!reviewId) return;

      try {
        setLoading(true);
        setError(null);
        
        const structure = await reviewService.getCourseStructure(Number(reviewId));
        
        if (!mounted) return;

        setCourseData(structure);
        
        // 默认选择第一章节和小节
        if (structure.chapters && structure.chapters.length > 0) {
          const firstChapter = structure.chapters[0];
          setSelectedChapterId(firstChapter.id);
          
          if (firstChapter.sections && firstChapter.sections.length > 0) {
            const firstSection = firstChapter.sections[0];
            setSelectedSectionId(firstSection.id);
            setSelectedSection(firstSection);
          }
        }
      } catch (err: any) {
        if (!mounted) return;
        console.error('加载课程数据失败:', err);
        setError(err.message || '无法加载课程数据');
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }
    
    loadData();

    return () => {
      mounted = false;
    };
  }, [reviewId]);
  
  // 根据选择的小节ID加载小节数据
  useEffect(() => {
    if (!selectedSectionId || !courseData) return;
    
    // 查找当前选择的小节
    let foundSection: SectionVO | null = null;
    
    for (const chapter of courseData.chapters) {
      for (const section of chapter.sections) {
        if (section.id === selectedSectionId) {
          foundSection = section;
          break;
        }
      }
      if (foundSection) break;
    }
    
    setSelectedSection(foundSection);
  }, [selectedSectionId, courseData]);
  
  // 点击章节
  const handleChapterClick = (chapterId: number) => {
    if (!courseData) return;

    setSelectedChapterId(chapterId);
    
    // 选择该章节的第一个小节
    const chapter = courseData.chapters.find((c: ChapterVO) => c.id === chapterId);
    if (chapter && chapter.sections && chapter.sections.length > 0) {
      setSelectedSectionId(chapter.sections[0].id);
      setSelectedSection(chapter.sections[0]);
    } else {
      setSelectedSectionId(null);
      setSelectedSection(null);
    }
  };
  
  // 点击小节
  const handleSectionClick = (sectionId: number) => {
    if (!courseData) return;

    setSelectedSectionId(sectionId);
    
    // 查找这个小节
    for (const chapter of courseData.chapters) {
      for (const section of chapter.sections) {
        if (section.id === sectionId) {
          setSelectedSection(section);
          return;
        }
      }
    }
  };
  
  // 处理通过课程
  const handleApprove = async () => {
    if (!reviewId) return;
    
    try {
      setSubmitting(true);
      await reviewService.approveCourse(reviewId, reviewComment);
      toast.success('已成功通过课程');
      router.push('/dashboard/reviews');
    } catch (err: any) {
      console.error('通过课程失败:', err);
      toast.error(err.message || '通过课程失败');
    } finally {
      setSubmitting(false);
    }
  };
  
  // 处理拒绝课程
  const handleReject = async () => {
    if (!reviewId) return;
    
    try {
      setSubmitting(true);
      await reviewService.rejectCourse(reviewId, reviewComment);
      toast.success('已拒绝课程');
      router.push('/dashboard/reviews');
    } catch (err: any) {
      console.error('拒绝课程失败:', err);
      toast.error(err.message || '拒绝课程失败');
    } finally {
      setSubmitting(false);
    }
  };
  
  // 渲染章节菜单
  const renderChaptersMenu = () => {
    if (!courseData?.chapters) return null;
    
    return (
      <div className="space-y-4">
        {courseData.chapters.map((chapter: ChapterVO) => (
          <div key={chapter.id} className="space-y-2">
            <div
              className={`flex items-center justify-between p-2 rounded-md cursor-pointer hover:bg-accent ${
                selectedChapterId === chapter.id ? 'bg-accent' : ''
              }`}
              onClick={() => handleChapterClick(chapter.id)}
            >
              <span className="font-medium">{chapter.title}</span>
              <span className="text-xs text-muted-foreground">
                {chapter.sections.length} 小节
              </span>
            </div>
            {selectedChapterId === chapter.id && (
              <div className="ml-4 space-y-1">
                {chapter.sections.map((section: SectionVO) => (
                  <div
                    key={section.id}
                    className={`p-2 rounded-md cursor-pointer hover:bg-accent ${
                      selectedSectionId === section.id ? 'bg-accent' : ''
                    }`}
                    onClick={() => handleSectionClick(section.id)}
                  >
                    <span className="text-sm">{section.title}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    );
  };
  
  if (loading) {
    return (
      <div className="container py-6 flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <Loader2 className="h-10 w-10 animate-spin text-primary mx-auto mb-4" />
          <p className="text-muted-foreground">加载课程数据...</p>
        </div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="container py-6">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>加载失败</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
        
        <Button 
          variant="outline" 
          className="mt-4"
          onClick={() => router.push('/dashboard/reviews')}
        >
          <ChevronLeft className="mr-2 h-4 w-4" />
          返回列表
        </Button>
      </div>
    );
  }
  
  if (!courseData) {
    return (
      <div className="container py-6">
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>没有数据</AlertTitle>
          <AlertDescription>没有找到课程数据</AlertDescription>
        </Alert>
        
        <Button 
          variant="outline" 
          className="mt-4"
          onClick={() => router.push('/dashboard/reviews')}
        >
          <ChevronLeft className="mr-2 h-4 w-4" />
          返回列表
        </Button>
      </div>
    );
  }
  
  return (
    <div className="container py-6 space-y-8">
      <div className="flex items-center justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">{courseData.course.title}</h1>
          <p className="text-muted-foreground">审核预览</p>
        </div>
        
        <Button 
          variant="outline" 
          onClick={() => router.push('/dashboard/reviews')}
        >
          <ChevronLeft className="mr-2 h-4 w-4" />
          返回
        </Button>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* 侧边栏 */}
        <div className="lg:col-span-3 space-y-6">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle>课程章节</CardTitle>
              <CardDescription>
                共 {courseData.chapters?.length || 0} 章节
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-[60vh]">
                {renderChaptersMenu()}
              </ScrollArea>
            </CardContent>
          </Card>
        </div>
        
        {/* 主内容区 */}
        <div className="lg:col-span-9 space-y-6">
          <Card>
            <Tabs
              defaultValue="overview"
              value={activeTab}
              onValueChange={setActiveTab}
              className="w-full"
            >
              <CardHeader>
                <div className="flex flex-col md:flex-row gap-4 md:items-center justify-between">
                  <TabsList>
                    <TabsTrigger value="overview">课程概览</TabsTrigger>
                    <TabsTrigger value="content">课程内容</TabsTrigger>
                    <TabsTrigger value="review">审核表单</TabsTrigger>
                  </TabsList>
                </div>
              </CardHeader>
              
              <CardContent>
                <TabsContent value="overview" className="space-y-6">
                  <div className="space-y-4">
                    <div>
                      <h3 className="text-lg font-medium">基本信息</h3>
                      <div className="mt-2 space-y-2">
                        <div className="flex items-start">
                          <FileText className="h-5 w-5 mr-2 mt-0.5 text-muted-foreground" />
                          <div>
                            <span className="font-medium">标题：</span>
                            <span>{courseData.course.title}</span>
                          </div>
                        </div>
                        <div className="flex items-start">
                          <Bookmark className="h-5 w-5 mr-2 mt-0.5 text-muted-foreground" />
                          <div>
                            <span className="font-medium">所属机构：</span>
                            <span>{courseData.course.institution?.name || '未知'}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <Separator />
                    
                    <div>
                      <h3 className="text-lg font-medium">课程介绍</h3>
                      <p className="mt-2 text-muted-foreground whitespace-pre-line">
                        {courseData.course.description || '暂无课程介绍'}
                      </p>
                    </div>
                  </div>
                </TabsContent>
                
                <TabsContent value="content">
                  {selectedSection ? (
                    <ReviewContentPlayer section={selectedSection} />
                  ) : (
                    <div className="text-center py-12 text-muted-foreground">
                      <FileText className="h-12 w-12 mx-auto mb-4" />
                      <h3 className="text-lg font-medium">未选择小节</h3>
                      <p>请在左侧选择要预览的小节</p>
                    </div>
                  )}
                </TabsContent>
                
                <TabsContent value="review">
                  <div className="space-y-6">
                    <div>
                      <h3 className="text-lg font-medium mb-2">审核信息</h3>
                      <div className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                          <div className="p-4 border rounded-lg">
                            <div className="font-medium">课程状态</div>
                            <div className="mt-1 text-muted-foreground">
                              {courseData.course.status === 2 ? '审核中' : 
                               courseData.course.status === 3 ? '已发布' : 
                               courseData.course.status === 4 ? '已拒绝' : 
                               courseData.course.status === 5 ? '已下线' : 
                               '草稿'}
                            </div>
                          </div>
                          
                          {courseData.course.submittedAt && (
                            <div className="p-4 border rounded-lg">
                              <div className="font-medium">提交审核时间</div>
                              <div className="mt-1 text-muted-foreground">
                                {formatDate(courseData.course.submittedAt)}
                              </div>
                            </div>
                          )}
                          
                          {courseData.course.reviewStartedAt && (
                            <div className="p-4 border rounded-lg">
                              <div className="font-medium">开始审核时间</div>
                              <div className="mt-1 text-muted-foreground">
                                {formatDate(courseData.course.reviewStartedAt)}
                              </div>
                            </div>
                          )}
                          
                          {courseData.course.reviewedAt && (
                            <div className="p-4 border rounded-lg">
                              <div className="font-medium">完成审核时间</div>
                              <div className="mt-1 text-muted-foreground">
                                {formatDate(courseData.course.reviewedAt)}
                              </div>
                            </div>
                          )}
                          
                          {courseData.course.reviewerName && (
                            <div className="p-4 border rounded-lg">
                              <div className="font-medium">审核人</div>
                              <div className="mt-1 text-muted-foreground">
                                {courseData.course.reviewerName}
                              </div>
                            </div>
                          )}
                        </div>
                        
                        {courseData.course.reviewComment && (
                          <div className="p-4 border rounded-lg">
                            <div className="font-medium">审核意见</div>
                            <div className="mt-2 text-muted-foreground whitespace-pre-wrap">
                              {courseData.course.reviewComment}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                    
                    <div>
                      <h3 className="text-lg font-medium mb-2">审核意见</h3>
                      <Textarea
                        placeholder="请输入审核意见..."
                        value={reviewComment}
                        onChange={(e) => setReviewComment(e.target.value)}
                        className="resize-none"
                        rows={6}
                      />
                    </div>
                    
                    <div className="flex justify-end space-x-4">
                      <Button
                        variant="destructive"
                        onClick={handleReject}
                        disabled={submitting}
                      >
                        {submitting ? (
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        ) : (
                          <XCircle className="mr-2 h-4 w-4" />
                        )}
                        拒绝课程
                      </Button>
                      <Button
                        variant="default"
                        onClick={handleApprove}
                        disabled={submitting}
                      >
                        {submitting ? (
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        ) : (
                          <CheckCircle className="mr-2 h-4 w-4" />
                        )}
                        通过课程
                      </Button>
                    </div>
                  </div>
                </TabsContent>
              </CardContent>
            </Tabs>
          </Card>
        </div>
      </div>
    </div>
  );
} 