'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Image from 'next/image';
import CourseForm from '@/components/dashboard/courses/course-form';
import { courseService, chapterService, sectionService } from '@/services';
import { Course, CourseStatus, Chapter, Section } from '@/types/course';
import { Button } from '@/components/ui/button';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/ui/tabs';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { 
  AlertCircle, 
  Upload, 
  Info, 
  FileCheck, 
  Loader2, 
  Clock, 
  CheckCircle, 
  Ban, 
  Edit, 
  FileEdit, 
  ArrowLeft,
  ChevronRight,
  BookOpen,
  Eye
} from 'lucide-react';
import { formatDate } from '@/utils/date';
import CourseStatusBadge from '@/components/dashboard/courses/CourseStatusBadge';
import { CourseContentPlayer } from '@/components/preview/course-content-player';

export default function CourseDetailPage() {
  const params = useParams();
  const router = useRouter();
  const courseId = Number(params.id);
  
  const [course, setCourse] = useState<Course | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('info');
  
  // 预览相关状态
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(null);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [sections, setSections] = useState<Record<number, Section[]>>({});
  const [activeSection, setActiveSection] = useState<Section | null>(null);
  const [isLoadingPreview, setIsLoadingPreview] = useState(false);
  
  // 加载课程详情
  useEffect(() => {
    const fetchCourse = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const data = await courseService.getCourseById(courseId);
        setCourse(data);
      } catch (err: any) {
        setError(err.message || '获取课程详情失败');
        console.error('获取课程详情失败:', err);
      } finally {
        setIsLoading(false);
      }
    };
    
    if (courseId) {
      fetchCourse();
    }
  }, [courseId]);
  
  // 处理课程更新成功
  const handleUpdateSuccess = (updatedCourse: Course) => {
    setCourse(updatedCourse);
  };
  
  // 处理封面文件选择
  const handleCoverChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setCoverFile(e.target.files[0]);
      setUploadError(null);
    }
  };
  
  // 上传封面
  const handleCoverUpload = async () => {
    if (!coverFile || !course) return;
    
    try {
      setIsUploading(true);
      setUploadError(null);
      
      const updatedCourse = await courseService.updateCourseCover(course.id, coverFile);
      setCourse(updatedCourse);
      setCoverFile(null);
      
      // 重置文件输入
      const fileInput = document.getElementById('cover-upload') as HTMLInputElement;
      if (fileInput) fileInput.value = '';
      
    } catch (err: any) {
      setUploadError(err.message || '上传封面失败');
      console.error('上传封面失败:', err);
    } finally {
      setIsUploading(false);
    }
  };
  
  // 提交审核
  const handleSubmitForReview = async () => {
    if (!course) return;
    
    try {
      setIsLoading(true);
      const updatedCourse = await courseService.submitForReview(course.id);
      setCourse(updatedCourse);
    } catch (err: any) {
      setError(err.message || '提交审核失败');
    } finally {
      setIsLoading(false);
    }
  };
  
  // 重新编辑被拒绝的课程
  const handleReEdit = async () => {
    if (!course) return;
    
    try {
      setIsLoading(true);
      const updatedCourse = await courseService.reEditRejectedCourse(course.id);
      setCourse(updatedCourse);
    } catch (err: any) {
      setError(err.message || '重新编辑课程失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 加载课程章节和小节（用于预览）
  useEffect(() => {
    const loadPreviewData = async () => {
      if (!course || activeTab !== 'content') return;
      
      try {
        setIsLoadingPreview(true);
        
        // 加载章节
        const chaptersData = await chapterService.getChaptersByCourse(course.id);
        setChapters(chaptersData);
        
        // 默认选择第一个章节
        if (chaptersData.length > 0 && !selectedChapterId) {
          setSelectedChapterId(chaptersData[0].id);
        }
      } catch (err) {
        console.error('加载预览数据失败:', err);
      } finally {
        setIsLoadingPreview(false);
      }
    };
    
    loadPreviewData();
  }, [course, activeTab, selectedChapterId]);
  
  // 根据选中的章节加载小节
  useEffect(() => {
    const loadSections = async (chapterId: number) => {
      if (!chapterId || activeTab !== 'content') return;
      
      try {
        // 如果已经加载过该章节的小节，则不重复加载
        if (sections[chapterId]) return;
        
        const sectionData = await sectionService.getSectionsByChapter(chapterId);
        setSections(prev => ({
          ...prev,
          [chapterId]: sectionData
        }));
        
        // 默认选择第一个小节
        if (sectionData.length > 0 && !selectedSectionId) {
          setSelectedSectionId(sectionData[0].id);
          setActiveSection(sectionData[0]);
        }
      } catch (err) {
        console.error(`加载章节 ${chapterId} 的小节失败:`, err);
      }
    };
    
    if (selectedChapterId) {
      loadSections(selectedChapterId);
    }
  }, [selectedChapterId, sections, selectedSectionId, activeTab]);
  
  // 处理小节选择
  const handleSectionSelect = (section: Section) => {
    setSelectedSectionId(section.id);
    setActiveSection(section);
  };
  
  // 生成预览链接并在新窗口打开
  const handleOpenFullPreview = async () => {
    if (!course) return;
    
    try {
      const previewData = await courseService.generatePreviewUrl(course.id);
      if (previewData && previewData.url) {
        window.open(`/preview/${previewData.url.split('/').pop()}`, '_blank');
      }
    } catch (err) {
      console.error('生成预览链接失败:', err);
    }
  };
  
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
      </div>
    );
  }
  
  if (error) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>错误</AlertTitle>
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }
  
  if (!course) {
    return (
      <Alert>
        <Info className="h-4 w-4" />
        <AlertTitle>未找到课程</AlertTitle>
        <AlertDescription>找不到指定ID的课程</AlertDescription>
      </Alert>
    );
  }
  
  return (
    <div className="container mx-auto py-6">
      <Button 
        variant="ghost" 
        className="mb-4" 
        onClick={() => router.push('/dashboard/courses')}
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        返回课程列表
      </Button>

      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">{course.title}</h1>
            <div className="flex items-center space-x-2 mt-2">
              <span className="text-sm text-muted-foreground">创建于 {course.createdAt ? formatDate(course.createdAt) : '未知时间'}</span>
              <span className="text-sm text-muted-foreground">•</span>
              <CourseStatusBadge status={course.status} />
            </div>
          </div>
          
          <div className="flex space-x-2">
            <Button 
              variant="outline" 
              onClick={handleOpenFullPreview}
            >
              <Eye className="mr-2 h-4 w-4" />
              全屏预览
            </Button>
            
            {course.status === CourseStatus.DRAFT && (
              <Button onClick={handleSubmitForReview}>
                <FileCheck className="mr-2 h-4 w-4" />
                提交审核
              </Button>
            )}
            
            {course.status === CourseStatus.REJECTED && (
              <Button onClick={handleReEdit}>
                重新编辑
              </Button>
            )}
          </div>
        </div>
        
        {course.status === CourseStatus.REJECTED && course.reviewComment && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>审核未通过</AlertTitle>
            <AlertDescription>{course.reviewComment}</AlertDescription>
          </Alert>
        )}
        
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList>
            <TabsTrigger value="info">基本信息</TabsTrigger>
            <TabsTrigger value="cover">封面图片</TabsTrigger>
            <TabsTrigger value="content" disabled={course.status === CourseStatus.REVIEWING}>
              课程内容
            </TabsTrigger>
          </TabsList>
          
          <TabsContent value="info" className="mt-6">
            {(course.status === CourseStatus.DRAFT || course.status === CourseStatus.REJECTED) ? (
              <CourseForm course={course} onSuccess={handleUpdateSuccess} />
            ) : (
              <div className="rounded-md bg-amber-50 p-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <Info className="h-5 w-5 text-amber-400" />
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-amber-800">课程审核中</h3>
                    <div className="mt-2 text-sm text-amber-700">
                      <p>课程正在审核中，无法编辑信息。审核通过后可以添加内容。</p>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </TabsContent>
          
          <TabsContent value="cover" className="mt-6">
            <Card>
              <CardHeader>
                <CardTitle>课程封面</CardTitle>
                <CardDescription>
                  上传吸引人的课程封面图片，推荐尺寸 1280x720 像素
                </CardDescription>
              </CardHeader>
              
              <CardContent>
                {uploadError && (
                  <Alert variant="destructive" className="mb-6">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>上传失败</AlertTitle>
                    <AlertDescription>{uploadError}</AlertDescription>
                  </Alert>
                )}
                
                <div className="flex justify-center mb-6">
                  {course.coverUrl ? (
                    <div className="relative w-full max-w-md h-64 rounded-md overflow-hidden">
                      <img
                        src={course.coverUrl}
                        alt={course.title}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  ) : (
                    <div className="border border-dashed border-gray-300 rounded-md p-12 text-center">
                      <Upload className="mx-auto h-12 w-12 text-gray-400" />
                      <p className="mt-2 text-sm text-gray-500">尚未上传封面图片</p>
                    </div>
                  )}
                </div>
                
                {(course.status === CourseStatus.DRAFT || course.status === CourseStatus.REJECTED) && (
                  <div className="space-y-4">
                    <div className="flex flex-col space-y-4">
                      <div className="flex items-center space-x-4">
                        <Button 
                          variant="outline"
                          onClick={() => {
                            const input = document.getElementById('cover-upload');
                            if (input) input.click();
                          }}
                        >
                          <Upload className="mr-2 h-4 w-4" />
                          选择图片
                        </Button>
                        
                        <input
                          id="cover-upload"
                          type="file"
                          accept="image/*"
                          onChange={handleCoverChange}
                          className="hidden"
                        />
                        
                        {coverFile && (
                          <span className="text-sm text-gray-600">
                            已选择: {coverFile.name}
                          </span>
                        )}
                      </div>
                      
                      {coverFile && (
                        <Button 
                          onClick={handleCoverUpload} 
                          disabled={isUploading}
                        >
                          {isUploading ? (
                            <>
                              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                              上传中...
                            </>
                          ) : (
                            '上传封面'
                          )}
                        </Button>
                      )}
                    </div>
                    
                    <p className="text-xs text-muted-foreground mt-4">
                      支持JPG、PNG格式，文件大小不超过5MB。推荐使用16:9比例的图片以获得最佳显示效果。
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="content" className="mt-6">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <div>
                  <CardTitle>课程内容预览</CardTitle>
                  <CardDescription>
                    预览课程内容展示效果
                  </CardDescription>
                </div>
                <Button 
                  variant="outline" 
                  onClick={handleOpenFullPreview}
                >
                  <Eye className="mr-2 h-4 w-4" />
                  全屏预览
                </Button>
              </CardHeader>
              
              <CardContent>
                {isLoadingPreview ? (
                  <div className="flex items-center justify-center h-64">
                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                  </div>
                ) : chapters.length === 0 ? (
                  <div className="text-center py-12 border-2 border-dashed rounded-md">
                    <BookOpen className="h-10 w-10 mx-auto text-muted-foreground mb-4" />
                    <h3 className="text-lg font-medium mb-2">尚未添加课程内容</h3>
                    <p className="text-muted-foreground mb-4">请先添加章节和小节内容</p>
                    <Button 
                      onClick={() => router.push(`/dashboard/courses/${course.id}/chapters`)}
                    >
                      <FileEdit className="mr-2 h-4 w-4" />
                      管理课程内容
                    </Button>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* 左侧章节导航 */}
                    <div className="md:col-span-1">
                      <div className="border rounded-md">
                        <div className="p-4 bg-muted font-medium border-b">
                          课程章节
                        </div>
                        <div className="max-h-[60vh] overflow-y-auto p-2">
                          <div className="space-y-2">
                            {chapters.map((chapter) => (
                              <div key={chapter.id} className="space-y-1">
                                <Button
                                  variant={selectedChapterId === chapter.id ? "secondary" : "ghost"}
                                  className="w-full justify-start text-left font-medium"
                                  onClick={() => setSelectedChapterId(chapter.id)}
                                >
                                  {chapter.title}
                                </Button>
                                
                                {selectedChapterId === chapter.id && sections[chapter.id] && (
                                  <div className="pl-4 space-y-1 border-l ml-3">
                                    {sections[chapter.id].map((section) => (
                                      <Button
                                        key={section.id}
                                        variant={selectedSectionId === section.id ? "default" : "ghost"}
                                        size="sm"
                                        className="w-full justify-start text-left"
                                        onClick={() => handleSectionSelect(section)}
                                      >
                                        <div className="flex items-center gap-2">
                                          {selectedSectionId === section.id && (
                                            <ChevronRight className="h-3 w-3" />
                                          )}
                                          <span>{section.title}</span>
                                        </div>
                                      </Button>
                                    ))}
                                  </div>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    {/* 右侧内容预览 */}
                    <div className="md:col-span-2">
                      {activeSection ? (
                        <CourseContentPlayer section={activeSection} />
                      ) : (
                        <Card>
                          <CardContent className="py-12 text-center">
                            <p className="text-muted-foreground">请从左侧选择一个小节以查看内容</p>
                          </CardContent>
                        </Card>
                      )}
                    </div>
                  </div>
                )}
              </CardContent>
              
              <CardFooter className="border-t pt-6">
                <Button 
                  variant="default" 
                  className="ml-auto"
                  onClick={() => router.push(`/dashboard/courses/${course.id}/chapters`)}
                >
                  <FileEdit className="mr-2 h-4 w-4" />
                  管理章节内容
                </Button>
              </CardFooter>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
} 