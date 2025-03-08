import { Metadata } from 'next';
import Link from 'next/link';
import Image from 'next/image';

export const metadata: Metadata = {
  title: '认证 - 在线课程平台',
  description: '登录或注册在线课程平台账户',
};

export default function AuthLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="min-h-screen flex flex-col md:flex-row">
      {/* 左侧品牌区域 */}
      <div className="w-full md:w-1/2 bg-primary p-8 flex flex-col justify-between text-white">
        <div>
          <div className="mb-4">
            <Link href="/" className="text-xl font-bold flex items-center">
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
                className="mr-2"
              >
                <path d="M22 2 11 13"></path>
                <path d="m22 2-7 20-4-9-9-4 20-7z"></path>
              </svg>
              在线课程平台
            </Link>
          </div>
          <h1 className="text-3xl md:text-4xl font-bold mb-4">欢迎来到在线教育新时代</h1>
          <p className="text-lg mb-6">
            我们提供高质量的在线课程，帮助您在任何时间、任何地点拓展知识和技能。
          </p>
        </div>

        <div className="hidden md:block">
          <p className="text-sm">© {new Date().getFullYear()} 在线课程平台. 保留所有权利.</p>
        </div>
      </div>

      {/* 右侧表单区域 */}
      <div className="w-full md:w-1/2 p-8 flex items-center justify-center">
        <div className="w-full max-w-md">
          {children}
        </div>
      </div>
    </div>
  );
} 