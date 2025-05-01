'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Loader2, FileText, Eye, CheckCircle, XCircle } from 'lucide-react';
import { formatDate } from '@/utils/date';
import { reviewRecordService } from '@/services/review-record-service';
import reviewerInstitutionService from '@/services/reviewerInstitution';
import { ReviewRecordVO, ReviewType, ReviewResult } from '@/types/review-record';
import { useAuthStore } from '@/stores/auth-store';
import { toast } from 'sonner';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious
} from '@/components/ui/pagination';

interface ReviewHistoryTableProps {
  isAdmin?: boolean;
  reviewType: ReviewType;
  targetId?: number;
}

export default function ReviewHistoryTable({ isAdmin = false, reviewType, targetId }: ReviewHistoryTableProps) {
  const router = useRouter();
  const [records, setRecords] = useState<ReviewRecordVO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const pageSize = 10;

  const { user } = useAuthStore();
  const userIsAdmin = user?.roles?.some(role => role.code === 'ROLE_ADMIN');

  // 加载审核记录
  useEffect(() => {
    loadReviewRecords();
  }, [currentPage, reviewType, targetId, isAdmin]);

  const loadReviewRecords = async () => {
    setIsLoading(true);
    try {
      // 根据不同条件加载不同的审核记录
      if (targetId && reviewType === ReviewType.COURSE) {
        try {
          // 加载特定课程的审核历史
          const data = await reviewRecordService.getCourseReviewHistory(targetId);
          setRecords(data);
          setTotalItems(data.length);
          setTotalPages(Math.ceil(data.length / pageSize));
        } catch (error) {
          console.error(`获取课程审核历史失败, 课程ID: ${targetId}:`, error);
          toast.error('获取课程审核历史失败');
          setRecords([]);
          setTotalItems(0);
          setTotalPages(1);
        }
      } else if (targetId && reviewType === ReviewType.INSTITUTION) {
        try {
          // 加载特定机构的审核历史
          const data = await reviewRecordService.getInstitutionReviewHistory(targetId);
          setRecords(data);
          setTotalItems(data.length);
          setTotalPages(Math.ceil(data.length / pageSize));
        } catch (error) {
          console.error(`获取机构审核历史失败, 机构ID: ${targetId}:`, error);
          toast.error('获取机构审核历史失败');
          setRecords([]);
          setTotalItems(0);
          setTotalPages(1);
        }
      } else {
        // 加载所有审核记录或当前审核员的记录
        if (reviewType === ReviewType.INSTITUTION) {
          // 对于机构审核历史，使用机构申请表中的数据
          try {
            // 这里应该调用获取机构申请列表的API，包括已审核的申请
            // 注意：这里需要修改为使用reviewerInstitutionService获取机构申请列表
            const params = {
              page: currentPage,
              size: pageSize,
              // 不传递 status 参数，让后端使用默认值
            };

            // 调用机构申请列表API
            const result = await reviewerInstitutionService.getApplications(params);

            // 过滤出已审核的申请（状态为1-已通过或2-已拒绝）
            const reviewedApplications = result.content.filter(app => app.status === 1 || app.status === 2);

            // 将机构申请转换为审核记录格式
            const mappedRecords = reviewedApplications.map(app => ({
              id: app.id,
              reviewType: ReviewType.INSTITUTION,
              targetId: app.id,
              targetName: app.name,
              result: app.status === 1 ? ReviewResult.APPROVED : ReviewResult.REJECTED,
              reviewerId: app.reviewerId,
              reviewerName: app.reviewerName || '未知审核员',
              reviewedAt: app.reviewedAt,
              comment: app.reviewComment || '',
              institutionId: app.institutionId,
            }));

            setRecords(mappedRecords);
            setTotalItems(result.totalElements);
            setTotalPages(result.totalPages);
          } catch (error) {
            console.error('获取机构申请列表失败:', error);
            toast.error('获取机构审核历史失败');
            setRecords([]);
            setTotalItems(0);
            setTotalPages(1);
          }
        } else {
          // 对于其他类型的审核记录，使用review_records表中的数据
          if (isAdmin && userIsAdmin) {
            try {
              // 管理员可以查看所有审核记录
              const data = await reviewRecordService.getAllReviewRecords(
                reviewType,
                currentPage,
                pageSize
              );
              setRecords(data.content);
              setTotalItems(data.totalElements);
              setTotalPages(data.totalPages);
            } catch (error) {
              console.error('管理员获取审核记录失败:', error);
              toast.error('获取审核记录失败');
              setRecords([]);
              setTotalItems(0);
              setTotalPages(1);
            }
          } else {
            try {
              // 审核员只能查看自己的审核记录，但可以按类型过滤
              const data = await reviewRecordService.getReviewerRecords(currentPage, pageSize, reviewType);
              setRecords(data.content);
              setTotalItems(data.totalElements);
              setTotalPages(data.totalPages);
            } catch (error) {
              console.error('审核员获取审核记录失败:', error);
              toast.error('获取审核记录失败');
              setRecords([]);
              setTotalItems(0);
              setTotalPages(1);
            }
          }
        }
      }
    } finally {
      setIsLoading(false);
    }
  };

  // 处理页码变更
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  // 查看详情
  const handleViewDetail = (record: ReviewRecordVO) => {
    if (record.reviewType === ReviewType.COURSE) {
      router.push(`/dashboard/courses/${record.targetId}`);
    } else if (record.reviewType === ReviewType.INSTITUTION) {
      router.push(`/dashboard/institutions/${record.institutionId}`);
    }
  };

  // 渲染审核结果徽章
  const renderResultBadge = (result: number) => {
    if (result === ReviewResult.APPROVED) {
      return (
        <Badge className="bg-green-100 text-green-800 border-green-200">
          <CheckCircle className="h-3 w-3 mr-1" />
          通过
        </Badge>
      );
    } else {
      return (
        <Badge className="bg-red-100 text-red-800 border-red-200">
          <XCircle className="h-3 w-3 mr-1" />
          拒绝
        </Badge>
      );
    }
  };

  // 渲染审核类型
  const renderReviewType = (type: number) => {
    if (type === ReviewType.COURSE) {
      return '课程审核';
    } else if (type === ReviewType.INSTITUTION) {
      return '机构审核';
    }
    return '未知类型';
  };

  // 分页显示的记录
  const paginatedRecords = records.slice(currentPage * pageSize, (currentPage + 1) * pageSize);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2">加载中...</span>
      </div>
    );
  }

  if (records.length === 0) {
    return (
      <div className="text-center py-12 border rounded-md bg-muted/20">
        <FileText className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
        <h3 className="text-lg font-medium mb-2">暂无审核记录</h3>
        <p className="text-muted-foreground">当有审核记录时会显示在这里</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>审核对象</TableHead>
            <TableHead>审核类型</TableHead>
            <TableHead>审核结果</TableHead>
            <TableHead>审核时间</TableHead>
            <TableHead>审核人</TableHead>
            <TableHead>审核意见</TableHead>
            <TableHead className="text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {paginatedRecords.map((record) => (
            <TableRow key={record.id}>
              <TableCell>
                <div className="flex items-center gap-2">
                  <FileText className="h-4 w-4 text-primary" />
                  <span>{record.targetName}</span>
                </div>
              </TableCell>
              <TableCell>{renderReviewType(record.reviewType)}</TableCell>
              <TableCell>{renderResultBadge(record.result)}</TableCell>
              <TableCell>{formatDate(record.reviewedAt)}</TableCell>
              <TableCell>{record.reviewerName}</TableCell>
              <TableCell className="max-w-[200px] truncate">
                {record.comment || '-'}
              </TableCell>
              <TableCell className="text-right">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleViewDetail(record)}
                >
                  查看详情
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {totalPages > 1 && (
        <Pagination className="mt-4">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
                className={currentPage === 0 ? 'pointer-events-none opacity-50' : ''}
              />
            </PaginationItem>

            {Array.from({ length: totalPages }).map((_, index) => (
              <PaginationItem key={index}>
                <PaginationLink
                  onClick={() => handlePageChange(index)}
                  isActive={currentPage === index}
                >
                  {index + 1}
                </PaginationLink>
              </PaginationItem>
            ))}

            <PaginationItem>
              <PaginationNext
                onClick={() => handlePageChange(Math.min(totalPages - 1, currentPage + 1))}
                className={currentPage === totalPages - 1 ? 'pointer-events-none opacity-50' : ''}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}
    </div>
  );
}
