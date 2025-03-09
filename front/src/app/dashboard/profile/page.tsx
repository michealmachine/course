'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import ReactCrop, { Crop, PixelCrop, centerCrop, makeAspectCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { useAuthStore } from '@/stores/auth-store';
import userService from '@/services/user';
import authService from '@/services/auth';

// 个人资料表单 Schema
const profileFormSchema = z.object({
  nickname: z.string().optional(),
  phone: z.string().regex(/^1[3-9]\d{9}$/, '请输入正确的手机号码').optional(),
});

// 密码修改表单 Schema
const passwordFormSchema = z.object({
  oldPassword: z.string().min(6, '旧密码长度至少6个字符'),
  newPassword: z.string().min(6, '新密码长度至少6个字符'),
  confirmPassword: z.string().min(6, '确认密码长度至少6个字符'),
}).refine(data => data.newPassword === data.confirmPassword, {
  message: "两次输入的密码不一致",
  path: ["confirmPassword"],
});

// 邮箱更新表单 Schema
const emailFormSchema = z.object({
  newEmail: z.string().email('请输入有效的邮箱地址'),
  captchaKey: z.string().optional(),
  captchaCode: z.string().optional(),
  emailCode: z.string().length(6, '验证码需要6位数字'),
  password: z.string().min(6, '密码长度至少6个字符'),
});

// 裁剪图片的工具函数
function centerAspectCrop(
  mediaWidth: number,
  mediaHeight: number,
  aspect: number,
) {
  return centerCrop(
    makeAspectCrop(
      {
        unit: '%',
        width: 90,
      },
      aspect,
      mediaWidth,
      mediaHeight,
    ),
    mediaWidth,
    mediaHeight,
  );
}

// 将裁剪后的图片转换为文件
async function getCroppedImg(
  image: HTMLImageElement,
  crop: PixelCrop,
  fileName: string,
): Promise<File> {
  const canvas = document.createElement('canvas');
  const scaleX = image.naturalWidth / image.width;
  const scaleY = image.naturalHeight / image.height;
  const ctx = canvas.getContext('2d');

  if (!ctx) {
    throw new Error('No 2d context');
  }

  canvas.width = crop.width;
  canvas.height = crop.height;

  ctx.drawImage(
    image,
    crop.x * scaleX,
    crop.y * scaleY,
    crop.width * scaleX,
    crop.height * scaleY,
    0,
    0,
    crop.width,
    crop.height,
  );

  return new Promise((resolve, reject) => {
    canvas.toBlob(blob => {
      if (!blob) {
        reject(new Error('Canvas is empty'));
        return;
      }
      // 创建File对象
      const file = new File([blob], fileName, {
        type: 'image/jpeg',
        lastModified: Date.now(),
      });
      resolve(file);
    }, 'image/jpeg', 0.95);
  });
}

export default function ProfilePage() {
  const router = useRouter();
  const imgRef = useRef<HTMLImageElement | null>(null);
  const { user, setUser } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);
  const [selectedTab, setSelectedTab] = useState('profile');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [crop, setCrop] = useState<Crop>();
  const [completedCrop, setCompletedCrop] = useState<PixelCrop>();
  const [aspect, setAspect] = useState<number>(1);
  const [isCropperOpen, setIsCropperOpen] = useState(false);
  const [croppedImageUrl, setCroppedImageUrl] = useState<string>('');
  const [croppedImageFile, setCroppedImageFile] = useState<File | null>(null);
  const [isEmailSending, setIsEmailSending] = useState(false);
  const [emailCodeCountdown, setEmailCodeCountdown] = useState(0);
  const [captchaKey, setCaptchaKey] = useState<string>('');
  const [captchaImage, setCaptchaImage] = useState<string>('');
  const [isCaptchaLoading, setIsCaptchaLoading] = useState(false);

  // 个人资料表单
  const profileForm = useForm<z.infer<typeof profileFormSchema>>({
    resolver: zodResolver(profileFormSchema),
    defaultValues: {
      nickname: user?.nickname || '',
      phone: user?.phone || '',
    },
  });

  // 密码修改表单
  const passwordForm = useForm<z.infer<typeof passwordFormSchema>>({
    resolver: zodResolver(passwordFormSchema),
    defaultValues: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  // 邮箱更新表单
  const emailForm = useForm<z.infer<typeof emailFormSchema>>({
    resolver: zodResolver(emailFormSchema),
    defaultValues: {
      newEmail: '',
      captchaKey: '',
      captchaCode: '',
      emailCode: '',
      password: '',
    },
    mode: 'onChange',
  });

  // 加载用户信息（仅在组件挂载和user为空时）
  useEffect(() => {
    // 创建一个信号控制器，用于在组件卸载时取消请求
    const controller = new AbortController();
    let isMounted = true;
    
    const fetchUserInfo = async () => {
      try {
        // 如果已经有用户信息，不需要重新获取
        if (user) {
          // 更新表单默认值
          profileForm.reset({
            nickname: user.nickname || '',
            phone: user.phone || '',
          });
          return;
        }
        
        // 如果用户已登出，不要尝试获取用户信息
        if (!user) {
          router.push('/login');
          return;
        }
        
        const userData = await userService.getCurrentUser();
        
        // 确保组件仍然挂载
        if (isMounted) {
          // 更新全局状态
          setUser(userData);
          // 更新表单默认值
          profileForm.reset({
            nickname: userData.nickname || '',
            phone: userData.phone || '',
          });
        }
      } catch (error: any) { // 使用any类型以便访问response属性
        // 确保组件仍然挂载
        if (isMounted) {
          console.error('加载用户信息失败:', error);
          // 如果是401或403错误，说明用户未认证或令牌已过期，重定向到登录页
          if (error?.response?.status === 401 || error?.response?.status === 403) {
            toast.error('您的登录已过期，请重新登录');
            router.push('/login');
          } else {
            toast.error('加载用户信息失败，请稍后重试');
          }
        }
      }
    };

    fetchUserInfo();
    
    // 清理函数
    return () => {
      isMounted = false;
      controller.abort();
    };
  }, [user?.id, setUser, profileForm, router]);

  // 邮箱验证码倒计时
  useEffect(() => {
    if (emailCodeCountdown <= 0) return;
    
    const timer = setTimeout(() => {
      setEmailCodeCountdown(prev => prev - 1);
    }, 1000);
    
    return () => clearTimeout(timer);
  }, [emailCodeCountdown]);

  // 选中邮箱选项卡时获取验证码
  useEffect(() => {
    if (selectedTab === 'email' && !captchaImage) {
      loadCaptcha();
    }
  }, [selectedTab]);

  // 加载验证码
  const loadCaptcha = async () => {
    setIsCaptchaLoading(true);
    try {
      const captchaData = await authService.getCaptcha();
      setCaptchaKey(captchaData.captchaId);
      setCaptchaImage(captchaData.captchaImage);
      // 只有当当前值与新值不同时才设置，避免不必要的状态更新
      const currentCaptchaKey = emailForm.getValues('captchaKey');
      if (currentCaptchaKey !== captchaData.captchaId) {
        emailForm.setValue('captchaKey', captchaData.captchaId, { 
          shouldValidate: false,
          shouldDirty: false,
          shouldTouch: false
        });
      }
    } catch (error) {
      console.error('获取验证码失败:', error);
      toast.error('获取验证码失败，请刷新页面重试');
    } finally {
      setIsCaptchaLoading(false);
    }
  };

  // 刷新验证码
  const refreshCaptcha = () => {
    loadCaptcha();
  };

  // 图片加载时设置初始裁剪区域
  const onImageLoad = (e: React.SyntheticEvent<HTMLImageElement>) => {
    const { width, height } = e.currentTarget;
    imgRef.current = e.currentTarget;
    setCrop(centerAspectCrop(width, height, aspect));
  };

  // 处理裁剪完成
  const handleCropComplete = (c: PixelCrop) => {
    setCompletedCrop(c);
  };

  // 应用裁剪并关闭对话框
  const handleApplyCrop = async () => {
    if (!imgRef.current || !completedCrop || !selectedFile) {
      return;
    }

    try {
      // 获取裁剪后的图片文件
      const croppedFile = await getCroppedImg(
        imgRef.current,
        completedCrop,
        selectedFile.name,
      );

      // 保存裁剪后的图片文件
      setCroppedImageFile(croppedFile);

      // 创建裁剪后图片的URL预览
      const newPreviewUrl = URL.createObjectURL(croppedFile);
      setCroppedImageUrl(newPreviewUrl);

      // 关闭裁剪对话框
      setIsCropperOpen(false);
    } catch (error) {
      console.error('裁剪图片失败:', error);
      toast.error('裁剪图片失败，请重试');
    }
  };

  // 提交个人资料表单
  const onProfileSubmit = async (data: z.infer<typeof profileFormSchema>) => {
    if (!user) {
      toast.error('您需要先登录');
      router.push('/login');
      return;
    }
    
    setIsLoading(true);
    try {
      const updatedUser = await userService.updateProfile(data);
      setUser(updatedUser);
      toast.success('个人资料更新成功');
    } catch (error: any) {
      console.error('更新个人资料失败:', error);
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error('您的登录已过期，请重新登录');
        router.push('/login');
      } else {
        toast.error('更新个人资料失败，请稍后重试');
      }
    } finally {
      setIsLoading(false);
    }
  };

  // 提交密码修改表单
  const onPasswordSubmit = async (data: z.infer<typeof passwordFormSchema>) => {
    if (!user) {
      toast.error('您需要先登录');
      router.push('/login');
      return;
    }
    
    setIsLoading(true);
    try {
      await userService.changePassword(data);
      toast.success('密码修改成功');
      passwordForm.reset();
    } catch (error: any) {
      console.error('修改密码失败:', error);
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error('您的登录已过期，请重新登录');
        router.push('/login');
      } else {
        toast.error('修改密码失败，请检查旧密码是否正确');
      }
    } finally {
      setIsLoading(false);
    }
  };

  // 发送邮箱验证码
  const handleSendEmailCode = async () => {
    // 获取并验证新邮箱和密码
    const newEmail = emailForm.getValues('newEmail');
    const captchaCode = emailForm.getValues('captchaCode');
    const password = emailForm.getValues('password');
    
    // 验证邮箱和验证码
    const emailValid = await emailForm.trigger('newEmail');
    if (!emailValid || !newEmail) {
      toast.error('请输入有效的邮箱地址');
      return;
    }
    
    if (!captchaCode) {
      toast.error('请输入图形验证码');
      return;
    }
    
    // 验证密码
    const passwordValid = await emailForm.trigger('password');
    if (!passwordValid || !password) {
      toast.error('请输入当前密码');
      return;
    }
    
    if (!user) {
      toast.error('您需要先登录');
      router.push('/login');
      return;
    }
    
    setIsEmailSending(true);
    try {
      // 发送验证码
      await userService.sendEmailUpdateCode({
        email: newEmail,
        captchaKey: captchaKey,
        captchaCode: captchaCode,
      });
      toast.success('验证码已发送，请查收邮件');
      // 开始倒计时
      setEmailCodeCountdown(60);
      // 刷新验证码
      refreshCaptcha();
    } catch (error: any) {
      console.error('发送邮箱验证码失败:', error);
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error('您的登录已过期，请重新登录');
        router.push('/login');
      } else if (error?.response?.status === 400) {
        toast.error('验证码错误，请重新输入');
        refreshCaptcha(); // 刷新验证码
      } else {
        toast.error('发送验证码失败，请稍后重试');
      }
    } finally {
      setIsEmailSending(false);
    }
  };

  // 提交邮箱更新表单
  const onEmailSubmit = async (data: z.infer<typeof emailFormSchema>) => {
    if (!user) {
      toast.error('您需要先登录');
      router.push('/login');
      return;
    }
    
    setIsLoading(true);
    try {
      await userService.updateEmail({
        newEmail: data.newEmail,
        emailCode: data.emailCode,
        password: data.password,
      });
      // 更新用户信息，重新获取以保证邮箱已更新
      const updatedUser = await userService.getCurrentUser();
      setUser(updatedUser);
      toast.success('邮箱更新成功');
      
      // 重置表单，确保所有字段都被正确重置
      emailForm.reset({
        newEmail: '',
        captchaKey: '',
        captchaCode: '',
        emailCode: '',
        password: '',
      });
      
      // 刷新验证码
      refreshCaptcha();
    } catch (error: any) {
      console.error('更新邮箱失败:', error);
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error('您的登录已过期，请重新登录');
        router.push('/login');
      } else if (error?.response?.status === 400) {
        toast.error('更新邮箱失败，验证码可能已过期或不正确');
      } else {
        toast.error('更新邮箱失败，请检查验证码和密码是否正确');
      }
    } finally {
      setIsLoading(false);
    }
  };

  // 处理头像文件选择
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      toast.error('请选择图片文件');
      return;
    }

    // 验证文件大小 (最大2MB)
    if (file.size > 2 * 1024 * 1024) {
      toast.error('图片大小不能超过2MB');
      return;
    }

    setSelectedFile(file);
    const imageUrl = URL.createObjectURL(file);
    setPreviewUrl(imageUrl);
    setIsCropperOpen(true);
  };

  // 提交头像更新
  const handleAvatarUpload = async () => {
    if (!user) {
      toast.error('您需要先登录');
      router.push('/login');
      return;
    }
    
    if (!croppedImageFile) {
      toast.error('请先选择并裁剪头像');
      return;
    }
    
    setIsLoading(true);
    try {
      const response = await userService.uploadAvatar(croppedImageFile);
      // 更新用户头像
      setUser({ ...user!, avatar: response.avatarUrl });
      toast.success('头像上传成功');
      // 清理
      setSelectedFile(null);
      setPreviewUrl('');
    } catch (error: any) {
      console.error('上传头像失败:', error);
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error('您的登录已过期，请重新登录');
        router.push('/login');
      } else {
        toast.error('上传头像失败，请稍后重试');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mx-auto py-6">
      <h1 className="text-3xl font-bold mb-6">个人资料</h1>

      <Tabs value={selectedTab} onValueChange={setSelectedTab} className="w-full">
        <TabsList className="mb-6">
          <TabsTrigger value="profile">基本信息</TabsTrigger>
          <TabsTrigger value="avatar">头像设置</TabsTrigger>
          <TabsTrigger value="password">修改密码</TabsTrigger>
          <TabsTrigger value="email">更新邮箱</TabsTrigger>
        </TabsList>

        {/* 基本信息表单 */}
        <TabsContent value="profile">
          <Card>
            <CardHeader>
              <CardTitle>个人资料</CardTitle>
              <CardDescription>管理您的个人资料信息，如昵称和手机号码</CardDescription>
            </CardHeader>
            <form onSubmit={profileForm.handleSubmit(onProfileSubmit)}>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="username">用户名</Label>
                  <Input id="username" value={user?.username || ''} disabled />
                  <p className="text-sm text-muted-foreground">用户名不可修改</p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">邮箱</Label>
                  <Input id="email" value={user?.email || ''} disabled />
                  <p className="text-sm text-muted-foreground">
                    如需修改邮箱，请前往"更新邮箱"选项卡
                  </p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="nickname">昵称</Label>
                  <Input
                    id="nickname"
                    {...profileForm.register('nickname')}
                    placeholder="请输入昵称"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">手机号码</Label>
                  <Input
                    id="phone"
                    {...profileForm.register('phone')}
                    placeholder="请输入手机号码"
                  />
                  {profileForm.formState.errors.phone && (
                    <p className="text-sm text-red-500">
                      {profileForm.formState.errors.phone.message}
                    </p>
                  )}
                </div>
              </CardContent>
              <CardFooter className="flex justify-end">
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? '保存中...' : '保存修改'}
                </Button>
              </CardFooter>
            </form>
          </Card>
        </TabsContent>

        {/* 头像设置表单 */}
        <TabsContent value="avatar">
          <Card>
            <CardHeader>
              <CardTitle>头像设置</CardTitle>
              <CardDescription>更新您的个人头像</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex flex-col items-center space-y-4">
                <Avatar className="h-24 w-24">
                  <AvatarImage 
                    src={croppedImageUrl || user?.avatar} 
                    alt={user?.username} 
                  />
                  <AvatarFallback>{user?.username?.slice(0, 2).toUpperCase()}</AvatarFallback>
                </Avatar>
                <div className="flex flex-col items-center space-y-2">
                  <Label htmlFor="avatar" className="cursor-pointer">
                    <div className="px-4 py-2 bg-muted rounded-md hover:bg-muted/80 transition">
                      选择图片
                    </div>
                    <input
                      id="avatar"
                      type="file"
                      className="hidden"
                      accept="image/*"
                      onChange={handleFileChange}
                    />
                  </Label>
                  <p className="text-sm text-muted-foreground">
                    支持 JPG, PNG 格式，文件大小不超过 2MB
                  </p>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-end">
              <Button
                onClick={handleAvatarUpload}
                disabled={isLoading || !croppedImageFile}
              >
                {isLoading ? '上传中...' : '上传头像'}
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>

        {/* 密码修改表单 */}
        <TabsContent value="password">
          <Card>
            <CardHeader>
              <CardTitle>修改密码</CardTitle>
              <CardDescription>更新您的登录密码</CardDescription>
            </CardHeader>
            <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)}>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="oldPassword">当前密码</Label>
                  <Input
                    id="oldPassword"
                    type="password"
                    {...passwordForm.register('oldPassword')}
                    placeholder="请输入当前密码"
                  />
                  {passwordForm.formState.errors.oldPassword && (
                    <p className="text-sm text-red-500">
                      {passwordForm.formState.errors.oldPassword.message}
                    </p>
                  )}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="newPassword">新密码</Label>
                  <Input
                    id="newPassword"
                    type="password"
                    {...passwordForm.register('newPassword')}
                    placeholder="请输入新密码"
                  />
                  {passwordForm.formState.errors.newPassword && (
                    <p className="text-sm text-red-500">
                      {passwordForm.formState.errors.newPassword.message}
                    </p>
                  )}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">确认新密码</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    {...passwordForm.register('confirmPassword')}
                    placeholder="请再次输入新密码"
                  />
                  {passwordForm.formState.errors.confirmPassword && (
                    <p className="text-sm text-red-500">
                      {passwordForm.formState.errors.confirmPassword.message}
                    </p>
                  )}
                </div>
              </CardContent>
              <CardFooter className="flex justify-end">
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? '更新中...' : '更新密码'}
                </Button>
              </CardFooter>
            </form>
          </Card>
        </TabsContent>

        {/* 更新邮箱表单 */}
        <TabsContent value="email">
          <Card>
            <CardHeader>
              <CardTitle>更新邮箱</CardTitle>
              <CardDescription>
                更新您的登录邮箱，需要验证您的身份和新邮箱的有效性。
                请按照以下步骤操作：先输入当前密码验证身份，再填写新邮箱并获取验证码。
              </CardDescription>
            </CardHeader>
            <form onSubmit={emailForm.handleSubmit(onEmailSubmit)}>
              <CardContent className="space-y-6">
                {/* 当前密码和邮箱 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="password" className="font-semibold">当前密码</Label>
                    <Input
                      id="password"
                      type="password"
                      {...emailForm.register('password')}
                      placeholder="请输入当前密码"
                      autoComplete="current-password"
                    />
                    <p className="text-xs text-muted-foreground">为确保安全，需要先验证您的身份</p>
                    {emailForm.formState.errors.password && (
                      <p className="text-sm text-red-500">
                        {emailForm.formState.errors.password.message}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="currentEmail">当前邮箱</Label>
                    <Input
                      id="currentEmail"
                      value={user?.email || ''}
                      disabled
                    />
                    <p className="text-xs text-muted-foreground">您当前使用的邮箱地址</p>
                  </div>
                </div>
                
                {/* 新邮箱和图形验证码 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="newEmail">新邮箱</Label>
                    <Input
                      id="newEmail"
                      type="email"
                      {...emailForm.register('newEmail')}
                      placeholder="请输入新邮箱地址"
                      autoComplete="email"
                    />
                    <p className="text-xs text-muted-foreground">请确保这是一个有效的邮箱，验证码将发送至此</p>
                    {emailForm.formState.errors.newEmail && (
                      <p className="text-sm text-red-500">
                        {emailForm.formState.errors.newEmail.message}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="captchaCode">图形验证码</Label>
                    <div className="flex space-x-2">
                      <Input
                        id="captchaCode"
                        {...emailForm.register('captchaCode')}
                        placeholder="请输入图形验证码"
                      />
                      <div 
                        className="h-10 w-24 flex-shrink-0 cursor-pointer border rounded overflow-hidden"
                        onClick={refreshCaptcha}
                        title="点击刷新验证码"
                      >
                        {isCaptchaLoading ? (
                          <div className="h-full flex items-center justify-center bg-muted">
                            <span className="text-xs">加载中...</span>
                          </div>
                        ) : (
                          captchaImage && <img src={captchaImage} alt="验证码" className="h-full w-full object-cover" />
                        )}
                      </div>
                    </div>
                    <p className="text-xs text-muted-foreground">输入图片中显示的验证码，点击图片可刷新</p>
                  </div>
                </div>
                
                {/* 邮箱验证码 */}
                <div className="space-y-2">
                  <Label htmlFor="emailCode">邮箱验证码</Label>
                  <div className="flex space-x-2">
                    <Input
                      id="emailCode"
                      {...emailForm.register('emailCode')}
                      placeholder="请输入邮箱验证码"
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handleSendEmailCode}
                      disabled={isEmailSending || emailCodeCountdown > 0}
                      className="whitespace-nowrap"
                      title={emailCodeCountdown > 0 ? `${emailCodeCountdown}秒后可重新发送` : '发送验证码到新邮箱'}
                    >
                      {isEmailSending 
                        ? '发送中...' 
                        : emailCodeCountdown > 0 
                          ? `${emailCodeCountdown}s` 
                          : '获取验证码'}
                    </Button>
                  </div>
                  <p className="text-xs text-muted-foreground">输入发送到新邮箱的6位数字验证码</p>
                  {emailForm.formState.errors.emailCode && (
                    <p className="text-sm text-red-500">
                      {emailForm.formState.errors.emailCode.message}
                    </p>
                  )}
                </div>
              </CardContent>
              <CardFooter className="flex justify-between">
                <p className="text-sm text-muted-foreground">
                  更新邮箱后，将使用新邮箱作为您的登录凭证
                </p>
                <Button 
                  type="submit" 
                  disabled={isLoading}
                  className="min-w-24"
                >
                  {isLoading ? '更新中...' : '更新邮箱'}
                </Button>
              </CardFooter>
            </form>
          </Card>
        </TabsContent>
      </Tabs>

      {/* 头像裁剪对话框 */}
      <Dialog open={isCropperOpen} onOpenChange={setIsCropperOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>裁剪头像</DialogTitle>
            <DialogDescription>
              调整头像的裁剪区域，保持图片比例为1:1的正方形
            </DialogDescription>
          </DialogHeader>
          <div className="my-4 flex justify-center">
            {previewUrl && (
              <ReactCrop
                crop={crop}
                onChange={(c) => setCrop(c)}
                onComplete={handleCropComplete}
                aspect={aspect}
                circularCrop
              >
                <img
                  ref={imgRef}
                  src={previewUrl}
                  alt="头像裁剪预览"
                  style={{ maxHeight: '400px' }}
                  onLoad={onImageLoad}
                />
              </ReactCrop>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCropperOpen(false)}>
              取消
            </Button>
            <Button onClick={handleApplyCrop}>
              应用裁剪
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 