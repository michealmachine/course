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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useAuthStore } from '@/stores/auth-store';
import authService from '@/services/auth';
import { Captcha } from '@/components/ui/captcha';

// 第一步表单：基本信息
const basicInfoSchema = z.object({
  username: z.string().min(4, '用户名至少需要4个字符').max(20, '用户名最多20个字符'),
  password: z.string().min(6, '密码至少需要6个字符').max(20, '密码最多20个字符'),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: '两次输入的密码不一致',
  path: ['confirmPassword'],
});

// 第二步表单：邮箱验证
const emailVerifySchema = z.object({
  email: z.string().email('请输入有效的邮箱地址'),
  captchaCode: z.string().min(4, '验证码格式不正确'),
  emailCode: z.string().length(6, '邮箱验证码必须是6位数字'),
});

// 类型推断
type BasicInfoFormValues = z.infer<typeof basicInfoSchema>;
type EmailVerifyFormValues = z.infer<typeof emailVerifySchema>;

export default function RegisterPage() {
  const router = useRouter();

  const [activeTab, setActiveTab] = useState<string>("basic-info");
  const [captchaKey, setCaptchaKey] = useState<string>('');
  const [username, setUsername] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [codeSent, setCodeSent] = useState<boolean>(false);
  const [countdown, setCountdown] = useState<number>(0);

  const { register: registerUser, sendEmailVerificationCode, isLoading, error, clearError } = useAuthStore();

  // 第一步表单
  const basicInfoForm = useForm<BasicInfoFormValues>({
    resolver: zodResolver(basicInfoSchema),
    defaultValues: {
      username: '',
      password: '',
      confirmPassword: '',
    },
  });

  // 第二步表单
  const emailVerifyForm = useForm<EmailVerifyFormValues>({
    resolver: zodResolver(emailVerifySchema),
    defaultValues: {
      email: '',
      captchaCode: '',
      emailCode: '',
    },
  });

  // 处理验证码Key变化
  const handleCaptchaKeyChange = (newCaptchaKey: string) => {
    console.log('注册页面：验证码Key已更新', newCaptchaKey);
    setCaptchaKey(newCaptchaKey);
  };

  // 处理基本信息提交
  const onBasicInfoSubmit = (data: BasicInfoFormValues) => {
    clearError();

    // 保存用户名和密码
    setUsername(data.username);
    setPassword(data.password);

    // 切换到第二步
    setActiveTab("email-verify");
  };

  // 发送邮箱验证码
  const onSendEmailCode = async () => {
    clearError();

    const email = emailVerifyForm.getValues('email');
    const captchaCode = emailVerifyForm.getValues('captchaCode');

    // 验证邮箱和验证码
    const emailResult = z.string().email('请输入有效的邮箱地址').safeParse(email);
    const captchaResult = z.string().min(4, '验证码格式不正确').safeParse(captchaCode);

    if (!emailResult.success) {
      emailVerifyForm.setError('email', { message: emailResult.error.errors[0].message });
      return;
    }

    if (!captchaResult.success) {
      emailVerifyForm.setError('captchaCode', { message: captchaResult.error.errors[0].message });
      return;
    }

    // 确保验证码Key存在
    if (!captchaKey) {
      console.error('验证码Key不存在，重新获取验证码');
      toast.error('验证码已失效，请点击验证码图片刷新');
      return;
    }

    try {
      await sendEmailVerificationCode({
        email,
        captchaCode,
        captchaKey,
      });

      toast.success('验证码已发送到您的邮箱，请查收');
      setCodeSent(true);
      setCountdown(60); // 设置60秒倒计时
    } catch (error: any) {
      console.error('发送邮箱验证码失败：', error);

      // 针对验证码错误提供特定提示
      if (error.message?.includes('验证码')) {
        toast.error('验证码错误，请重新输入');
      } else {
        toast.error(error.message || '发送验证码失败，请重试');
      }
    }
  };

  // 注册提交
  const onRegisterSubmit = async (data: EmailVerifyFormValues) => {
    clearError();

    if (!username || !password) {
      toast.error('请先填写基本信息');
      setActiveTab("basic-info");
      return;
    }

    try {
      // 将所有信息合并到注册请求中（不包含confirmPassword字段）
      const registerData = {
        username,
        password,
        email: data.email,
        captchaKey,
        captchaCode: data.captchaCode,
        emailCode: data.emailCode
      };

      console.log('发送注册请求，数据：', JSON.stringify(registerData));
      await registerUser(registerData);

      toast.success('注册成功，请登录');
      router.push('/login');
    } catch (error: any) {
      console.error('注册失败：', error);
      toast.error(error.message || '注册失败，请重试');
    }
  };

  // 倒计时效果
  useEffect(() => {
    if (countdown > 0) {
      const timer = setInterval(() => {
        setCountdown(prev => prev - 1);
      }, 1000);
      return () => clearInterval(timer);
    } else if (countdown === 0 && codeSent) {
      setCodeSent(false);
    }
  }, [countdown, codeSent]);

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl">注册账号</CardTitle>
        <CardDescription>
          创建您的账户以开始使用在线课程平台
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="grid w-full grid-cols-2 mb-6">
            <TabsTrigger value="basic-info">基本信息</TabsTrigger>
            <TabsTrigger value="email-verify" disabled={!username || !password}>邮箱验证</TabsTrigger>
          </TabsList>

          <TabsContent value="basic-info">
            <Form {...basicInfoForm}>
              <form onSubmit={basicInfoForm.handleSubmit(onBasicInfoSubmit)} className="space-y-4">
                {error && (
                  <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md">
                    {error}
                  </div>
                )}

                <FormField
                  control={basicInfoForm.control}
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
                  control={basicInfoForm.control}
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
                  control={basicInfoForm.control}
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

                <Button type="submit" className="w-full">
                  下一步
                </Button>
              </form>
            </Form>
          </TabsContent>

          <TabsContent value="email-verify">
            <Form {...emailVerifyForm}>
              <form onSubmit={emailVerifyForm.handleSubmit(onRegisterSubmit)} className="space-y-4">
                {error && (
                  <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md">
                    {error}
                  </div>
                )}

                {username && password && (
                  <div className="bg-muted/50 p-3 rounded-md mb-4">
                    <p className="text-sm text-muted-foreground">用户名: <span className="font-medium">{username}</span></p>
                  </div>
                )}

                <FormField
                  control={emailVerifyForm.control}
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
                  control={emailVerifyForm.control}
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

                <Button
                  type="button"
                  variant="outline"
                  className="w-full"
                  onClick={onSendEmailCode}
                  disabled={isLoading || codeSent}
                >
                  {isLoading ? '发送中...' : codeSent ? `重新发送(${countdown}s)` : '发送验证码'}
                </Button>

                <FormField
                  control={emailVerifyForm.control}
                  name="emailCode"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>邮箱验证码</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入邮箱验证码" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <Button type="submit" className="w-full" disabled={isLoading || !codeSent}>
                  {isLoading ? '注册中...' : '完成注册'}
                </Button>

                <Button
                  type="button"
                  variant="ghost"
                  className="w-full"
                  onClick={() => setActiveTab("basic-info")}
                >
                  返回上一步
                </Button>
              </form>
            </Form>
          </TabsContent>
        </Tabs>
      </CardContent>
      <CardFooter className="flex flex-col items-center justify-center space-y-4">
        <div className="text-sm text-muted-foreground">
          已有账号？ <Link href="/login" className="text-primary hover:underline">立即登录</Link>
        </div>

        <div className="w-full border-t pt-4">
          <div className="text-sm text-center">
            如需创建机构账号或申请机构入驻，请前往
            <Link href="/institution" className="text-primary hover:underline">机构中心</Link>
          </div>
        </div>
      </CardFooter>
    </Card>
  );
}