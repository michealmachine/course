'use client';

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import Link from "next/link";

export default function InstitutionPage() {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl w-full space-y-8">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">机构中心</h1>
          <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
            欢迎来到机构中心，您可以申请机构入驻或注册机构账号
          </p>
        </div>
        
        <div className="grid md:grid-cols-2 gap-6">
          <Card className="bg-white dark:bg-slate-800 shadow hover:shadow-md transition-shadow border-none">
            <CardHeader className="pb-3">
              <CardTitle className="text-xl text-slate-900 dark:text-slate-100">申请机构入驻</CardTitle>
              <CardDescription className="text-slate-500 dark:text-slate-400">
                成为我们认证的机构，发布优质课程并获得更多曝光
              </CardDescription>
            </CardHeader>
            <CardContent className="text-slate-600 dark:text-slate-300 pt-0">
              <p className="text-sm">
                填写机构资料并提交审核，平台将在1-3个工作日内完成审核。
                审核通过后，您将获得机构注册码，用于注册机构管理员账号。
              </p>
            </CardContent>
            <CardFooter>
              <Button asChild className="w-full">
                <Link href="/institution/apply">立即申请</Link>
              </Button>
            </CardFooter>
          </Card>

          <Card className="bg-white dark:bg-slate-800 shadow hover:shadow-md transition-shadow border-none">
            <CardHeader className="pb-3">
              <CardTitle className="text-xl text-slate-900 dark:text-slate-100">机构用户注册</CardTitle>
              <CardDescription className="text-slate-500 dark:text-slate-400">
                使用机构注册码注册机构管理员或教师账号
              </CardDescription>
            </CardHeader>
            <CardContent className="text-slate-600 dark:text-slate-300 pt-0">
              <p className="text-sm">
                如果您已获得机构的注册码，可以直接注册成为该机构的用户。
                机构管理员可以管理课程、学员等资源，并邀请更多教师加入。
              </p>
            </CardContent>
            <CardFooter>
              <Button asChild variant="outline" className="w-full">
                <Link href="/institution/register">立即注册</Link>
              </Button>
            </CardFooter>
          </Card>
        </div>
        
        <div className="mt-10 bg-white dark:bg-slate-800 p-8 rounded-lg shadow text-center">
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-4">已提交申请?</h2>
          <p className="mb-6 text-slate-600 dark:text-slate-300 text-sm">
            查询您的申请状态，了解审核进度
          </p>
          <Button asChild variant="secondary">
            <Link href="/institution/status">查询申请状态</Link>
          </Button>
        </div>
        
        <div className="mt-8 pt-6 border-t border-slate-200 dark:border-slate-700 text-center">
          <Link href="/" className="text-primary hover:underline text-sm">
            返回首页
          </Link>
        </div>
      </div>
    </div>
  );
} 