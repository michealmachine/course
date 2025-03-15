import { useState, useEffect } from 'react';

/**
 * 防抖钩子函数
 * @param value 需要防抖的值
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的值
 */
function useDebounce<T>(value: T, delay: number = 300): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);
  
  useEffect(() => {
    // 设置定时器延迟更新防抖值
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    
    // 在下一次effect运行之前或组件卸载时清除定时器
    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);
  
  return debouncedValue;
}

export default useDebounce; 