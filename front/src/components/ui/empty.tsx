import React, { ReactNode } from 'react';
import { cn } from '@/lib/utils';

interface EmptyProps {
  /** 图标元素 */
  icon?: ReactNode;
  /** 标题文字 */
  title?: string;
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
      'flex flex-col items-center justify-center py-12 text-center',
      className
    )}>
      {icon && (
        <div className="text-muted-foreground mb-4">
          {icon}
        </div>
      )}
      {title && (
        <h3 className="text-lg font-semibold mb-2">
          {title}
        </h3>
      )}
      {description && (
        <p className="text-sm text-muted-foreground max-w-sm">
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