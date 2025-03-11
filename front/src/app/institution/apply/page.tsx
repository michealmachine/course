'use client';

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import Link from "next/link";

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Captcha } from "@/components/ui/captcha";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useAuthStore } from "@/stores/auth-store";
import institutionService from "@/services/institution";  // 导入机构服务

// 定义表单验证Schema
const applySchema = z.object({
  name: z.string()
    .min(2, "机构名称至少需要2个字符")
    .max(100, "机构名称最多100个字符"),
  logo: z.string().url("请输入有效的图片URL").optional().or(z.literal("")),
  description: z.string()
    .max(500, "机构描述最多500个字符")
    .optional(),
  contactPerson: z.string()
    .min(2, "联系人姓名至少需要2个字符")
    .max(50, "联系人姓名最多50个字符"),
  contactPhone: z.string()
    .regex(/^1[3-9]\d{9}$/, "请输入有效的手机号码")
    .optional(),
  contactEmail: z.string()
    .email("请输入有效的邮箱地址"),
  address: z.string()
    .max(255, "地址最多255个字符")
    .optional(),
  captchaKey: z.string().min(1, "验证码key不能为空"),
  captchaCode: z.string().min(1, "验证码不能为空"),
});

type ApplyFormValues = z.infer<typeof applySchema>;

export default function InstitutionApplyPage() {
  const router = useRouter();
  const [captchaKey, setCaptchaKey] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const { clearError } = useAuthStore();
  
  // 初始化表单
  const form = useForm<ApplyFormValues>({
    resolver: zodResolver(applySchema),
    defaultValues: {
      name: "",
      logo: "",
      description: "",
      contactPerson: "",
      contactPhone: "",
      contactEmail: "",
      address: "",
      captchaKey: "",
      captchaCode: ""
    }
  });
  
  // 处理验证码Key变化
  const handleCaptchaKeyChange = (newCaptchaKey: string) => {
    setCaptchaKey(newCaptchaKey);
    form.setValue("captchaKey", newCaptchaKey);
  };
  
  // 提交申请
  const onSubmit = async (data: ApplyFormValues) => {
    clearError();
    setIsSubmitting(true);
    
    try {
      // 调用API提交机构申请
      const applicationId = await institutionService.applyInstitution(data);
      
      toast.success("申请提交成功！");
      
      // 跳转到申请状态查询页面
      router.push(`/institution/status?id=${applicationId}&email=${encodeURIComponent(data.contactEmail)}`);
    } catch (error: any) {
      console.error("申请提交失败:", error);
      toast.error(error.message || "申请提交失败，请稍后重试");
    } finally {
      setIsSubmitting(false);
    }
  };
  
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl w-full">
        <Card className="bg-white dark:bg-slate-800 shadow border-none">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-center text-slate-900 dark:text-slate-100">申请机构入驻</CardTitle>
            <CardDescription className="text-center text-slate-500 dark:text-slate-400">
              填写机构信息，提交申请后将等待平台审核
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                <Alert className="bg-blue-50 dark:bg-blue-950 text-blue-800 dark:text-blue-300 border-blue-200 dark:border-blue-900">
                  <AlertDescription>
                    请填写真实信息，以便我们能够及时联系您，并确保审核顺利通过。
                  </AlertDescription>
                </Alert>
                
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-slate-700 dark:text-slate-300">机构名称</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入机构名称" {...field} className="bg-slate-50 dark:bg-slate-900" />
                      </FormControl>
                      <FormDescription className="text-slate-500 dark:text-slate-400 text-xs">
                        您的机构的官方名称
                      </FormDescription>
                      <FormMessage className="text-red-500" />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="logo"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-slate-700 dark:text-slate-300">机构LOGO地址（可选）</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入图片URL地址" {...field} className="bg-slate-50 dark:bg-slate-900" />
                      </FormControl>
                      <FormDescription className="text-slate-500 dark:text-slate-400 text-xs">
                        输入公开可访问的图片URL，建议使用正方形图片
                      </FormDescription>
                      <FormMessage className="text-red-500" />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="description"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-slate-700 dark:text-slate-300">机构描述（可选）</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="请输入机构介绍"
                          className="min-h-[100px] bg-slate-50 dark:bg-slate-900"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription className="text-slate-500 dark:text-slate-400 text-xs">
                        简要介绍您的机构背景、专业领域等信息
                      </FormDescription>
                      <FormMessage className="text-red-500" />
                    </FormItem>
                  )}
                />
                
                <div className="grid md:grid-cols-2 gap-4">
                  <FormField
                    control={form.control}
                    name="contactPerson"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel className="text-slate-700 dark:text-slate-300">联系人</FormLabel>
                        <FormControl>
                          <Input placeholder="请输入联系人姓名" {...field} className="bg-slate-50 dark:bg-slate-900" />
                        </FormControl>
                        <FormMessage className="text-red-500" />
                      </FormItem>
                    )}
                  />
                  
                  <FormField
                    control={form.control}
                    name="contactPhone"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel className="text-slate-700 dark:text-slate-300">联系电话（可选）</FormLabel>
                        <FormControl>
                          <Input placeholder="请输入联系电话" {...field} className="bg-slate-50 dark:bg-slate-900" />
                        </FormControl>
                        <FormMessage className="text-red-500" />
                      </FormItem>
                    )}
                  />
                </div>
                
                <FormField
                  control={form.control}
                  name="contactEmail"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-slate-700 dark:text-slate-300">联系邮箱</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入邮箱地址" {...field} className="bg-slate-50 dark:bg-slate-900" />
                      </FormControl>
                      <FormDescription className="text-slate-500 dark:text-slate-400 text-xs">
                        审核结果将发送到此邮箱
                      </FormDescription>
                      <FormMessage className="text-red-500" />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="address"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-slate-700 dark:text-slate-300">机构地址（可选）</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入机构地址" {...field} className="bg-slate-50 dark:bg-slate-900" />
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
                      <FormLabel className="text-slate-700 dark:text-slate-300">验证码</FormLabel>
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
                
                <div className="flex flex-col gap-4 pt-2">
                  <Button type="submit" disabled={isSubmitting} className="w-full">
                    {isSubmitting ? "提交中..." : "提交申请"}
                  </Button>
                  
                  <Button type="button" variant="outline" asChild className="w-full">
                    <Link href="/institution">返回</Link>
                  </Button>
                </div>
              </form>
            </Form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
} 