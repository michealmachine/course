'use client';

import { usePathname } from 'next/navigation';
import Link from 'next/link';
import { UserRole } from '@/types/auth';
import { useAuthStore } from '@/stores/auth-store';
import { useUIStore } from '@/stores/ui-store';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import {
  Home,
  User,
  Users,
  ShieldCheck,
  BookOpen,
  FileText,
  BarChart2,
  Heart,
  ShoppingCart,
  Menu,
  X,
} from 'lucide-react';

// 侧边栏菜单项
interface MenuItem {
  title: string;
  href: string;
  icon: React.ReactNode;
  roles?: UserRole[];
}

const menuItems: MenuItem[] = [
  {
    title: '仪表盘',
    href: '/dashboard',
    icon: <Home className="w-5 h-5" />,
  },
  {
    title: '个人资料',
    href: '/dashboard/profile',
    icon: <User className="w-5 h-5" />,
  },
  {
    title: '我的收藏',
    href: '/dashboard/favorites',
    icon: <Heart className="w-5 h-5" />,
  },
  {
    title: '我的订单',
    href: '/dashboard/orders',
    icon: <ShoppingCart className="w-5 h-5" />,
  },
  {
    title: '用户管理',
    href: '/dashboard/users',
    icon: <Users className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
  {
    title: '角色管理',
    href: '/dashboard/roles',
    icon: <ShieldCheck className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
  {
    title: '权限管理',
    href: '/dashboard/permissions',
    icon: <ShieldCheck className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
  {
    title: '课程管理',
    href: '/dashboard/courses',
    icon: <BookOpen className="w-5 h-5" />,
    roles: [UserRole.ADMIN, UserRole.TEACHER],
  },
  {
    title: '内容审核',
    href: '/dashboard/reviews',
    icon: <FileText className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
  {
    title: '统计数据',
    href: '/dashboard/statistics',
    icon: <BarChart2 className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { user } = useAuthStore();
  const { sidebarOpen, setSidebarOpen } = useUIStore();

  // 根据用户角色过滤菜单项
  const filteredMenuItems = menuItems.filter(
    (item) => !item.roles || (user && item.roles.includes(user.role))
  );

  return (
    <>
      {/* 移动端侧边栏遮罩 */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-background/80 backdrop-blur-sm md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* 侧边栏 */}
      <aside
        className={cn(
          'fixed top-0 left-0 z-50 h-full w-72 bg-card border-r shadow-sm md:static md:z-0',
          'transform transition-transform duration-200 ease-in-out',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        )}
      >
        <div className="flex h-16 items-center justify-between px-4 py-4">
          <Link href="/" className="flex items-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="mr-2 text-primary"
            >
              <path d="M22 2 11 13"></path>
              <path d="m22 2-7 20-4-9-9-4 20-7z"></path>
            </svg>
            <span className="text-xl font-semibold">课程平台</span>
          </Link>
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={() => setSidebarOpen(false)}
          >
            <X className="h-5 w-5" />
          </Button>
        </div>

        <Separator />

        <nav className="flex flex-col gap-1 p-4">
          {filteredMenuItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors',
                pathname === item.href
                  ? 'bg-primary text-primary-foreground'
                  : 'hover:bg-muted'
              )}
            >
              {item.icon}
              <span>{item.title}</span>
            </Link>
          ))}
        </nav>
      </aside>
    </>
  );
} 