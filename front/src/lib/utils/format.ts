/**
 * 格式化时长，将秒转换为友好的显示格式
 * @param seconds 秒数
 * @returns 格式化后的时长字符串，例如"5分钟"、"1小时30分钟"
 */
export function formatDuration(seconds: number): string {
  if (!seconds) return '0分钟';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  
  if (hours > 0) {
    return `${hours}小时${minutes > 0 ? `${minutes}分钟` : ''}`;
  }
  return `${minutes > 0 ? minutes : '<1'}分钟`;
}

/**
 * 格式化时长为简短格式，适用于热力图等空间有限的场景
 * @param seconds 秒数
 * @returns 格式化后的简短时长字符串，例如"5m"、"1h30m"
 */
export function formatDurationShort(seconds: number): string {
  if (!seconds) return '';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  
  if (hours > 0) {
    return `${hours}h${minutes > 0 ? `${minutes}m` : ''}`;
  }
  return `${minutes > 0 ? minutes : '<1'}m`;
}
