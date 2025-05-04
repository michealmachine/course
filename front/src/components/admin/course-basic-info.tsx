'use client';

import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import { BookOpen, Tag } from 'lucide-react';
import { CourseVO } from '@/types/course';

interface CourseBasicInfoProps {
  course: CourseVO;
  isLoading: boolean;
}

export function CourseBasicInfo({ course, isLoading }: CourseBasicInfoProps) {
  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatPrice = (price: number) => {
    if (price === 0) return '免费';
    return `¥${(price / 100).toFixed(2)}`;
  };

  const formatDuration = (seconds: number) => {
    if (!seconds) return '0分钟';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}小时${minutes > 0 ? ` ${minutes}分钟` : ''}`;
    }
    return `${minutes}分钟`;
  };

  const getDifficultyText = (difficulty: number) => {
    switch (difficulty) {
      case 1: return '入门';
      case 2: return '初级';
      case 3: return '中级';
      case 4: return '高级';
      case 5: return '专家';
      default: return '未设置';
    }
  };

  const getStatusText = (status: number) => {
    switch (status) {
      case 1: return '草稿';
      case 2: return '审核中';
      case 3: return '已拒绝';
      case 4: return '已发布';
      default: return '未知';
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center space-x-4">
          <Skeleton className="h-32 w-48 rounded-md" />
          <div className="space-y-2">
            <Skeleton className="h-4 w-[250px]" />
            <Skeleton className="h-4 w-[200px]" />
            <Skeleton className="h-4 w-[150px]" />
          </div>
        </div>
        <Separator />
        <div className="space-y-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="grid grid-cols-3 gap-4">
              <Skeleton className="h-4 w-[100px]" />
              <Skeleton className="h-4 w-full col-span-2" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-start space-x-4">
        <div className="h-32 w-48 rounded-md bg-muted flex items-center justify-center overflow-hidden">
          {course.coverUrl ? (
            <img
              src={course.coverUrl}
              alt={course.title}
              className="h-full w-full object-cover"
            />
          ) : (
            <BookOpen className="h-12 w-12 text-muted-foreground" />
          )}
        </div>
        <div>
          <h3 className="text-lg font-medium">{course.title}</h3>
          <p className="text-sm text-muted-foreground">
            {course.institutionName || '未知机构'} · 创建于 {formatDate(course.createdAt)}
          </p>
          <div className="flex items-center gap-2 mt-2">
            <Badge variant="outline" className={
              course.status === 4 ? 'bg-green-50 text-green-700 border-green-200' :
              course.status === 3 ? 'bg-red-50 text-red-700 border-red-200' :
              course.status === 2 ? 'bg-yellow-50 text-yellow-700 border-yellow-200' :
              'bg-gray-50 text-gray-700 border-gray-200'
            }>
              {getStatusText(course.status)}
            </Badge>
            <Badge variant="outline">{formatPrice(course.price)}</Badge>
            <Badge variant="outline">{getDifficultyText(course.difficulty)} 难度</Badge>
          </div>
        </div>
      </div>

      <Separator />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <h4 className="text-sm font-medium">基本信息</h4>

          <div className="text-sm">
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">课程ID:</span>
              <span className="col-span-2">{course.id}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">课程描述:</span>
              <span className="col-span-2">{course.description || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">章节数量:</span>
              <span className="col-span-2">{course.chapterCount || 0}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">总时长:</span>
              <span className="col-span-2">{formatDuration(course.totalDuration || 0)}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">更新时间:</span>
              <span className="col-span-2">{formatDate(course.updatedAt)}</span>
            </div>
          </div>
        </div>

        <div className="space-y-2">
          <h4 className="text-sm font-medium">统计信息</h4>

          <div className="text-sm">
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">学习人数:</span>
              <span className="col-span-2">{course.learningCount || 0}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">收藏数:</span>
              <span className="col-span-2">{course.favoriteCount || 0}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">评分:</span>
              <span className="col-span-2">{course.averageRating?.toFixed(1) || '-'} / 5.0</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">评价数:</span>
              <span className="col-span-2">{course.reviewCount || 0}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">标签:</span>
              <div className="col-span-2 flex flex-wrap gap-1">
                {course.tags && course.tags.length > 0 ? (
                  course.tags.map((tag, index) => (
                    <Badge key={index} variant="outline" className="text-xs">
                      {typeof tag === 'string' ? tag : tag.name}
                    </Badge>
                  ))
                ) : (
                  <span className="text-muted-foreground">无标签</span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
