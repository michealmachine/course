'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Loader2, Search, Play } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import { userCourseService } from '@/services';
import { UserCourseVO } from '@/types/userCourse';
import { formatDate } from '@/lib/utils';
import { toast } from 'sonner';

export function MyCoursesList() {
  const router = useRouter();
  const { user } = useAuthStore();
  const [courses, setCourses] = useState<UserCourseVO[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  // 获取用户的课程列表
  const fetchCourses = async () => {
    try {
      setLoading(true);
      const response = await userCourseService.getUserPurchasedCourses();
      setCourses(response);
    } catch (error) {
      console.error('获取课程列表失败:', error);
      toast.error('获取课程列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.id) {
      fetchCourses();
    }
  }, [user?.id]);

  // 过滤课程列表
  const filteredCourses = courses.filter(course =>
    course && course.courseTitle && course.courseTitle.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // 处理课程点击
  const handleCourseClick = (courseId: number) => {
    router.push(`/dashboard/learn/${courseId}`);
  };

  // 处理继续学习
  const handleContinueLearning = (courseId: number) => {
    router.push(`/dashboard/learn/${courseId}`);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loader2 className="h-6 w-6 animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* 搜索框 */}
      <div className="flex items-center gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="搜索课程..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-8"
          />
        </div>
      </div>

      {/* 课程列表 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredCourses.map((course) => (
          <Card key={course.id} className="overflow-hidden">
            {course.courseCover && (
              <div className="relative h-48 cursor-pointer" onClick={() => handleCourseClick(course.courseId)}>
                <img
                  src={course.courseCover}
                  alt={course.courseTitle}
                  className="w-full h-full object-cover"
                />
                <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-4">
                  <Badge variant="success">
                    {course.status === 0 ? "学习中" : course.status === 1 ? "已过期" : "已退款"}
                  </Badge>
                </div>
              </div>
            )}
            <CardContent className="p-4">
              <h3 className="font-semibold text-lg line-clamp-2 cursor-pointer" onClick={() => handleCourseClick(course.courseId)}>
                {course.courseTitle}
              </h3>
              <p className="text-sm text-gray-500 mt-1">
                机构：{course.institutionName || "未知机构"}
              </p>
              <div className="flex flex-col gap-2 mt-3">
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span>学习进度</span>
                    <span className="text-primary">{course.progress}%</span>
                  </div>
                  <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-primary transition-all"
                      style={{ width: `${course.progress}%` }}
                    />
                  </div>
                </div>
                <div className="flex justify-between mt-2 text-xs text-gray-500">
                  <span>最后学习：{course.lastLearnAt ? formatDate(course.lastLearnAt) : '尚未开始'}</span>
                </div>
                <div className="flex justify-end mt-2">
                  <Button size="sm" onClick={() => handleContinueLearning(course.courseId)}>
                    {course.progress > 0 ? '继续学习' : '开始学习'}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* 空状态 */}
      {filteredCourses.length === 0 && (
        <div className="text-center py-8">
          <p className="text-muted-foreground">暂无课程</p>
        </div>
      )}
    </div>
  );
} 