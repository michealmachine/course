'use client';

import { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Pagination } from '@/components/ui/pagination';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { Users, Clock, BarChart2, Activity } from 'lucide-react';
import { StudentLearningVO } from '@/types/institution-stats';
import { Page } from '@/types/api';
import institutionLearningStatsService from '@/services/institution-learning-stats-service';
import { formatDuration } from '@/lib/utils';

interface CourseStudentStatsProps {
  courseId: number;
}

export function CourseStudentStats({ courseId }: CourseStudentStatsProps) {
  const [students, setStudents] = useState<StudentLearningVO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  // 加载学生学习统计数据
  const loadStudentStats = async (pageNumber: number = 0) => {
    try {
      setLoading(true);
      setError(null);

      console.log(`开始加载学生学习统计数据, 课程ID: ${courseId}, 页码: ${pageNumber}, 每页数量: ${pageSize}`);

      // 尝试获取学生学习统计数据
      const response = await institutionLearningStatsService.getCourseStudentStatistics(
        courseId,
        pageNumber,
        pageSize
      );

      console.log('获取到的学生学习统计数据:', response);

      // 安全地设置数据
      if (response && response.content) {
        console.log('学生学习统计数据内容:', response.content);
        setStudents(response.content);
        setTotalPages(response.totalPages || 1);
        setTotalElements(response.totalElements || 0);
      } else {
        // 如果没有数据，设置为空数组
        console.warn('返回的学生学习统计数据为空');
        setStudents([]);
        setTotalPages(1);
        setTotalElements(0);
      }
    } catch (err: any) {
      console.error('加载学生学习统计失败:', err);
      setError(err.message || '加载学生学习统计失败');
      // 出错时设置为空数组
      setStudents([]);
      setTotalPages(1);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载和页码变化时重新加载数据
  useEffect(() => {
    loadStudentStats(page);
  }, [courseId, page]); // eslint-disable-line react-hooks/exhaustive-deps

  // 格式化最后学习时间
  const formatLastLearnTime = (dateString: string) => {
    if (!dateString) return '从未学习';

    try {
      const date = new Date(dateString);
      return formatDistanceToNow(date, { addSuffix: true, locale: zhCN });
    } catch (error) {
      return '未知时间';
    }
  };

  // 处理页码变化
  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-xl">学生学习统计</CardTitle>
            <CardDescription>
              共 {totalElements} 名学生学习了此课程
            </CardDescription>
          </div>
          <Button variant="outline" size="sm" onClick={() => loadStudentStats(page)}>
            刷新
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {loading ? (
          // 加载状态
          <div className="space-y-2">
            <Skeleton className="h-8 w-full" />
            {Array(5).fill(0).map((_, i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        ) : error ? (
          // 错误状态
          <div className="text-center py-8 text-muted-foreground">
            <p>{error}</p>
            <Button
              variant="outline"
              size="sm"
              className="mt-4"
              onClick={() => loadStudentStats(page)}
            >
              重试
            </Button>
          </div>
        ) : students.length === 0 ? (
          // 空状态
          <div className="text-center py-8 text-muted-foreground">
            <Users className="mx-auto h-12 w-12 opacity-20 mb-2" />
            <p>暂无学生学习数据</p>
          </div>
        ) : (
          // 数据表格
          <>
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>学生</TableHead>
                    <TableHead>学习进度</TableHead>
                    <TableHead>学习时长</TableHead>
                    <TableHead>活动次数</TableHead>
                    <TableHead>最后学习</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {students.map((student) => (
                    <TableRow key={student?.userId || 'unknown'}>
                      <TableCell className="font-medium">{student?.username || '未知用户'}</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <div className="w-24 h-2 bg-gray-100 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-primary"
                              style={{ width: `${student?.progress || 0}%` }}
                            />
                          </div>
                          <span className="text-sm">{student?.progress || 0}%</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3 text-muted-foreground" />
                          <span>{formatDuration(student?.learningDuration || 0)}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1">
                          <Activity className="h-3 w-3 text-muted-foreground" />
                          <span>{student?.activityCount || 0}</span>
                        </div>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formatLastLearnTime(student?.lastLearnTime || '')}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {/* 分页控件 */}
            {totalPages > 1 && (
              <div className="flex justify-center mt-4">
                <Pagination
                  currentPage={page + 1}
                  totalPages={totalPages}
                  onPageChange={(p) => handlePageChange(p - 1)}
                />
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}
