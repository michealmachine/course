'use client';

import { Course } from "@/types/course";
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell
} from '@/components/ui/table';
import { Badge } from "@/components/ui/badge";
import { Loader2, Star } from "lucide-react";
import { formatNumber } from "@/lib/utils";
import { CourseStatus } from "@/types/enums";

interface CourseListViewProps {
  courses: Course[];
  loading: boolean;
  onCourseClick?: (courseId: number) => void;
}

export function CourseListView({ courses, loading, onCourseClick }: CourseListViewProps) {
  if (loading) {
    return (
      <div className="flex justify-center py-8">
        <Loader2 className="h-6 w-6 animate-spin" />
      </div>
    );
  }

  if (courses.length === 0) {
    return (
      <div className="text-center text-muted-foreground py-4">
        暂无课程
      </div>
    );
  }

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>课程名称</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>价格</TableHead>
            <TableHead>难度</TableHead>
            <TableHead>学习人数</TableHead>
            <TableHead>收藏数</TableHead>
            <TableHead>评分</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {courses.map((course) => (
            <TableRow
              key={course.id}
              className="cursor-pointer hover:bg-muted/50"
              onClick={() => onCourseClick && onCourseClick(course.id)}
            >
              <TableCell className="font-medium">
                <div className="flex items-center gap-2">
                  {course.coverUrl && (
                    <div className="w-10 h-10 rounded overflow-hidden flex-shrink-0">
                      <img
                        src={course.coverUrl}
                        alt={course.title}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  )}
                  <span className="line-clamp-1">{course.title}</span>
                </div>
              </TableCell>
              <TableCell>
                {course.status === CourseStatus.PUBLISHED ? (
                  <Badge variant="success" className="bg-green-100 text-green-800 hover:bg-green-200">已发布</Badge>
                ) : (
                  <Badge variant="outline" className="bg-gray-100 text-gray-800 hover:bg-gray-200">未发布</Badge>
                )}
              </TableCell>
              <TableCell>
                {course.paymentType === 0 ? (
                  <Badge variant="secondary">免费</Badge>
                ) : (
                  <Badge variant="default">¥{course.price}</Badge>
                )}
              </TableCell>
              <TableCell>
                {course.difficulty === 1 ? '初级' :
                 course.difficulty === 2 ? '中级' :
                 course.difficulty === 3 ? '高级' : '未知'}
              </TableCell>
              <TableCell>{formatNumber(course.studentCount || 0)}</TableCell>
              <TableCell>{formatNumber(course.favoriteCount || 0)}</TableCell>
              <TableCell>
                <div className="flex items-center">
                  <Star className="h-4 w-4 fill-yellow-400 text-yellow-400 mr-1" />
                  <span>{course.averageRating?.toFixed(1) || '暂无'}</span>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
