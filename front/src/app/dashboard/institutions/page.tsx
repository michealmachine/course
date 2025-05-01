'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { Building2, Eye, Check, X, History } from 'lucide-react';

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
import { Badge } from '@/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/ui/tabs';
import reviewerInstitutionService from '@/services/reviewerInstitution';
import { InstitutionApplicationResponse } from '@/types/institution';
import ReviewHistoryTable from '@/components/dashboard/review-history/review-history-table';
import { ReviewType } from '@/types/review-record';
import { useAuthStore } from '@/stores/auth-store';

export default function InstitutionsPage() {
  const router = useRouter();
  const [applications, setApplications] = useState<InstitutionApplicationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [status, setStatus] = useState<string>('0'); // 默认显示待审核
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState<string>('applications'); // 默认显示申请列表

  // 定义状态映射
  const statusMap = {
    '0': { label: '待审核', color: 'bg-yellow-100 text-yellow-800 border-yellow-200' },
    '1': { label: '已通过', color: 'bg-green-100 text-green-800 border-green-200' },
    '2': { label: '已拒绝', color: 'bg-red-100 text-red-800 border-red-200' },
  };

  // 加载数据
  useEffect(() => {
    fetchApplications();
  }, [currentPage, status, searchTerm]);

  // 获取机构申请列表
  const fetchApplications = async () => {
    setIsLoading(true);
    try {
      const params = {
        page: currentPage,
        size: 10,
        status: status === 'all' ? undefined : parseInt(status),
        ...(searchTerm && { name: searchTerm }),
      };

      const result = await reviewerInstitutionService.getApplications(params);
      setApplications(result.content);
      setTotalPages(result.totalPages);
      setTotalItems(result.totalElements);
    } catch (error) {
      console.error('获取机构申请列表失败', error);
      toast.error('获取机构申请列表失败，请重试');
    } finally {
      setIsLoading(false);
    }
  };

  // 查看申请详情
  const handleViewDetail = (id: number) => {
    router.push(`/dashboard/institutions/${id}`);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">机构审核</h2>
          <p className="text-muted-foreground">
            审核机构申请，确保平台机构的合规性和质量
          </p>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="applications">机构申请</TabsTrigger>
          <TabsTrigger value="history">审核历史</TabsTrigger>
        </TabsList>

        <TabsContent value="applications" className="mt-6">
          <div className="flex items-center justify-between gap-4 mb-6">
            <div className="flex flex-1 items-center gap-2">
              <Input
                placeholder="搜索机构名称..."
                className="max-w-xs"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <Button
                variant="outline"
                onClick={() => {
                  setSearchTerm('');
                  setCurrentPage(0);
                }}
              >
                重置
              </Button>
            </div>

            <Select
              value={status}
              onValueChange={(value) => {
                setStatus(value);
                setCurrentPage(0);
              }}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="选择状态" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="0">待审核</SelectItem>
                <SelectItem value="1">已通过</SelectItem>
                <SelectItem value="2">已拒绝</SelectItem>
                <SelectItem value="all">全部状态</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>机构申请列表</CardTitle>
              <CardDescription>
                {totalItems > 0
                  ? `共 ${totalItems} 条申请记录，第 ${currentPage + 1}/${totalPages} 页`
                  : '暂无申请记录'}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                // 加载骨架屏
                <div className="space-y-4">
                  {Array.from({ length: 5 }).map((_, index) => (
                    <div key={index} className="flex items-center gap-4">
                      <Skeleton className="h-12 w-12 rounded-full" />
                      <div className="space-y-2 flex-1">
                        <Skeleton className="h-4 w-1/3" />
                        <Skeleton className="h-4 w-1/2" />
                      </div>
                      <Skeleton className="h-8 w-24" />
                    </div>
                  ))}
                </div>
              ) : (
                <>
                  {applications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-60 text-muted-foreground">
                      <Building2 className="h-16 w-16 mb-4 opacity-20" />
                      <p className="text-lg font-medium">暂无申请记录</p>
                      <p className="text-sm">当前筛选条件下没有找到机构申请</p>
                    </div>
                  ) : (
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>申请ID</TableHead>
                          <TableHead>机构名称</TableHead>
                          <TableHead>联系人</TableHead>
                          <TableHead>联系方式</TableHead>
                          <TableHead>申请时间</TableHead>
                          <TableHead>状态</TableHead>
                          <TableHead className="text-right">操作</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {applications.map((application) => (
                          <TableRow key={application.id}>
                            <TableCell className="font-medium">
                              {application.applicationId}
                            </TableCell>
                            <TableCell>{application.name}</TableCell>
                            <TableCell>{application.contactPerson}</TableCell>
                            <TableCell>
                              {application.contactPhone || application.contactEmail}
                            </TableCell>
                            <TableCell>
                              {new Date(application.createdAt).toLocaleString()}
                            </TableCell>
                            <TableCell>
                              <Badge
                                className={
                                  statusMap[
                                    application.status.toString() as keyof typeof statusMap
                                  ].color + ' border'
                                }
                              >
                                {
                                  statusMap[
                                    application.status.toString() as keyof typeof statusMap
                                  ].label
                                }
                              </Badge>
                            </TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => handleViewDetail(application.id)}
                                title="查看详情"
                              >
                                <Eye className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  )}

                  {/* 分页 */}
                  {applications.length > 0 && (
                    <Pagination className="mt-6">
                      <PaginationContent>
                        <PaginationItem>
                          <PaginationPrevious
                            onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
                            className={currentPage === 0 ? 'pointer-events-none opacity-50' : ''}
                          />
                        </PaginationItem>

                        {Array.from({ length: totalPages }).map((_, index) => {
                          // 只显示当前页面附近的页码
                          if (
                            index === 0 ||
                            index === totalPages - 1 ||
                            (index >= currentPage - 1 && index <= currentPage + 1)
                          ) {
                            return (
                              <PaginationItem key={index}>
                                <PaginationLink
                                  isActive={currentPage === index}
                                  onClick={() => setCurrentPage(index)}
                                >
                                  {index + 1}
                                </PaginationLink>
                              </PaginationItem>
                            );
                          }

                          // 添加省略号
                          if (
                            (index === 1 && currentPage > 2) ||
                            (index === totalPages - 2 && currentPage < totalPages - 3)
                          ) {
                            return (
                              <PaginationItem key={index}>
                                <span className="px-4 py-2">...</span>
                              </PaginationItem>
                            );
                          }

                          return null;
                        })}

                        <PaginationItem>
                          <PaginationNext
                            onClick={() =>
                              setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))
                            }
                            className={
                              currentPage === totalPages - 1 ? 'pointer-events-none opacity-50' : ''
                            }
                          />
                        </PaginationItem>
                      </PaginationContent>
                    </Pagination>
                  )}
                </>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="history" className="mt-6">
          <div className="mb-4">
            <h2 className="text-xl font-semibold mb-2">审核历史记录</h2>
            <p className="text-muted-foreground">
              查看所有机构的审核历史记录
            </p>
          </div>

          <ReviewHistoryTable
            isAdmin={useAuthStore.getState().user?.roles?.some(role => role.code === 'ROLE_ADMIN')}
            reviewType={ReviewType.INSTITUTION}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
}