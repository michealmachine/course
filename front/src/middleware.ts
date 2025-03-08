import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// 需要保护的路由前缀
const PROTECTED_PATHS = ['/dashboard'];

// 不需要认证的路由
const PUBLIC_PATHS = ['/login', '/register', '/courses', '/'];

// 路由中间件
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  
  // 检查是否是受保护的路由
  const isProtectedPath = PROTECTED_PATHS.some(path => pathname.startsWith(path));
  
  // 如果不是受保护的路由，直接放行
  if (!isProtectedPath) return NextResponse.next();
  
  // 获取令牌
  const token = request.cookies.get('token')?.value;
  
  // 如果没有令牌，重定向到登录页
  if (!token) {
    // 创建登录重定向URL，包含原始目标URL作为参数
    const redirectUrl = new URL('/login', request.url);
    redirectUrl.searchParams.set('redirectTo', pathname);
    
    return NextResponse.redirect(redirectUrl);
  }
  
  // 有令牌，放行请求
  return NextResponse.next();
}

// 配置需要执行中间件的路径
export const config = {
  matcher: [
    // 需要保护的路由
    '/dashboard/:path*',
    // 也可以添加其他需要保护的路由
  ],
}; 