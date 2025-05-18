'use client';

import { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, Download, Maximize, ZoomIn, ZoomOut } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { cn } from '@/lib/utils';
import { MediaVO } from '@/types/learning';

interface CourseDocumentViewerProps {
  media: MediaVO;
  onProgress?: (progress: number) => void;
  initialProgress?: number;
}

export function CourseDocumentViewer({
  media,
  onProgress,
  initialProgress = 0
}: CourseDocumentViewerProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [zoom, setZoom] = useState(100);
  const [viewingTime, setViewingTime] = useState(0);
  const isPdf = media.type?.toLowerCase().includes('pdf');

  // 记录阅读时间和进度
  useEffect(() => {
    const interval = setInterval(() => {
      setViewingTime(prev => prev + 1);

      // 每30秒更新一次进度
      if (viewingTime > 0 && viewingTime % 30 === 0 && onProgress) {
        // 文档没有真正的进度，这里简单根据阅读时间计算进度
        // 假设5分钟为完成阅读
        const progressValue = Math.min(Math.round((viewingTime / 300) * 100), 100);
        onProgress(progressValue);
      }
    }, 1000);

    return () => {
      clearInterval(interval);
      // 退出时更新进度
      if (viewingTime > 0 && onProgress) {
        const progressValue = Math.min(Math.round((viewingTime / 300) * 100), 100);
        onProgress(progressValue);
      }
    };
  }, [viewingTime, onProgress]);

  // 处理加载
  const handleLoad = () => {
    setLoading(false);
    setError(null);
  };

  // 处理加载错误
  const handleError = () => {
    setLoading(false);
    setError('文档加载失败');
  };

  // 放大
  const zoomIn = () => {
    setZoom(prev => Math.min(prev + 25, 200));
  };

  // 缩小
  const zoomOut = () => {
    setZoom(prev => Math.max(prev - 25, 50));
  };

  // 下载文档
  const downloadDocument = () => {
    if (!media.accessUrl) return;

    const link = document.createElement('a');
    link.href = media.accessUrl;
    link.download = media.title || '文档下载';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // 全屏查看
  const viewFullscreen = () => {
    const viewer = document.getElementById('document-viewer');
    if (!viewer) return;

    if (document.fullscreenElement) {
      document.exitFullscreen().catch(err => {
        console.error('退出全屏失败:', err);
      });
    } else {
      viewer.requestFullscreen().catch(err => {
        console.error('进入全屏失败:', err);
      });
    }
  };

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-[500px] bg-background text-center">
        <div className="text-destructive text-lg mb-4">文档加载失败</div>
        <p className="text-muted-foreground">{error}</p>
        <Button
          variant="outline"
          className="mt-4"
          onClick={() => window.location.reload()}
        >
          重试
        </Button>
      </div>
    );
  }

  return (
    <div className="relative w-full h-full bg-background overflow-hidden">
      {/* 工具栏 */}
      <div className="bg-muted p-2 flex items-center justify-between border-b">
        <div className="flex items-center space-x-2">
          <TooltipProvider>
            {/* 缩小按钮 */}
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={zoomOut}
                  disabled={zoom <= 50}
                >
                  <ZoomOut className="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="bottom">
                <p>缩小</p>
              </TooltipContent>
            </Tooltip>

            {/* 缩放百分比 */}
            <span className="text-sm">{zoom}%</span>

            {/* 放大按钮 */}
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={zoomIn}
                  disabled={zoom >= 200}
                >
                  <ZoomIn className="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="bottom">
                <p>放大</p>
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        </div>

        <div className="flex items-center space-x-2">
          <TooltipProvider>
            {/* 下载按钮 */}
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={downloadDocument}
                >
                  <Download className="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="bottom">
                <p>下载文档</p>
              </TooltipContent>
            </Tooltip>

            {/* 全屏按钮 */}
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={viewFullscreen}
                >
                  <Maximize className="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="bottom">
                <p>全屏查看</p>
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        </div>
      </div>

      {/* 文档查看区域 */}
      <div
        id="document-viewer"
        className="w-full overflow-auto"
        style={{ height: '650px' }}
      >
        <div className="flex justify-center min-h-full">
          {isPdf ? (
            <iframe
              src={`${media.accessUrl}#view=FitH`}
              className={cn(
                "w-full h-full border-0",
                loading ? "hidden" : "block"
              )}
              style={{ transform: `scale(${zoom/100})`, transformOrigin: 'center top' }}
              onLoad={handleLoad}
              onError={handleError}
            />
          ) : (
            <iframe
              src={media.accessUrl}
              className={cn(
                "w-full h-full border-0",
                loading ? "hidden" : "block"
              )}
              style={{ transform: `scale(${zoom/100})`, transformOrigin: 'center top' }}
              onLoad={handleLoad}
              onError={handleError}
            />
          )}

          {/* 加载状态 */}
          {loading && (
            <div className="w-full flex flex-col items-center justify-center p-8">
              <Skeleton className="w-full h-[600px]" />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}