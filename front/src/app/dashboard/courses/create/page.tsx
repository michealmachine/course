'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import CourseForm from '@/components/dashboard/courses/course-form';
import { Course } from '@/types/course';

export default function CreateCoursePage() {
  const router = useRouter();
  
  // 处理课程创建成功
  const handleSuccess = (course: Course) => {
    // 创建成功后跳转到课程详情页
    router.push(`/dashboard/courses/${course.id}`);
  };
  
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold tracking-tight">创建新课程</h1>
      <p className="text-muted-foreground">
        填写基本信息创建课程，创建后可以添加章节和内容
      </p>
      
      <CourseForm onSuccess={handleSuccess} />
    </div>
  );
} 