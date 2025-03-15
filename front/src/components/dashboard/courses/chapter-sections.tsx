'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Chapter, Section } from '@/types/course';
import { sectionService } from '@/services';
import { 
  ChevronDown, 
  ChevronRight, 
  Edit, 
  Trash2, 
  File, 
  Video, 
  FileText, 
  Headphones,
  Image as ImageIcon,
  FileCode,
  Plus,
  Loader2,
  BookOpen
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { SectionDialog } from '@/components/dashboard/sections/section-dialog';
import { SectionFormValues } from '@/components/dashboard/sections/section-form';
import { toast } from 'sonner';
import useDebounce from '@/hooks/useDebounce';

// 内容类型图标映射
const contentTypeIcons = {
  video: <Video className="h-4 w-4 mr-2" />,
  document: <FileText className="h-4 w-4 mr-2" />,
  audio: <Headphones className="h-4 w-4 mr-2" />,
  text: <FileCode className="h-4 w-4 mr-2" />,
  image: <ImageIcon className="h-4 w-4 mr-2" />,
  mixed: <File className="h-4 w-4 mr-2" />,
  default: <File className="h-4 w-4 mr-2" />
};

interface ChapterSectionsProps {
  chapter: Chapter;
  courseId: number;
  expanded?: boolean;
  onChapterClick?: (chapter: Chapter) => void;
  onSectionCreated?: () => void;
  onSectionUpdated?: () => void;
}

export function ChapterSections({ 
  chapter, 
  courseId, 
  expanded = false,
  onChapterClick,
  onSectionCreated,
  onSectionUpdated
}: ChapterSectionsProps) {
  const router = useRouter();
  const [isExpanded, setIsExpanded] = useState(expanded);
  const [sections, setSections] = useState<Section[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedSection, setSelectedSection] = useState<Section | null>(null);
  
  // 章节ID防抖，避免频繁请求
  const debouncedChapterId = useDebounce(chapter.id, 500);

  // 根据展开状态加载小节
  useEffect(() => {
    // 仅在展开状态且还没有加载小节时加载数据
    if (isExpanded && (!sections.length || debouncedChapterId !== chapter.id)) {
      loadSections();
    }
  }, [isExpanded, debouncedChapterId]);

  // 加载小节数据
  const loadSections = async () => {
    try {
      setError(null);
      setIsLoading(true);
      const data = await sectionService.getSectionsByChapter(chapter.id);
      setSections(data);
    } catch (err: any) {
      console.error(`加载章节 ${chapter.id} 的小节失败:`, err);
      setError(err.message || '无法加载小节');
      toast.error('获取小节列表失败', {
        description: err.message || '请稍后重试'
      });
    } finally {
      setIsLoading(false);
    }
  };

  // 切换展开/折叠状态
  const toggleExpanded = () => {
    setIsExpanded(!isExpanded);
  };

  // 点击章节
  const handleChapterClick = () => {
    toggleExpanded();
    if (onChapterClick) {
      onChapterClick(chapter);
    }
  };

  // 点击小节，导航到小节详情页
  const handleSectionClick = (section: Section) => {
    router.push(`/dashboard/courses/${courseId}/chapters/${chapter.id}/sections/${section.id}`);
  };

  // 点击编辑小节，打开编辑弹窗
  const handleEditSection = (e: React.MouseEvent, section: Section) => {
    e.stopPropagation(); // 阻止冒泡到小节点击
    setSelectedSection(section);
    setIsEditDialogOpen(true);
  };

  // 创建新小节，打开创建弹窗
  const handleCreateSection = (e: React.MouseEvent) => {
    e.stopPropagation(); // 阻止冒泡到章节点击
    setIsCreateDialogOpen(true);
  };

  // 处理小节创建提交
  const handleCreateSubmit = async (values: SectionFormValues) => {
    try {
      await sectionService.createSection({
        ...values,
        chapterId: chapter.id
      });
      
      toast.success('小节已创建', {
        description: '小节已成功添加到课程中'
      });
      
      // 重新加载小节列表
      loadSections();
      
      // 通知父组件
      if (onSectionCreated) {
        onSectionCreated();
      }
    } catch (error: any) {
      console.error('创建小节失败:', error);
      throw error;
    }
  };

  // 处理小节编辑提交
  const handleEditSubmit = async (values: SectionFormValues) => {
    if (!selectedSection) return;
    
    try {
      await sectionService.updateSection(selectedSection.id, {
        ...values,
        chapterId: chapter.id
      });
      
      toast.success('小节已更新', {
        description: '小节信息已成功保存'
      });
      
      // 重新加载小节列表
      loadSections();
      
      // 通知父组件
      if (onSectionUpdated) {
        onSectionUpdated();
      }
    } catch (error: any) {
      console.error('更新小节失败:', error);
      throw error;
    }
  };

  // 渲染内容类型图标
  const getContentTypeIcon = (contentType: string) => {
    return contentTypeIcons[contentType as keyof typeof contentTypeIcons] || contentTypeIcons.default;
  };

  return (
    <>
      <Card className="mb-4 border shadow-sm">
        <CardHeader 
          className={`flex flex-row items-center justify-between p-4 cursor-pointer ${isExpanded ? 'border-b' : ''}`}
          onClick={handleChapterClick}
        >
          <div className="flex items-center">
            {isExpanded ? 
              <ChevronDown className="h-5 w-5 mr-2 text-muted-foreground" /> : 
              <ChevronRight className="h-5 w-5 mr-2 text-muted-foreground" />
            }
            <div>
              <CardTitle className="text-lg font-medium">{chapter.title}</CardTitle>
              {chapter.description && (
                <p className="text-sm text-muted-foreground mt-1 line-clamp-1">
                  {chapter.description}
                </p>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            {chapter.estimatedMinutes && (
              <Badge variant="outline" className="text-xs">
                <BookOpen className="h-3 w-3 mr-1" />
                {chapter.estimatedMinutes}分钟
              </Badge>
            )}
            <Button 
              variant="ghost" 
              size="icon"
              onClick={(e) => handleCreateSection(e)}
            >
              <Plus className="h-4 w-4" />
            </Button>
          </div>
        </CardHeader>
        
        {isExpanded && (
          <CardContent className="p-0">
            {isLoading ? (
              <div className="p-4 space-y-3">
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
              </div>
            ) : error ? (
              <div className="p-4 text-sm text-red-500">{error}</div>
            ) : sections.length === 0 ? (
              <div className="p-4 text-center text-muted-foreground">
                <p>该章节暂无小节</p>
                <Button 
                  variant="link" 
                  className="mt-2"
                  onClick={(e) => handleCreateSection(e)}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  添加小节
                </Button>
              </div>
            ) : (
              <ul className="divide-y">
                {sections.map((section) => (
                  <li 
                    key={section.id}
                    className="p-3 pl-12 hover:bg-accent transition-colors flex items-center justify-between cursor-pointer"
                    onClick={() => handleSectionClick(section)}
                  >
                    <div className="flex items-center">
                      {getContentTypeIcon(section.contentType)}
                      <span>{section.title}</span>
                    </div>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 opacity-50 hover:opacity-100"
                      onClick={(e) => handleEditSection(e, section)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        )}
      </Card>
      
      {/* 创建小节弹窗 */}
      <SectionDialog
        open={isCreateDialogOpen}
        onOpenChange={setIsCreateDialogOpen}
        chapterId={chapter.id}
        courseId={courseId}
        onSubmit={handleCreateSubmit}
        mode="create"
      />
      
      {/* 编辑小节弹窗 */}
      {selectedSection && (
        <SectionDialog
          open={isEditDialogOpen}
          onOpenChange={setIsEditDialogOpen}
          chapterId={chapter.id}
          courseId={courseId}
          section={selectedSection}
          onSubmit={handleEditSubmit}
          mode="edit"
        />
      )}
    </>
  );
} 