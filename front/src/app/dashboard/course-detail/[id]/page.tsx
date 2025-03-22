'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion';
import { Badge } from '@/components/ui/badge';
import { 
  ChevronLeft, 
  AlertCircle, 
  BookOpen, 
  Clock, 
  Star, 
  Users, 
  ChevronRight,
  Info,
  Heart,
  HeartOff
} from 'lucide-react';
import courseService from '@/services/course-service';
import { toast } from 'sonner';
import { CourseStructureVO, SectionVO, CoursePaymentType, Section } from '@/types/course';
import { ReviewContentPlayer } from '@/components/dashboard/reviews/review-content-player';
import { formatDate } from '@/lib/utils';
import { ScrollArea } from '@/components/ui/scroll-area';
import favoriteService from '@/services/favorite-service';
import { CourseReviewSection } from '@/components/course/course-review-section';
import { useAuthStore } from '@/stores/auth-store';
import { userCourseService } from '@/services';

// 将SectionVO转换为Section
function convertSectionVOToSection(sectionVO: SectionVO, chapterId: number): Section {
  return {
    ...sectionVO,
    orderIndex: sectionVO.order,
    contentType: sectionVO.resourceTypeDiscriminator === 'MEDIA' ? 'video' : 'text',
    chapterId: chapterId,
    chapterTitle: undefined,
    accessType: sectionVO.accessType,
    estimatedMinutes: sectionVO.duration ? Math.round(sectionVO.duration / 60) : undefined,
    resourceTypeDiscriminator: sectionVO.resourceTypeDiscriminator,
    mediaId: sectionVO.mediaId,
    questionGroupId: sectionVO.questionGroupId,
    media: undefined,
    questionGroup: undefined,
    randomOrder: undefined,
    orderByDifficulty: undefined,
    showAnalysis: undefined,
    resources: undefined,
    questionGroups: undefined,
    createdTime: undefined,
    updatedTime: undefined
  };
}

export default function CourseDetailPage() {
  const params = useParams();
  const router = useRouter();
  const courseId = Number(params.id);
  const { isAuthenticated } = useAuthStore();
  
  // 状态管理
  const [course, setCourse] = useState<CourseStructureVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(null);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [selectedSection, setSelectedSection] = useState<SectionVO | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [expandedChapters, setExpandedChapters] = useState<Set<number>>(new Set());
  const [isFavorite, setIsFavorite] = useState<boolean>(false);
  const [favoritesLoading, setFavoritesLoading] = useState<boolean>(false);
  const [isUserEnrolled, setIsUserEnrolled] = useState<boolean>(false);
  const [enrollmentLoading, setEnrollmentLoading] = useState<boolean>(false);
  
  // 加载课程结构数据
  useEffect(() => {
    const loadCourseData = async () => {
      if (!courseId) return;
      
      try {
        setLoading(true);
        setError(null);
        
        // 获取课程结构数据
        const data = await courseService.getPublicCourseStructure(courseId);
        setCourse(data);
        
        // 默认选择第一章节和小节
        if (data.chapters && data.chapters.length > 0) {
          const firstChapter = data.chapters[0];
          setSelectedChapterId(firstChapter.id);
          
          if (firstChapter.sections && firstChapter.sections.length > 0) {
            const firstSection = firstChapter.sections[0];
            setSelectedSectionId(firstSection.id);
            setSelectedSection(firstSection);
          }
        }
      } catch (err: any) {
        console.error('加载课程数据失败:', err);
        setError(err.message || '加载课程数据失败');
        toast.error('加载课程数据失败');
      } finally {
        setLoading(false);
      }
    };
    
    loadCourseData();
  }, [courseId]);
  
  // 检查用户是否已购买课程
  useEffect(() => {
    const checkEnrollment = async () => {
      if (!courseId || !isAuthenticated) {
        setIsUserEnrolled(false);
        return;
      }
      
      try {
        setEnrollmentLoading(true);
        
        // 调用API检查用户是否已购买课程
        const userCourse = await userCourseService.getUserCourseRecord(courseId);
        setIsUserEnrolled(!!userCourse);
      } catch (err) {
        console.error('检查课程购买状态失败:', err);
        setIsUserEnrolled(false);
      } finally {
        setEnrollmentLoading(false);
      }
    };
    
    checkEnrollment();
  }, [courseId, isAuthenticated]);
  
  // 检查是否已收藏
  useEffect(() => {
    const checkFavorite = async () => {
      if (!courseId) return;
      
      try {
        setFavoritesLoading(true);
        const isFavorited = await favoriteService.checkFavorite(courseId);
        setIsFavorite(isFavorited);
      } catch (err) {
        console.error('检查收藏状态失败:', err);
      } finally {
        setFavoritesLoading(false);
      }
    };
    
    checkFavorite();
  }, [courseId]);
  
  // 处理收藏/取消收藏
  const handleToggleFavorite = async () => {
    if (!courseId) return;
    
    try {
      setFavoritesLoading(true);
      
      if (isFavorite) {
        await favoriteService.removeFavorite(courseId);
        toast.success('已取消收藏');
      } else {
        await favoriteService.addFavorite(courseId);
        toast.success('收藏成功');
      }
      
      setIsFavorite(!isFavorite);
    } catch (err: any) {
      console.error('操作收藏失败:', err);
      toast.error(isFavorite ? '取消收藏失败' : '收藏失败');
    } finally {
      setFavoritesLoading(false);
    }
  };
  
  // 处理章节点击
  const handleChapterClick = (chapterId: number) => {
    if (selectedChapterId === chapterId) {
      setSelectedChapterId(null);
    } else {
      setSelectedChapterId(chapterId);
      
      // 查找该章节的第一个小节并选中
      const chapter = course?.chapters.find(c => c.id === chapterId);
      if (chapter && chapter.sections.length > 0) {
        handleSectionClick(chapter.sections[0].id);
      }
    }
  };
  
  // 处理小节点击
  const handleSectionClick = (sectionId: number) => {
    setSelectedSectionId(sectionId);
    
    // 查找选中的小节
    if (course) {
      for (const chapter of course.chapters) {
        for (const section of chapter.sections) {
          if (section.id === sectionId) {
            setSelectedSection(section);
            setActiveTab('content');
            return;
          }
        }
      }
    }
  };
  
  // 返回按钮处理
  const handleBack = () => {
    router.back();
  };
  
  // 处理购买课程
  const handleEnrollCourse = () => {
    if (!course) return;
    
    // 跳转到课程购买页面 (可根据实际情况调整)
    router.push(`/dashboard/checkout/${course.course.id}`);
  };
  
  // 渲染章节目录
  const renderChapters = () => {
    if (!course || !course.chapters) return null;
    
    return (
      <Accordion
        type="multiple"
        defaultValue={[course.chapters[0]?.id.toString()]}
        className="w-full"
      >
        {course.chapters.map((chapter) => (
          <AccordionItem key={chapter.id} value={chapter.id.toString()}>
            <AccordionTrigger className="hover:bg-muted rounded-md px-2">
              <div className="flex justify-between w-full items-center pr-4">
                <span>{chapter.title}</span>
                <span className="text-xs text-muted-foreground">
                  {chapter.sections.length} 小节
                </span>
              </div>
            </AccordionTrigger>
            <AccordionContent>
              <div className="pl-4 space-y-1">
                {chapter.sections.map((section) => (
                  <div
                    key={section.id}
                    className={`flex items-center justify-between p-2 rounded-md hover:bg-muted cursor-pointer ${
                      selectedSectionId === section.id ? 'bg-muted' : ''
                    }`}
                    onClick={() => handleSectionClick(section.id)}
                  >
                    <span className="text-sm">{section.title}</span>
                    
                    {/* 显示付费标记或资源类型 */}
                    {section.accessType === 1 ? (
                      <Badge variant="outline" className="text-xs">付费</Badge>
                    ) : (
                      <Badge variant="secondary" className="text-xs">免费试看</Badge>
                    )}
                  </div>
                ))}
              </div>
            </AccordionContent>
          </AccordionItem>
        ))}
      </Accordion>
    );
  };
  
  // 加载中状态
  if (loading) {
    return (
      <div className="container py-8">
        <div className="flex items-center mb-6">
          <Button variant="ghost" onClick={handleBack}>
            <ChevronLeft className="h-4 w-4 mr-2" />
            返回
          </Button>
          <Skeleton className="h-6 w-48" />
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-1">
            <Skeleton className="h-[600px] w-full rounded-md" />
          </div>
          <div className="md:col-span-2">
            <Skeleton className="h-[600px] w-full rounded-md" />
          </div>
        </div>
      </div>
    );
  }
  
  // 错误状态
  if (error) {
    return (
      <div className="container py-8">
        <Button variant="ghost" onClick={handleBack} className="mb-6">
          <ChevronLeft className="h-4 w-4 mr-2" />
          返回
        </Button>
        
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>加载失败</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }
  
  // 课程不存在
  if (!course) {
    return (
      <div className="container py-8">
        <Button variant="ghost" onClick={handleBack} className="mb-6">
          <ChevronLeft className="h-4 w-4 mr-2" />
          返回
        </Button>
        
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>课程不存在</AlertTitle>
          <AlertDescription>
            无法找到该课程的详细信息，请确认课程ID是否正确。
          </AlertDescription>
        </Alert>
      </div>
    );
  }
  
  return (
    <div className="min-h-screen bg-slate-50">
      <div className="container py-8">
        {/* 顶部导航栏 */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-4">
            <Button 
              variant="ghost" 
              onClick={handleBack}
              className="hover:bg-slate-100"
            >
              <ChevronLeft className="h-5 w-5 mr-2" />
              返回
            </Button>
            {course?.course.title && (
              <h1 className="text-2xl font-bold text-slate-800">{course.course.title}</h1>
            )}
          </div>
          
          {/* 收藏按钮 */}
          <Button
            variant="outline"
            size="sm"
            onClick={handleToggleFavorite}
            disabled={favoritesLoading}
            className="flex items-center gap-2"
          >
            {favoritesLoading ? (
              <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-solid border-current border-r-transparent" />
            ) : isFavorite ? (
              <Heart className="h-4 w-4 text-red-500 fill-red-500" />
            ) : (
              <Heart className="h-4 w-4" />
            )}
            {isFavorite ? '已收藏' : '收藏'}
          </Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* 左侧课程目录 */}
          <div className="md:col-span-1 space-y-6">
            <Card className="border-0 shadow-sm hover:shadow-md transition-shadow duration-200">
              <CardHeader className="space-y-1">
                <CardTitle className="text-xl font-bold text-slate-800">课程目录</CardTitle>
                <CardDescription className="text-slate-500">
                  共 {course?.chapters.length || 0} 章节，
                  {course?.chapters.reduce((sum, chapter) => sum + chapter.sections.length, 0) || 0} 小节
                </CardDescription>
              </CardHeader>
              <CardContent className="p-0">
                {renderChapters()}
              </CardContent>
            </Card>
          </div>
          
          {/* 右侧内容区 */}
          <div className="md:col-span-2">
            <Card className="border-0 shadow-sm h-[calc(100vh-180px)]">
              <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full h-full flex flex-col">
                <CardHeader className="pb-0 px-4 pt-3">
                  <TabsList className="w-full bg-slate-100 p-1">
                    <TabsTrigger 
                      value="overview" 
                      className="flex-1 data-[state=active]:bg-white data-[state=active]:shadow-sm"
                    >
                      课程信息
                    </TabsTrigger>
                    <TabsTrigger 
                      value="content" 
                      disabled={!selectedSection}
                      className="flex-1 data-[state=active]:bg-white data-[state=active]:shadow-sm"
                    >
                      课程内容
                    </TabsTrigger>
                    <TabsTrigger 
                      value="reviews" 
                      className="flex-1 data-[state=active]:bg-white data-[state=active]:shadow-sm"
                    >
                      评论
                    </TabsTrigger>
                  </TabsList>
                </CardHeader>
                
                <CardContent className="p-4 flex-1 overflow-hidden">
                  <TabsContent value="overview" className="mt-0 h-full">
                    <ScrollArea className="h-[calc(100vh-240px)]">
                      <div className="space-y-6 pr-4">
                        {course?.course.coverUrl && (
                          <div className="relative aspect-video rounded-md overflow-hidden mb-2">
                            <img
                              src={course.course.coverUrl}
                              alt={course.course.title}
                              className="w-full h-full object-cover rounded-md shadow-sm"
                            />
                          </div>
                        )}
                        
                        <div>
                          <h3 className="text-lg font-semibold mb-2 text-slate-800">课程简介</h3>
                          <p className="text-slate-600 leading-relaxed">{course?.course.description || "暂无课程简介"}</p>
                        </div>
                        
                        <Separator className="my-2" />
                        
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                          {course?.course.institution && (
                            <div className="bg-white p-3 rounded-lg border border-slate-100">
                              <p className="text-sm font-medium text-slate-500 mb-1">所属机构</p>
                              <div className="flex items-center">
                                <span className="font-medium text-slate-800">{course.course.institution.name}</span>
                              </div>
                            </div>
                          )}
                          
                          {course?.course.paymentType !== undefined && (
                            <div className="bg-white p-3 rounded-lg border border-slate-100">
                              <p className="text-sm font-medium text-slate-500 mb-1">付费类型</p>
                              <Badge variant={course.course.paymentType === CoursePaymentType.FREE ? "secondary" : "default"}
                                className={course.course.paymentType === CoursePaymentType.FREE ? 
                                  "bg-emerald-50 text-emerald-700 border-emerald-200" : ""}>
                                {course.course.paymentType === CoursePaymentType.FREE ? '免费' : '付费'}
                              </Badge>
                            </div>
                          )}
                          
                          {course?.course.price !== undefined && course.course.paymentType === CoursePaymentType.PAID && (
                            <div className="bg-white p-3 rounded-lg border border-slate-100">
                              <p className="text-sm font-medium text-slate-500 mb-1">价格</p>
                              <p className="text-primary font-medium text-lg">¥{course.course.price}</p>
                            </div>
                          )}
                          
                          {course?.course.totalDuration !== undefined && (
                            <div className="bg-white p-3 rounded-lg border border-slate-100">
                              <p className="text-sm font-medium text-slate-500 mb-1">总时长</p>
                              <div className="flex items-center">
                                <Clock className="h-4 w-4 mr-2 text-slate-500" />
                                <span className="font-medium text-slate-800">{Math.round(course.course.totalDuration / 60)} 分钟</span>
                              </div>
                            </div>
                          )}
                          
                          {course?.course.averageRating !== undefined && (
                            <div className="bg-white p-3 rounded-lg border border-slate-100">
                              <p className="text-sm font-medium text-slate-500 mb-1">课程评分</p>
                              <div className="flex items-center">
                                <Star className="h-4 w-4 text-yellow-500 fill-yellow-500 mr-2" />
                                <span className="font-medium text-slate-800">
                                  {course.course.averageRating !== undefined && course.course.averageRating !== null
                                    ? course.course.averageRating.toFixed(1)
                                    : '暂无评分'}
                                </span>
                                <span className="text-sm text-slate-500 ml-1">
                                  ({course.course.ratingCount || 0})
                                </span>
                              </div>
                            </div>
                          )}
                          
                          {course?.course.studentCount !== undefined && (
                            <div className="bg-white p-3 rounded-lg border border-slate-100">
                              <p className="text-sm font-medium text-slate-500 mb-1">学习人数</p>
                              <div className="flex items-center">
                                <Users className="h-4 w-4 mr-2 text-slate-500" />
                                <span className="font-medium text-slate-800">{course.course.studentCount}</span>
                              </div>
                            </div>
                          )}
                        </div>
                        
                        {(course?.course.targetAudience || course?.course.learningObjectives) && (
                          <>
                            <Separator className="my-2" />
                            
                            <div className="space-y-4">
                              {course?.course.targetAudience && (
                                <div>
                                  <h3 className="text-lg font-semibold mb-1 text-slate-800">适合人群</h3>
                                  <p className="text-slate-600">{course.course.targetAudience}</p>
                                </div>
                              )}
                              
                              {course?.course.learningObjectives && (
                                <div>
                                  <h3 className="text-lg font-semibold mb-1 text-slate-800">学习目标</h3>
                                  <p className="text-slate-600">{course.course.learningObjectives}</p>
                                </div>
                              )}
                            </div>
                          </>
                        )}
                      </div>
                    </ScrollArea>
                  </TabsContent>
                  
                  <TabsContent value="content" className="mt-0 h-full">
                    {selectedSection ? (
                      <ScrollArea className="h-[calc(100vh-240px)]">
                        <div className="pr-4">
                          <div className="mb-4">
                            <h2 className="text-xl font-bold text-slate-800 mb-2">{selectedSection.title}</h2>
                            {selectedSection.description && (
                              <p className="text-slate-600 mb-2">{selectedSection.description}</p>
                            )}
                            
                            <div className="flex flex-wrap gap-2">
                              {selectedSection.duration && (
                                <Badge variant="outline" className="bg-white flex items-center px-2 py-0.5">
                                  <Clock className="h-3 w-3 mr-1 text-slate-500" />
                                  {Math.round(selectedSection.duration / 60)} 分钟
                                </Badge>
                              )}
                              
                              <Badge 
                                variant={selectedSection.accessType === 0 ? "secondary" : "outline"}
                                className={`flex items-center px-2 py-0.5 ${
                                  selectedSection.accessType === 0 
                                    ? 'bg-emerald-50 text-emerald-700 border-emerald-200' 
                                    : 'bg-white'
                                }`}
                              >
                                {selectedSection.accessType === 0 ? '免费试看' : '付费内容'}
                              </Badge>
                              
                              <Badge 
                                variant="outline" 
                                className="bg-white flex items-center px-2 py-0.5"
                              >
                                {selectedSection.resourceTypeDiscriminator === 'MEDIA' ? '媒体' : 
                                 selectedSection.resourceTypeDiscriminator === 'QUESTION_GROUP' ? '题组' : '未知类型'}
                              </Badge>
                            </div>
                          </div>
                          
                          {/* 小节内容播放器 */}
                          {(selectedSection.resourceTypeDiscriminator === 'MEDIA' && selectedSection.mediaId) || 
                           (selectedSection.resourceTypeDiscriminator === 'QUESTION_GROUP' && selectedSection.questionGroupId) ? (
                            <div className="bg-white rounded-lg shadow-sm">
                              <ReviewContentPlayer section={selectedSection} />
                            </div>
                          ) : (
                            <div className="bg-slate-50 p-6 rounded-lg text-center border border-slate-100">
                              <Info className="h-12 w-12 mx-auto text-slate-400 mb-4" />
                              <h3 className="text-lg font-semibold text-slate-800 mb-2">内容需要付费查看</h3>
                              <p className="text-slate-600 mb-4">此内容为付费内容，请购买课程后查看完整内容</p>
                              <Button variant="default" className="bg-primary hover:bg-primary/90">
                                立即购买课程
                              </Button>
                            </div>
                          )}
                        </div>
                      </ScrollArea>
                    ) : (
                      <div className="flex items-center justify-center h-[calc(100vh-240px)] bg-slate-50 rounded-lg">
                        <div className="text-center p-6">
                          <BookOpen className="h-12 w-12 mx-auto text-slate-400 mb-4" />
                          <h3 className="text-lg font-semibold text-slate-800 mb-2">请选择小节</h3>
                          <p className="text-slate-600">请从左侧章节目录中选择一个小节来查看内容</p>
                        </div>
                      </div>
                    )}
                  </TabsContent>
                  
                  <TabsContent value="reviews" className="mt-0 h-full">
                    <ScrollArea className="h-[calc(100vh-240px)] pr-4">
                      <div className="pb-6">
                        {!isUserEnrolled && !enrollmentLoading && course && course.course.paymentType !== 0 && (
                          <div className="mb-6 bg-gradient-to-r from-primary/10 to-primary/5 p-4 rounded-lg border border-primary/20 flex justify-between items-center">
                            <div>
                              <h3 className="font-medium text-primary mb-1">购买课程，参与评论</h3>
                              <p className="text-sm text-slate-600">
                                购买课程后，您可以为课程评分并分享您的学习体验
                              </p>
                            </div>
                            <Button onClick={handleEnrollCourse} className="whitespace-nowrap">
                              购买课程
                            </Button>
                          </div>
                        )}
                        
                        {course && (
                          <CourseReviewSection 
                            courseId={course.course.id} 
                            isUserEnrolled={isUserEnrolled}
                          />
                        )}
                      </div>
                    </ScrollArea>
                  </TabsContent>
                </CardContent>
              </Tabs>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
} 