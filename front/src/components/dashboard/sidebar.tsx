'use client';

import { usePathname } from 'next/navigation';
import Link from 'next/link';
import { UserRole, Role } from '@/types/auth';
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
  Building2,
  HardDrive,
  Film,
  Database,
  BookOpen as BookOpenIcon,
  FileQuestion,
  Search,
  AlertCircle,
  Settings,
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
    roles: [UserRole.ADMIN, UserRole.USER, UserRole.INSTITUTION],
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
    roles: [UserRole.USER],
  },
  {
    title: '我的订单',
    href: '/dashboard/orders',
    icon: <ShoppingCart className="w-5 h-5" />,
    roles: [UserRole.ADMIN, UserRole.USER, UserRole.INSTITUTION],
  },
  {
    title: '课程搜索',
    href: '/dashboard/course-search',
    icon: <Search className="w-5 h-5" />,
    roles: [UserRole.USER],
  },
  {
    title: '我的课程',
    href: '/dashboard/my-courses',
    icon: <BookOpenIcon className="w-5 h-5" />,
    roles: [UserRole.USER],
  },
  {
    title: '错题本',
    href: '/dashboard/wrong-questions',
    icon: <AlertCircle className="w-5 h-5" />,
    roles: [UserRole.USER],
  },
  {
    title: '资源管理',
    href: '/dashboard/media',
    icon: <Film className="w-5 h-5" />,
    roles: [UserRole.INSTITUTION],
  },
  {
    title: '存储配额',
    href: '/dashboard/storage',
    icon: <Database className="w-5 h-5" />,
    roles: [UserRole.INSTITUTION],
  },
  {
    title: '机构管理',
    href: '/dashboard/institution-management',
    icon: <Settings className="w-5 h-5" />,
    roles: [UserRole.INSTITUTION],
  },
  {
    title: '题库管理',
    href: '/dashboard/questions',
    icon: <FileQuestion className="w-5 h-5" />,
    roles: [UserRole.INSTITUTION],
  },
  {
    title: '课程元数据',
    href: '/dashboard/course-metadata',
    icon: <BookOpen className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
  {
    title: '用户管理',
    href: '/dashboard/users',
    icon: <Users className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
  // 角色管理和权限管理导航已移除，但页面保留
  // {
  //   title: '角色管理',
  //   href: '/dashboard/roles',
  //   icon: <ShieldCheck className="w-5 h-5" />,
  //   roles: [UserRole.ADMIN],
  // },
  // {
  //   title: '权限管理',
  //   href: '/dashboard/permissions',
  //   icon: <ShieldCheck className="w-5 h-5" />,
  //   roles: [UserRole.ADMIN],
  // },
  {
    title: '配额管理',
    href: '/dashboard/admin-quota',
    icon: <HardDrive className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
  {
    title: '课程管理',
    href: '/dashboard/courses',
    icon: <BookOpen className="w-5 h-5" />,
    roles: [UserRole.INSTITUTION],
  },
  {
    title: '机构审核',
    href: '/dashboard/institutions',
    icon: <Building2 className="w-5 h-5" />,
    roles: [UserRole.ADMIN, UserRole.REVIEWER],
  },
  {
    title: '内容审核',
    href: '/dashboard/reviews',
    icon: <FileText className="w-5 h-5" />,
    roles: [UserRole.ADMIN, UserRole.REVIEWER],
  },
  {
    title: '机构课程管理',
    href: '/dashboard/admin-institutions',
    icon: <Building2 className="w-5 h-5" />,
    roles: [UserRole.ADMIN],
  },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { user } = useAuthStore();
  const { sidebarOpen, setSidebarOpen } = useUIStore();

  // 根据用户角色过滤菜单项
  const filteredMenuItems = menuItems.filter(
    (item) => {
      // 如果菜单项没有角色限制，所有人可见
      if (!item.roles) return true;

      // 如果用户不存在或没有角色信息，不显示
      if (!user || !user.roles || user.roles.length === 0) return false;

      // 检查用户角色数组中是否有菜单要求的角色
      return item.roles.some(requiredRole =>
        user.roles.some(userRole => {
          // 从角色代码中提取角色名，考虑多种可能的格式
          const userRoleCode = userRole.code || '';
          const roleName = userRoleCode.replace('ROLE_', '').toUpperCase();
          const requiredRoleUpper = requiredRole.toUpperCase();

          // 输出调试信息
          console.log(`检查角色: 需要 ${requiredRoleUpper}, 用户有 ${roleName} (原始: ${userRoleCode})`);

          // 比较角色是否匹配（忽略大小写，去除前缀）
          return roleName === requiredRoleUpper;
        })
      );
    }
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