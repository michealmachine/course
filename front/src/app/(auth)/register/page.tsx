'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
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

// 注册表单模式验证
const registerSchema = z.object({
  username: z.string().min(4, '用户名至少需要4个字符').max(20, '用户名最多20个字符'),
  email: z.string().email('请输入有效的邮箱地址'),
  password: z.string().min(6, '密码至少需要6个字符').max(20, '密码最多20个字符'),
  confirmPassword: z.string(),
  captchaCode: z.string().min(4, '验证码格式不正确'),
}).refine((data) => data.password === data.confirmPassword, {
  message: '两次输入的密码不一致',
  path: ['confirmPassword'],
});

// 类型推断
type RegisterFormValues = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const router = useRouter();
  
  const [captchaKey, setCaptchaKey] = useState('');
  const { register, isLoading, error, clearError } = useAuthStore();
  
  // 初始化表单
  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      captchaCode: '',
    },
  });
  
  // 处理验证码Key变化
  const handleCaptchaKeyChange = (newCaptchaKey: string) => {
    console.log('注册页面：验证码Key已更新', newCaptchaKey);
    setCaptchaKey(newCaptchaKey);
  };
  
  // 提交表单
  const onSubmit = async (data: RegisterFormValues) => {
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
      await register({
        ...data,
        captchaKey,
      });
      
      toast.success('注册成功，请登录');
      router.push('/login');
    } catch (error: any) {
      console.error('注册失败：', error);
      
      // 针对验证码错误提供特定提示
      if (error.message?.includes('验证码')) {
        toast.error('验证码错误，请重新输入');
      } else {
        toast.error(error.message || '注册失败，请重试');
      }
    }
  };
  
  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl">注册</CardTitle>
        <CardDescription>
          创建您的账户以开始使用在线课程平台
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
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>电子邮件</FormLabel>
                  <FormControl>
                    <Input type="email" placeholder="请输入邮箱地址" {...field} />
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
                  <FormLabel>密码</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="请输入密码" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            <FormField
              control={form.control}
              name="confirmPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>确认密码</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="请再次输入密码" {...field} />
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
              {isLoading ? '注册中...' : '注册'}
            </Button>
          </form>
        </Form>
      </CardContent>
      <CardFooter className="flex justify-center">
        <p className="text-sm">
          已有账号？{' '}
          <Link href="/login" className="text-primary hover:underline">
            登录
          </Link>
        </p>
      </CardFooter>
    </Card>
  );
} 