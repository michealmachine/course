'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { BookOpen, BarChart, MessageSquare, ListTree } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { CourseVO } from '@/types/course';
import { ApiResponse } from '@/types/api';
import { CourseBasicInfo } from './course-basic-info';
import { CourseChapterList } from './course-chapter-list';
import { CourseStatistics } from './course-statistics';
import { CourseReviews } from './course-reviews';

interface CourseDetailDialogProps {
  course: CourseVO;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CourseDetailDialog({
  course,
  open,
  onOpenChange
}: CourseDetailDialogProps) {
  const [activeTab, setActiveTab] = useState('basic');
  const [detailedCourse, setDetailedCourse] = useState<CourseVO | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [stats, setStats] = useState<any>(null);

  useEffect(() => {
    if (open && course.id) {
      fetchCourseDetail();
      fetchCourseStats();
    }
  }, [open, course.id]);

  const fetchCourseDetail = async () => {
    if (!course.id) return;

    setIsLoading(true);
    try {
      const response = await request.get<CourseVO>(`/admin/courses/${course.id}`);

      if (response.data.code === 200 && response.data.data) {
        setDetailedCourse(response.data.data);
      } else {
        toast.error('获取课程详情失败');
      }
    } catch (error) {
      console.error('获取课程详情出错:', error);
      toast.error('获取课程详情出错');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchCourseStats = async () => {
    if (!course.id) return;

    try {
      // 由于后端API尚未实现，使用模拟数据
      // 但使用课程对象中的真实学习人数

      // 模拟API调用延迟
      await new Promise(resolve => setTimeout(resolve, 500));

      // 生成随机但合理的完成率（0.3-0.8之间）
      const completionRate = 0.3 + (Math.random() * 0.5);

      // 生成随机但合理的平均学习时长（10-60分钟）
      const averageDuration = Math.floor(Math.random() * 3000) + 600;

      // 生成随机但合理的进度分布
      const notStarted = Math.random() * 0.4;
      const completed = completionRate;
      const inProgress = 1 - notStarted - completed;

      // 生成随机但合理的评分分布
      // 根据课程的平均评分生成更真实的分布
      const avgRating = course.averageRating || 4.0;
      let ratingDistribution = {};

      if (avgRating >= 4.5) {
        // 高评分课程
        ratingDistribution = {
          5: 0.6,
          4: 0.25,
          3: 0.1,
          2: 0.03,
          1: 0.02
        };
      } else if (avgRating >= 4.0) {
        // 良好评分课程
        ratingDistribution = {
          5: 0.4,
          4: 0.4,
          3: 0.15,
          2: 0.03,
          1: 0.02
        };
      } else if (avgRating >= 3.5) {
        // 中等评分课程
        ratingDistribution = {
          5: 0.25,
          4: 0.35,
          3: 0.25,
          2: 0.1,
          1: 0.05
        };
      } else {
        // 低评分课程
        ratingDistribution = {
          5: 0.15,
          4: 0.25,
          3: 0.3,
          2: 0.2,
          1: 0.1
        };
      }

      setStats({
        // 使用课程对象中的真实学习人数
        totalLearners: course.learningCount || 0,
        completionRate,
        averageDuration,
        progressDistribution: {
          notStarted,
          inProgress,
          completed
        },
        ratingDistribution
      });
    } catch (error) {
      console.error('获取课程统计数据出错:', error);
      // 出错时使用默认数据，但仍然使用课程对象中的学习人数
      setStats({
        totalLearners: course.learningCount || 0,
        completionRate: 0.5,
        averageDuration: 1800,
        progressDistribution: {
          notStarted: 0.33,
          inProgress: 0.34,
          completed: 0.33
        },
        ratingDistribution: {
          5: 0.2,
          4: 0.2,
          3: 0.2,
          2: 0.2,
          1: 0.2
        }
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <BookOpen className="h-5 w-5" />
            {course.title}
          </DialogTitle>
          <DialogDescription>
            查看课程详细信息、章节、学习统计和评价
          </DialogDescription>
        </DialogHeader>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 overflow-hidden flex flex-col">
          <TabsList className="grid grid-cols-4">
            <TabsTrigger value="basic">基本信息</TabsTrigger>
            <TabsTrigger value="chapters">章节内容</TabsTrigger>
            <TabsTrigger value="statistics">学习统计</TabsTrigger>
            <TabsTrigger value="reviews">评价管理</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-auto mt-4">
            <TabsContent value="basic" className="h-full">
              <CourseBasicInfo
                course={detailedCourse || course}
                isLoading={isLoading}
              />
            </TabsContent>

            <TabsContent value="chapters" className="h-full">
              <CourseChapterList
                courseId={course.id}
              />
            </TabsContent>

            <TabsContent value="statistics" className="h-full">
              <CourseStatistics
                courseId={course.id}
                stats={stats}
              />
            </TabsContent>

            <TabsContent value="reviews" className="h-full">
              <CourseReviews
                courseId={course.id}
                stats={stats}
              />
            </TabsContent>
          </div>
        </Tabs>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            关闭
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
