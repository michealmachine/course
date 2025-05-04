'use client';

import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import { Building2, Copy } from 'lucide-react';
import { toast } from 'sonner';
import { InstitutionVO } from '@/types/institution';

interface InstitutionBasicInfoProps {
  institution: InstitutionVO;
  isLoading: boolean;
}

export function InstitutionBasicInfo({ institution, isLoading }: InstitutionBasicInfoProps) {
  const copyRegisterCode = () => {
    if (institution.registerCode) {
      navigator.clipboard.writeText(institution.registerCode);
      toast.success('注册码已复制到剪贴板');
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center space-x-4">
          <Skeleton className="h-16 w-16 rounded-md" />
          <div className="space-y-2">
            <Skeleton className="h-4 w-[250px]" />
            <Skeleton className="h-4 w-[200px]" />
          </div>
        </div>
        <Separator />
        <div className="space-y-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="grid grid-cols-3 gap-4">
              <Skeleton className="h-4 w-[100px]" />
              <Skeleton className="h-4 w-full col-span-2" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-start space-x-4">
        <div className="h-16 w-16 rounded-md bg-muted flex items-center justify-center overflow-hidden">
          {institution.logo ? (
            <img 
              src={institution.logo} 
              alt={institution.name} 
              className="h-full w-full object-cover"
            />
          ) : (
            <Building2 className="h-8 w-8 text-muted-foreground" />
          )}
        </div>
        <div>
          <h3 className="text-lg font-medium">{institution.name}</h3>
          <p className="text-sm text-muted-foreground">
            创建于 {formatDate(institution.createdAt)}
          </p>
        </div>
      </div>
      
      <Separator />
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <h4 className="text-sm font-medium">基本信息</h4>
          
          <div className="text-sm">
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">机构ID:</span>
              <span className="col-span-2">{institution.id}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">机构状态:</span>
              <span className="col-span-2">
                {institution.status === 1 ? '正常' : '禁用'}
              </span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">机构描述:</span>
              <span className="col-span-2">{institution.description || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">注册码:</span>
              <div className="col-span-2 flex items-center">
                <code className="bg-muted px-2 py-1 rounded text-xs">
                  {institution.registerCode || '-'}
                </code>
                {institution.registerCode && (
                  <Button variant="ghost" size="icon" className="h-8 w-8 ml-1" onClick={copyRegisterCode}>
                    <Copy className="h-4 w-4" />
                  </Button>
                )}
              </div>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">更新时间:</span>
              <span className="col-span-2">{formatDate(institution.updatedAt)}</span>
            </div>
          </div>
        </div>
        
        <div className="space-y-2">
          <h4 className="text-sm font-medium">联系信息</h4>
          
          <div className="text-sm">
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">联系人:</span>
              <span className="col-span-2">{institution.contactPerson || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">联系电话:</span>
              <span className="col-span-2">{institution.contactPhone || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">联系邮箱:</span>
              <span className="col-span-2">{institution.contactEmail || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">地址:</span>
              <span className="col-span-2">{institution.address || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-2">
              <span className="text-muted-foreground">网站:</span>
              <span className="col-span-2">
                {institution.website ? (
                  <a 
                    href={institution.website} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="text-blue-600 hover:underline"
                  >
                    {institution.website}
                  </a>
                ) : '-'}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
