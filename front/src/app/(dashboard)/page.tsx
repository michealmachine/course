'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BookOpen, Users, ShoppingCart, Activity } from 'lucide-react';

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

  return (
    <div className="space-y-6">
      {/* 欢迎区域 */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          {greeting}，{user?.username || '同学'}
        </h1>
        <p className="text-muted-foreground mt-2">
          欢迎回到您的在线课程平台仪表盘，这里是您的学习中心。
        </p>
      </div>

      {/* 统计卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
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
      </div>

      {/* 最近课程 */}
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
    </div>
  );
} 