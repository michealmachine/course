'use client';

import { useState } from 'react';
import { Star } from 'lucide-react';

interface StarRatingProps {
  rating: number;
  onChange?: (rating: number) => void;
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
}

export function StarRating({ 
  rating = 0, 
  onChange, 
  size = 'medium',
  disabled = false 
}: StarRatingProps) {
  const [hoverRating, setHoverRating] = useState(0);
  
  // 星星大小根据size属性设置
  const starSize = {
    small: 'h-4 w-4',
    medium: 'h-5 w-5',
    large: 'h-6 w-6',
  }[size];
  
  // 星星间距根据size属性设置
  const spacing = {
    small: 'gap-1',
    medium: 'gap-1.5',
    large: 'gap-2',
  }[size];
  
  // 处理星星点击
  const handleStarClick = (index: number) => {
    if (disabled || !onChange) return;
    onChange(index);
  };
  
  // 处理鼠标悬停
  const handleStarHover = (index: number) => {
    if (disabled) return;
    setHoverRating(index);
  };
  
  // 处理鼠标离开
  const handleMouseLeave = () => {
    setHoverRating(0);
  };
  
  return (
    <div 
      className={`flex ${spacing} items-center`}
      onMouseLeave={handleMouseLeave}
    >
      {[1, 2, 3, 4, 5].map((index) => (
        <Star
          key={index}
          className={`${starSize} cursor-pointer ${
            index <= (hoverRating || rating)
              ? 'text-yellow-500 fill-yellow-500'
              : 'text-slate-300'
          } ${disabled ? 'cursor-default opacity-80' : 'cursor-pointer hover:scale-110 transition-transform'}`}
          onClick={() => handleStarClick(index)}
          onMouseEnter={() => handleStarHover(index)}
        />
      ))}
    </div>
  );
} 