'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  HardDrive,
  RefreshCw,
  Check,
  X,
  Filter,
  Search,
  Info
} from 'lucide-react';

import { Button } from '@/components/ui/button';
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip';

import quotaApplicationService from '@/services/quota-application';
import { 
  QuotaApplicationVO,
  QuotaApplicationStatus,
  QuotaType,
} from '@/types/quota';

export default function QuotaApplicationsPage() {
  const router = useRouter();
  const [applications, setApplications] = useState<QuotaApplicationVO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [pageSize] = useState(10);
  
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedApplication, setSelectedApplication] = useState<QuotaApplicationVO | null>(null);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  
  // 加载配额申请数据
  useEffect(() => {
    fetchApplications();
  }, [currentPage, selectedStatus]);
  
  const fetchApplications = async () => {
    setIsLoading(true);
    try {
      const status = selectedStatus ? parseInt(selectedStatus) : undefined;
      const response = await quotaApplicationService.getAllApplications({
        status,
        pageNum: currentPage,
        pageSize
      });
      
      setApplications(response.content);
      setTotalPages(response.totalPages);
      setTotalItems(response.totalElements);
      setIsLoading(false);
    } catch (error) {
      console.error('获取配额申请列表失败:', error);
      toast.error('获取配额申请列表失败');
      setIsLoading(false);
    }
  };
  
  // 查看申请详情
  const handleViewDetail = async (id: number) => {
    try {
      const application = await quotaApplicationService.getApplicationDetail(id);
      setSelectedApplication(application);
      setDetailDialogOpen(true);
    } catch (error) {
      console.error(`获取申请详情失败, ID: ${id}:`, error);
      toast.error('获取申请详情失败');
    }
  };
  
  // 审核通过申请
  const handleApprove = async (id: number) => {
    setIsProcessing(true);
    try {
      await quotaApplicationService.approveApplication(id);
      toast.success('已批准申请');
      fetchApplications();
      setDetailDialogOpen(false);
    } catch (error) {
      console.error(`审核通过申请失败, ID: ${id}:`, error);
      toast.error('审核操作失败');
    } finally {
      setIsProcessing(false);
    }
  };
  
  // 审核拒绝申请
  const handleReject = async (id: number) => {
    if (!rejectReason.trim()) {
      toast.error('请输入拒绝原因');
      return;
    }
    
    setIsProcessing(true);
    try {
      await quotaApplicationService.rejectApplication(id, rejectReason);
      toast.success('已拒绝申请');
      fetchApplications();
      setRejectDialogOpen(false);
      setDetailDialogOpen(false);
      setRejectReason('');
    } catch (error) {
      console.error(`审核拒绝申请失败, ID: ${id}:`, error);
      toast.error('审核操作失败');
    } finally {
      setIsProcessing(false);
    }
  };
  
  // 显示配额类型名称
  const getQuotaTypeName = (type: QuotaType): string => {
    switch (type) {
      case QuotaType.VIDEO: return '视频';
      case QuotaType.DOCUMENT: return '文档';
      case QuotaType.TOTAL: return '总配额';
      default: return '未知';
    }
  };
  
  // 格式化文件大小
  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  };
  
  // 获取状态标签
  const getStatusBadge = (status: QuotaApplicationStatus) => {
    switch (status) {
      case QuotaApplicationStatus.PENDING:
        return <Badge variant="secondary">待审核</Badge>;
      case QuotaApplicationStatus.APPROVED:
        return <Badge variant="success">已通过</Badge>;
      case QuotaApplicationStatus.REJECTED:
        return <Badge variant="destructive">已拒绝</Badge>;
      default:
        return <Badge>未知</Badge>;
    }
  };
  
  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">配额申请管理</h1>
          <p className="text-muted-foreground">审核和管理机构用户的存储配额申请</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchApplications}
            disabled={isLoading}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            刷新
          </Button>
        </div>
      </div>
      
      {/* 过滤器 */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row gap-4 items-end">
            <div className="w-full sm:w-48">
              <Label htmlFor="status-filter">状态</Label>
              <Select
                value={selectedStatus}
                onValueChange={setSelectedStatus}
              >
                <SelectTrigger id="status-filter">
                  <SelectValue placeholder="所有状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">所有状态</SelectItem>
                  <SelectItem value={QuotaApplicationStatus.PENDING.toString()}>待审核</SelectItem>
                  <SelectItem value={QuotaApplicationStatus.APPROVED.toString()}>已通过</SelectItem>
                  <SelectItem value={QuotaApplicationStatus.REJECTED.toString()}>已拒绝</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <Button 
              variant="outline" 
              className="sm:h-10"
              onClick={() => {
                setSelectedStatus('');
                setCurrentPage(1);
              }}
            >
              <Filter className="h-4 w-4 mr-2" />
              重置筛选
            </Button>
          </div>
        </CardContent>
      </Card>
      
      {/* 申请列表 */}
      <Card>
        <CardHeader>
          <CardTitle>配额申请列表</CardTitle>
          <CardDescription>
            共 {totalItems} 个申请，当前显示第 {currentPage} 页
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-4">
              <Skeleton className="h-8 w-full" />
              {Array(5).fill(0).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : applications.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              暂无符合条件的申请记录
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>申请编号</TableHead>
                    <TableHead>机构</TableHead>
                    <TableHead>申请人</TableHead>
                    <TableHead>配额类型</TableHead>
                    <TableHead>申请容量</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead>申请时间</TableHead>
                    <TableHead className="text-right">操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {applications.map((app) => (
                    <TableRow key={app.id}>
                      <TableCell className="font-mono">{app.applicationId}</TableCell>
                      <TableCell>{app.institutionName}</TableCell>
                      <TableCell>{app.applicantUsername}</TableCell>
                      <TableCell>{getQuotaTypeName(app.quotaType)}</TableCell>
                      <TableCell>{formatFileSize(app.requestedBytes)}</TableCell>
                      <TableCell>{getStatusBadge(app.status)}</TableCell>
                      <TableCell>{new Date(app.createdAt).toLocaleString()}</TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewDetail(app.id)}
                        >
                          <Info className="h-4 w-4 mr-2" />
                          详情
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
          
          {/* 分页 */}
          {totalPages > 1 && (
            <div className="mt-4">
              <Pagination>
                <PaginationContent>
                  <PaginationItem>
                    <PaginationPrevious 
                      href="#" 
                      onClick={(e) => {
                        e.preventDefault();
                        if (currentPage > 1) {
                          setCurrentPage(currentPage - 1);
                        }
                      }}
                      className={currentPage <= 1 ? 'pointer-events-none opacity-50' : ''}
                    />
                  </PaginationItem>
                  
                  {Array.from({ length: totalPages }, (_, i) => i + 1)
                    .filter(page => {
                      // 显示第一页、最后一页和当前页附近的页码
                      return page === 1 || page === totalPages || 
                        Math.abs(page - currentPage) <= 1;
                    })
                    .map((page, index, array) => {
                      // 添加省略号
                      if (index > 0 && array[index - 1] !== page - 1) {
                        return (
                          <PaginationItem key={`ellipsis-${page}`}>
                            <span className="px-4 py-2">...</span>
                          </PaginationItem>
                        );
                      }
                      
                      return (
                        <PaginationItem key={page}>
                          <PaginationLink
                            href="#"
                            onClick={(e) => {
                              e.preventDefault();
                              setCurrentPage(page);
                            }}
                            isActive={currentPage === page}
                          >
                            {page}
                          </PaginationLink>
                        </PaginationItem>
                      );
                    })}
                  
                  <PaginationItem>
                    <PaginationNext 
                      href="#" 
                      onClick={(e) => {
                        e.preventDefault();
                        if (currentPage < totalPages) {
                          setCurrentPage(currentPage + 1);
                        }
                      }}
                      className={currentPage >= totalPages ? 'pointer-events-none opacity-50' : ''}
                    />
                  </PaginationItem>
                </PaginationContent>
              </Pagination>
            </div>
          )}
        </CardContent>
      </Card>
      
      {/* 申请详情对话框 */}
      {selectedApplication && (
        <Dialog open={detailDialogOpen} onOpenChange={setDetailDialogOpen}>
          <DialogContent className="sm:max-w-[600px]">
            <DialogHeader>
              <DialogTitle>配额申请详情</DialogTitle>
              <DialogDescription>
                申请编号: {selectedApplication.applicationId}
              </DialogDescription>
            </DialogHeader>
            
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <Label className="text-muted-foreground">机构</Label>
                  <p className="font-medium">{selectedApplication.institutionName}</p>
                </div>
                
                <div className="space-y-1">
                  <Label className="text-muted-foreground">申请人</Label>
                  <p className="font-medium">{selectedApplication.applicantUsername}</p>
                </div>
                
                <div className="space-y-1">
                  <Label className="text-muted-foreground">配额类型</Label>
                  <p className="font-medium">{getQuotaTypeName(selectedApplication.quotaType)}</p>
                </div>
                
                <div className="space-y-1">
                  <Label className="text-muted-foreground">申请容量</Label>
                  <p className="font-medium">{formatFileSize(selectedApplication.requestedBytes)}</p>
                </div>
                
                <div className="space-y-1">
                  <Label className="text-muted-foreground">状态</Label>
                  <div>{getStatusBadge(selectedApplication.status)}</div>
                </div>
                
                <div className="space-y-1">
                  <Label className="text-muted-foreground">申请时间</Label>
                  <p className="font-medium">{new Date(selectedApplication.createdAt).toLocaleString()}</p>
                </div>
              </div>
              
              <div className="space-y-1">
                <Label className="text-muted-foreground">申请原因</Label>
                <div className="p-3 border rounded-md bg-muted/50">{selectedApplication.reason}</div>
              </div>
              
              {selectedApplication.status !== QuotaApplicationStatus.PENDING && (
                <>
                  <div className="space-y-1">
                    <Label className="text-muted-foreground">审核人</Label>
                    <p className="font-medium">{selectedApplication.reviewerUsername}</p>
                  </div>
                  
                  <div className="space-y-1">
                    <Label className="text-muted-foreground">审核时间</Label>
                    <p className="font-medium">
                      {selectedApplication.reviewedAt ? new Date(selectedApplication.reviewedAt).toLocaleString() : '-'}
                    </p>
                  </div>
                  
                  {selectedApplication.status === QuotaApplicationStatus.REJECTED && (
                    <div className="space-y-1">
                      <Label className="text-muted-foreground">拒绝原因</Label>
                      <div className="p-3 border rounded-md bg-muted/50">
                        {selectedApplication.reviewComment || '无'}
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
            
            <DialogFooter className="gap-2">
              {selectedApplication.status === QuotaApplicationStatus.PENDING && (
                <>
                  <Button 
                    variant="outline" 
                    onClick={() => setRejectDialogOpen(true)}
                  >
                    <X className="h-4 w-4 mr-2" />
                    拒绝
                  </Button>
                  <Button 
                    onClick={() => handleApprove(selectedApplication.id)}
                    disabled={isProcessing}
                  >
                    <Check className="h-4 w-4 mr-2" />
                    批准
                  </Button>
                </>
              )}
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
      
      {/* 拒绝原因对话框 */}
      {selectedApplication && (
        <Dialog open={rejectDialogOpen} onOpenChange={setRejectDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>拒绝申请</DialogTitle>
              <DialogDescription>
                请输入拒绝的原因，这将发送给申请人
              </DialogDescription>
            </DialogHeader>
            <div className="py-4">
              <Label htmlFor="reject-reason">拒绝原因</Label>
              <Textarea 
                id="reject-reason" 
                value={rejectReason} 
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="请输入拒绝原因"
                className="min-h-32 mt-2"
              />
            </div>
            <DialogFooter>
              <Button 
                variant="outline" 
                onClick={() => setRejectDialogOpen(false)}
              >
                取消
              </Button>
              <Button 
                variant="destructive" 
                onClick={() => handleReject(selectedApplication.id)}
                disabled={isProcessing || !rejectReason.trim()}
              >
                {isProcessing ? '处理中...' : '确认拒绝'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
} 