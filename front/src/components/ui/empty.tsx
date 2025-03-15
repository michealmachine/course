import React from 'react';
import { cn } from '@/lib/utils';

interface EmptyProps {
  /** 图标元素 */
  icon?: React.ReactNode;
  /** 标题文字 */
  title: string;
  /** 描述文字 */
  description?: string;
  /** 操作按钮 */
  action?: React.ReactNode;
  /** 额外的className */
  className?: string;
}

/**
 * 空状态组件，用于显示列表或内容为空的状态
 */
export function Empty({
  icon,
  title,
  description,
  action,
  className,
}: EmptyProps) {
  return (
    <div className={cn(
      'flex flex-col items-center justify-center text-center p-8 border border-dashed rounded-lg',
      className
    )}>
      {icon && (
        <div className="mb-4 text-muted-foreground">
          {icon}
        </div>
      )}
      <h3 className="text-lg font-medium">{title}</h3>
      {description && (
        <p className="mt-2 text-sm text-muted-foreground max-w-md">
          {description}
        </p>
      )}
      {action && (
        <div className="mt-4">
          {action}
        </div>
      )}
    </div>
  );
} 