import React from 'react';
import { CheckCircle, XCircle } from 'lucide-react';
import { Course } from '@/types/course';

interface CoursePublishBadgeProps {
  course: Course;
  showIcon?: boolean;
}

/**
 * 显示课程是否已发布的徽章组件
 */
export const CoursePublishBadge = ({ course, showIcon = true }: CoursePublishBadgeProps) => {
  // 工作区版本课程，且有publishedVersionId，表示已发布
  const isPublished = !course.isPublishedVersion && course.publishedVersionId != null;
  
  if (isPublished) {
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
        {showIcon && <CheckCircle className="h-3.5 w-3.5 mr-1" />}
        已发布
      </span>
    );
  } else {
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
        {showIcon && <XCircle className="h-3.5 w-3.5 mr-1" />}
        未发布
      </span>
    );
  }
};

export default CoursePublishBadge; 