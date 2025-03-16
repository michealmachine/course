'use client';

import React, { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { 
  ArrowLeft, 
  Loader2, 
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbSeparator, BreadcrumbPage, BreadcrumbList } from '@/components/ui/breadcrumb';

import sectionService from '@/services/section';
import chapterService from '@/services/chapter';
import courseService from '@/services/course';
import { SectionForm, SectionFormValues } from '@/components/dashboard/sections/section-form';
import { Course, Chapter } from '@/types/course';

export default function CreateSectionPage() {
  const router = useRouter();
  const params = useParams();
  const courseId = Number(params.id);
  const chapterId = Number(params.chapterId);
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [course, setCourse] = useState<Course | null>(null);
  const [chapter, setChapter] = useState<Chapter | null>(null);
  
  // 加载课程和章节数据
  React.useEffect(() => {
    const loadData = async () => {
      try {
        // 加载课程信息
        const courseData = await courseService.getCourseById(courseId);
        setCourse(courseData);
        
        // 加载章节信息
        const chapterData = await chapterService.getChapterById(chapterId);
        setChapter(chapterData);
      } catch (err: any) {
        console.error('加载数据失败:', err);
        toast.error('加载数据失败', {
          description: err.message || '请稍后重试'
        });
      }
    };
    
    loadData();
  }, [courseId, chapterId]);
  
  // 处理表单提交
  const handleSubmit = async (values: SectionFormValues) => {
    try {
      setIsSubmitting(true);
      setError(null);
      
      // 构建创建小节的请求数据
      const sectionData = {
        ...values,
        chapterId: chapterId
      };
      
      // 调用API创建小节
      const newSection = await sectionService.createSection(sectionData);
      
      toast.success('小节创建成功', {
        description: '新小节已成功添加到章节中'
      });
      
      // 创建成功后进入小节编辑页面，而不是返回到章节列表
      router.push(`/dashboard/courses/${courseId}/chapters/${chapterId}/sections/${newSection.id}`);
    } catch (err: any) {
      console.error('创建小节失败:', err);
      setError(err.message || '创建小节失败');
      throw err; // 向上传递错误
    } finally {
      setIsSubmitting(false);
    }
  };
  
  // 处理取消
  const handleCancel = () => {
    router.push(`/dashboard/courses/${courseId}/chapters/${chapterId}`);
  };
  
  return (
    <div className="container mx-auto py-6 space-y-6">
      {/* 面包屑导航 */}
      <Breadcrumb className="mb-6">
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="/dashboard/courses">课程</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink href={`/dashboard/courses/${courseId}`}>
              {course?.title || '课程详情'}
            </BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink href={`/dashboard/courses/${courseId}/chapters/${chapterId}`}>
              {chapter?.title || '章节详情'}
            </BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage>创建小节</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
      
      {/* 标题和返回按钮 */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">创建新小节</h1>
        <Button variant="outline" onClick={handleCancel}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          返回章节
        </Button>
      </div>
      
      {/* 小节表单 */}
      <Card className="border-none shadow-none">
        <CardContent className="p-0">
          {isSubmitting ? (
            <div className="flex flex-col items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <p className="mt-4 text-muted-foreground">创建小节中...</p>
            </div>
          ) : (
            <SectionForm
              chapterId={chapterId}
              onSubmit={handleSubmit}
              onCancel={handleCancel}
              isSubmitting={isSubmitting}
              error={error}
              mode="create"
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
} 