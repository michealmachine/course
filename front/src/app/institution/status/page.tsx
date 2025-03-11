'use client';

import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import Link from "next/link";

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import institutionService from "@/services/institution";  // 导入机构服务

// 定义表单验证Schema
const querySchema = z.object({
  applicationId: z.string().min(1, "申请ID不能为空"),
  email: z.string().email("请输入有效的邮箱地址"),
});

// 定义申请状态类型
const statusMap = {
  0: { label: "待审核", color: "bg-yellow-100 text-yellow-800 border-yellow-200" },
  1: { label: "已通过", color: "bg-green-100 text-green-800 border-green-200" },
  2: { label: "已拒绝", color: "bg-red-100 text-red-800 border-red-200" },
};

// 模拟申请数据类型
interface ApplicationData {
  applicationId: string;
  name: string;
  contactPerson: string;
  contactEmail: string;
  contactPhone: string | null;
  status: 0 | 1 | 2;
  createdAt: string;
  reviewComment?: string;
  reviewedAt?: string;
}

export default function InstitutionStatusPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [application, setApplication] = useState<ApplicationData | null>(null);
  const [isSearch, setIsSearch] = useState<boolean>(false);
  
  // 从URL参数获取
  const applicationId = searchParams.get("id");
  const email = searchParams.get("email");
  
  // 初始化表单
  const form = useForm<z.infer<typeof querySchema>>({
    resolver: zodResolver(querySchema),
    defaultValues: {
      applicationId: applicationId || "",
      email: email || "",
    },
  });
  
  // 如果有URL参数，自动查询
  useEffect(() => {
    if (applicationId && email) {
      fetchApplication(applicationId, email);
    }
  }, [applicationId, email]);
  
  // 查询申请状态
  const onSubmit = async (data: z.infer<typeof querySchema>) => {
    fetchApplication(data.applicationId, data.email);
  };
  
  // 获取申请状态
  const fetchApplication = async (applicationId: string, email: string) => {
    setIsLoading(true);
    setIsSearch(true);
    
    try {
      // 调用API查询申请状态
      const application = await institutionService.getApplicationStatus(applicationId, email);
      setApplication(application);
      
      if (application.status === 1) {
        toast.success("恭喜！您的申请已通过审核");
      } else if (application.status === 2) {
        toast.error("抱歉，您的申请未通过审核");
      }
    } catch (error: any) {
      console.error("查询失败:", error);
      toast.error(error.message || "查询失败，请稍后重试");
      setApplication(null);
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl w-full">
        <Card className="bg-white dark:bg-slate-800 shadow border-none">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-center text-slate-900 dark:text-slate-100">申请状态查询</CardTitle>
            <CardDescription className="text-center text-slate-500 dark:text-slate-400">
              查询机构入驻申请的审核状态
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                <div className="grid md:grid-cols-2 gap-4">
                  <FormField
                    control={form.control}
                    name="applicationId"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel className="text-slate-700 dark:text-slate-300">申请ID</FormLabel>
                        <FormControl>
                          <Input placeholder="请输入申请ID" {...field} className="bg-slate-50 dark:bg-slate-900" />
                        </FormControl>
                        <FormMessage className="text-red-500" />
                      </FormItem>
                    )}
                  />
                  
                  <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel className="text-slate-700 dark:text-slate-300">联系邮箱</FormLabel>
                        <FormControl>
                          <Input placeholder="请输入申请时使用的邮箱" {...field} className="bg-slate-50 dark:bg-slate-900" />
                        </FormControl>
                        <FormMessage className="text-red-500" />
                      </FormItem>
                    )}
                  />
                </div>
                
                <div className="flex justify-between gap-4">
                  <Button type="submit" disabled={isLoading}>
                    {isLoading ? "查询中..." : "查询申请状态"}
                  </Button>
                  
                  <Button type="button" variant="outline" asChild>
                    <Link href="/institution">返回</Link>
                  </Button>
                </div>
              </form>
            </Form>
            
            {isLoading && (
              <div className="mt-8 space-y-4">
                <Skeleton className="h-8 w-full" />
                <Skeleton className="h-20 w-full" />
                <Skeleton className="h-16 w-full" />
              </div>
            )}
            
            {!isLoading && isSearch && application && (
              <div className="mt-8 space-y-6 border border-slate-200 dark:border-slate-700 rounded-lg p-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100">{application.name}</h3>
                  <Badge className={`${statusMap[application.status].color} border`}>
                    {statusMap[application.status].label}
                  </Badge>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-400">申请ID</p>
                    <p className="font-medium text-slate-900 dark:text-slate-100">{application.applicationId}</p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-400">联系人</p>
                    <p className="font-medium text-slate-900 dark:text-slate-100">{application.contactPerson}</p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-400">联系邮箱</p>
                    <p className="font-medium text-slate-900 dark:text-slate-100">{application.contactEmail}</p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-400">联系电话</p>
                    <p className="font-medium text-slate-900 dark:text-slate-100">{application.contactPhone || '-'}</p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-400">申请时间</p>
                    <p className="font-medium text-slate-900 dark:text-slate-100">{new Date(application.createdAt).toLocaleString()}</p>
                  </div>
                  {application.reviewedAt && (
                    <div>
                      <p className="text-sm text-slate-500 dark:text-slate-400">审核时间</p>
                      <p className="font-medium text-slate-900 dark:text-slate-100">{new Date(application.reviewedAt).toLocaleString()}</p>
                    </div>
                  )}
                </div>
                
                {application.status === 1 && (
                  <div className="bg-green-50 dark:bg-green-900/20 p-4 rounded-md border border-green-200 dark:border-green-900">
                    <p className="font-medium text-green-800 dark:text-green-300">恭喜！您的机构申请已通过审核</p>
                    <p className="text-green-700 dark:text-green-400 mt-1 text-sm">请使用审核通过邮件中的机构注册码注册机构管理员账号。</p>
                    <div className="mt-4">
                      <Button asChild size="sm" className="bg-green-600 hover:bg-green-700">
                        <Link href="/institution/register">前往注册机构账号</Link>
                      </Button>
                    </div>
                  </div>
                )}
                
                {application.status === 2 && application.reviewComment && (
                  <div className="bg-red-50 dark:bg-red-900/20 p-4 rounded-md border border-red-200 dark:border-red-900">
                    <p className="font-medium text-red-800 dark:text-red-300">申请被拒绝</p>
                    <p className="text-red-700 dark:text-red-400 mt-1 text-sm">拒绝原因：{application.reviewComment}</p>
                    <div className="mt-4">
                      <Button asChild size="sm" variant="outline" className="border-red-300 text-red-700 hover:bg-red-50 dark:border-red-800 dark:text-red-300 dark:hover:bg-red-900/30">
                        <Link href="/institution/apply">重新申请</Link>
                      </Button>
                    </div>
                  </div>
                )}
                
                {application.status === 0 && (
                  <div className="bg-yellow-50 dark:bg-yellow-900/20 p-4 rounded-md border border-yellow-200 dark:border-yellow-900">
                    <p className="font-medium text-yellow-800 dark:text-yellow-300">您的申请正在审核中</p>
                    <p className="text-yellow-700 dark:text-yellow-400 mt-1 text-sm">我们会在1-3个工作日内完成审核，审核结果将发送到您的邮箱。</p>
                  </div>
                )}
              </div>
            )}
            
            {!isLoading && isSearch && !application && (
              <div className="mt-8 bg-red-50 dark:bg-red-900/20 p-4 rounded-md border border-red-200 dark:border-red-900">
                <p className="font-medium text-red-800 dark:text-red-300">未找到申请记录</p>
                <p className="text-red-700 dark:text-red-400 mt-1 text-sm">请检查申请ID和邮箱是否正确，或联系客服寻求帮助。</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
} 