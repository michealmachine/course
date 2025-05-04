'use client';

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import Link from "next/link";

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Captcha } from "@/components/ui/captcha";
import { useAuthStore } from "@/stores/auth-store";
import institutionAuthService from "@/services/institutionAuth";  // 导入机构认证服务

// 定义表单验证Schema
const registerSchema = z.object({
  username: z.string()
    .min(4, "用户名至少需要4个字符")
    .max(20, "用户名最多20个字符")
    .regex(/^[a-zA-Z0-9_]+$/, "用户名只能包含字母、数字和下划线"),
  password: z.string()
    .min(6, "密码至少需要6个字符")
    .max(20, "密码最多20个字符"),
  confirmPassword: z.string(),
  email: z.string().email("请输入有效的邮箱地址"),
  phone: z.string().regex(/^1[3-9]\d{9}$/, "请输入有效的手机号码").optional(),
  institutionCode: z.string().min(1, "机构注册码不能为空"),
  captchaKey: z.string().min(1, "验证码key不能为空"),
  captchaCode: z.string().min(1, "验证码不能为空"),
  emailCode: z.string().length(6, "邮箱验证码必须是6位数字"),
}).refine(data => data.password === data.confirmPassword, {
  message: "两次输入的密码不一致",
  path: ["confirmPassword"]
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export default function InstitutionRegisterPage() {
  const router = useRouter();
  const [captchaKey, setCaptchaKey] = useState<string>("");
  const [emailCaptchaKey, setEmailCaptchaKey] = useState<string>("");
  const [activeTab, setActiveTab] = useState<string>("basic-info");
  const [countdown, setCountdown] = useState<number>(0);
  const [isEmailVerified, setIsEmailVerified] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isEmailSubmitting, setIsEmailSubmitting] = useState<boolean>(false);
  const { clearError, sendEmailVerificationCode } = useAuthStore();

  // 初始化表单
  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: "",
      password: "",
      confirmPassword: "",
      email: "",
      phone: "",
      institutionCode: "",
      captchaKey: "",
      captchaCode: "",
      emailCode: "",
    }
  });

  // 处理验证码Key变化
  const handleCaptchaKeyChange = (newCaptchaKey: string) => {
    setCaptchaKey(newCaptchaKey);
    form.setValue("captchaKey", newCaptchaKey);
  };

  // 发送邮箱验证码
  const onSendEmailCode = async () => {
    clearError();

    const email = form.getValues("email");
    const captchaCode = form.getValues("captchaCode");

    // 验证邮箱和验证码
    const emailResult = z.string().email("请输入有效的邮箱地址").safeParse(email);
    const captchaResult = z.string().min(1, "验证码不能为空").safeParse(captchaCode);

    if (!emailResult.success) {
      form.setError("email", { message: emailResult.error.errors[0].message });
      return;
    }

    if (!captchaResult.success) {
      form.setError("captchaCode", { message: captchaResult.error.errors[0].message });
      return;
    }

    // 确保验证码Key存在
    if (!captchaKey) {
      console.error("验证码Key不存在，重新获取验证码");
      toast.error("验证码已失效，请点击验证码图片刷新");
      return;
    }

    setIsEmailSubmitting(true);

    try {
      await sendEmailVerificationCode({
        email,
        captchaCode,
        captchaKey,
      });

      // 保存用于验证的captchaKey
      setEmailCaptchaKey(captchaKey);

      toast.success("验证码已发送到您的邮箱，请查收");
      setCountdown(60); // 设置60秒倒计时
    } catch (error: any) {
      console.error("发送邮箱验证码失败：", error);

      if (error.message?.includes("验证码")) {
        toast.error("验证码错误，请重新输入");
      } else {
        toast.error(error.message || "发送验证码失败，请重试");
      }
    } finally {
      setIsEmailSubmitting(false);
    }
  };

  // 提交基本信息
  const onBasicInfoSubmit = () => {
    const basicFields = ["username", "password", "confirmPassword", "institutionCode"];
    const hasErrors = basicFields.some(field => !!form.formState.errors[field as keyof RegisterFormValues]);

    if (hasErrors) {
      return;
    }

    setActiveTab("email-verify");
  };

  // 验证邮箱
  const onVerifyEmail = async () => {
    clearError();

    const emailCode = form.getValues("emailCode");

    if (!emailCode || emailCode.length !== 6) {
      form.setError("emailCode", { message: "请输入6位数字验证码" });
      return;
    }

    setIsEmailVerified(true);
    toast.success("邮箱验证成功");
    setActiveTab("submit");
  };

  // 最终提交注册
  const onSubmit = async (data: RegisterFormValues) => {
    clearError();
    setIsSubmitting(true);

    console.log("准备提交机构用户注册请求:", {
      username: data.username,
      email: data.email,
      institutionCode: data.institutionCode,
      captchaKey: data.captchaKey,
      // 不输出密码信息
    });

    try {
      // 创建一个新对象，排除confirmPassword字段
      const { confirmPassword, ...registerData } = data;

      console.log("准备发送注册请求，已移除confirmPassword字段");
      console.log("注册数据:", JSON.stringify(registerData));

      // 调用API注册机构用户
      await institutionAuthService.register(registerData);

      console.log("注册请求成功发送");
      toast.success("注册成功！");

      // 跳转到登录页面
      router.push("/login");
    } catch (error: any) {
      console.error("注册失败:", error);
      // 输出更详细的错误信息
      if (error.code) {
        console.error(`错误代码: ${error.code}, 错误信息: ${error.message}`);
      }
      if (error.errors) {
        console.error("字段错误:", error.errors);
      }
      toast.error(error.message || "注册失败，请稍后重试");
    } finally {
      setIsSubmitting(false);
    }
  };

  // 倒计时效果
  useEffect(() => {
    if (countdown > 0) {
      const timer = setInterval(() => {
        setCountdown(prev => prev - 1);
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [countdown]);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-lg w-full">
        <Card className="bg-white dark:bg-slate-800 shadow border-none">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-center text-slate-900 dark:text-slate-100">机构用户注册</CardTitle>
            <CardDescription className="text-center text-slate-500 dark:text-slate-400">
              使用机构注册码注册成为机构管理员或教师
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
              <TabsList className="grid w-full grid-cols-3 mb-6">
                <TabsTrigger
                  value="basic-info"
                  className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
                >
                  基本信息
                </TabsTrigger>
                <TabsTrigger
                  value="email-verify"
                  disabled={form.formState.errors.username || form.formState.errors.password || form.formState.errors.confirmPassword || form.formState.errors.institutionCode}
                  className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
                >
                  邮箱验证
                </TabsTrigger>
                <TabsTrigger
                  value="submit"
                  disabled={!isEmailVerified}
                  className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
                >
                  提交注册
                </TabsTrigger>
              </TabsList>

              <Form {...form}>
                <TabsContent value="basic-info">
                  <div className="space-y-4 mt-4">
                    <Alert className="bg-blue-50 dark:bg-blue-950 text-blue-800 dark:text-blue-300 border-blue-200 dark:border-blue-900">
                      <AlertDescription>
                        请填写基本信息，点击下一步进行邮箱验证
                      </AlertDescription>
                    </Alert>

                    <FormField
                      control={form.control}
                      name="username"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">用户名</FormLabel>
                          <FormControl>
                            <Input placeholder="请输入用户名" {...field} className="bg-slate-50 dark:bg-slate-900" />
                          </FormControl>
                          <FormDescription className="text-slate-500 dark:text-slate-400 text-xs">
                            用户名将用于登录，仅支持字母、数字和下划线
                          </FormDescription>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="password"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">密码</FormLabel>
                          <FormControl>
                            <Input type="password" placeholder="请输入密码" {...field} className="bg-slate-50 dark:bg-slate-900" />
                          </FormControl>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="confirmPassword"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">确认密码</FormLabel>
                          <FormControl>
                            <Input type="password" placeholder="请再次输入密码" {...field} className="bg-slate-50 dark:bg-slate-900" />
                          </FormControl>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="institutionCode"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">机构注册码</FormLabel>
                          <FormControl>
                            <Input placeholder="请输入机构注册码" {...field} className="bg-slate-50 dark:bg-slate-900" />
                          </FormControl>
                          <FormDescription className="text-slate-500 dark:text-slate-400 text-xs">
                            注册码在机构申请审核通过后通过邮件发送
                          </FormDescription>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <div className="pt-4 flex justify-between">
                      <Button type="button" variant="outline" asChild>
                        <Link href="/institution">返回</Link>
                      </Button>
                      <Button type="button" onClick={onBasicInfoSubmit}>
                        下一步
                      </Button>
                    </div>
                  </div>
                </TabsContent>

                <TabsContent value="email-verify">
                  <div className="space-y-4 mt-4">
                    <Alert className="bg-blue-50 dark:bg-blue-950 text-blue-800 dark:text-blue-300 border-blue-200 dark:border-blue-900">
                      <AlertDescription>
                        请提供您的电子邮箱并完成验证
                      </AlertDescription>
                    </Alert>

                    <FormField
                      control={form.control}
                      name="email"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">电子邮箱</FormLabel>
                          <FormControl>
                            <Input type="email" placeholder="请输入邮箱地址" {...field} className="bg-slate-50 dark:bg-slate-900" />
                          </FormControl>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="phone"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">手机号码（可选）</FormLabel>
                          <FormControl>
                            <Input placeholder="请输入手机号码" {...field} className="bg-slate-50 dark:bg-slate-900" />
                          </FormControl>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="captchaCode"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">图形验证码</FormLabel>
                          <div className="flex items-center gap-2">
                            <FormControl>
                              <Input placeholder="请输入验证码" {...field} className="bg-slate-50 dark:bg-slate-900" />
                            </FormControl>
                            <Captcha onCaptchaKeyChange={handleCaptchaKeyChange} />
                          </div>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="emailCode"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 dark:text-slate-300">邮箱验证码</FormLabel>
                          <div className="flex items-center gap-2">
                            <FormControl>
                              <Input placeholder="请输入邮箱验证码" {...field} className="bg-slate-50 dark:bg-slate-900" />
                            </FormControl>
                            <Button
                              type="button"
                              variant="outline"
                              onClick={onSendEmailCode}
                              disabled={countdown > 0 || isEmailSubmitting}
                              className="whitespace-nowrap"
                            >
                              {isEmailSubmitting ? "发送中..." : countdown > 0 ? `${countdown}秒后重发` : "发送验证码"}
                            </Button>
                          </div>
                          <FormMessage className="text-red-500" />
                        </FormItem>
                      )}
                    />

                    <div className="pt-4 flex justify-between">
                      <Button type="button" onClick={() => setActiveTab("basic-info")} variant="outline">
                        返回上一步
                      </Button>
                      <Button type="button" onClick={onVerifyEmail} disabled={!form.getValues("emailCode")}>
                        验证邮箱
                      </Button>
                    </div>
                  </div>
                </TabsContent>

                <TabsContent value="submit">
                  <div className="space-y-4 mt-4">
                    <Alert className="bg-green-50 dark:bg-green-950 text-green-800 dark:text-green-300 border-green-200 dark:border-green-900">
                      <AlertDescription>
                        请确认以下信息无误，点击"提交注册"完成注册
                      </AlertDescription>
                    </Alert>

                    <div className="border border-slate-200 dark:border-slate-700 rounded-md p-4 space-y-3">
                      <div className="grid grid-cols-2 gap-2">
                        <div>
                          <p className="text-sm text-slate-500 dark:text-slate-400">用户名</p>
                          <p className="font-medium text-slate-900 dark:text-slate-100">{form.getValues("username")}</p>
                        </div>
                        <div>
                          <p className="text-sm text-slate-500 dark:text-slate-400">电子邮箱</p>
                          <p className="font-medium text-slate-900 dark:text-slate-100">{form.getValues("email")}</p>
                        </div>
                        {form.getValues("phone") && (
                          <div>
                            <p className="text-sm text-slate-500 dark:text-slate-400">手机号码</p>
                            <p className="font-medium text-slate-900 dark:text-slate-100">{form.getValues("phone")}</p>
                          </div>
                        )}
                        <div>
                          <p className="text-sm text-slate-500 dark:text-slate-400">机构注册码</p>
                          <p className="font-medium text-slate-900 dark:text-slate-100">{form.getValues("institutionCode")}</p>
                        </div>
                      </div>
                    </div>

                    <div className="border border-slate-200 dark:border-slate-700 rounded-md p-4">
                      <p className="text-sm font-medium text-slate-900 dark:text-slate-100 mb-2">验证码确认</p>
                      <p className="text-xs text-slate-500 dark:text-slate-400 mb-3">
                        为确保注册成功，请在提交前刷新验证码
                      </p>
                      <div className="flex items-center gap-2">
                        <Input
                          placeholder="请输入验证码"
                          defaultValue={form.getValues("captchaCode")}
                          onChange={(e) => form.setValue("captchaCode", e.target.value, { shouldValidate: true })}
                          className="bg-slate-50 dark:bg-slate-900 w-32"
                        />
                        <Captcha onCaptchaKeyChange={handleCaptchaKeyChange} />
                      </div>
                    </div>

                    <div className="pt-4 flex justify-between">
                      <Button type="button" onClick={() => setActiveTab("email-verify")} variant="outline">
                        返回上一步
                      </Button>
                      <Button type="button" onClick={form.handleSubmit(onSubmit)} disabled={isSubmitting}>
                        {isSubmitting ? "注册中..." : "提交注册"}
                      </Button>
                    </div>
                  </div>
                </TabsContent>
              </Form>
            </Tabs>
          </CardContent>
          <CardFooter className="flex justify-center border-t border-slate-200 dark:border-slate-700 pt-4">
            <p className="text-sm text-slate-500 dark:text-slate-400">
              已有账号？{" "}
              <Link href="/login" className="text-primary hover:underline">
                立即登录
              </Link>
            </p>
          </CardFooter>
        </Card>
      </div>
    </div>
  );
}