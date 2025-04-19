"use client"

import * as React from "react"
import { addDays, format, getDay, getDaysInMonth, isSameMonth, startOfMonth, subMonths } from "date-fns"
import { ChevronLeft, ChevronRight } from "lucide-react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

export interface HeatmapData {
  [date: string]: number; // 日期字符串(YYYY-MM-DD)到活动次数的映射
}

interface SimpleCalendarHeatmapProps {
  heatmapData?: HeatmapData;
  maxValue?: number;
  className?: string;
  initialMonth?: Date;
  locale?: string;
}

export function SimpleCalendarHeatmap({
  heatmapData = {},
  maxValue = 0,
  className,
  initialMonth = new Date(),
  locale = "zh-CN"
}: SimpleCalendarHeatmapProps) {
  const [currentMonth, setCurrentMonth] = React.useState(initialMonth);

  // 获取热度颜色 - 使用黑色到灰色的色阶
  const getHeatColor = (count: number) => {
    if (count === 0) return "bg-muted hover:bg-muted";
    if (maxValue === 0) return "bg-muted hover:bg-muted";

    const intensity = Math.min(Math.max(count / maxValue, 0), 1);

    if (intensity < 0.2) return "bg-zinc-200";
    if (intensity < 0.4) return "bg-zinc-300";
    if (intensity < 0.6) return "bg-zinc-500";
    if (intensity < 0.8) return "bg-zinc-700";
    return "bg-zinc-900";
  };

  // 获取当前月的天数
  const daysInMonth = getDaysInMonth(currentMonth);

  // 获取当前月的第一天
  const firstDayOfMonth = startOfMonth(currentMonth);

  // 获取当前月的第一天是星期几 (0-6, 0 是星期日)
  const firstDayOfWeek = getDay(firstDayOfMonth);

  // 星期几的标签
  const weekdayLabels = ["日", "一", "二", "三", "四", "五", "六"];

  // 上个月
  const prevMonth = () => {
    setCurrentMonth(subMonths(currentMonth, 1));
  };

  // 下个月
  const nextMonth = () => {
    setCurrentMonth(addDays(firstDayOfMonth, daysInMonth));
  };

  // 生成日历网格
  const generateCalendarGrid = () => {
    const days = [];

    // 添加空白格子填充月初
    for (let i = 0; i < firstDayOfWeek; i++) {
      days.push(<div key={`empty-${i}`} className="h-9 w-9"></div>);
    }

    // 添加当月的天数
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
      const dateStr = format(date, "yyyy-MM-dd");
      const count = heatmapData[dateStr] || 0;
      const heatClass = getHeatColor(count);

      days.push(
        <div
          key={dateStr}
          className={cn(
            "h-9 w-9 rounded-md flex items-center justify-center relative text-sm",
            heatClass,
            isSameMonth(date, new Date()) && date.getDate() === new Date().getDate() ? "border border-primary" : ""
          )}
          title={`${dateStr}: ${count}次学习活动`}
        >
          {day}
        </div>
      );
    }

    return days;
  };

  // 计算总活动次数和活跃天数
  const totalActivities = Object.values(heatmapData).reduce((sum, count) => sum + count, 0);
  const activeDays = Object.values(heatmapData).filter(count => count > 0).length;

  return (
    <div className={cn("p-4", className)}>
      {/* 月份导航 */}
      <div className="flex items-center justify-between mb-4">
        <Button variant="ghost" size="icon" onClick={prevMonth} className="h-7 w-7 bg-transparent p-0 opacity-50 hover:opacity-100">
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <h3 className="text-sm font-medium">
          {currentMonth.toLocaleDateString(locale, { year: 'numeric', month: 'long' })}
        </h3>
        <Button variant="ghost" size="icon" onClick={nextMonth} className="h-7 w-7 bg-transparent p-0 opacity-50 hover:opacity-100">
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>

      {/* 星期几标签 */}
      <div className="grid grid-cols-7 gap-1 mb-1">
        {weekdayLabels.map((day, index) => (
          <div key={index} className="h-6 flex items-center justify-center">
            <span className="text-xs text-muted-foreground">{day}</span>
          </div>
        ))}
      </div>

      {/* 日历网格 */}
      <div className="grid grid-cols-7 gap-1">
        {generateCalendarGrid()}
      </div>

      {/* 统计信息 */}
      <div className="mt-4 text-center text-xs text-muted-foreground">
        <div>颜色越深表示该星期的学习活动越多</div>
      </div>
    </div>
  );
}
