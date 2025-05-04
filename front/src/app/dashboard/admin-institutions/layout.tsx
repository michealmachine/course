import { Metadata } from 'next';

export const metadata: Metadata = {
  title: '机构课程管理 | 课程平台',
  description: '管理平台上的所有机构及其课程，查看统计数据和用户信息。',
};

export default function AdminInstitutionsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="p-6">
      {children}
    </div>
  );
}
