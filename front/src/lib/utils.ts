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

/**
 * 格式化时长（秒）为可读字符串
 * @param seconds 秒数
 * @returns 格式化后的字符串，如 "2小时30分钟"
 */
export function formatDuration(seconds: number): string {
  if (!seconds) return '0分钟';

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);

  if (hours > 0) {
    return `${hours}小时${minutes > 0 ? `${minutes}分钟` : ''}`;
  }
  return `${minutes}分钟`;
}

/**
 * 格式化字节数为可读字符串
 * @param bytes 字节数
 * @param decimals 小数位数
 * @returns 格式化后的字符串，如 "1.5 MB"
 */
export function formatBytes(bytes: number, decimals: number = 2): string {
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

/**
 * 格式化百分比
 * @param value 百分比值（0-100或0-1）
 * @param decimals 小数位数
 * @returns 格式化后的字符串，如 "45.5%"
 */
export function formatPercentage(value: number, decimals: number = 2): string {
  // 判断是否为0-1范围内的小数
  const normalizedValue = value > 1 ? value : value * 100;
  return normalizedValue.toFixed(decimals) + '%';
}

/**
 * 格式化数字，大于1000的数字显示为1k+的形式
 * @param num 数字
 * @returns 格式化后的字符串
 */
export function formatNumber(num: number): string {
  if (num === undefined || num === null) return '0';

  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M+';
  } else if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'W+';
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K+';
  }

  return num.toString();
}
