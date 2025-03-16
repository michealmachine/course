'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ChapterList } from '@/components/dashboard/courses/chapter-list';
import { ChapterSections } from '@/components/dashboard/courses/chapter-sections';
import { courseService, chapterService } from '@/services';
import { Course, Chapter } from '@/types/course';
import { Button } from '@/components/ui/button';
import { 
  ArrowLeft, 
  Loader2, 
  BookOpen,
  AlertCircle,
  Plus
} from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator } from '@/components/ui/breadcrumb';
import { toast } from 'sonner';
import useDebounce from '@/hooks/useDebounce';

export default function CourseChaptersPage() {
  const params = useParams();
  const router = useRouter();
  const courseId = Number(params.id);
  
  const [course, setCourse] = useState<Course | null>(null);
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [isLoadingCourse, setIsLoadingCourse] = useState(true);
  const [isLoadingChapters, setIsLoadingChapters] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // 防抖处理courseId
  const debouncedCourseId = useDebounce(courseId, 300);
  
  // 加载课程详情
  useEffect(() => {
    const fetchCourse = async () => {
      if (!debouncedCourseId) return;
      
      try {
        setIsLoadingCourse(true);
        setError(null);
        const data = await courseService.getCourseById(debouncedCourseId);
        setCourse(data);
      } catch (err: any) {
        setError(err.message || '获取课程详情失败');
        toast.error('获取课程详情失败', {
          description: err.message || '请稍后重试'
        });
        console.error('获取课程详情失败:', err);
      } finally {
        setIsLoadingCourse(false);
      }
    };
    
    fetchCourse();
  }, [debouncedCourseId]);
  
  // 加载章节列表
  const loadChapters = async () => {
    try {
      setIsLoadingChapters(true);
      const data = await chapterService.getChaptersByCourse(courseId);
      setChapters(data);
    } catch (err: any) {
      toast.error('获取章节列表失败', {
        description: err.message || '请稍后重试'
      });
      console.error('获取章节列表失败:', err);
    } finally {
      setIsLoadingChapters(false);
    }
  };
  
  // 加载章节列表
  useEffect(() => {
    if (debouncedCourseId) {
      loadChapters();
    }
  }, [debouncedCourseId]);
  
  // 处理添加新章节
  const handleAddChapter = () => {
    // 不再需要处理弹窗，由ChapterList组件自己管理
  };
  
  // 章节点击处理
  const handleChapterClick = (chapter: Chapter) => {
    // 章节点击不再跳转到小节列表页面
    // 而是在当前页面直接展开/折叠
  };
  
  // 返回课程详情
  const handleBackToCourse = () => {
    router.push(`/dashboard/courses/${courseId}`);
  };
  
  // 处理章节创建后刷新列表
  const handleChapterUpdated = () => {
    // 重新加载章节列表数据
    loadChapters();
  };
  
  if (isLoadingCourse || isLoadingChapters) {
    return (
      <div className="container max-w-7xl py-10">
        <div className="flex flex-col items-center justify-center h-[60vh]">
          <Loader2 className="h-10 w-10 animate-spin text-primary mb-4" />
          <p className="text-muted-foreground">加载章节数据...</p>
        </div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="container max-w-7xl py-10">
        <Alert variant="destructive" className="mb-6">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>错误</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
        <Button 
          variant="outline" 
          onClick={handleBackToCourse}
          className="mt-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          返回课程
        </Button>
      </div>
    );
  }
  
  if (!course) {
    return (
      <div className="container max-w-7xl py-10">
        <Alert variant="destructive" className="mb-6">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>未找到课程</AlertTitle>
          <AlertDescription>找不到指定ID的课程</AlertDescription>
        </Alert>
        <Button 
          variant="outline" 
          onClick={() => router.push('/dashboard/courses')}
          className="mt-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          课程列表
        </Button>
      </div>
    );
  }
  
  return (
    <div className="container max-w-7xl py-10">
      {/* 面包屑导航 */}
      <Breadcrumb className="mb-6">
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="/dashboard">仪表盘</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink href="/dashboard/courses">课程</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink href={`/dashboard/courses/${courseId}`}>{course.title}</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage>章节管理</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
      
      {/* 页面标题 */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <Button 
            variant="ghost" 
            onClick={handleBackToCourse}
            className="mb-2 pl-0 hover:pl-0"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            返回课程详情
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">章节管理</h1>
          <p className="text-muted-foreground mt-2">
            管理 {course?.title} 的章节和小节
          </p>
        </div>
      </div>
      
      {/* 使用ChapterList组件显示和管理章节 */}
      <ChapterList 
        courseId={courseId}
        onChapterClick={handleChapterClick}
        onChapterCreated={handleChapterUpdated}
        onChapterUpdated={handleChapterUpdated}
      />
    </div>
  );
} 