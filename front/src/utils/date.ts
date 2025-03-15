/**
 * 格式化日期为友好的显示格式
 * @param dateString ISO日期字符串或Date对象
 * @param format 格式化模式，默认为 'yyyy-MM-dd HH:mm'
 * @returns 格式化后的日期字符串
 */
export function formatDate(dateString: string | Date, format: string = 'yyyy-MM-dd HH:mm'): string {
  if (!dateString) return '';
  
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString;
  
  if (isNaN(date.getTime())) {
    return '';
  }
  
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();
  
  // 补零函数
  const pad = (num: number): string => num.toString().padStart(2, '0');
  
  // 替换格式化字符串
  return format
    .replace('yyyy', year.toString())
    .replace('MM', pad(month))
    .replace('dd', pad(day))
    .replace('HH', pad(hours))
    .replace('mm', pad(minutes))
    .replace('ss', pad(seconds));
}

/**
 * 计算日期之间的时间差，返回友好的文本
 * @param dateString 要计算的日期
 * @param compareWith 比较的日期，默认为当前时间
 * @returns 友好的时间差显示，如 "3小时前"
 */
export function timeAgo(dateString: string | Date, compareWith: string | Date = new Date()): string {
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString;
  const compareDate = typeof compareWith === 'string' ? new Date(compareWith) : compareWith;
  
  const seconds = Math.floor((compareDate.getTime() - date.getTime()) / 1000);
  
  if (seconds < 0) {
    return formatDate(date);
  }
  
  // 定义时间间隔
  const intervals = {
    年: 31536000,
    月: 2592000,
    周: 604800,
    天: 86400,
    小时: 3600,
    分钟: 60,
    秒: 1
  };
  
  // 找到最合适的时间单位
  for (const [unit, secondsInUnit] of Object.entries(intervals)) {
    const interval = Math.floor(seconds / secondsInUnit);
    if (interval >= 1) {
      return `${interval}${unit}前`;
    }
  }
  
  return '刚刚';
} 