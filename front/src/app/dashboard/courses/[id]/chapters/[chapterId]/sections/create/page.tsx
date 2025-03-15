'use client';

import { useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';

export default function CreateSectionPageRedirect() {
  const router = useRouter();
  const params = useParams();
  const courseId = params.id;
  const chapterId = params.chapterId;
  
  useEffect(() => {
    // 重定向到章节页面
    router.push(`/dashboard/courses/${courseId}/chapters/${chapterId}`);
  }, [courseId, chapterId, router]);
  
  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <Loader2 className="h-8 w-8 animate-spin text-primary mb-4" />
      <p className="text-muted-foreground">正在重定向到章节页面...</p>
    </div>
  );
} 