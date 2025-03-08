'use client';

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import Image from 'next/image';
import authService from '@/services/auth';

interface CaptchaProps {
  onCaptchaKeyChange: (captchaKey: string) => void;
}

export function Captcha({ onCaptchaKeyChange }: CaptchaProps) {
  const [captchaImage, setCaptchaImage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  
  const fetchCaptcha = async () => {
    setIsLoading(true);
    try {
      console.log('验证码组件：开始获取验证码');
      const captchaData = await authService.getCaptcha();
      
      if (!captchaData.captchaId) {
        throw new Error('获取验证码Key失败');
      }
      
      console.log('验证码组件：获取到验证码Key', captchaData.captchaId);
      setCaptchaImage(captchaData.captchaImage);
      onCaptchaKeyChange(captchaData.captchaId);
    } catch (error) {
      console.error('验证码组件：获取验证码失败', error);
      toast.error('获取验证码失败，请点击刷新');
    } finally {
      setIsLoading(false);
    }
  };
  
  // 组件挂载后获取验证码
  useEffect(() => {
    fetchCaptcha();
  }, []);
  
  return (
    <div 
      className="h-10 cursor-pointer border rounded-md overflow-hidden flex items-center justify-center min-w-[100px] captcha-component"
      onClick={fetchCaptcha}
      title="点击刷新验证码"
    >
      {isLoading ? (
        <div className="animate-pulse flex space-x-1 items-center">
          <div className="h-2 w-2 bg-gray-300 rounded-full"></div>
          <div className="h-2 w-2 bg-gray-300 rounded-full"></div>
          <div className="h-2 w-2 bg-gray-300 rounded-full"></div>
        </div>
      ) : captchaImage ? (
        <img 
          src={captchaImage} 
          alt="验证码" 
          className="h-full w-auto"
        />
      ) : (
        <span className="text-sm text-gray-500">点击获取</span>
      )}
    </div>
  );
} 