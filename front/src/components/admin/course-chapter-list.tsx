'use client';

import { useState, useEffect } from 'react';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/accordion';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Loader2, Video, FileText, Play, Clock } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { ApiResponse } from '@/types/api';

interface ChapterVO {
  id: number;
  title: string;
  description: string;
  sort: number;
  sections: SectionVO[];
}

interface SectionVO {
  id: number;
  title: string;
  description: string;
  sort: number;
  duration: number;
  type: number; // 1-视频, 2-文档, 3-练习
  isFree: boolean;
}

interface CourseChapterListProps {
  courseId: number;
}

export function CourseChapterList({ courseId }: CourseChapterListProps) {
  const [chapters, setChapters] = useState<ChapterVO[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (courseId) {
      fetchChapters();
    }
  }, [courseId]);

  const fetchChapters = async () => {
    setIsLoading(true);
    try {
      const response = await request.get<ChapterVO[]>(`/admin/courses/${courseId}/chapters`);

      if (response.data.code === 200 && response.data.data) {
        setChapters(response.data.data);
      } else {
        toast.error('获取章节列表失败');
      }
    } catch (error) {
      console.error('获取章节列表出错:', error);
      toast.error('获取章节列表出错');
    } finally {
      setIsLoading(false);
    }
  };

  const formatDuration = (seconds: number) => {
    if (!seconds) return '0分钟';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}小时${minutes > 0 ? ` ${minutes}分钟` : ''}`;
    }
    return `${minutes}分钟`;
  };

  const getSectionTypeIcon = (type: number) => {
    switch (type) {
      case 1: return <Video className="h-4 w-4" />;
      case 2: return <FileText className="h-4 w-4" />;
      case 3: return <Play className="h-4 w-4" />;
      default: return <FileText className="h-4 w-4" />;
    }
  };

  const getSectionTypeText = (type: number) => {
    switch (type) {
      case 1: return '视频';
      case 2: return '文档';
      case 3: return '练习';
      default: return '未知';
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="space-y-2">
            <Skeleton className="h-10 w-full" />
            <div className="pl-6 space-y-2">
              {Array.from({ length: 3 }).map((_, j) => (
                <Skeleton key={j} className="h-8 w-full" />
              ))}
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (chapters.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
        <FileText className="h-12 w-12 mb-4" />
        <p>该课程暂无章节内容</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-medium">章节内容</h3>

      <Accordion type="multiple" className="w-full">
        {chapters.map((chapter) => (
          <AccordionItem key={chapter.id} value={chapter.id.toString()}>
            <AccordionTrigger className="hover:bg-muted/50 px-4 py-2 rounded-md">
              <div className="flex items-center gap-2">
                <span className="font-medium">第 {chapter.sort} 章: {chapter.title}</span>
                <Badge variant="outline" className="ml-2">
                  {chapter.sections.length} 小节
                </Badge>
              </div>
            </AccordionTrigger>
            <AccordionContent className="px-4">
              <div className="pl-4 border-l space-y-2 py-2">
                {chapter.description && (
                  <p className="text-sm text-muted-foreground mb-2">{chapter.description}</p>
                )}

                {chapter.sections.map((section) => (
                  <div
                    key={section.id}
                    className="flex items-center justify-between p-2 rounded-md hover:bg-muted/50"
                  >
                    <div className="flex items-center gap-2">
                      <div className="text-muted-foreground">
                        {getSectionTypeIcon(section.type)}
                      </div>
                      <span className="text-sm">{section.title}</span>
                      {section.isFree && (
                        <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200 text-xs">
                          免费
                        </Badge>
                      )}
                    </div>
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      <Badge variant="outline" className="text-xs">
                        {getSectionTypeText(section.type)}
                      </Badge>
                      <div className="flex items-center">
                        <Clock className="h-3 w-3 mr-1" />
                        {formatDuration(section.duration)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </AccordionContent>
          </AccordionItem>
        ))}
      </Accordion>
    </div>
  );
}
