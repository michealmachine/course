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
  Save
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
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
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { storageService } from '@/services/storage-service';
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

// 存储配额页面
export default function StoragePage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [quotas, setQuotas] = useState<StorageQuota[]>([]);
  const [activeTab, setActiveTab] = useState('overview');
  const [useRequestDialogOpen, setUseRequestDialogOpen] = useState(false);
  const [requestAmount, setRequestAmount] = useState(1); // 默认请求1GB
  
  // 加载配额数据
  useEffect(() => {
    fetchQuotas();
  }, []);
  
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
            onClick={fetchQuotas}
            disabled={isLoading}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            刷新
          </Button>
          <Dialog open={useRequestDialogOpen} onOpenChange={setUseRequestDialogOpen}>
            <DialogTrigger asChild>
              <Button size="sm">
                <HardDrive className="h-4 w-4 mr-2" />
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
                <div className="grid gap-2">
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
                <div className="grid gap-2">
                  <Label htmlFor="reason">申请原因</Label>
                  <Input id="reason" placeholder="请简要说明申请原因" />
                </div>
              </div>
              
              <DialogFooter>
                <Button variant="outline" onClick={() => setUseRequestDialogOpen(false)}>
                  取消
                </Button>
                <Button 
                  type="submit" 
                  onClick={() => {
                    toast.success(`存储空间申请已提交，申请${requestAmount}GB空间`);
                    setUseRequestDialogOpen(false);
                  }}
                >
                  提交申请
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
      </Tabs>
    </div>
  );
} 