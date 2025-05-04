'use client';

import { Database } from 'lucide-react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { useEffect, useState } from 'react';
import { storageService } from '@/services/storage-service';
import { Skeleton } from '@/components/ui/skeleton';
import { useAuth } from '@/hooks/use-auth';

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 获取使用百分比的颜色
const getUsageColor = (percentage: number): string => {
  if (percentage > 90) return 'bg-red-100 dark:bg-red-900';
  if (percentage > 70) return 'bg-amber-100 dark:bg-amber-900';
  return 'bg-emerald-100 dark:bg-emerald-900';
};

export function StorageQuotaCard() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [totalQuota, setTotalQuota] = useState<{
    totalQuota: number;
    usedQuota: number;
    usagePercentage: number;
  }>({
    totalQuota: 0,
    usedQuota: 0,
    usagePercentage: 0
  });

  useEffect(() => {
    fetchQuotaStats();
  }, []);

  const fetchQuotaStats = async () => {
    if (!user?.institutionId) return;
    
    setLoading(true);
    try {
      const response = await storageService.getQuotaStats(user.institutionId);
      if (response.data && response.data.totalQuota) {
        setTotalQuota({
          totalQuota: response.data.totalQuota.totalQuota,
          usedQuota: response.data.totalQuota.usedQuota,
          usagePercentage: response.data.totalQuota.usagePercentage
        });
      }
    } catch (error) {
      console.error('获取存储配额统计失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center gap-3">
          <Database className="h-6 w-6 text-purple-500" />
          <div>
            <CardTitle>存储配额</CardTitle>
            <CardDescription>
              您的机构存储空间使用情况
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="pb-2">
        {loading ? (
          <div className="space-y-2">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-2 w-full" />
          </div>
        ) : (
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span>已使用 {formatFileSize(totalQuota.usedQuota)}</span>
              <span>{totalQuota.usagePercentage.toFixed(1)}%</span>
            </div>
            <Progress
              value={totalQuota.usagePercentage}
              className={`h-2 ${getUsageColor(totalQuota.usagePercentage)}`}
            />
          </div>
        )}
      </CardContent>
      <CardFooter className="pt-0 pb-2">
        <p className="text-xs text-muted-foreground">
          剩余 {formatFileSize(totalQuota.totalQuota - totalQuota.usedQuota)}
        </p>
      </CardFooter>
    </Card>
  );
}
