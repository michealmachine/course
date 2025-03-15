'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Image from 'next/image';
import CourseForm from '@/components/dashboard/courses/course-form';
import { courseService } from '@/services';
import { Course, CourseStatus } from '@/types/course';
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
import { AlertCircle, Upload, Info, FileCheck } from 'lucide-react';
import { formatDate } from '@/utils/date';

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
  
  // 渲染课程状态
  const renderStatusBadge = (status: CourseStatus) => {
    switch (status) {
      case CourseStatus.DRAFT:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">草稿</span>;
      case CourseStatus.REVIEWING:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">审核中</span>;
      case CourseStatus.PUBLISHED:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">已发布</span>;
      case CourseStatus.REJECTED:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">已拒绝</span>;
      default:
        return <span>未知</span>;
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
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{course.title}</h1>
          <div className="flex items-center space-x-2 mt-2">
            <span className="text-sm text-muted-foreground">创建于 {formatDate(course.createdAt)}</span>
            <span className="text-sm text-muted-foreground">•</span>
            {renderStatusBadge(course.status)}
          </div>
        </div>
        
        <div className="flex space-x-2">
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
          
          <Button 
            variant="outline" 
            onClick={() => router.push('/dashboard/courses')}
          >
            返回列表
          </Button>
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
                {course.coverImageUrl ? (
                  <div className="relative w-full max-w-md h-64 rounded-md overflow-hidden">
                    <Image
                      src={course.coverImageUrl}
                      alt={course.title}
                      fill
                      style={{ objectFit: 'cover' }}
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
                  <div className="flex items-center space-x-4">
                    <input
                      id="cover-upload"
                      type="file"
                      accept="image/*"
                      onChange={handleCoverChange}
                      className="block w-full text-sm text-gray-500
                        file:mr-4 file:py-2 file:px-4
                        file:rounded-md file:border-0
                        file:text-sm file:font-semibold
                        file:bg-gray-50 file:text-gray-700
                        hover:file:bg-gray-100"
                    />
                    <Button 
                      onClick={handleCoverUpload} 
                      disabled={!coverFile || isUploading}
                    >
                      {isUploading ? '上传中...' : '上传封面'}
                    </Button>
                  </div>
                  
                  {coverFile && (
                    <p className="text-sm text-green-600">
                      已选择文件: {coverFile.name}
                    </p>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
        
        <TabsContent value="content" className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle>课程内容</CardTitle>
              <CardDescription>
                管理课程章节和小节内容
              </CardDescription>
            </CardHeader>
            
            <CardContent>
              {/* TODO: 章节管理组件 */}
              <p className="text-center py-8 text-muted-foreground">
                课程内容管理待实现...
              </p>
            </CardContent>
            
            <CardFooter className="border-t pt-6">
              <Button 
                variant="outline" 
                className="ml-auto"
                onClick={() => router.push(`/dashboard/courses/${course.id}/chapters`)}
              >
                管理章节内容
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
} 