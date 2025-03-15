'use client';

import { useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';

/**
 * 小节列表页面 - 重定向到章节页面
 * 
 * 由于我们现在在章节页面使用折叠式展示小节，
 * 这个页面已经不再需要，重定向到章节页面。
 */
export default function ChapterSectionsPageRedirect() {
  const params = useParams();
  const router = useRouter();
  
  const courseId = Number(params.id);
  
  // 页面加载时立即重定向到章节页面
  useEffect(() => {
    router.replace(`/dashboard/courses/${courseId}/chapters`);
  }, [courseId, router]);
  
  // 渲染简单的加载状态
  return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <div className="text-center">
        <p className="text-muted-foreground">正在重定向...</p>
      </div>
    </div>
  );
} 