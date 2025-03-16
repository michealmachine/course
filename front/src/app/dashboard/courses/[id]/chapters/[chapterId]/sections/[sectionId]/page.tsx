'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { 
  ArrowLeft, 
  PencilIcon, 
  Trash2, 
  Loader2, 
  Video, 
  FileText,
  BookOpen,
  Grip,
  Plus,
  Save,
  Package
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Breadcrumb, 
  BreadcrumbItem, 
  BreadcrumbLink, 
  BreadcrumbSeparator,
  BreadcrumbPage,
  BreadcrumbList
} from '@/components/ui/breadcrumb';
import { Badge } from '@/components/ui/badge';
import { Avatar } from '@/components/ui/avatar';
import { ScrollArea } from '@/components/ui/scroll-area';

import courseService from '@/services/course';
import chapterService from '@/services/chapter';
import sectionService from '@/services/section';
import { SectionResourceList } from '@/components/dashboard/sections/section-resource-list';
import { AddResourceDialog } from '@/components/dashboard/sections/add-resource-dialog';
import { Section, Chapter, Course } from '@/types/course';
import { SectionDialog } from '@/components/dashboard/sections/section-dialog';
import { SectionFormValues } from '@/components/dashboard/sections/section-form';

export default function SectionDetailsPage() {
  const router = useRouter();
  const params = useParams();
  const courseId = Number(params.id);
  const chapterId = Number(params.chapterId);
  const sectionId = Number(params.sectionId);
  
  const [course, setCourse] = useState<Course | null>(null);
  const [chapter, setChapter] = useState<Chapter | null>(null);
  const [section, setSection] = useState<Section | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [resourceUpdated, setResourceUpdated] = useState(false);
  
  // 加载数据
  useEffect(() => {
    const loadCourse = async () => {
      try {
        const data = await courseService.getCourseById(courseId);
        setCourse(data);
      } catch (err: any) {
        console.error('加载课程失败:', err);
        toast.error('加载课程失败', {
          description: err.message || '请稍后重试'
        });
      }
    };
    
    loadCourse();
  }, [courseId]);
  
  useEffect(() => {
    const loadChapter = async () => {
      try {
        const data = await chapterService.getChapterById(chapterId);
        setChapter(data);
      } catch (err: any) {
        console.error('加载章节失败:', err);
        toast.error('加载章节失败', {
          description: err.message || '请稍后重试'
        });
      }
    };
    
    loadChapter();
  }, [chapterId]);
  
  const loadSection = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await sectionService.getSectionById(sectionId);
      setSection(data);
      setResourceUpdated(false);
    } catch (err: any) {
      console.error('加载小节失败:', err);
      setError(err.message || '加载小节数据失败');
    } finally {
      setIsLoading(false);
    }
  };
  
  useEffect(() => {
    loadSection();
  }, [sectionId]);
  
  // 刷新资源列表
  const handleResourceUpdated = () => {
    setResourceUpdated(true);
    loadSection(); // 重新加载小节数据，包括资源信息
    toast.success('资源已更新', {
      description: '小节资源列表已更新'
    });
  };
  
  // 打开编辑弹窗
  const handleEdit = () => {
    setIsEditDialogOpen(true);
  };
  
  // 处理编辑提交
  const handleEditSubmit = async (values: SectionFormValues) => {
    try {
      await sectionService.updateSection(sectionId, {
        ...values,
        chapterId: chapterId
      });
      
      toast.success('小节已更新', {
        description: '小节信息已成功保存'
      });
      
      // 重新加载小节数据
      loadSection();
    } catch (error: any) {
      console.error('更新小节失败:', error);
      throw error;
    }
  };
  
  // 返回到章节页面
  const handleBack = () => {
    router.push(`/dashboard/courses/${courseId}/chapters/${chapterId}`);
  };
  
  // 删除小节
  const handleDelete = async () => {
    if (!confirm('确定要删除该小节吗？此操作无法撤销。')) {
      return;
    }
    
    try {
      await sectionService.deleteSection(sectionId);
      toast.success('小节已删除', {
        description: '小节已成功删除'
      });
      router.push(`/dashboard/courses/${courseId}/chapters/${chapterId}`);
    } catch (err: any) {
      console.error('删除小节失败:', err);
      toast.error('删除小节失败', {
        description: err.message || '请稍后重试'
      });
    }
  };
  
  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <Loader2 className="h-12 w-12 animate-spin text-primary" />
        <p className="mt-4 text-lg">加载小节信息...</p>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="container mx-auto py-6 space-y-8">
        <div className="flex flex-col items-center justify-center min-h-[40vh]">
          <div className="bg-destructive/10 text-destructive text-sm p-4 rounded-md mb-4 max-w-md">
            <p>加载小节数据失败: {error}</p>
          </div>
          <Button onClick={handleBack} variant="outline" className="mt-4">
            <ArrowLeft className="mr-2 h-4 w-4" />
            返回章节页面
          </Button>
        </div>
      </div>
    );
  }
  
  if (!section) {
    return (
      <div className="container mx-auto py-6 space-y-8">
        <div className="flex flex-col items-center justify-center min-h-[40vh]">
          <div className="bg-destructive/10 text-destructive text-sm p-4 rounded-md mb-4 max-w-md">
            <p>找不到小节数据</p>
          </div>
          <Button onClick={handleBack} variant="outline" className="mt-4">
            <ArrowLeft className="mr-2 h-4 w-4" />
            返回章节页面
          </Button>
        </div>
      </div>
    );
  }
  
  return (
    <>
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
              <BreadcrumbPage>{section.title}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>
        
        {/* 操作按钮 */}
        <div className="flex justify-between items-center">
          <Button variant="outline" onClick={handleBack}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            返回章节
          </Button>
          
          <div className="flex space-x-2">
            <Button 
              variant="outline" 
              size="sm" 
              onClick={handleEdit}
            >
              <PencilIcon className="mr-2 h-4 w-4" />
              编辑小节
            </Button>
            <Button 
              variant="destructive" 
              size="sm"
              onClick={handleDelete}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              删除小节
            </Button>
          </div>
        </div>
        
        {/* 小节标题和描述 */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">{section.title}</h1>
            <p className="text-muted-foreground mt-1">{section.description || '无描述'}</p>
          </div>
          
          <div className="flex items-center gap-4">
            <Badge variant="outline" className="text-sm px-3 py-1">
              内容类型: {section.contentType || '未指定'}
            </Badge>
            {section.estimatedMinutes && (
              <Badge variant="outline" className="text-sm px-3 py-1">
                学习时长: {section.estimatedMinutes} 分钟
              </Badge>
            )}
            <Badge variant="outline" className={
              section.accessType === 0 ? 'bg-green-100 border-green-200 text-green-800' : 
              'bg-blue-100 border-blue-200 text-blue-800'
            }>
              {section.accessType === 0 ? '免费' : '需购买'}
            </Badge>
          </div>
        </div>
        
        <Separator className="mb-6" />
        
        {/* 内容选项卡 */}
        <div className="container mx-auto">
          <Tabs 
            defaultValue="overview" 
            value={activeTab} 
            onValueChange={setActiveTab}
            className="w-full"
          >
            <TabsList className="mb-4">
              <TabsTrigger value="overview" className="flex items-center">
                <FileText className="mr-2 h-4 w-4" />
                基本信息
              </TabsTrigger>
              <TabsTrigger value="resources" className="flex items-center">
                <Package className="mr-2 h-4 w-4" />
                资源管理
              </TabsTrigger>
              <TabsTrigger value="preview" className="flex items-center">
                <BookOpen className="mr-2 h-4 w-4" />
                内容预览
              </TabsTrigger>
            </TabsList>
            
            {/* 基本信息选项卡 */}
            <TabsContent value="overview" className="min-h-[400px]">
              <Card>
                <CardHeader>
                  <CardTitle>小节信息</CardTitle>
                  <CardDescription>小节的基本信息和设置</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <h3 className="text-sm font-medium text-muted-foreground mb-1">标题</h3>
                      <p className="text-base">{section.title}</p>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-muted-foreground mb-1">内容类型</h3>
                      <p className="text-base">{section.contentType || '未指定'}</p>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-muted-foreground mb-1">访问权限</h3>
                      <p className="text-base">{section.accessType === 0 ? '免费试看' : '付费访问'}</p>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-muted-foreground mb-1">预计学习时间</h3>
                      <p className="text-base">{section.estimatedMinutes ? `${section.estimatedMinutes} 分钟` : '未设置'}</p>
                    </div>
                    <div className="md:col-span-2">
                      <h3 className="text-sm font-medium text-muted-foreground mb-1">描述</h3>
                      <p className="text-base">{section.description || '无描述'}</p>
                    </div>
                  </div>
                </CardContent>
                <CardFooter>
                  <Button variant="outline" onClick={handleEdit}>
                    <PencilIcon className="mr-2 h-4 w-4" />
                    编辑信息
                  </Button>
                </CardFooter>
              </Card>
            </TabsContent>
            
            {/* 资源管理选项卡 */}
            <TabsContent value="resources" className="min-h-[400px]">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <div>
                    <CardTitle>小节资源</CardTitle>
                    <CardDescription>管理小节的媒体资源和题目组</CardDescription>
                  </div>
                  <AddResourceDialog 
                    sectionId={sectionId}
                    onResourceAdded={handleResourceUpdated}
                    trigger={
                      <Button id="add-resource-button">
                        <Plus className="mr-2 h-4 w-4" />
                        添加资源
                      </Button>
                    }
                  />
                </CardHeader>
                <CardContent>
                  <SectionResourceList 
                    sectionId={sectionId} 
                    onAddResource={() => {
                      document.getElementById('add-resource-button')?.click();
                    }} 
                    key={resourceUpdated ? 'updated' : 'initial'}
                  />
                </CardContent>
              </Card>
            </TabsContent>
            
            {/* 内容预览选项卡 */}
            <TabsContent value="preview" className="min-h-[400px]">
              <Card>
                <CardHeader>
                  <CardTitle>内容预览</CardTitle>
                  <CardDescription>预览小节的内容显示效果</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="text-center p-8 border rounded-md">
                    <p className="text-muted-foreground">内容预览功能开发中...</p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
      
      {/* 编辑小节弹窗 */}
      {section && (
        <SectionDialog
          open={isEditDialogOpen}
          onOpenChange={setIsEditDialogOpen}
          chapterId={chapterId}
          courseId={courseId}
          section={section}
          onSubmit={handleEditSubmit}
          mode="edit"
        />
      )}
    </>
  );
} 