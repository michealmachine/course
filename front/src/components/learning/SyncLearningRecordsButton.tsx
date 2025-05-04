'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Loader2, RefreshCw } from 'lucide-react';
import { toast } from 'sonner';
import adminService from '@/services/admin-service';
import { useRouter } from 'next/navigation';

/**
 * 同步学习记录按钮组件
 * 用于手动触发将Redis中的学习记录同步到数据库
 */
export default function SyncLearningRecordsButton() {
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const handleSync = async () => {
    setIsLoading(true);
    try {
      await adminService.triggerLearningRecordAggregation();
      toast.success('学习记录同步成功');
      
      // 刷新页面以显示最新数据
      setTimeout(() => {
        router.refresh();
      }, 1000);
    } catch (error) {
      console.error('同步学习记录失败:', error);
      toast.error('同步学习记录失败，请稍后再试');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleSync}
      disabled={isLoading}
      className="flex items-center gap-1"
    >
      {isLoading ? (
        <Loader2 className="h-4 w-4 animate-spin" />
      ) : (
        <RefreshCw className="h-4 w-4" />
      )}
      同步学习记录
    </Button>
  );
}
