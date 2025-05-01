'use client';

import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Course } from "@/types/course";
import { BookOpen } from "lucide-react";
import { cn } from "@/lib/utils";

interface CourseCardProps {
  course: Course;
  onClick?: (courseId: number) => void;
}

export function CourseCard({ course, onClick }: CourseCardProps) {
  const handleClick = () => {
    if (onClick) {
      onClick(course.id);
    }
  };

  return (
    <Card 
      className="overflow-hidden hover:shadow-md transition-all duration-300 cursor-pointer group"
      onClick={handleClick}
    >
      <div className="relative aspect-video bg-muted overflow-hidden">
        {course.coverUrl ? (
          <img
            src={course.coverUrl}
            alt={course.title}
            className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        ) : (
          <div className="flex items-center justify-center w-full h-full">
            <BookOpen className="w-8 h-8 text-muted-foreground/50" />
          </div>
        )}
      </div>
      <CardContent className="p-3">
        <h3 className="font-medium text-sm line-clamp-2 mb-1">{course.title}</h3>
        <div className="flex justify-between items-center mt-2 text-xs">
          <span className="text-muted-foreground">{course.institutionName || '未知机构'}</span>
          <Badge variant={course.paymentType === 0 ? "secondary" : "default"} className="text-xs">
            {course.paymentType === 0 ? '免费' : `¥${course.price}`}
          </Badge>
        </div>
      </CardContent>
    </Card>
  );
}
