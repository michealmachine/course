'use client';

import { useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';

export default function EditSectionPageRedirect() {
  const router = useRouter();
  const params = useParams();
  const courseId = params.id;
  const chapterId = params.chapterId;
  const sectionId = params.sectionId;
  
  useEffect(() => {
    // 重定向到小节详情页面
    router.push(`/dashboard/courses/${courseId}/chapters/${chapterId}/sections/${sectionId}`);
  }, [courseId, chapterId, sectionId, router]);
  
  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <Loader2 className="h-8 w-8 animate-spin text-primary mb-4" />
      <p className="text-muted-foreground">正在重定向到小节详情页面...</p>
    </div>
  );
} 