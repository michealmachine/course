'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { BookOpen, Activity, AlertCircle } from 'lucide-react';
import { userCourseService, learningService, wrongQuestionService } from '@/services';
import { formatDuration } from '@/lib/utils';
import { toast } from 'sonner';

export function StudentDashboardCards() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [courseCount, setCourseCount] = useState(0);
  const [totalLearningDuration, setTotalLearningDuration] = useState(0);
  const [averageProgress, setAverageProgress] = useState(0);
  const [wrongQuestionCount, setWrongQuestionCount] = useState(0);
  const [unresolvedWrongQuestionCount, setUnresolvedWrongQuestionCount] = useState(0);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);

        // 获取学习统计数据
        const learningStats = await learningService.getLearningStatistics();
        setCourseCount(learningStats.totalCourses);
        setTotalLearningDuration(learningStats.totalLearningDuration);

        // 计算平均学习进度
        if (learningStats.courseStatistics && learningStats.courseStatistics.length > 0) {
          const totalProgress = learningStats.courseStatistics.reduce(
            (sum, course) => sum + course.progress,
            0
          );
          setAverageProgress(Math.round(totalProgress / learningStats.courseStatistics.length));
        }

        // 获取错题数量
        try {
          const wrongQuestions = await wrongQuestionService.getWrongQuestions({ size: 1 });
          setWrongQuestionCount(wrongQuestions.totalElements);

          // 获取未解决的错题数量
          const unresolvedWrongQuestions = await wrongQuestionService.getUnresolvedWrongQuestions({ size: 1 });
          setUnresolvedWrongQuestionCount(unresolvedWrongQuestions.totalElements);
        } catch (error) {
          console.error('获取错题数量失败:', error);
          // 使用学习统计中的错题数量作为备选
          setWrongQuestionCount(learningStats.wrongQuestions || 0);
          setUnresolvedWrongQuestionCount(0);
        }
      } catch (error) {
        console.error('获取学员统计数据失败:', error);
        toast.error('获取统计数据失败');
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  const navigateToMyCourses = () => {
    router.push('/dashboard/my-courses');
  };

  const navigateToWrongQuestions = () => {
    router.push('/dashboard/wrong-questions');
  };

  return (
    <>
      {/* 已学课程卡片 */}
      <Card className="cursor-pointer hover:shadow-md transition-shadow" onClick={navigateToMyCourses}>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">已学课程</CardTitle>
          <BookOpen className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          {loading ? (
            <Skeleton className="h-8 w-20" />
          ) : (
            <>
              <div className="text-2xl font-bold">{courseCount}</div>
              <p className="text-xs text-muted-foreground mt-1">
                共计学习 {formatDuration(totalLearningDuration)}
              </p>
            </>
          )}
        </CardContent>
      </Card>

      {/* 错题数量卡片 */}
      <Card className="cursor-pointer hover:shadow-md transition-shadow" onClick={navigateToWrongQuestions}>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">错题数量</CardTitle>
          <AlertCircle className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          {loading ? (
            <Skeleton className="h-8 w-20" />
          ) : (
            <>
              <div className="text-2xl font-bold">{wrongQuestionCount}</div>
              <p className="text-xs text-muted-foreground mt-1">
                其中未解决 {unresolvedWrongQuestionCount} 道
              </p>
            </>
          )}
        </CardContent>
      </Card>

      {/* 学习进度卡片 */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">平均学习进度</CardTitle>
          <Activity className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          {loading ? (
            <Skeleton className="h-8 w-20" />
          ) : (
            <>
              <div className="text-2xl font-bold">{averageProgress}%</div>
              <p className="text-xs text-muted-foreground mt-1">
                所有课程的平均完成度
              </p>
            </>
          )}
        </CardContent>
      </Card>

      {/* 最近学习卡片 */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">学习天数</CardTitle>
          <Activity className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          {loading ? (
            <Skeleton className="h-8 w-20" />
          ) : (
            <>
              <div className="text-2xl font-bold">{loading ? 0 : 0}</div>
              <p className="text-xs text-muted-foreground mt-1">
                连续学习 {loading ? 0 : 0} 天
              </p>
            </>
          )}
        </CardContent>
      </Card>
    </>
  );
}
