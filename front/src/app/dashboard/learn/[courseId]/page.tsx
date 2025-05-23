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
  BarChart2,
  Headphones,
  ExternalLink
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
import { CourseStatistics } from '@/components/learning/course-statistics';

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
  const [activeTab, setActiveTab] = useState<'content' | 'statistics'>('content');

  // 学习时长记录
  const [learningDuration, setLearningDuration] = useState(0);
  const durationTimerRef = useRef<NodeJS.Timeout | null>(null);
  const lastDurationUpdateRef = useRef<number>(0);
  const learningDurationRef = useRef<number>(0); // 使用ref存储时长，避免频繁更新
  // 当前学习活动类型
  const [currentActivityType, setCurrentActivityType] = useState<string | null>(null);

  // 检查并同步本地存储的学习记录
  const syncPendingLearningRecord = () => {
    try {
      const pendingRecord = localStorage.getItem('pendingLearningRecord');
      if (pendingRecord) {
        console.log('发现未同步的学习记录，尝试同步...');

        // 如果有网络连接，尝试发送记录
        if (navigator.onLine) {
          const success = navigator.sendBeacon('/api/learning/records/completed', pendingRecord);
          if (success) {
            console.log('同步本地存储的学习记录成功');
            localStorage.removeItem('pendingLearningRecord');
          } else {
            console.error('同步本地存储的学习记录失败');
          }
        }
      }
    } catch (err) {
      console.error('检查本地存储的学习记录失败:', err);
    }
  };

  // 获取课程结构
  useEffect(() => {
    const fetchCourseStructure = async () => {
      if (!courseId) return;

      // 检查并同步本地存储的学习记录
      syncPendingLearningRecord();

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

    // 直接启动学习计时器，不等待其他条件
    startLearningTimer();

    // 监听网络状态变化，在网络恢复时尝试同步记录
    const handleOnline = () => {
      console.log('网络连接恢复，尝试同步学习记录...');
      syncPendingLearningRecord();
    };

    window.addEventListener('online', handleOnline);

    // 组件卸载时清理
    return () => {
      // 组件卸载时记录最终学习时长
      if (learningDurationRef.current > 0) {
        console.log(`组件卸载，记录最终学习时长: ${learningDurationRef.current}秒`);
        recordFinalLearningDuration(learningDurationRef.current);
      }
      stopLearningTimer();
      window.removeEventListener('online', handleOnline);
    };
  }, []);

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

    console.log(`学习计时器启动，准备记录学习活动，当前活动类型: ${currentActivityType || 'VIDEO_WATCH'}`);

    // 每秒增加学习时长，但减少状态更新频率
    durationTimerRef.current = setInterval(() => {
      // 增加持续时间但不立即更新状态
      learningDurationRef.current += 1;

      // 每10秒才更新一次UI上的时长显示，减少渲染
      if (learningDurationRef.current % 10 === 0) {
        setLearningDuration(learningDurationRef.current);
        console.log(`当前学习时长: ${learningDurationRef.current}秒, 活动类型: ${currentActivityType || 'VIDEO_WATCH'}`);
      }

      // 每30秒更新一次学习时长（改为30秒更频繁地记录）
      if (learningDurationRef.current - lastDurationUpdateRef.current >= 30) {
        // 使用setTimeout避免阻塞UI
        setTimeout(() => {
          console.log(`即将记录学习活动，时长: ${learningDurationRef.current - lastDurationUpdateRef.current}秒, 活动类型: ${currentActivityType || 'VIDEO_WATCH'}`);
          recordLearningActivity(learningDurationRef.current - lastDurationUpdateRef.current);
          lastDurationUpdateRef.current = learningDurationRef.current;
        }, 100);
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
  const recordFinalLearningDuration = async (duration = learningDuration) => {
    if (duration <= 0 || !courseId || !currentSectionId) return;

    try {
      console.log(`记录最终学习时长: ${duration}秒, 活动类型: ${currentActivityType || 'VIDEO_WATCH'}`);

      // 使用setTimeout避免阻塞UI和导致闪动
      setTimeout(async () => {
        try {
          // 使用新的API记录学习活动
          await learningService.recordCompletedActivity({
            courseId,
            chapterId: currentChapterId || undefined,
            sectionId: currentSectionId,
            activityType: currentActivityType || 'VIDEO_WATCH',  // 默认使用视频观看类型
            durationSeconds: Math.max(duration, 1), // 确保时长至少为1秒
            contextData: JSON.stringify({
              source: 'final_record',
              progress: currentProgress
            })
          });
          console.log('最终学习时长记录成功');
        } catch (err) {
          console.error('记录最终学习时长失败:', err);
        }
      }, 100);

      // 重置学习时长
      setLearningDuration(0);
    } catch (err) {
      console.error('记录最终学习时长失败:', err);
    }
  };

  // 定期记录学习活动
  const recordLearningActivity = async (duration = learningDuration) => {
    // 移除courseId和currentSectionId检查，以确保在任何情况下都尝试记录时长
    if (duration <= 0) {
      console.log(`学习时长 ${duration}秒 不能为0或负数，不记录`);
      return;
    }

    // 降低节流阈值为1秒，确保几乎所有的学习时长都能被记录
    if (duration < 1) {
      console.log(`学习时长 ${duration}秒 小于1秒，不记录`);
      return;
    }

    // 如果没有courseId或currentSectionId，记录警告但仍然尝试继续
    if (!courseId || !currentSectionId) {
      console.warn(`缺少课程ID(${courseId})或小节ID(${currentSectionId})，但仍尝试记录学习时长`);
    }

    console.log(`定期记录学习时长: ${duration}秒, 活动类型: ${currentActivityType || 'VIDEO_WATCH'}, 课程ID: ${courseId}, 小节ID: ${currentSectionId}`);

    // 使用setTimeout避免阻塞UI和导致闪动
    setTimeout(async () => {
      try {
        // 使用新的API记录学习活动
        await learningService.recordCompletedActivity({
          courseId: Number(courseId),
          chapterId: currentChapterId ? Number(currentChapterId) : undefined,
          sectionId: Number(currentSectionId),
          activityType: currentActivityType || 'VIDEO_WATCH',  // 默认使用视频观看类型
          durationSeconds: Math.max(duration, 1), // 确保时长至少为1秒
          contextData: JSON.stringify({
            source: 'periodic_update',
            progress: currentProgress
          })
        });
        console.log('定期学习活动记录成功');
      } catch (err) {
        console.error('记录学习活动失败:', err);
      }
    }, 100);

    // 不重置学习时长，只在lastDurationUpdateRef中记录最后更新时间点
    // 注意：这里不再重置setLearningDuration(0)
  };

  // 记录小节开始活动
  const recordSectionStart = (resourceType: string | null) => {
    // 避免频繁调用API
    if (!courseId || !currentSectionId) return;

    console.log(`准备记录小节开始: 章节ID=${currentChapterId}, 小节ID=${currentSectionId}, 资源类型=${resourceType}`);

    // 使用setTimeout避免阻塞UI
    setTimeout(async () => {
      try {
        await learningService.recordCompletedActivity({
          courseId,
          chapterId: currentChapterId || undefined,
          sectionId: currentSectionId,
          activityType: 'SECTION_START',
          durationSeconds: 1,  // 确保为1秒，避免后端验证失败
          contextData: JSON.stringify({
            resourceType,
            timestamp: Date.now()
          })
        });
        console.log(`记录小节开始成功: 章节ID=${currentChapterId}, 小节ID=${currentSectionId}`);
      } catch (err) {
        console.error('记录小节开始失败:', err);
      }
    }, 200);
  };

  // 记录小节完成
  const recordSectionComplete = () => {
    // 避免频繁调用API
    if (!courseId || !currentChapterId || !currentSectionId) return;

    console.log(`准备记录小节完成: 章节ID=${currentChapterId}, 小节ID=${currentSectionId}`);

    // 使用setTimeout避免阻塞UI
    setTimeout(async () => {
      try {
        await learningService.recordCompletedActivity({
          courseId,
          chapterId: currentChapterId,
          sectionId: currentSectionId,
          activityType: 'SECTION_END',
          durationSeconds: 1,  // 确保为1秒，避免后端验证失败
          contextData: JSON.stringify({
            completed: true,
            progress: 100,
            timestamp: Date.now()
          })
        });
        console.log(`记录小节完成成功: 章节ID=${currentChapterId}, 小节ID=${currentSectionId}`);
      } catch (err) {
        console.error('记录小节完成失败:', err);
      }
    }, 200);
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

      // 设置当前学习活动类型
      if (effectiveResourceType === 'MEDIA') {
        setCurrentActivityType('VIDEO_WATCH');
      } else if (effectiveResourceType === 'QUESTION_GROUP') {
        setCurrentActivityType('QUIZ_ATTEMPT'); // 正确设置为测验尝试类型
      } else {
        setCurrentActivityType('DOCUMENT_READ'); // 默认为文档阅读
      }

      // 记录小节开始活动 - 使用提取的函数
      recordSectionStart(effectiveResourceType);

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

  // 判断是否是往回学习
  const isBackwardNavigation = (chapterId: number, sectionId: number): boolean => {
    if (!courseStructure) return false;

    // 获取当前小节和目标小节的索引
    const currentIndex = getSectionIndex(currentChapterId, currentSectionId);
    const targetIndex = getSectionIndex(chapterId, sectionId);

    // 如果目标索引小于当前索引，则是往回学习
    return targetIndex < currentIndex;
  };

  // 获取小节在课程中的索引位置
  const getSectionIndex = (chapterId: number | null, sectionId: number | null): number => {
    if (!courseStructure || !chapterId || !sectionId) return 0;

    let index = 0;
    let found = false;

    for (const chapter of courseStructure.chapters) {
      for (const section of chapter.sections) {
        index++;
        if (chapter.id === chapterId && section.id === sectionId) {
          found = true;
          break;
        }
      }
      if (found) break;
    }

    return found ? index : 0;
  };

  // 更新学习进度
  const updateLearningProgress = async (progress: number) => {
    if (!courseId || !currentChapterId || !currentSectionId) return;

    try {
      setCurrentProgress(progress);

      // 检查是否是往回学习
      const reviewing = isBackwardNavigation(currentChapterId, currentSectionId);

      // 更新进度
      const positionData = {
        courseId,
        chapterId: currentChapterId,
        sectionId: currentSectionId,
        sectionProgress: progress,
        isReviewing: reviewing // 添加复习模式标记
      };

      const result = await learningService.updateLearningPosition(positionData);
      console.log(`更新学习进度: ${progress}%, 复习模式: ${reviewing}`);

      // 如果进度为100%，记录小节完成
      if (progress === 100 && currentActivityType) {
        recordSectionComplete();
      }

      return result;
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

  // 处理Tab切换
  const handleTabChange = (tab: string) => {
    // 只处理我们关心的两个tab值
    if (tab !== 'content' && tab !== 'statistics') return;

    // 如果从学习内容切换到统计Tab
    if (activeTab === 'content' && tab === 'statistics') {
      console.log('切换到统计Tab，暂停学习计时器');
      // 停止计时器
      stopLearningTimer();
      // 记录当前累计的学习时长
      if (learningDurationRef.current > 0) {
        console.log(`切换Tab，记录累计学习时长: ${learningDurationRef.current}秒`);
        recordFinalLearningDuration(learningDurationRef.current);
        // 重置学习时长
        learningDurationRef.current = 0;
        setLearningDuration(0);
      }
    }
    // 如果从统计Tab切换回学习内容
    else if (activeTab === 'statistics' && tab === 'content') {
      console.log('切换回学习内容Tab，重新启动学习计时器');
      // 重新启动计时器
      startLearningTimer();
    }

    // 更新当前活动的Tab
    setActiveTab(tab as 'content' | 'statistics');
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
      // 获取媒体类型
      const mediaType = (sectionContent.media.type || '').toLowerCase();
      console.log('媒体类型:', mediaType, '媒体ID:', sectionContent.media.id);

      // 如果是文档类型，使用CourseDocumentViewer
      if (mediaType.includes('document') || mediaType === 'document') {
        console.log('使用CourseDocumentViewer渲染文档');
        return (
          <CourseDocumentViewer
            media={sectionContent.media}
            onProgress={(progress) => updateLearningProgress(progress)}
          />
        );
      }
      // 如果是音频类型，使用原生音频播放器
      else if (mediaType.includes('audio') || mediaType === 'audio') {
        console.log('使用原生音频播放器渲染音频');
        return (
          <div className="p-6 bg-muted rounded-lg">
            <div className="flex flex-col items-center space-y-4">
              <div className="w-40 h-40 bg-primary/10 rounded-full flex items-center justify-center mb-2">
                <Headphones className="h-16 w-16 text-primary" />
              </div>

              <div className="text-center mb-2">
                <h3 className="text-lg font-medium">{sectionContent.media.title}</h3>
                {sectionContent.media.description && (
                  <p className="text-sm text-muted-foreground mt-1">{sectionContent.media.description}</p>
                )}
              </div>

              <div className="w-full max-w-md bg-card p-4 rounded-lg border shadow-sm">
                <audio
                  key={sectionContent.media.accessUrl}
                  src={sectionContent.media.accessUrl}
                  controls
                  className="w-full"
                  controlsList="nodownload"
                  preload="metadata"
                  onEnded={() => updateLearningProgress(100)}
                  onError={(e) => {
                    console.error('音频加载错误:', e);
                    toast.error('音频加载失败，请重试');
                  }}
                >
                  您的浏览器不支持HTML5音频播放，请更新浏览器版本。
                </audio>

                <div className="flex justify-between items-center mt-3 text-xs text-muted-foreground">
                  <div>
                    提示：可使用空格键暂停/播放
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => window.open(sectionContent.media.accessUrl, '_blank')}
                  >
                    <ExternalLink className="h-3 w-3 mr-1" />
                    新窗口打开
                  </Button>
                </div>
              </div>
            </div>
          </div>
        );
      }
      // 否则使用CourseMediaPlayer（适用于视频）
      else {
        console.log('使用CourseMediaPlayer渲染视频');
        return (
          <CourseMediaPlayer
            media={sectionContent.media}
            onComplete={() => updateLearningProgress(100)}
            onError={(error) => toast.error(error.toString())}
            courseId={Number(courseId)}
            chapterId={currentChapterId}
            sectionId={currentSectionId}
          />
        );
      }
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

  // 添加页面卸载事件处理
  useEffect(() => {
    // 页面卸载前处理
    const handleBeforeUnload = () => {
      if (learningDurationRef.current > 0) {
        console.log(`页面即将卸载，记录最终学习时长: ${learningDurationRef.current}秒`);

        // 使用 navigator.sendBeacon API 替代同步 XMLHttpRequest
        const data = JSON.stringify({
          courseId: Number(courseId),
          chapterId: currentChapterId ? Number(currentChapterId) : undefined,
          sectionId: Number(currentSectionId),
          activityType: currentActivityType || 'VIDEO_WATCH',
          durationSeconds: Math.max(learningDurationRef.current, 1),
          contextData: JSON.stringify({
            source: 'page_unload',
            progress: currentProgress
          })
        });

        // 使用 sendBeacon API
        const success = navigator.sendBeacon('/api/learning/records/completed', data);

        if (!success) {
          console.error('使用 sendBeacon 发送学习记录失败');
          // 如果 sendBeacon 失败，尝试将数据保存到 localStorage
          try {
            localStorage.setItem('pendingLearningRecord', data);
          } catch (storageErr) {
            console.error('保存学习记录到本地存储失败:', storageErr);
          }
        }
      }
    };

    // 添加页面卸载事件监听
    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      // 移除事件监听
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [courseId, currentChapterId, currentSectionId, currentActivityType, currentProgress]);

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
          <Tabs defaultValue="content" value={activeTab} onValueChange={handleTabChange}>
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
              <CourseStatistics courseId={courseId} />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
}