import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * 格式化日期为 YYYY-MM-DD HH:mm:ss 格式
 * @param date 日期字符串或Date对象
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: string | Date | undefined): string {
  if (!date) return '-';
  
  const d = typeof date === 'string' ? new Date(date) : date;
  
  // 检查日期是否有效
  if (isNaN(d.getTime())) return '-';
  
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

export function formatPrice(amount: number) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
  }).format(amount);
}
