import { CourseStatus } from '@/types/course';
import { FileEdit, Clock, CheckCircle, Ban, HourglassIcon, XCircle } from 'lucide-react';

interface CourseStatusBadgeProps {
  status: CourseStatus | number;
  showIcon?: boolean;
}

export const CourseStatusBadge = ({ status, showIcon = true }: CourseStatusBadgeProps) => {
  switch (status) {
    case CourseStatus.DRAFT:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
          {showIcon && <FileEdit className="h-3.5 w-3.5 mr-1" />}
          草稿
        </span>
      );
    case CourseStatus.PENDING_REVIEW:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
          {showIcon && <HourglassIcon className="h-3.5 w-3.5 mr-1" />}
          待审核
        </span>
      );
    case CourseStatus.REVIEWING:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          {showIcon && <Clock className="h-3.5 w-3.5 mr-1" />}
          审核中
        </span>
      );
    case CourseStatus.PUBLISHED:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
          {showIcon && <CheckCircle className="h-3.5 w-3.5 mr-1" />}
          已发布
        </span>
      );
    case CourseStatus.REJECTED:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
          {showIcon && <Ban className="h-3.5 w-3.5 mr-1" />}
          已拒绝
        </span>
      );
    case CourseStatus.UNPUBLISHED:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
          {showIcon && <XCircle className="h-3.5 w-3.5 mr-1" />}
          已下线
        </span>
      );
    default:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
          未知状态
        </span>
      );
  }
};

export default CourseStatusBadge; 