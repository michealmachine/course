'use client';

import { useState, useEffect, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { 
  AlertCircle, 
  ChevronLeft, 
  ChevronRight, 
  CheckCircle,
  Loader2,
  BookOpen,
  FileText,
  Video,
  Play,
  Pause,
  Volume2,
  VolumeX,
  Maximize,
  RefreshCw,
  Clock,
  BarChart2
} from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';
import { learningService } from '@/services';
import { 
  CourseStructureVO, 
  SectionVO, 
  ChapterVO,
  MediaVO,
  QuestionGroupVO,
  UserQuestionAnswerDTO, 
  LearningPosition,
  UserLearningProgressVO,
  SectionResourceVO
} from '@/types/learning';
import { formatTime, formatDuration } from '@/utils/format';

// 学习内容播放器组件
import { CourseMediaPlayer } from '@/components/learning/course-media-player';
import { CourseDocumentViewer } from '@/components/learning/course-document-viewer';
import { CourseQuestionGroup } from '@/components/learning/course-question-group';

// 从learning-service.ts导入
import { LearningCourseStructureVO } from '@/services/learning-service';

// 为了适配SectionVO的类型要求，创建接口拓展
interface SectionWithResource extends SectionVO {
  resourceType: 'MEDIA' | 'QUESTION_GROUP';
  resourceTypeDiscriminator?: 'MEDIA' | 'QUESTION_GROUP' | 'NONE';
  progress: number;
  completed: boolean;
}

export default function LearnCoursePage() {
  const params = useParams();
  const router = useRouter();
  const courseId = params?.courseId ? Number(params.courseId) : 0;
  
  // 课程结构和加载状态
  const [courseStructure, setCourseStructure] = useState<LearningCourseStructureVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // 当前学习位置
  const [currentChapterId, setCurrentChapterId] = useState<number | null>(null);
  const [currentSectionId, setCurrentSectionId] = useState<number | null>(null);
  const [currentProgress, setCurrentProgress] = useState<number>(0);
  
  // 当前小节内容
  const [currentSection, setCurrentSection] = useState<SectionWithResource | null>(null);
  const [sectionContent, setSectionContent] = useState<SectionResourceVO | null>(null);
  const [contentLoading, setContentLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('content');
  
  // 学习时长记录
  const [learningDuration, setLearningDuration] = useState(0);
  const durationTimerRef = useRef<NodeJS.Timeout | null>(null);
  const lastDurationUpdateRef = useRef<number>(0);
  
  // 获取课程结构
  useEffect(() => {
    const fetchCourseStructure = async () => {
      if (!courseId) return;
      
      try {
        setLoading(true);
        setError(null);
        setSectionContent(null); // 清除之前的内容状态
        
        const data = await learningService.getCourseStructure(courseId);
        console.log('获取到的课程结构数据:', data); // 添加日志，查看返回的数据结构
        
        if (!data) {
          setError('获取课程数据失败');
          return;
        }
        
        setCourseStructure(data);
        
        // 设置当前学习位置，添加更严格的空值检查
        if (data.currentPosition && 
            typeof data.currentPosition.chapterId !== 'undefined' && 
            typeof data.currentPosition.sectionId !== 'undefined') {
          
          const chapterId = Number(data.currentPosition.chapterId);
          const sectionId = Number(data.currentPosition.sectionId);
          const progress = Number(data.currentPosition.sectionProgress || 0);
          
          setCurrentChapterId(chapterId);
          setCurrentSectionId(sectionId);
          setCurrentProgress(progress);
          
          console.log(`设置学习位置: 章节ID=${chapterId}, 小节ID=${sectionId}, 进度=${progress}%`);
        } else if (data.chapters && data.chapters.length > 0) {
          // 如果没有当前位置，默认选择第一章第一节
          const firstChapter = data.chapters[0];
          const firstSection = firstChapter.sections && firstChapter.sections.length > 0 
            ? firstChapter.sections[0] 
            : null;
            
          setCurrentChapterId(firstChapter.id);
          
          if (firstSection) {
            setCurrentSectionId(firstSection.id);
            console.log(`默认选择第一章第一节: 章节ID=${firstChapter.id}, 小节ID=${firstSection.id}`);
          } else {
            console.log(`第一章没有小节: 章节ID=${firstChapter.id}`);
          }
        }
      } catch (err: any) {
        console.error('获取课程结构失败:', err);
        setError(err.message || '获取课程内容失败');
      } finally {
        setLoading(false);
      }
    };
    
    fetchCourseStructure();
    
    // 开始计时
    startLearningTimer();
    
    // 清理函数
    return () => {
      stopLearningTimer();
      recordFinalLearningDuration();
    };
  }, [courseId]);
  
  // 监听当前小节变化，加载小节内容
  useEffect(() => {
    if (!currentSectionId) return;
    
    loadCurrentSectionContent();
  }, [currentSectionId]);
  
  // 学习计时器
  const startLearningTimer = () => {
    if (durationTimerRef.current) {
      clearInterval(durationTimerRef.current);
    }
    
    // 每秒增加学习时长
    durationTimerRef.current = setInterval(() => {
      setLearningDuration(prev => prev + 1);
      
      // 每5分钟更新一次学习时长
      if (learningDuration - lastDurationUpdateRef.current >= 300) {
        recordLearningDuration();
        lastDurationUpdateRef.current = learningDuration;
      }
    }, 1000);
  };
  
  const stopLearningTimer = () => {
    if (durationTimerRef.current) {
      clearInterval(durationTimerRef.current);
      durationTimerRef.current = null;
    }
  };
  
  // 记录最终学习时长
  const recordFinalLearningDuration = async () => {
    if (learningDuration <= 0 || !courseId || !currentSectionId) return;
    
    try {
      await learningService.recordLearningDuration({
        courseId,
        sectionId: currentSectionId,
        duration: learningDuration
      });
      console.log(`记录学习时长: ${learningDuration}秒`);
    } catch (err) {
      console.error('记录学习时长失败:', err);
    }
  };
  
  // 定期记录学习时长
  const recordLearningDuration = async () => {
    if (learningDuration <= 0 || !courseId || !currentSectionId) return;
    
    try {
      await learningService.recordLearningDuration({
        courseId,
        sectionId: currentSectionId,
        duration: learningDuration
      });
      console.log(`定期记录学习时长: ${learningDuration}秒`);
    } catch (err) {
      console.error('记录学习时长失败:', err);
    }
  };
  
  // 加载当前小节内容
  const loadCurrentSectionContent = async () => {
    if (!currentSectionId || !courseStructure) return;
    
    try {
      setContentLoading(true);
      setSectionContent(null);
      
      // 查找当前小节
      let section: SectionWithResource | null = null;
      for (const chapter of courseStructure.chapters) {
        const found = chapter.sections.find(s => s.id === currentSectionId);
        if (found) {
          // 类型断言，确保有所需的属性
          section = {
            ...found,
            resourceType: found.resourceType || 'MEDIA' // 如果没有resourceType，默认为MEDIA
          } as SectionWithResource;
          break;
        }
      }
      
      if (!section) {
        console.error('未找到当前小节');
        return;
      }
      
      setCurrentSection(section);
      
      // 获取后端返回的资源类型，优先使用resourceTypeDiscriminator
      const resourceTypeDiscriminator = section.resourceTypeDiscriminator || null;
      const resourceType = section.resourceType || null;
      const effectiveResourceType = resourceTypeDiscriminator || resourceType || null;
      
      console.log(`小节 ${section.id} 的资源类型:`, {
        resourceTypeDiscriminator, 
        resourceType,
        effectiveResourceType
      });
      
      // 根据资源类型加载不同内容
      if (effectiveResourceType === 'MEDIA') {
        // 加载媒体资源
        console.log(`加载小节 ${section.id} 的媒体资源`);
        try {
          const media = await learningService.getSectionMedia(section.id);
          console.log('成功加载媒体资源:', media);
          setSectionContent({
            id: section.id,
            title: section.title,
            description: section.description,
            type: 'section',
            resourceType: 'MEDIA',
            media: media
          } as SectionResourceVO);
          // 更新小节的资源类型
          section.resourceType = 'MEDIA';
          section.resourceTypeDiscriminator = 'MEDIA';
          return;
        } catch (mediaErr) {
          console.error('加载媒体资源失败:', mediaErr);
          throw mediaErr; // 重新抛出错误，让外层catch处理
        }
      } else if (effectiveResourceType === 'QUESTION_GROUP') {
        // 加载题组资源
        console.log(`加载小节 ${section.id} 的题组资源`);
        try {
          const questionGroup = await learningService.getSectionQuestionGroup(section.id);
          console.log('成功加载题组资源:', questionGroup);
          
          // 确保questionGroup有所需属性
          if (!questionGroup || typeof questionGroup !== 'object') {
            console.error('题组资源格式无效:', questionGroup);
            throw new Error('题组资源格式无效');
          }
          
          // 确保questionGroup有sectionId
          const questionGroupWithSection = {
            ...questionGroup,
            sectionId: section.id
          };
          
          // 创建完整的SectionResourceVO对象
          const sectionResource: SectionResourceVO = {
            id: section.id,
            title: section.title,
            description: section.description,
            type: 'section',
            resourceType: 'QUESTION_GROUP',
            questionGroup: questionGroupWithSection
          };
          
          console.log('创建的SectionResourceVO对象:', sectionResource);
          setSectionContent(sectionResource);
          
          // 更新小节的资源类型
          section.resourceType = 'QUESTION_GROUP';
          section.resourceTypeDiscriminator = 'QUESTION_GROUP';
          return;
        } catch (questionErr) {
          console.error('加载题组资源失败:', questionErr);
          throw questionErr; // 重新抛出错误，让外层catch处理
        }
      } else {
        // 资源类型未知或为NONE，尝试自动检测
        console.log(`小节 ${section.id} 的资源类型未知或为NONE，尝试自动检测`);
        let mediaLoaded = false;
        let questionGroupLoaded = false;
        
        try {
          // 尝试加载题组资源（先检查题组，因为题组加载更轻量）
          console.log(`尝试加载小节 ${section.id} 的题组资源`);
          const questionGroup = await learningService.getSectionQuestionGroup(section.id);
          
          if (questionGroup) {
            console.log('成功加载题组资源:', questionGroup);
            
            // 确保questionGroup有sectionId
            const questionGroupWithSection = {
              ...questionGroup,
              sectionId: section.id
            };
            
            // 创建完整的SectionResourceVO对象
            const sectionResource: SectionResourceVO = {
              id: section.id,
              title: section.title,
              description: section.description,
              type: 'section',
              resourceType: 'QUESTION_GROUP',
              questionGroup: questionGroupWithSection
            };
            
            console.log('自动检测创建的SectionResourceVO对象:', sectionResource);
            setSectionContent(sectionResource);
            
            // 更新小节的资源类型
            section.resourceType = 'QUESTION_GROUP';
            section.resourceTypeDiscriminator = 'QUESTION_GROUP';
            questionGroupLoaded = true;
            return;
          }
        } catch (questionErr) {
          console.log('加载题组资源失败，尝试加载媒体资源');
        }
        
        // 如果题组资源加载失败，尝试媒体资源
        if (!questionGroupLoaded) {
          try {
            console.log(`尝试加载小节 ${section.id} 的媒体资源`);
            const media = await learningService.getSectionMedia(section.id);
            
            if (media) {
              console.log('成功加载媒体资源:', media);
              setSectionContent({
                id: section.id,
                title: section.title,
                description: section.description,
                type: 'section',
                resourceType: 'MEDIA',
                media: media
              } as SectionResourceVO);
              // 更新小节的资源类型
              section.resourceType = 'MEDIA';
              section.resourceTypeDiscriminator = 'MEDIA';
              mediaLoaded = true;
              return;
            }
          } catch (mediaErr) {
            console.log('加载媒体资源失败');
          }
        }
        
        // 如果两种资源都没有，设置为无内容
        if (!mediaLoaded && !questionGroupLoaded) {
          console.log('该小节没有关联任何媒体或题组资源');
          setSectionContent(null);
        }
      }
    } catch (err: any) {
      console.error('加载小节内容失败:', err);
      toast.error('加载小节内容失败: ' + (err.message || '未知错误'));
    } finally {
      setContentLoading(false);
    }
  };
  
  // 更新学习进度
  const updateLearningProgress = async (progress: number) => {
    if (!courseId || !currentChapterId || !currentSectionId) return;
    
    try {
      setCurrentProgress(progress);
      
      // 更新进度
      const positionData = {
        courseId,
        chapterId: currentChapterId,
        sectionId: currentSectionId,
        sectionProgress: progress
      };
      
      await learningService.updateLearningPosition(positionData);
      console.log(`更新学习进度: ${progress}%`);
    } catch (err) {
      console.error('更新学习进度失败:', err);
    }
  };
  
  // 处理进度更新（视频播放进度）
  const handleProgressUpdate = (progress: number) => {
    // 避免过于频繁的更新，只在进度变化较大时更新
    if (Math.abs(progress - currentProgress) > 5) {
      updateLearningProgress(progress);
    }
  };
  
  // 提交问题答案
  const handleSubmitAnswer = async (answer: UserQuestionAnswerDTO) => {
    if (!currentSectionId) return;
    
    try {
      await learningService.submitQuestionAnswer(currentSectionId, answer);
      console.log('答案提交成功');
      
      // 如果已经完成了小节，更新进度为100%
      if (currentProgress < 100) {
        updateLearningProgress(100);
      }
    } catch (err: any) {
      console.error('提交答案失败:', err);
      toast.error('提交答案失败: ' + (err.message || '未知错误'));
    }
  };
  
  // 切换到上一小节
  const goToPreviousSection = () => {
    if (!courseStructure) return;
    
    // 找到当前章节和小节
    let prevSectionId: number | null = null;
    let prevChapterId: number | null = null;
    
    let foundCurrentSection = false;
    
    // 倒序遍历章节和小节
    for (let i = courseStructure.chapters.length - 1; i >= 0; i--) {
      const chapter = courseStructure.chapters[i];
      
      for (let j = chapter.sections.length - 1; j >= 0; j--) {
        const section = chapter.sections[j];
        
        if (foundCurrentSection) {
          prevSectionId = section.id;
          prevChapterId = chapter.id;
          break;
        }
        
        if (section.id === currentSectionId) {
          foundCurrentSection = true;
          
          // 如果不是当前章节的第一个小节，前一个小节就是当前章节的上一个
          if (j > 0) {
            prevSectionId = chapter.sections[j - 1].id;
            prevChapterId = chapter.id;
            break;
          }
          // 否则需要找上一章节的最后一个小节
        }
      }
      
      if (prevSectionId !== null) break;
      
      // 如果已经找到当前小节，但还没找到前一个小节，说明需要跳到上一章
      if (foundCurrentSection && i > 0) {
        const prevChapter = courseStructure.chapters[i - 1];
        if (prevChapter.sections.length > 0) {
          prevSectionId = prevChapter.sections[prevChapter.sections.length - 1].id;
          prevChapterId = prevChapter.id;
          break;
        }
      }
    }
    
    if (prevSectionId !== null && prevChapterId !== null) {
      setCurrentChapterId(prevChapterId);
      setCurrentSectionId(prevSectionId);
      setCurrentProgress(0);
    }
  };
  
  // 切换到下一小节
  const goToNextSection = () => {
    if (!courseStructure) return;
    
    // 找到当前章节和小节
    let nextSectionId: number | null = null;
    let nextChapterId: number | null = null;
    
    let foundCurrentSection = false;
    
    // 遍历章节和小节
    for (let i = 0; i < courseStructure.chapters.length; i++) {
      const chapter = courseStructure.chapters[i];
      
      for (let j = 0; j < chapter.sections.length; j++) {
        const section = chapter.sections[j];
        
        if (foundCurrentSection) {
          nextSectionId = section.id;
          nextChapterId = chapter.id;
          break;
        }
        
        if (section.id === currentSectionId) {
          foundCurrentSection = true;
          
          // 如果不是当前章节的最后一个小节，下一个小节就是当前章节的下一个
          if (j < chapter.sections.length - 1) {
            nextSectionId = chapter.sections[j + 1].id;
            nextChapterId = chapter.id;
            break;
          }
          // 否则需要找下一章节的第一个小节
        }
      }
      
      if (nextSectionId !== null) break;
      
      // 如果已经找到当前小节，但还没找到下一个小节，说明需要跳到下一章
      if (foundCurrentSection && i < courseStructure.chapters.length - 1) {
        const nextChapter = courseStructure.chapters[i + 1];
        if (nextChapter.sections.length > 0) {
          nextSectionId = nextChapter.sections[0].id;
          nextChapterId = nextChapter.id;
          break;
        }
      }
    }
    
    if (nextSectionId !== null && nextChapterId !== null) {
      setCurrentChapterId(nextChapterId);
      setCurrentSectionId(nextSectionId);
      setCurrentProgress(0);
    }
  };
  
  // 处理章节点击
  const handleChapterClick = (chapterId: number) => {
    setCurrentChapterId(chapterId);
    
    // 查找章节下的第一个小节
    const chapter = courseStructure?.chapters.find(c => c.id === chapterId);
    if (chapter && chapter.sections.length > 0) {
      setCurrentSectionId(chapter.sections[0].id);
      setCurrentProgress(0);
    }
  };
  
  // 处理小节点击
  const handleSectionClick = (chapterId: number, sectionId: number) => {
    console.log(`点击小节: 章节ID=${chapterId}, 小节ID=${sectionId}`);
    // 清除之前的内容状态
    setSectionContent(null);
    setCurrentProgress(0);
    // 设置新的ID
    setCurrentChapterId(chapterId);
    setCurrentSectionId(sectionId);
  };
  
  // 渲染章节列表
  const renderChapterList = () => {
    if (!courseStructure) return null;
    
    return (
      <ScrollArea className="h-[calc(100vh-220px)]">
        <div className="space-y-4 p-2">
          {courseStructure.chapters.map((chapter) => (
            <div key={chapter.id} className="space-y-2">
              <div 
                className={cn(
                  "flex items-center justify-between p-2 rounded-md cursor-pointer hover:bg-accent",
                  currentChapterId === chapter.id ? "bg-accent" : ""
                )}
                onClick={() => handleChapterClick(chapter.id)}
              >
                <div className="flex items-center space-x-2">
                  <BookOpen className="h-4 w-4 text-primary" />
                  <span className="font-medium line-clamp-1">{chapter.title}</span>
                </div>
                <span className="text-xs text-muted-foreground">
                  {chapter.sections.length} 小节
                </span>
              </div>
              
              {currentChapterId === chapter.id && (
                <div className="ml-4 space-y-1">
                  {chapter.sections.map((section) => {
                    // 类型断言，确保section有resourceType属性
                    const sectionWithResource = section as unknown as SectionWithResource;
                    // 优先使用resourceTypeDiscriminator字段，其次是resourceType
                    const resourceType = sectionWithResource.resourceTypeDiscriminator || sectionWithResource.resourceType || 'NONE';
                    return (
                      <div
                        key={section.id}
                        className={cn(
                          "flex items-center p-2 rounded-md cursor-pointer hover:bg-accent text-sm",
                          currentSectionId === section.id ? "bg-accent font-medium" : ""
                        )}
                        onClick={() => handleSectionClick(chapter.id, section.id)}
                      >
                        {resourceType === 'MEDIA' ? (
                          <Video className="h-3.5 w-3.5 mr-2 text-muted-foreground" />
                        ) : resourceType === 'QUESTION_GROUP' ? (
                          <FileText className="h-3.5 w-3.5 mr-2 text-muted-foreground" />
                        ) : (
                          <FileText className="h-3.5 w-3.5 mr-2 text-muted-foreground" />
                        )}
                        <span className="line-clamp-1">{section.title}</span>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          ))}
        </div>
      </ScrollArea>
    );
  };
  
  // 渲染内容区域
  const renderContent = () => {
    if (contentLoading) {
      return (
        <div className="flex items-center justify-center h-[500px]">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      );
    }
    
    if (!currentSection) {
      return (
        <div className="flex flex-col items-center justify-center h-[500px] text-center">
          <FileText className="h-12 w-12 mb-4 text-muted-foreground" />
          <h3 className="text-lg font-medium">未选择小节</h3>
          <p className="text-muted-foreground">请从左侧选择要学习的小节</p>
        </div>
      );
    }
    
    if (!sectionContent) {
      return (
        <div className="flex flex-col items-center justify-center h-[500px] text-center">
          <FileText className="h-12 w-12 mb-4 text-muted-foreground" />
          <h3 className="text-lg font-medium">暂无内容</h3>
          <p className="text-muted-foreground">该小节没有关联内容，或内容加载失败</p>
          <Button 
            variant="outline" 
            className="mt-4"
            onClick={loadCurrentSectionContent}
          >
            <RefreshCw className="mr-2 h-4 w-4" />
            重新加载
          </Button>
        </div>
      );
    }

    // 添加调试信息，确认sectionContent的具体内容
    console.log("渲染内容区域，sectionContent:", sectionContent);

    if (sectionContent.resourceType === 'MEDIA' && sectionContent.media) {
      return (
        <CourseMediaPlayer 
          media={sectionContent.media}
          onComplete={() => updateLearningProgress(100)}
          onError={(error) => toast.error(error.toString())}
        />
      );
    } else if (sectionContent.resourceType === 'QUESTION_GROUP' && sectionContent.questionGroup) {
      // 添加调试信息，确认questionGroup的具体内容
      console.log("渲染题组内容，questionGroup:", sectionContent.questionGroup);
      return (
        <CourseQuestionGroup 
          questionGroup={sectionContent.questionGroup}
          onComplete={(isAllCorrect) => {
            console.log("题组完成，是否全部正确:", isAllCorrect);
            updateLearningProgress(100);
          }}
          onError={(error) => {
            console.error("题组渲染错误:", error);
            toast.error(error.toString());
          }}
        />
      );
    } else {
      // 更详细的错误信息
      console.error("无法渲染内容，资源类型不匹配:", {
        resourceType: sectionContent.resourceType,
        hasMedia: !!sectionContent.media,
        hasQuestionGroup: !!sectionContent.questionGroup,
        sectionContent
      });
      
      return (
        <div className="flex flex-col items-center justify-center h-[500px] text-center">
          <FileText className="h-12 w-12 mb-4 text-muted-foreground" />
          <h3 className="text-lg font-medium">未知内容类型</h3>
          <p className="text-muted-foreground">系统无法加载此类型的内容</p>
          <p className="text-sm text-muted-foreground mt-2">
            资源类型: {sectionContent.resourceType || '未知'}，
            媒体: {sectionContent.media ? '有效' : '无效'}，
            题组: {sectionContent.questionGroup ? '有效' : '无效'}
          </p>
          <Button 
            variant="outline" 
            className="mt-4"
            onClick={loadCurrentSectionContent}
          >
            <RefreshCw className="mr-2 h-4 w-4" />
            重新加载
          </Button>
        </div>
      );
    }
  };
  
  if (loading) {
    return (
      <div className="container py-6 space-y-8">
        <div className="flex items-center justify-between">
          <Skeleton className="h-8 w-72" />
          <Skeleton className="h-9 w-20" />
        </div>
        
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <div className="lg:col-span-3">
            <Card>
              <CardContent className="p-4">
                <Skeleton className="h-6 w-48 mb-4" />
                <div className="space-y-2">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Skeleton key={i} className="h-10 w-full" />
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
          
          <div className="lg:col-span-9">
            <Card>
              <CardContent className="p-0">
                <Skeleton className="h-[500px] w-full" />
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="container py-6 space-y-4">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>加载失败</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
        
        <Button 
          variant="outline" 
          onClick={() => router.push('/dashboard/my-courses')}
        >
          <ChevronLeft className="mr-2 h-4 w-4" />
          返回我的课程
        </Button>
      </div>
    );
  }
  
  if (!courseStructure) {
    return (
      <div className="container py-6 space-y-4">
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>未找到课程</AlertTitle>
          <AlertDescription>该课程不存在或您没有访问权限</AlertDescription>
        </Alert>
        
        <Button 
          variant="outline" 
          onClick={() => router.push('/dashboard/my-courses')}
        >
          <ChevronLeft className="mr-2 h-4 w-4" />
          返回我的课程
        </Button>
      </div>
    );
  }
  
  return (
    <div className="container py-4 space-y-6">
      <div className="flex flex-col md:flex-row justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            {courseStructure.course?.title || '课程学习'}
          </h1>
          <div className="flex items-center mt-1 text-sm text-muted-foreground">
            <span>学习进度: {courseStructure.userProgress?.progress || 0}%</span>
            <span className="mx-2">|</span>
            <span>学习时长: {formatDuration(learningDuration + (courseStructure.userProgress?.learnDuration || 0))}</span>
          </div>
        </div>
        
        <Button 
          variant="outline" 
          onClick={() => router.push('/dashboard/my-courses')}
        >
          <ChevronLeft className="mr-2 h-4 w-4" />
          返回我的课程
        </Button>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* 左侧章节导航 */}
        <div className="lg:col-span-3 space-y-4">
          <Card>
            <CardContent className="p-4">
              <h2 className="text-lg font-medium mb-4">课程目录</h2>
              {renderChapterList()}
            </CardContent>
          </Card>
        </div>
        
        {/* 右侧内容区域 */}
        <div className="lg:col-span-9 space-y-4">
          <Tabs defaultValue="content" value={activeTab} onValueChange={setActiveTab}>
            <TabsList>
              <TabsTrigger value="content">学习内容</TabsTrigger>
              <TabsTrigger value="statistics">学习统计</TabsTrigger>
            </TabsList>
            
            <TabsContent value="content" className="space-y-4">
              {/* 小节标题和进度 */}
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-medium">
                    {currentSection?.title || '未选择小节'}
                  </h3>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={goToPreviousSection}
                    disabled={!currentSection}
                  >
                    <ChevronLeft className="h-4 w-4" />
                    <span className="ml-1 hidden sm:inline">上一节</span>
                  </Button>
                  
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={goToNextSection}
                    disabled={!currentSection}
                  >
                    <span className="mr-1 hidden sm:inline">下一节</span>
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                </div>
              </div>
              
              {/* 学习内容 */}
              <Card>
                <CardContent className="p-0 overflow-hidden rounded-lg">
                  {renderContent()}
                </CardContent>
              </Card>
              
              {/* 学习进度 */}
              {currentSection && (
                <div className="flex flex-col sm:flex-row items-center space-y-2 sm:space-y-0 sm:space-x-4">
                  <div className="text-sm text-muted-foreground flex items-center">
                    <Clock className="h-4 w-4 mr-1" />
                    学习时长: {formatDuration(learningDuration)}
                  </div>
                  
                  <div className="flex-1 flex items-center space-x-4">
                    <span className="text-sm font-medium">进度:</span>
                    <Progress value={currentProgress} className="h-2" />
                    <span className="text-sm text-muted-foreground">{currentProgress}%</span>
                  </div>
                </div>
              )}
            </TabsContent>
            
            <TabsContent value="statistics">
              <Card>
                <CardContent className="p-6">
                  <div className="flex flex-col items-center justify-center py-8 space-y-4">
                    <BarChart2 className="h-16 w-16 text-primary mb-2" />
                    <h3 className="text-xl font-semibold">学习统计功能即将上线</h3>
                    <p className="text-center text-muted-foreground max-w-md">
                      我们正在努力开发学习统计功能，帮助您更好地了解学习情况。敬请期待！
                    </p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
} 