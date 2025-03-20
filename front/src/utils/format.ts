/**
 * 将秒数格式化为时:分:秒格式
 * 例如: 65 -> 00:01:05
 */
export function formatTime(seconds: number): string {
  if (isNaN(seconds) || seconds < 0) return '00:00:00';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);
  
  return [hours, minutes, secs]
    .map(val => val.toString().padStart(2, '0'))
    .join(':');
}

/**
 * 格式化持续时间，将秒数转为更友好的形式
 * 例如: 65 -> 1分5秒, 3665 -> 1小时1分5秒
 */
export function formatDuration(seconds: number): string {
  if (isNaN(seconds) || seconds < 0) return '0秒';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);
  
  let result = '';
  if (hours > 0) result += `${hours}小时`;
  if (minutes > 0) result += `${minutes}分`;
  if (secs > 0 || (hours === 0 && minutes === 0)) result += `${secs}秒`;
  
  return result;
}

/**
 * 格式化日期，接受日期字符串或时间戳
 */
export function formatDate(date: string | number | Date): string {
  if (!date) return '';
  
  const dateObj = typeof date === 'string' || typeof date === 'number' 
    ? new Date(date) 
    : date;
    
  return dateObj.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * 格式化文件大小，将字节转为更友好的形式
 * 例如: 1024 -> 1 KB, 1048576 -> 1 MB
 */
export function formatFileSize(bytes: number): string {
  if (isNaN(bytes) || bytes < 0) return '0 B';
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let i = 0;
  
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024;
    i++;
  }
  
  return `${bytes.toFixed(2)} ${units[i]}`;
} 