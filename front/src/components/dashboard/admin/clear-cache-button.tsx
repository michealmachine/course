'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { RefreshCw } from 'lucide-react';
import { useToast } from '@/components/ui/use-toast';
import adminCacheService from '@/services/admin-cache-service';

/**
 * 清除缓存按钮组件
 * 用于管理员清除系统缓存
 */
export function ClearCacheButton() {
  const [isClearing, setIsClearing] = useState(false);
  const { toast } = useToast();

  const handleClearCache = async () => {
    setIsClearing(true);
    try {
      const clearedCaches = await adminCacheService.clearAllCaches();
      toast({
        title: '缓存清除成功',
        description: `已清除 ${clearedCaches.length} 个缓存: ${clearedCaches.join(', ')}`,
        variant: 'default',
      });
    } catch (error) {
      console.error('清除缓存失败:', error);
      toast({
        title: '清除缓存失败',
        description: '请稍后重试或联系系统管理员',
        variant: 'destructive',
      });
    } finally {
      setIsClearing(false);
    }
  };

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleClearCache}
      disabled={isClearing}
    >
      <RefreshCw className={`h-4 w-4 mr-2 ${isClearing ? 'animate-spin' : ''}`} />
      {isClearing ? '清除中...' : '清除缓存'}
    </Button>
  );
}
