/**
 * 缓存工具模块
 * 提供前端状态管理的简单缓存机制
 */

// 缓存最长有效期（毫秒）：5分钟
export const MAX_CACHE_AGE = 5 * 60 * 1000;

// 缓存时间戳记录
const cacheTimestamps: Record<string, number> = {};

/**
 * 判断缓存是否已过期
 * @param key 缓存键
 * @param maxAge 最大有效期（毫秒），默认为 MAX_CACHE_AGE
 * @returns 是否已过期
 */
export function isCacheExpired(key: string, maxAge: number = MAX_CACHE_AGE): boolean {
  const timestamp = cacheTimestamps[key];
  if (!timestamp) return true;
  
  const now = Date.now();
  return now - timestamp > maxAge;
}

/**
 * 设置缓存并记录时间戳
 * @param key 缓存键
 * @param data 要缓存的数据
 * @returns 原始数据（方便链式调用）
 */
export function setCache<T>(key: string, data: T): T {
  cacheTimestamps[key] = Date.now();
  return data;
}

/**
 * 清除指定键的缓存
 * @param key 缓存键
 */
export function clearCache(key: string): void {
  delete cacheTimestamps[key];
}

/**
 * 清除所有缓存
 */
export function clearAllCache(): void {
  Object.keys(cacheTimestamps).forEach(key => {
    delete cacheTimestamps[key];
  });
} 