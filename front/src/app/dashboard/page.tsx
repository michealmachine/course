'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { UserRole } from '@/types/auth';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { BookOpen, Users, ShoppingCart, Activity, ShieldCheck, FileText, BarChart2 } from 'lucide-react';
import { ClearCacheButton } from '@/components/dashboard/admin/clear-cache-button';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const [greeting, setGreeting] = useState('欢迎');
  const [mounted, setMounted] = useState(false);

  // 确保只在客户端运行
  useEffect(() => {
    setMounted(true);

    // 根据时间设置问候语
    const hours = new Date().getHours();
    let greet = '';

    if (hours < 6) {
      greet = '夜深了';
    } else if (hours < 9) {
      greet = '早上好';
    } else if (hours < 12) {
      greet = '上午好';
    } else if (hours < 14) {
      greet = '中午好';
    } else if (hours < 17) {
      greet = '下午好';
    } else if (hours < 22) {
      greet = '晚上好';
    } else {
      greet = '夜深了';
    }

    setGreeting(greet);
  }, []);

  // 根据用户角色获取角色名称
  const getRoleName = () => {
    if (!user || !user.roles || user.roles.length === 0) return '用户';

    // 获取第一个角色代码（通常是最高权限的角色）
    const primaryRoleCode = user.roles[0].code?.replace('ROLE_', '');

    switch (primaryRoleCode) {
      case UserRole.ADMIN:
        return '管理员';
      case UserRole.REVIEWER:
        return '审核员';
      case UserRole.INSTITUTION:
        return '机构用户';
      case UserRole.USER:
      default:
        return '学员';
    }
  };

  // 检查用户是否有特定角色
  const hasRole = (role: UserRole) => {
    if (!user || !user.roles || user.roles.length === 0) return false;
    return user.roles.some(userRole => userRole.code?.replace('ROLE_', '') === role);
  };

  // 判断是否为管理员
  const isAdmin = hasRole(UserRole.ADMIN);

  // 判断是否为审核员
  const isReviewer = hasRole(UserRole.REVIEWER);

  // 判断是否为机构用户
  const isInstitution = hasRole(UserRole.INSTITUTION);

  return (
    <div className="space-y-6">
      {/* 欢迎区域 */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          {greeting}，{user?.username || '同学'}
        </h1>
        <p className="text-muted-foreground mt-2">
          欢迎回到您的在线课程平台仪表盘，您当前的身份是：<span className="font-medium">{getRoleName()}</span>
        </p>
      </div>

      {/* 角色提示 */}
      {isAdmin && (
        <Alert>
          <ShieldCheck className="h-4 w-4" />
          <AlertTitle>管理员权限</AlertTitle>
          <AlertDescription>
            您拥有管理员权限，可以管理用户、角色和权限，以及查看系统数据。
          </AlertDescription>
        </Alert>
      )}

      {isReviewer && (
        <Alert>
          <FileText className="h-4 w-4" />
          <AlertTitle>审核员权限</AlertTitle>
          <AlertDescription>
            您拥有审核员权限，可以审核课程内容和用户评论。
          </AlertDescription>
        </Alert>
      )}

      {isInstitution && (
        <Alert>
          <BookOpen className="h-4 w-4" />
          <AlertTitle>机构用户权限</AlertTitle>
          <AlertDescription>
            您拥有机构用户权限，可以创建和管理课程，查看学员数据。
          </AlertDescription>
        </Alert>
      )}

      {/* 统计卡片 - 根据角色显示不同内容 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {/* 普通用户和所有角色都可见 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">已学课程</CardTitle>
            <BookOpen className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">12</div>
            <p className="text-xs text-muted-foreground mt-1">
              共计学习 42 小时
            </p>
          </CardContent>
        </Card>

        {/* 普通用户和所有角色都可见 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">学习社区</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">+573</div>
            <p className="text-xs text-muted-foreground mt-1">
              平台活跃学习者
            </p>
          </CardContent>
        </Card>

        {/* 普通用户和所有角色都可见 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">订单记录</CardTitle>
            <ShoppingCart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">5</div>
            <p className="text-xs text-muted-foreground mt-1">
              最近30天内
            </p>
          </CardContent>
        </Card>

        {/* 根据角色显示不同的第四个卡片 */}
        {isAdmin ? (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">系统用户</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">1,234</div>
              <p className="text-xs text-muted-foreground mt-1">
                平台注册用户总数
              </p>
            </CardContent>
          </Card>
        ) : isReviewer ? (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">待审核</CardTitle>
              <FileText className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">8</div>
              <p className="text-xs text-muted-foreground mt-1">
                待审核课程内容
              </p>
            </CardContent>
          </Card>
        ) : isInstitution ? (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">课程数据</CardTitle>
              <BarChart2 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">24</div>
              <p className="text-xs text-muted-foreground mt-1">
                已发布课程总数
              </p>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">学习进度</CardTitle>
              <Activity className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">82%</div>
              <p className="text-xs text-muted-foreground mt-1">
                当前课程完成度
              </p>
            </CardContent>
          </Card>
        )}
      </div>

      {/* 最近课程 - 普通用户和所有角色都可见 */}
      <div>
        <h2 className="text-xl font-semibold mb-4">最近学习</h2>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <CardHeader className="pb-2">
                <CardTitle className="text-lg">Web开发进阶课程 {i}</CardTitle>
                <CardDescription>前端框架与工程化实践</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-2 w-full bg-muted rounded-full overflow-hidden">
                  <div
                    className="h-full bg-primary rounded-full"
                    style={{ width: `${30 * i}%` }}
                  />
                </div>
                <div className="text-sm text-muted-foreground mt-2">
                  完成度: {30 * i}%
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* 管理员专属内容 */}
      {isAdmin && (
        <div>
          <h2 className="text-xl font-semibold mb-4">系统概览</h2>
          <Card>
            <CardHeader>
              <CardTitle>管理员控制面板</CardTitle>
              <CardDescription>系统关键指标和操作</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span>系统用户总数</span>
                  <span className="font-medium">1,234</span>
                </div>
                <div className="flex justify-between">
                  <span>今日新增用户</span>
                  <span className="font-medium">12</span>
                </div>
                <div className="flex justify-between">
                  <span>课程总数</span>
                  <span className="font-medium">156</span>
                </div>
                <div className="flex justify-between">
                  <span>系统存储使用</span>
                  <span className="font-medium">45.8 GB</span>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-end">
              <ClearCacheButton />
            </CardFooter>
          </Card>
        </div>
      )}

      {/* 审核员专属内容 */}
      {isReviewer && (
        <div>
          <h2 className="text-xl font-semibold mb-4">待审核内容</h2>
          <Card>
            <CardHeader>
              <CardTitle>内容审核队列</CardTitle>
              <CardDescription>待审核的课程和评论</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span>待审核课程</span>
                  <span className="font-medium">8</span>
                </div>
                <div className="flex justify-between">
                  <span>待审核评论</span>
                  <span className="font-medium">24</span>
                </div>
                <div className="flex justify-between">
                  <span>今日已审核</span>
                  <span className="font-medium">12</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* 机构用户专属内容 */}
      {isInstitution && (
        <div>
          <h2 className="text-xl font-semibold mb-4">机构数据</h2>
          <Card>
            <CardHeader>
              <CardTitle>课程统计</CardTitle>
              <CardDescription>您的课程数据概览</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span>已发布课程</span>
                  <span className="font-medium">24</span>
                </div>
                <div className="flex justify-between">
                  <span>学员总数</span>
                  <span className="font-medium">1,568</span>
                </div>
                <div className="flex justify-between">
                  <span>本月收入</span>
                  <span className="font-medium">¥12,450</span>
                </div>
                <div className="flex justify-between">
                  <span>平均评分</span>
                  <span className="font-medium">4.8/5.0</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}