'use client';

import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { SectionForm, SectionFormValues } from './section-form';
import { Section } from '@/types/course';
import { Loader2 } from 'lucide-react';

interface SectionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  courseId: number;
  chapterId: number;
  section?: Section;
  onSubmit: (values: SectionFormValues) => Promise<void>;
  mode: 'create' | 'edit';
}

export function SectionDialog({
  open,
  onOpenChange,
  courseId,
  chapterId,
  section,
  onSubmit,
  mode = 'create'
}: SectionDialogProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 处理表单提交
  const handleSubmit = async (values: SectionFormValues) => {
    try {
      setIsSubmitting(true);
      setError(null);
      await onSubmit(values);
      onOpenChange(false); // 成功后关闭弹窗
    } catch (err: any) {
      setError(err.message || '提交时发生错误');
    } finally {
      setIsSubmitting(false);
    }
  };

  // 处理取消
  const handleCancel = () => {
    onOpenChange(false);
  };

  // 准备默认值
  let defaultValues: Partial<SectionFormValues> = {};
  
  if (section && mode === 'edit') {
    defaultValues = {
      title: section.title,
      description: section.description || '',
      accessType: section.accessType,
      contentType: section.contentType,
      estimatedMinutes: section.estimatedMinutes
    };
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[800px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {mode === 'create' ? '添加新小节' : '编辑小节'}
          </DialogTitle>
        </DialogHeader>
        
        {isSubmitting ? (
          <div className="flex items-center justify-center p-6">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <span className="ml-2">提交中...</span>
          </div>
        ) : (
          <>
            {error && (
              <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md mb-4">
                {error}
              </div>
            )}
            
            <SectionForm
              defaultValues={defaultValues}
              section={section}
              chapterId={chapterId}
              onSubmit={handleSubmit}
              onCancel={handleCancel}
              isSubmitting={isSubmitting}
              error={error}
              mode={mode}
            />
          </>
        )}
      </DialogContent>
    </Dialog>
  );
} 