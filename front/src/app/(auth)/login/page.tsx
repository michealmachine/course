'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuthStore } from '@/stores/auth-store';
import authService from '@/services/auth';
import { Captcha } from '@/components/ui/captcha';

// 登录表单模式验证
const loginSchema = z.object({
  username: z.string().min(2, '用户名至少需要2个字符'),
  password: z.string().min(6, '密码至少需要6个字符'),
  captchaCode: z.string().min(4, '验证码格式不正确'),
});

// 类型推断
type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectTo = searchParams.get('redirectTo') || '/dashboard';

  const [captchaKey, setCaptchaKey] = useState('');
  const { login, isLoading, error, clearError } = useAuthStore();

  // 初始化表单
  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
      captchaCode: '',
    },
  });

  // 处理验证码Key变化
  const handleCaptchaKeyChange = (newCaptchaKey: string) => {
    console.log('登录页面：验证码Key已更新', newCaptchaKey);
    setCaptchaKey(newCaptchaKey);
  };

  // 提交表单
  const onSubmit = async (data: LoginFormValues) => {
    clearError();

    // 确保验证码Key存在
    if (!captchaKey) {
      console.error('验证码Key不存在，重新获取验证码');
      toast.error('验证码已失效，请点击验证码图片刷新');
      return;
    }

    console.log('表单提交，验证码信息：', {
      captchaCode: data.captchaCode,
      captchaKey: captchaKey,
    });

    try {
      // 将captchaKey添加到请求中
      await login({
        ...data,
        captchaKey,
      });

      toast.success('登录成功');
      router.push(redirectTo);
    } catch (error: any) {
      console.error('登录失败详情：', error);

      // 提取详细的错误信息
      const errorMessage = error.message || '未知错误';
      const errorCode = error.code || '未知错误码';

      console.error(`登录错误：${errorCode} - ${errorMessage}`);

      // 针对不同类型的错误提供特定提示
      if (errorMessage.includes('验证码')) {
        toast.error('验证码错误，请重新输入');
        // 自动刷新验证码
        const captchaElement = document.querySelector('.captcha-component') as HTMLElement;
        if (captchaElement) {
          captchaElement.click();
        }
      } else if (errorMessage.includes('会话') || errorMessage.includes('过期')) {
        toast.error('登录信息已过期，请重新尝试');
        // 清理可能存在的旧令牌
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
      } else if (errorMessage.includes('用户名') || errorMessage.includes('密码')) {
        toast.error('用户名或密码错误');
      } else {
        toast.error(`登录失败: ${errorMessage}`);
      }
    }
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl">登录</CardTitle>
        <CardDescription>
          输入您的用户名和密码登录账户
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            {error && (
              <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md">
                {error}
              </div>
            )}

            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>用户名</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入用户名" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <div className="flex items-center justify-between">
                    <FormLabel>密码</FormLabel>
                    <Link href="/forgot-password" className="text-sm text-primary hover:underline">
                      忘记密码?
                    </Link>
                  </div>
                  <FormControl>
                    <Input type="password" placeholder="请输入密码" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="captchaCode"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>验证码</FormLabel>
                  <div className="flex space-x-2">
                    <FormControl>
                      <Input placeholder="请输入验证码" {...field} />
                    </FormControl>
                    <Captcha onCaptchaKeyChange={handleCaptchaKeyChange} />
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? '登录中...' : '登录'}
            </Button>
          </form>
        </Form>
      </CardContent>
      <CardFooter className="flex justify-center">
        <p className="text-sm">
          还没有账号？{' '}
          <Link href="/register" className="text-primary hover:underline">
            注册新账户
          </Link>
        </p>
      </CardFooter>
    </Card>
  );
}