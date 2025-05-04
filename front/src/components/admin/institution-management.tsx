'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
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
import { Badge } from '@/components/ui/badge';
import { Loader2, Search, Building2, ChevronDown, ChevronRight, Eye, Users, BookOpen } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { InstitutionVO } from '@/types/institution';
import { ApiResponse, PaginationResult } from '@/types/api';
import { InstitutionStatsCard } from './institution-stats-card';
import { InstitutionDetailDialog } from './institution-detail-dialog';
import { InstitutionExpandedRow } from './institution-expanded-row';
import adminLearningStatsService from '@/services/admin-learning-stats-service';

export default function InstitutionManagement() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [institutions, setInstitutions] = useState<InstitutionVO[]>([]);
  const [totalInstitutions, setTotalInstitutions] = useState(0);
  const [expandedInstitutionId, setExpandedInstitutionId] = useState<number | null>(null);
  const [selectedInstitution, setSelectedInstitution] = useState<InstitutionVO | null>(null);
  const [isDetailDialogOpen, setIsDetailDialogOpen] = useState(false);
  const [stats, setStats] = useState({
    totalInstitutions: 0,
    activeInstitutions: 0,
    newInstitutionsThisMonth: 0,
    totalUsers: 0
  });

  // 加载机构列表
  useEffect(() => {
    fetchInstitutions();
  }, [currentPage, pageSize, statusFilter]);

  // 加载统计数据
  useEffect(() => {
    fetchStats();
  }, []);

  const fetchInstitutions = async () => {
    setIsLoading(true);
    try {
      // 构建查询参数
      const params: any = {
        page: currentPage,
        size: pageSize
      };

      if (searchQuery) {
        params.name = searchQuery;
      }

      if (statusFilter !== 'all') {
        params.status = statusFilter === 'active' ? 1 : 0;
      }

      const response = await request.get<PaginationResult<InstitutionVO>>('/admin/institutions', { params });

      if (response.data.code === 200 && response.data.data) {
        setInstitutions(response.data.data.content);
        setTotalPages(response.data.data.totalPages);
        setTotalInstitutions(response.data.data.totalElements);
      } else {
        toast.error('获取机构列表失败');
      }
    } catch (error) {
      console.error('获取机构列表出错:', error);
      toast.error('获取机构列表出错');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      // 获取所有机构数据，用于计算统计信息
      const institutionsResponse = await request.get<PaginationResult<InstitutionVO>>('/admin/institutions', {
        params: {
          page: 0,
          size: 1000 // 获取足够多的机构以计算统计数据
        }
      });

      if (institutionsResponse.data.code === 200 && institutionsResponse.data.data) {
        const allInstitutions = institutionsResponse.data.data.content;
        const totalInstitutions = institutionsResponse.data.data.totalElements;

        // 计算活跃机构数（状态为1的机构）
        const activeInstitutions = allInstitutions.filter(inst => inst.status === 1).length;

        // 计算本月新增机构数
        const now = new Date();
        const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
        const newInstitutionsThisMonth = allInstitutions.filter(inst => {
          const createdAt = new Date(inst.createdAt);
          return createdAt >= firstDayOfMonth && createdAt <= now;
        }).length;

        // 计算总用户数（所有机构的用户数总和）
        const totalUsers = allInstitutions.reduce((sum, inst) => sum + (inst.userCount || 0), 0);

        setStats({
          totalInstitutions,
          activeInstitutions,
          newInstitutionsThisMonth,
          totalUsers
        });
      } else {
        console.warn('获取机构列表失败，使用默认值');
        setStats({
          totalInstitutions: 0,
          activeInstitutions: 0,
          newInstitutionsThisMonth: 0,
          totalUsers: 0
        });
      }
    } catch (error) {
      console.error('获取统计数据出错:', error);
      // 出错时使用默认数据
      setStats({
        totalInstitutions: 0,
        activeInstitutions: 0,
        newInstitutionsThisMonth: 0,
        totalUsers: 0
      });
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchInstitutions();
  };

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value);
    setCurrentPage(0);
  };

  const handleExpandRow = async (institutionId: number) => {
    if (expandedInstitutionId === institutionId) {
      setExpandedInstitutionId(null);
    } else {
      setExpandedInstitutionId(institutionId);

      // 获取机构学习统计数据
      try {
        const stats = await adminLearningStatsService.getInstitutionStatisticsOverview(institutionId);

        // 更新机构数据中的学习人数 - 确保stats不为null且totalLearners有值
        if (stats && stats.totalLearners !== undefined) {
          setInstitutions(prevInstitutions =>
            prevInstitutions.map(inst =>
              inst.id === institutionId
                ? { ...inst, totalLearners: stats.totalLearners }
                : inst
            )
          );
        }
      } catch (error) {
        console.error(`获取机构学习统计失败, 机构ID: ${institutionId}:`, error);
        // 即使出错也不影响展开行为
      }
    }
  };

  const handleOpenDetailDialog = (institution: InstitutionVO) => {
    setSelectedInstitution(institution);
    setIsDetailDialogOpen(true);
  };

  const renderStatusBadge = (status: number) => {
    if (status === 1) {
      return (
        <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">
          正常
        </Badge>
      );
    } else {
      return (
        <Badge variant="outline" className="bg-gray-50 text-gray-700 border-gray-200">
          禁用
        </Badge>
      );
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  return (
    <div className="space-y-6">
      {/* 统计卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <InstitutionStatsCard
          title="机构总数"
          value={stats.totalInstitutions}
          icon={<Building2 className="h-4 w-4" />}
        />
        <InstitutionStatsCard
          title="活跃机构"
          value={stats.activeInstitutions}
          icon={<Building2 className="h-4 w-4" />}
        />
        <InstitutionStatsCard
          title="本月新增"
          value={stats.newInstitutionsThisMonth}
          icon={<Building2 className="h-4 w-4" />}
        />
        <InstitutionStatsCard
          title="总用户数"
          value={stats.totalUsers}
          icon={<Users className="h-4 w-4" />}
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>机构列表</CardTitle>
          <CardDescription>
            管理平台上的所有机构，查看详情、用户和课程信息。
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
            <form onSubmit={handleSearch} className="flex gap-2">
              <Input
                placeholder="搜索机构名称..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="max-w-sm"
              />
              <Button type="submit" variant="outline">
                <Search className="h-4 w-4 mr-2" />
                搜索
              </Button>
            </form>
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">状态:</span>
              <Select value={statusFilter} onValueChange={handleStatusFilterChange}>
                <SelectTrigger className="w-[120px]">
                  <SelectValue placeholder="全部" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  <SelectItem value="active">正常</SelectItem>
                  <SelectItem value="inactive">禁用</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2">加载中...</span>
            </div>
          ) : (
            <>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-10"></TableHead>
                      <TableHead>机构名称</TableHead>
                      <TableHead>机构代码</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>用户数</TableHead>
                      <TableHead>课程数</TableHead>
                      <TableHead>创建时间</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {institutions.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={8} className="h-24 text-center">
                          没有找到机构
                        </TableCell>
                      </TableRow>
                    ) : (
                      institutions.map((institution) => (
                        <React.Fragment key={institution.id}>
                          <TableRow>
                            <TableCell>
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => handleExpandRow(institution.id)}
                              >
                                {expandedInstitutionId === institution.id ?
                                  <ChevronDown className="h-4 w-4" /> :
                                  <ChevronRight className="h-4 w-4" />
                                }
                              </Button>
                            </TableCell>
                            <TableCell>
                              <div className="flex items-center gap-2">
                                {institution.logo ? (
                                  <img
                                    src={institution.logo}
                                    alt={institution.name}
                                    className="h-8 w-8 rounded-full object-cover"
                                  />
                                ) : (
                                  <div className="flex h-8 w-8 rounded-full bg-muted items-center justify-center">
                                    <Building2 className="h-4 w-4 text-muted-foreground" />
                                  </div>
                                )}
                                <span>{institution.name}</span>
                              </div>
                            </TableCell>
                            <TableCell>{institution.registerCode || '-'}</TableCell>
                            <TableCell>{renderStatusBadge(institution.status)}</TableCell>
                            <TableCell>{institution.userCount || 0}</TableCell>
                            <TableCell>
                              {institution.publishedCourseCount || 0}/{institution.courseCount || 0}
                            </TableCell>
                            <TableCell>{formatDate(institution.createdAt)}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleOpenDetailDialog(institution)}
                              >
                                <Eye className="h-4 w-4 mr-1" />
                                查看
                              </Button>
                            </TableCell>
                          </TableRow>
                          {expandedInstitutionId === institution.id && (
                            <TableRow>
                              <TableCell colSpan={8} className="p-0 border-t-0">
                                <InstitutionExpandedRow institution={institution} />
                              </TableCell>
                            </TableRow>
                          )}
                        </React.Fragment>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>

              {totalPages > 1 && (
                <div className="flex items-center justify-end space-x-2 py-4">
                  <div className="text-sm text-muted-foreground">
                    共 <span className="font-medium">{totalInstitutions}</span> 个机构，
                    第 <span className="font-medium">{currentPage + 1}</span> 页，
                    共 <span className="font-medium">{totalPages}</span> 页
                  </div>
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious
                          onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                          className={currentPage === 0 ? 'pointer-events-none opacity-50' : ''}
                        />
                      </PaginationItem>
                      {Array.from({ length: Math.min(5, totalPages) }).map((_, index) => {
                        // 显示当前页附近的页码
                        let pageToShow = currentPage - 2 + index;
                        if (currentPage < 2) {
                          pageToShow = index;
                        } else if (currentPage > totalPages - 3) {
                          pageToShow = totalPages - 5 + index;
                        }

                        // 确保页码在有效范围内
                        if (pageToShow >= 0 && pageToShow < totalPages) {
                          return (
                            <PaginationItem key={pageToShow}>
                              <PaginationLink
                                onClick={() => setCurrentPage(pageToShow)}
                                isActive={currentPage === pageToShow}
                              >
                                {pageToShow + 1}
                              </PaginationLink>
                            </PaginationItem>
                          );
                        }
                        return null;
                      })}
                      <PaginationItem>
                        <PaginationNext
                          onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                          className={currentPage === totalPages - 1 ? 'pointer-events-none opacity-50' : ''}
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {/* 机构详情弹窗 */}
      {selectedInstitution && (
        <InstitutionDetailDialog
          institution={selectedInstitution}
          open={isDetailDialogOpen}
          onOpenChange={setIsDetailDialogOpen}
        />
      )}
    </div>
  );
}
