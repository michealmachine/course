import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { userCourseService } from '@/services';
import { UserCourseVO } from '@/types/userCourse';
import { toast } from 'sonner';
import { formatDate, formatDuration } from '@/lib/utils';
import { Clock, BookOpen } from 'lucide-react';

export default function MyLearningProgress() {
  const router = useRouter();
  const [recentCourses, setRecentCourses] = useState<UserCourseVO[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchRecentCourses = async () => {
    try {
      setLoading(true);
      const response = await userCourseService.getRecentLearnedCourses(5);
      setRecentCourses(response);
    } catch (error) {
      toast.error('获取最近学习记录失败');
      console.error('获取最近学习记录失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecentCourses();
  }, []);

  const handleContinueLearning = (courseId: number) => {
    router.push(`/dashboard/learn/${courseId}`);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (recentCourses.length === 0) {
    return (
      <Card className="p-6 text-center text-gray-500">
        暂无学习记录
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {recentCourses.map((course) => (
        <Card key={course.id} className="p-4">
          <div className="flex items-start gap-4">
            {course.courseCover && (
              <img
                src={course.courseCover}
                alt={course.courseTitle}
                className="w-24 h-24 object-cover rounded"
              />
            )}
            <div className="flex-1 min-w-0">
              <h3 className="font-semibold text-lg line-clamp-1">
                {course.courseTitle}
              </h3>
              <div className="mt-2 space-y-2">
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <Clock className="h-4 w-4" />
                  <span>学习时长：{formatDuration(course.learnDuration)}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <BookOpen className="h-4 w-4" />
                  <span>最近学习：{formatDate(course.lastLearnAt || course.updatedAt)}</span>
                </div>
                <div className="flex items-center gap-4">
                  <div className="flex-1">
                    <div className="flex justify-between text-sm mb-1">
                      <span>学习进度</span>
                      <span className="text-primary">{Math.round(course.progress * 100)}%</span>
                    </div>
                    <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-primary transition-all"
                        style={{ width: `${course.progress * 100}%` }}
                      />
                    </div>
                  </div>
                  <Button size="sm" onClick={() => handleContinueLearning(course.courseId)}>
                    继续学习
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
} 