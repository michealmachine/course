'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Loader2, RefreshCw } from 'lucide-react';
import { useToast } from '@/components/ui/use-toast';
import adminService from '@/services/admin-service';

/**
 * 同步学习记录按钮组件
 * 用于管理员手动触发将Redis中的学习记录同步到数据库
 */
export function SyncLearningRecordsButton() {
  const [isSyncing, setIsSyncing] = useState(false);
  const { toast } = useToast();

  const handleSyncRecords = async () => {
    setIsSyncing(true);
    try {
      await adminService.triggerLearningRecordAggregation();
      toast({
        title: '学习记录同步成功',
        description: '已将Redis中的学习记录同步到数据库',
        variant: 'default',
      });
    } catch (error) {
      console.error('同步学习记录失败:', error);
      toast({
        title: '同步学习记录失败',
        description: '请稍后重试或联系系统管理员',
        variant: 'destructive',
      });
    } finally {
      setIsSyncing(false);
    }
  };

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleSyncRecords}
      disabled={isSyncing}
      className="ml-2"
    >
      <RefreshCw className={`h-4 w-4 mr-2 ${isSyncing ? 'animate-spin' : ''}`} />
      {isSyncing ? '同步中...' : '同步学习记录'}
    </Button>
  );
}
