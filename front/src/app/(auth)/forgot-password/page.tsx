'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2, ArrowLeft, ArrowRight, CheckCircle } from 'lucide-react';
import authService from '@/services/auth';
import { EmailVerificationRequest } from '@/types/auth';
import { Captcha } from '@/components/ui/captcha';

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [captchaKey, setCaptchaKey] = useState('');
  const [captchaCode, setCaptchaCode] = useState('');
  const [email, setEmail] = useState('');
  const [emailCode, setEmailCode] = useState('');
  const [error, setError] = useState('');
  const [step, setStep] = useState(1); // 1: 验证邮箱, 2: 重置密码
  const [emailVerified, setEmailVerified] = useState(false);

  // 处理验证码Key变化
  const handleCaptchaKeyChange = (newCaptchaKey: string) => {
    setCaptchaKey(newCaptchaKey);
  };

  // 处理验证码输入变化
  const handleCaptchaCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCaptchaCode(e.target.value);
  };

  // 处理邮箱输入变化
  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
  };

  // 处理邮箱验证码输入变化
  const handleEmailCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmailCode(e.target.value);
  };

  // 发送验证码
  const handleSendCode = async () => {
    // 验证邮箱
    if (!email) {
      toast.error('请输入邮箱地址');
      return;
    }

    // 简单的邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      toast.error('请输入有效的邮箱地址');
      return;
    }

    // 验证图形验证码
    if (!captchaKey) {
      toast.error('请先获取图形验证码');
      return;
    }

    if (!captchaCode) {
      toast.error('请输入图形验证码');
      return;
    }

    setIsSendingCode(true);
    setError('');

    try {
      const data: EmailVerificationRequest = {
        email,
        captchaKey,
        captchaCode
      };

      await authService.sendPasswordResetCode(data);
      toast.success('验证码已发送，请查看邮箱');

      // 设置邮箱已验证
      setEmailVerified(true);

      // 开始倒计时
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error: any) {
      console.error('发送验证码失败:', error);
      setError(error.message || '发送验证码失败，请稍后重试');
    } finally {
      setIsSendingCode(false);
    }
  };

  // 进入下一步
  const goToNextStep = () => {
    if (!emailVerified) {
      toast.error('请先获取并验证邮箱验证码');
      return;
    }

    setStep(2);
  };

  // 返回上一步
  const goToPrevStep = () => {
    setStep(1);
  };

  // 重置密码
  const handleResetPassword = async () => {
    if (!email || !emailCode) {
      toast.error('请填写完整信息');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      await authService.resetPassword({
        email,
        emailCode
      });

      toast.success('密码重置成功，临时密码已发送到您的邮箱');

      // 重定向到登录页
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    } catch (error: any) {
      console.error('密码重置失败:', error);
      setError(error.message || '密码重置失败，请稍后重试');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl">忘记密码</CardTitle>
        <CardDescription>
          {step === 1 ? '第一步：验证您的邮箱' : '第二步：输入验证码重置密码'}
        </CardDescription>

        {/* 步骤指示器 */}
        <div className="flex items-center justify-between mt-4">
          <div className="flex items-center">
            <div className={`rounded-full w-8 h-8 flex items-center justify-center ${step === 1 ? 'bg-primary text-primary-foreground' : 'bg-primary/20'}`}>
              1
            </div>
            <div className="h-1 w-12 bg-border mx-2"></div>
            <div className={`rounded-full w-8 h-8 flex items-center justify-center ${step === 2 ? 'bg-primary text-primary-foreground' : 'bg-primary/20'}`}>
              2
            </div>
          </div>
          <div className="text-sm text-muted-foreground">
            {step}/2
          </div>
        </div>
      </CardHeader>

      <CardContent>
        {error && (
          <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md mb-4">
            {error}
          </div>
        )}

        {/* 步骤1：验证邮箱 */}
        {step === 1 && (
          <div className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-medium">
                邮箱
              </label>
              <Input
                id="email"
                type="email"
                placeholder="请输入邮箱"
                value={email}
                onChange={handleEmailChange}
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="captchaCode" className="text-sm font-medium">
                图形验证码
              </label>
              <div className="flex space-x-2">
                <Input
                  id="captchaCode"
                  placeholder="请输入验证码"
                  className="flex-1"
                  value={captchaCode}
                  onChange={handleCaptchaCodeChange}
                />
                <Captcha onCaptchaKeyChange={handleCaptchaKeyChange} />
              </div>
            </div>

            <div className="flex space-x-2">
              <Button
                type="button"
                variant="outline"
                onClick={handleSendCode}
                disabled={isSendingCode || countdown > 0}
                className="flex-1"
              >
                {isSendingCode && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {countdown > 0 ? `${countdown}秒后重试` : '获取邮箱验证码'}
              </Button>

              <Button
                type="button"
                onClick={goToNextStep}
                disabled={!emailVerified}
                className="flex-1"
              >
                下一步 <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </div>

            {emailVerified && (
              <div className="flex items-center text-sm text-green-600">
                <CheckCircle className="mr-2 h-4 w-4" />
                验证码已发送到您的邮箱
              </div>
            )}
          </div>
        )}

        {/* 步骤2：重置密码 */}
        {step === 2 && (
          <div className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="email-readonly" className="text-sm font-medium">
                邮箱
              </label>
              <Input
                id="email-readonly"
                type="email"
                value={email}
                readOnly
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="emailCode" className="text-sm font-medium">
                邮箱验证码
              </label>
              <Input
                id="emailCode"
                placeholder="请输入邮箱验证码"
                value={emailCode}
                onChange={handleEmailCodeChange}
              />
            </div>

            <div className="flex space-x-2">
              <Button
                type="button"
                variant="outline"
                onClick={goToPrevStep}
                className="flex-1"
              >
                <ArrowLeft className="mr-2 h-4 w-4" /> 上一步
              </Button>

              <Button
                type="button"
                onClick={handleResetPassword}
                className="flex-1"
                disabled={isLoading}
              >
                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {isLoading ? '提交中...' : '重置密码'}
              </Button>
            </div>
          </div>
        )}
      </CardContent>

      <CardFooter className="flex justify-center">
        <p className="text-sm">
          记起密码了？{' '}
          <Link href="/login" className="text-primary hover:underline">
            返回登录
          </Link>
        </p>
      </CardFooter>
    </Card>
  );
}
