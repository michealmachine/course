'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { courseService, chapterService, sectionService } from '@/services';
import { Course, Chapter, Section } from '@/types/course';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Separator } from '@/components/ui/separator';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { 
  BookOpen, 
  Clock, 
  FileText, 
  AlertCircle, 
  ChevronRight,
  Video,
  Headphones,
  FileCode,
  BrainCircuit
} from 'lucide-react';
import { CourseContentPlayer } from '@/components/preview/course-content-player';

export default function CoursePreviewPage() {
  const { token } = useParams() as { token: string };
  
  // 状态管理
  const [course, setCourse] = useState<Course | null>(null);
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(null);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [sections, setSections] = useState<Record<number, Section[]>>({});
  const [activeSection, setActiveSection] = useState<Section | null>(null);
  
  // 加载课程信息
  useEffect(() => {
    async function loadCourseData() {
      try {
        setLoading(true);
        setError(null);
        
        // 通过token获取课程信息
        const courseData = await courseService.previewCourse(token);
        setCourse(courseData);
        
        // 加载课程的章节
        const chaptersData = await chapterService.getChaptersByCourse(courseData.id);
        setChapters(chaptersData);
        
        // 默认选择第一个章节
        if (chaptersData.length > 0) {
          setSelectedChapterId(chaptersData[0].id);
        }
      } catch (err: any) {
        console.error('加载课程预览数据失败:', err);
        setError(err.message || '无法加载课程信息');
      } finally {
        setLoading(false);
      }
    }
    
    if (token) {
      loadCourseData();
    }
  }, [token]);
  
  // 根据选中的章节加载小节
  useEffect(() => {
    async function loadSections(chapterId: number) {
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
    }
    
    if (selectedChapterId) {
      loadSections(selectedChapterId);
    }
  }, [selectedChapterId, sections, selectedSectionId]);
  
  // 处理小节选择
  const handleSectionSelect = (section: Section) => {
    setSelectedSectionId(section.id);
    setActiveSection(section);
    setActiveTab('content');
  };
  
  // 渲染加载状态
  if (loading) {
    return (
      <div className="container mx-auto py-8 px-4">
        <Skeleton className="h-12 w-3/4 mb-4" />
        <Skeleton className="h-6 w-1/2 mb-8" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-1">
            <Skeleton className="h-[500px] w-full rounded-lg" />
          </div>
          <div className="md:col-span-2">
            <Skeleton className="h-[500px] w-full rounded-lg" />
          </div>
        </div>
      </div>
    );
  }
  
  // 渲染错误状态
  if (error) {
    return (
      <div className="container mx-auto py-8 px-4">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>加载失败</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }
  
  // 渲染课程不存在
  if (!course) {
    return (
      <div className="container mx-auto py-8 px-4">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>课程不存在</AlertTitle>
          <AlertDescription>
            无法找到该课程或预览链接已过期，请联系课程提供者获取新的预览链接。
          </AlertDescription>
        </Alert>
      </div>
    );
  }
  
  return (
    <div className="container mx-auto py-6 px-4">
      {/* 课程标题和描述 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">{course.title}</h1>
        {course.description && (
          <p className="text-muted-foreground">{course.description}</p>
        )}
        <div className="flex items-center gap-4 mt-4">
          <div className="flex items-center text-muted-foreground">
            <BookOpen className="h-4 w-4 mr-1" />
            <span>{chapters.length} 章节</span>
          </div>
          {course.totalDuration && (
            <div className="flex items-center text-muted-foreground">
              <Clock className="h-4 w-4 mr-1" />
              <span>{Math.round(course.totalDuration / 60)} 分钟</span>
            </div>
          )}
        </div>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {/* 左侧章节导航 */}
        <div className="md:col-span-1">
          <Card className="sticky top-6">
            <CardHeader className="pb-2">
              <CardTitle>课程内容</CardTitle>
              <CardDescription>
                共 {chapters.length} 章节 
                {Object.values(sections).flat().length > 0 && 
                  `, ${Object.values(sections).flat().length} 小节`
                }
              </CardDescription>
            </CardHeader>
            <CardContent className="max-h-[60vh] overflow-y-auto">
              <div className="space-y-4">
                {chapters.map((chapter) => (
                  <div key={chapter.id} className="space-y-2">
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
            </CardContent>
          </Card>
        </div>
        
        {/* 右侧内容区域 */}
        <div className="md:col-span-2 lg:col-span-3">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="mb-4">
              <TabsTrigger value="overview">课程概览</TabsTrigger>
              <TabsTrigger value="content" disabled={!activeSection}>
                课程内容
              </TabsTrigger>
            </TabsList>
            
            {/* 课程概览 */}
            <TabsContent value="overview">
              <Card>
                <CardHeader>
                  <CardTitle>课程简介</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {course.description && (
                      <div>
                        <h3 className="text-lg font-medium mb-2">课程描述</h3>
                        <p className="text-muted-foreground">{course.description}</p>
                      </div>
                    )}
                    
                    {course.coverUrl && (
                      <div>
                        <h3 className="text-lg font-medium mb-2">课程封面</h3>
                        <img 
                          src={course.coverUrl} 
                          alt={course.title} 
                          className="rounded-md max-w-full max-h-[300px] object-contain"
                        />
                      </div>
                    )}
                    
                    <div>
                      <h3 className="text-lg font-medium mb-2">章节列表</h3>
                      <div className="space-y-3">
                        {chapters.map((chapter, index) => (
                          <div key={chapter.id} className="pb-3 border-b last:border-0">
                            <h4 className="font-medium">
                              {index + 1}. {chapter.title}
                            </h4>
                            {chapter.description && (
                              <p className="text-sm text-muted-foreground mt-1">
                                {chapter.description}
                              </p>
                            )}
                            {sections[chapter.id] && (
                              <div className="mt-2 pl-4 space-y-1">
                                {sections[chapter.id].map((section, sIndex) => (
                                  <div key={section.id} className="flex items-center">
                                    <span className="text-sm text-muted-foreground mr-2">
                                      {index + 1}.{sIndex + 1}
                                    </span>
                                    <span className="text-sm">{section.title}</span>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
            
            {/* 课程内容 */}
            <TabsContent value="content">
              {activeSection ? (
                <CourseContentPlayer section={activeSection} />
              ) : (
                <Card>
                  <CardContent className="py-8 text-center">
                    <p className="text-muted-foreground">请从左侧选择一个小节以查看内容</p>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
} 