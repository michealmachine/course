'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  Database,
  HardDrive,
  FileText,
  PieChart, 
  BarChart,
  RefreshCw,
  Save,
  Plus,
  Filter,
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

import { storageService } from '@/services/storage-service';
import quotaApplicationService from '@/services/quota-application';
import { 
  QuotaApplicationVO, 
  QuotaApplicationStatus, 
  QuotaType,
  QuotaUsage
} from '@/types/quota';
import type { QuotaInfoVO } from '@/types/api';

// 配额类型
const QUOTA_TYPES = {
  VIDEO: 'VIDEO',
  AUDIO: 'AUDIO',
  IMAGE: 'IMAGE',
  DOCUMENT: 'DOCUMENT',
  TOTAL: 'TOTAL'
};

// 存储配额信息
interface StorageQuota {
  id: number;
  type: string;
  totalSpace: number; // 字节数
  usedSpace: number; // 字节数
  institutionId: number;
  createdAt: string;
  updatedAt: string;
}

// 将API返回的配额信息转换为页面使用的格式
const convertQuotaInfo = (apiQuota: QuotaInfoVO): StorageQuota => ({
  id: apiQuota.type === QUOTA_TYPES.TOTAL ? 5 : Math.floor(Math.random() * 1000),
  type: apiQuota.type,
  totalSpace: apiQuota.totalQuota,
  usedSpace: apiQuota.usedQuota,
  institutionId: 1, // 从用户信息或上下文中获取
  createdAt: new Date().toISOString(),
  updatedAt: apiQuota.lastUpdatedTime
});

export default function StoragePage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [quotas, setQuotas] = useState<StorageQuota[]>([]);
  const [activeTab, setActiveTab] = useState('overview');
  
  // 申请存储空间对话框状态
  const [requestDialogOpen, setRequestDialogOpen] = useState(false);
  const [requestAmount, setRequestAmount] = useState(1); // 默认请求1GB
  const [requestReason, setRequestReason] = useState('');
  const [selectedQuotaType, setSelectedQuotaType] = useState(QUOTA_TYPES.TOTAL);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // 申请记录相关状态
  const [applications, setApplications] = useState<QuotaApplicationVO[]>([]);
  const [isLoadingApplications, setIsLoadingApplications] = useState(true);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(5);
  
  // 加载配额数据
  useEffect(() => {
    fetchQuotas();
  }, []);
  
  // 标签切换时加载申请记录
  useEffect(() => {
    if (activeTab === "applications") {
      fetchApplications();
    }
  }, [activeTab, currentPage]);
  
  // 获取配额信息
  const fetchQuotas = async () => {
    setIsLoading(true);
    try {
      const response = await storageService.getCurrentQuotas();
      if (response.data) {
        const convertedQuotas = response.data.map(convertQuotaInfo);
        setQuotas(convertedQuotas);
      }
      setIsLoading(false);
    } catch (error) {
      console.error('获取存储配额失败:', error);
      toast.error('获取存储配额信息失败');
      setIsLoading(false);
    }
  };
  
  // 获取配额申请记录
  const fetchApplications = async () => {
    setIsLoadingApplications(true);
    try {
      const response = await quotaApplicationService.getMyApplications({
        pageNum: currentPage,
        pageSize,
      });
      setApplications(response.content);
      setTotalPages(response.totalPages);
      setTotalItems(response.totalElements);
      setIsLoadingApplications(false);
    } catch (error) {
      console.error('获取配额申请记录失败:', error);
      toast.error('获取配额申请记录失败');
      setIsLoadingApplications(false);
    }
  };
  
  // 获取总配额
  const getTotalQuota = () => {
    return quotas.find(quota => quota.type === QUOTA_TYPES.TOTAL);
  };
  
  // 获取配额类型名称
  const getQuotaTypeName = (type: string): string => {
    switch (type) {
      case QUOTA_TYPES.VIDEO: return '视频';
      case QUOTA_TYPES.AUDIO: return '音频';
      case QUOTA_TYPES.IMAGE: return '图片';
      case QUOTA_TYPES.DOCUMENT: return '文档';
      case QUOTA_TYPES.TOTAL: return '总配额';
      default: return '未知';
    }
  };
  
  // 获取配额类型图标
  const getQuotaTypeIcon = (type: string) => {
    switch (type) {
      case QUOTA_TYPES.VIDEO: return <HardDrive className="h-6 w-6 text-blue-500" />;
      case QUOTA_TYPES.AUDIO: return <HardDrive className="h-6 w-6 text-yellow-500" />;
      case QUOTA_TYPES.IMAGE: return <HardDrive className="h-6 w-6 text-green-500" />;
      case QUOTA_TYPES.DOCUMENT: return <FileText className="h-6 w-6 text-red-500" />;
      case QUOTA_TYPES.TOTAL: return <Database className="h-6 w-6 text-purple-500" />;
      default: return <HardDrive className="h-6 w-6 text-gray-500" />;
    }
  };
  
  // 格式化文件大小
  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  };
  
  // 计算使用百分比
  const calculateUsagePercentage = (quota: StorageQuota): number => {
    if (quota.totalSpace === 0) return 0;
    return Math.min(100, Math.round((quota.usedSpace / quota.totalSpace) * 100));
  };
  
  // 获取使用百分比的颜色
  const getUsageColor = (percentage: number): string => {
    if (percentage < 60) return 'bg-green-500';
    if (percentage < 80) return 'bg-yellow-500';
    return 'bg-red-500';
  };
  
  // 获取申请状态文本
  const getStatusText = (status: QuotaApplicationStatus): string => {
    switch (status) {
      case QuotaApplicationStatus.PENDING: return '待审核';
      case QuotaApplicationStatus.APPROVED: return '已通过';
      case QuotaApplicationStatus.REJECTED: return '已拒绝';
      case QuotaApplicationStatus.CANCELED: return '已取消';
      default: return '未知';
    }
  };
  
  // 提交配额申请
  const handleSubmitApplication = async () => {
    if (!requestReason.trim()) {
      toast.error('请输入申请原因');
      return;
    }
    
    setIsSubmitting(true);
    try {
      // 转换GB为字节
      const requestedBytes = requestAmount * 1024 * 1024 * 1024;
      
      // 映射配额类型
      let quotaType: QuotaType;
      switch (selectedQuotaType) {
        case QUOTA_TYPES.VIDEO:
          quotaType = QuotaType.VIDEO;
          break;
        case QUOTA_TYPES.DOCUMENT:
          quotaType = QuotaType.DOCUMENT;
          break;
        case QUOTA_TYPES.TOTAL:
        default:
          quotaType = QuotaType.TOTAL;
          break;
      }
      
      await quotaApplicationService.createApplication({
        quotaType,
        requestedBytes,
        reason: requestReason.trim()
      });
      
      toast.success('配额申请已提交');
      setRequestDialogOpen(false);
      
      // 重置表单
      setSelectedQuotaType(QUOTA_TYPES.TOTAL);
      setRequestAmount(1);
      setRequestReason('');
      
      // 如果当前是在申请记录标签，刷新列表
      if (activeTab === "applications") {
        fetchApplications();
      }
    } catch (error) {
      console.error('提交配额申请失败:', error);
      toast.error('提交配额申请失败');
    } finally {
      setIsSubmitting(false);
    }
  };
  
  // 取消申请
  const handleCancelApplication = async (id: number) => {
    try {
      await quotaApplicationService.cancelApplication(id);
      toast.success('申请已取消');
      fetchApplications();
    } catch (error) {
      console.error(`取消申请失败, ID: ${id}:`, error);
      toast.error('取消申请失败');
    }
  };
  
  // 同步刷新按钮的功能
  const handleRefresh = () => {
    if (activeTab === 'applications') {
      fetchApplications();
    } else {
      fetchQuotas();
    }
  };
  
  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">存储配额管理</h1>
          <p className="text-muted-foreground">监控和管理您的存储空间使用情况</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={handleRefresh}
            disabled={isLoading || isLoadingApplications}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${(isLoading || isLoadingApplications) ? 'animate-spin' : ''}`} />
            刷新
          </Button>
          <Dialog open={requestDialogOpen} onOpenChange={setRequestDialogOpen}>
            <DialogTrigger asChild>
              <Button size="sm">
                <Plus className="h-4 w-4 mr-2" />
                申请存储空间
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
              <DialogHeader>
                <DialogTitle>申请额外存储空间</DialogTitle>
                <DialogDescription>
                  您可以向系统管理员申请更多的存储空间。请填写您需要的存储空间大小。
                </DialogDescription>
              </DialogHeader>
              
              <div className="grid gap-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="quota-type">配额类型</Label>
                  <Select
                    value={selectedQuotaType}
                    onValueChange={setSelectedQuotaType}
                  >
                    <SelectTrigger id="quota-type">
                      <SelectValue placeholder="选择配额类型" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={QUOTA_TYPES.TOTAL}>总配额</SelectItem>
                      <SelectItem value={QUOTA_TYPES.VIDEO}>视频配额</SelectItem>
                      <SelectItem value={QUOTA_TYPES.DOCUMENT}>文档配额</SelectItem>
                      <SelectItem value={QUOTA_TYPES.AUDIO}>音频配额</SelectItem>
                      <SelectItem value={QUOTA_TYPES.IMAGE}>图片配额</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="amount">申请空间大小 (GB)</Label>
                  <Input 
                    id="amount" 
                    type="number" 
                    min="1" 
                    max="100" 
                    value={requestAmount} 
                    onChange={(e) => setRequestAmount(parseInt(e.target.value) || 1)}
                  />
                  <p className="text-sm text-muted-foreground">
                    当前总配额: {formatFileSize(getTotalQuota()?.totalSpace || 0)}
                  </p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="reason">申请原因</Label>
                  <Textarea
                    id="reason"
                    value={requestReason}
                    onChange={(e) => setRequestReason(e.target.value)}
                    placeholder="请详细说明申请增加配额的原因和用途"
                    className="min-h-32"
                  />
                </div>
              </div>
              
              <DialogFooter>
                <Button 
                  variant="outline" 
                  onClick={() => setRequestDialogOpen(false)}
                >
                  取消
                </Button>
                <Button 
                  onClick={handleSubmitApplication}
                  disabled={isSubmitting}
                >
                  {isSubmitting ? '提交中...' : '提交申请'}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </div>
      
      <Tabs defaultValue="overview" value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="overview">概览</TabsTrigger>
          <TabsTrigger value="details">详细信息</TabsTrigger>
          <TabsTrigger value="history">使用历史</TabsTrigger>
          <TabsTrigger value="applications">申请记录</TabsTrigger>
        </TabsList>
        
        <TabsContent value="overview" className="mt-6">
          {isLoading ? (
            <div className="space-y-4">
              <Card>
                <CardHeader className="p-4">
                  <Skeleton className="h-6 w-32" />
                  <Skeleton className="h-4 w-24" />
                </CardHeader>
                <CardContent className="p-4 pt-0">
                  <Skeleton className="h-8 w-full mb-2" />
                  <Skeleton className="h-4 w-1/3" />
                </CardContent>
              </Card>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {[1, 2, 3, 4].map((i) => (
                  <Card key={i}>
                    <CardHeader className="p-4">
                      <Skeleton className="h-5 w-24" />
                    </CardHeader>
                    <CardContent className="p-4 pt-0">
                      <Skeleton className="h-6 w-full mb-2" />
                      <Skeleton className="h-4 w-1/2" />
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              {/* 总配额概览 */}
              <Card>
                <CardHeader className="pb-2">
                  <div className="flex items-center gap-3">
                    <Database className="h-8 w-8 text-purple-500" />
                    <div>
                      <CardTitle>总存储配额</CardTitle>
                      <CardDescription>
                        您的机构总共分配了 {formatFileSize(getTotalQuota()?.totalSpace || 0)} 的存储空间
                      </CardDescription>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="pb-2">
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>已使用 {formatFileSize(getTotalQuota()?.usedSpace || 0)}</span>
                      <span>{calculateUsagePercentage(getTotalQuota() || { totalSpace: 1, usedSpace: 0 } as StorageQuota)}%</span>
                    </div>
                    <Progress 
                      value={calculateUsagePercentage(getTotalQuota() || { totalSpace: 1, usedSpace: 0 } as StorageQuota)} 
                      className="h-2.5"
                    />
                    <p className="text-sm text-muted-foreground">
                      剩余 {formatFileSize((getTotalQuota()?.totalSpace || 0) - (getTotalQuota()?.usedSpace || 0))}
                    </p>
                  </div>
                </CardContent>
              </Card>
              
              {/* 各类型配额概览 */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {quotas.filter(quota => quota.type !== QUOTA_TYPES.TOTAL).map(quota => (
                  <Card key={quota.id}>
                    <CardHeader className="pb-2">
                      <div className="flex items-center gap-2">
                        {getQuotaTypeIcon(quota.type)}
                        <CardTitle className="text-base">{getQuotaTypeName(quota.type)}存储</CardTitle>
                      </div>
                    </CardHeader>
                    <CardContent className="pb-2">
                      <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span>{formatFileSize(quota.usedSpace)} / {formatFileSize(quota.totalSpace)}</span>
                          <span>{calculateUsagePercentage(quota)}%</span>
                        </div>
                        <Progress 
                          value={calculateUsagePercentage(quota)} 
                          className={`h-2 ${getUsageColor(calculateUsagePercentage(quota))}`}
                        />
                      </div>
                    </CardContent>
                    <CardFooter className="pt-0 pb-2">
                      <p className="text-xs text-muted-foreground">
                        剩余 {formatFileSize(quota.totalSpace - quota.usedSpace)}
                      </p>
                    </CardFooter>
                  </Card>
                ))}
              </div>
            </div>
          )}
        </TabsContent>
        
        <TabsContent value="details" className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle>配额详细信息</CardTitle>
              <CardDescription>
                查看每种资源类型的存储配额详情和使用情况
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="space-y-4">
                  {[1, 2, 3, 4, 5].map((i) => (
                    <div key={i} className="flex items-center gap-4 p-2 border-b">
                      <Skeleton className="h-8 w-8 rounded-full" />
                      <div className="space-y-2 flex-1">
                        <Skeleton className="h-4 w-32" />
                        <Skeleton className="h-2 w-full" />
                      </div>
                      <Skeleton className="h-4 w-16" />
                    </div>
                  ))}
                </div>
              ) : (
                <div className="space-y-4">
                  {quotas.map(quota => (
                    <div key={quota.id} className="flex items-start gap-4 p-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-900 transition-colors">
                      <div className="pt-1">
                        {getQuotaTypeIcon(quota.type)}
                      </div>
                      <div className="flex-1 space-y-2">
                        <div className="flex justify-between items-center">
                          <h3 className="font-medium">{getQuotaTypeName(quota.type)}</h3>
                          <span className="text-sm">{calculateUsagePercentage(quota)}% 已使用</span>
                        </div>
                        <Progress 
                          value={calculateUsagePercentage(quota)} 
                          className={`h-2 ${getUsageColor(calculateUsagePercentage(quota))}`}
                        />
                        <div className="flex justify-between text-sm text-muted-foreground">
                          <span>已使用: {formatFileSize(quota.usedSpace)}</span>
                          <span>总计: {formatFileSize(quota.totalSpace)}</span>
                        </div>
                        <p className="text-xs text-muted-foreground">
                          最后更新: {new Date(quota.updatedAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
        
        <TabsContent value="history" className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle>使用历史</CardTitle>
              <CardDescription>
                查看存储空间的历史使用情况和趋势
              </CardDescription>
            </CardHeader>
            <CardContent className="relative h-96 flex items-center justify-center">
              <div className="text-center">
                <BarChart className="mx-auto h-12 w-12 text-muted-foreground mb-3" />
                <h3 className="text-lg font-medium">数据分析图表</h3>
                <p className="text-sm text-muted-foreground max-w-md mx-auto mt-1">
                  此功能将在后续版本中推出。您将能够查看存储使用的趋势和分析报告。
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
        
        {/* 新增申请记录标签页 */}
        <TabsContent value="applications">
          <Card>
            <CardHeader>
              <CardTitle>配额申请记录</CardTitle>
              <CardDescription>
                共 {totalItems} 条记录，当前显示第 {currentPage} 页
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoadingApplications ? (
                <div className="space-y-4">
                  <Skeleton className="h-10 w-full" />
                  {Array(3).fill(0).map((_, i) => (
                    <Skeleton key={i} className="h-16 w-full" />
                  ))}
                </div>
              ) : applications.length === 0 ? (
                <div className="text-center py-10 text-muted-foreground">
                  暂无申请记录
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>申请编号</TableHead>
                        <TableHead>配额类型</TableHead>
                        <TableHead>申请容量</TableHead>
                        <TableHead>申请时间</TableHead>
                        <TableHead>状态</TableHead>
                        <TableHead>审核意见</TableHead>
                        <TableHead>操作</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {applications.map((app) => (
                        <TableRow key={app.id}>
                          <TableCell className="font-mono">{app.applicationId}</TableCell>
                          <TableCell>{getQuotaTypeName(
                            app.quotaType === QuotaType.TOTAL ? QUOTA_TYPES.TOTAL :
                            app.quotaType === QuotaType.VIDEO ? QUOTA_TYPES.VIDEO :
                            app.quotaType === QuotaType.DOCUMENT ? QUOTA_TYPES.DOCUMENT :
                            '未知'
                          )}</TableCell>
                          <TableCell>{formatFileSize(app.requestedBytes)}</TableCell>
                          <TableCell>{new Date(app.createdAt).toLocaleString()}</TableCell>
                          <TableCell>{getStatusText(app.status)}</TableCell>
                          <TableCell>
                            {app.status === QuotaApplicationStatus.REJECTED ? app.reviewComment || '无' : '-'}
                          </TableCell>
                          <TableCell>
                            {app.status === QuotaApplicationStatus.PENDING && (
                              <Button 
                                variant="outline" 
                                size="sm"
                                onClick={() => handleCancelApplication(app.id)}
                              >
                                取消
                              </Button>
                            )}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
              
              {/* 分页控件 */}
              {totalPages > 1 && (
                <div className="flex justify-center mt-4 gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                    disabled={currentPage === 1}
                  >
                    上一页
                  </Button>
                  <span className="py-2 px-4">
                    {currentPage} / {totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                    disabled={currentPage === totalPages}
                  >
                    下一页
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
} 