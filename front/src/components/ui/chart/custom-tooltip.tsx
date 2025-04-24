'use client';

import React from 'react';
import { formatDuration } from '@/lib/utils/format';

/**
 * 自定义图表提示组件，用于显示学习时长
 */
export function CustomDurationTooltipContent({ active, payload }: any) {
  if (!active || !payload || !payload.length) {
    return null;
  }

  return (
    <div className="bg-white p-2 border border-gray-200 rounded shadow-md text-sm">
      {payload.map((entry: any, index: number) => {
        // 获取学习时长（秒）
        const seconds = entry.payload.seconds || (entry.value * 60);

        return (
          <div key={`item-${index}`} className="flex items-center gap-2">
            <div
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: entry.color }}
            />
            <span className="text-gray-600">{entry.name}:</span>
            <span className="font-medium">{formatDuration(seconds)}</span>
          </div>
        );
      })}
    </div>
  );
}
