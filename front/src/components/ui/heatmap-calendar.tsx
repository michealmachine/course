"use client"

import * as React from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { DayPicker } from "react-day-picker"
import { format } from "date-fns"

import { cn } from "@/lib/utils"
import { buttonVariants } from "@/components/ui/button"

export interface HeatmapData {
  [date: string]: number; // 日期字符串(YYYY-MM-DD)到活动次数的映射
}

interface HeatmapCalendarProps extends React.ComponentProps<typeof DayPicker> {
  heatmapData?: HeatmapData;
  maxValue?: number;
}

function HeatmapCalendar({
  className,
  classNames,
  showOutsideDays = true,
  heatmapData = {},
  maxValue = 0,
  ...props
}: HeatmapCalendarProps) {
  // 获取热度颜色
  const getHeatColor = (count: number) => {
    if (count === 0) return "bg-muted hover:bg-muted";
    if (maxValue === 0) return "bg-muted hover:bg-muted";

    const intensity = Math.min(Math.max(count / maxValue, 0), 1);

    if (intensity < 0.2) return "bg-blue-100 hover:bg-blue-200";
    if (intensity < 0.4) return "bg-blue-200 hover:bg-blue-300";
    if (intensity < 0.6) return "bg-blue-300 hover:bg-blue-400";
    if (intensity < 0.8) return "bg-blue-400 hover:bg-blue-500";
    return "bg-blue-500 hover:bg-blue-600";
  };

  // 使用修饰器来添加热力图样式
  const modifiersStyles: Record<string, React.CSSProperties> = {};
  const modifiers: Record<string, (date: Date) => boolean> = {};

  // 为每个有数据的日期创建修饰器
  Object.entries(heatmapData).forEach(([dateStr, count]) => {
    if (count > 0) {
      const modifierKey = `heatmap-${dateStr}`;
      modifiers[modifierKey] = (date) => format(date, "yyyy-MM-dd") === dateStr;

      // 根据活动数量设置背景颜色
      const intensity = Math.min(Math.max(count / maxValue, 0), 1);

      let bgColor;
      if (intensity < 0.2) bgColor = "rgb(219, 234, 254)"; // blue-100
      else if (intensity < 0.4) bgColor = "rgb(191, 219, 254)"; // blue-200
      else if (intensity < 0.6) bgColor = "rgb(147, 197, 253)"; // blue-300
      else if (intensity < 0.8) bgColor = "rgb(96, 165, 250)"; // blue-400
      else bgColor = "rgb(59, 130, 246)"; // blue-500

      modifiersStyles[modifierKey] = {
        backgroundColor: bgColor,
        position: 'relative',
      };

      // 我们不使用伪元素，因为它可能会导致问题
      // 我们将在日历下方显示活动次数的总和
    }
  });

  return (
    <DayPicker
      showOutsideDays={showOutsideDays}
      className={cn("p-3", className)}
      classNames={{
        months: "flex flex-col sm:flex-row gap-2",
        month: "flex flex-col gap-4",
        caption: "flex justify-center pt-1 relative items-center w-full",
        caption_label: "text-sm font-medium",
        nav: "flex items-center gap-1",
        nav_button: cn(
          buttonVariants({ variant: "outline" }),
          "size-7 bg-transparent p-0 opacity-50 hover:opacity-100"
        ),
        nav_button_previous: "absolute left-1",
        nav_button_next: "absolute right-1",
        table: "w-full border-collapse space-x-1",
        head_row: "flex",
        head_cell:
          "text-muted-foreground rounded-md w-9 font-normal text-[0.8rem]",
        row: "flex w-full mt-2",
        cell: "relative p-0 text-center text-sm focus-within:relative focus-within:z-20",
        day: cn(
          buttonVariants({ variant: "ghost" }),
          "size-9 p-0 font-normal aria-selected:opacity-100"
        ),
        day_selected:
          "bg-primary text-primary-foreground hover:bg-primary hover:text-primary-foreground focus:bg-primary focus:text-primary-foreground",
        day_today: "border border-primary",
        day_outside:
          "day-outside text-muted-foreground opacity-50",
        day_disabled: "text-muted-foreground opacity-50",
        day_hidden: "invisible",
        ...classNames,
      }}
      modifiers={modifiers}
      modifiersStyles={modifiersStyles}
      components={{
        IconLeft: ({ className, ...props }) => (
          <ChevronLeft className={cn("size-4", className)} {...props} />
        ),
        IconRight: ({ className, ...props }) => (
          <ChevronRight className={cn("size-4", className)} {...props} />
        ),
      }}
      footer={
        <div className="pt-2 text-center text-xs text-muted-foreground">
          <div>总计: {Object.values(heatmapData).reduce((sum, count) => sum + count, 0)} 次学习活动</div>
          <div>活跃天数: {Object.values(heatmapData).filter(count => count > 0).length} 天</div>
        </div>
      }
      {...props}
    />
  )
}

export { HeatmapCalendar }
