'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { ArrowLeft, Building2, Check, X } from 'lucide-react';
import React from 'react';

import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import reviewerInstitutionService from '@/services/reviewerInstitution';
import { InstitutionApplicationResponse, InstitutionResponse } from '@/types/institution';

interface PageParams {
  params: {
    id: string;
  };
}

export default function InstitutionDetailPage({ params }: PageParams) {
  const router = useRouter();
  const unwrappedParams = React.use(params as any) as { id: string };
  const id = parseInt(unwrappedParams.id);
  
  const [application, setApplication] = useState<InstitutionApplicationResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isApproving, setIsApproving] = useState(false);
  const [isRejecting, setIsRejecting] = useState(false);
  const [showRejectDialog, setShowRejectDialog] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  // 定义状态映射
  const statusMap = {
    0: { label: '待审核', color: 'bg-yellow-100 text-yellow-800 border-yellow-200' },
    1: { label: '已通过', color: 'bg-green-100 text-green-800 border-green-200' },
    2: { label: '已拒绝', color: 'bg-red-100 text-red-800 border-red-200' },
  };

  // 获取申请详情
  useEffect(() => {
    if (isNaN(id)) {
      toast.error('无效的申请ID');
      router.push('/dashboard/institutions');
      return;
    }

    const fetchApplicationDetail = async () => {
      setIsLoading(true);
      try {
        const data = await reviewerInstitutionService.getApplicationDetail(id);
        setApplication(data);
      } catch (error) {
        console.error('获取申请详情失败', error);
        toast.error('获取申请详情失败，请重试');
      } finally {
        setIsLoading(false);
      }
    };

    fetchApplicationDetail();
  }, [id, router]);

  // 通过申请
  const handleApprove = async () => {
    if (!application) return;
    
    setIsApproving(true);
    try {
      const institution = await reviewerInstitutionService.approveApplication(id);
      toast.success('审核通过成功');
      
      // 更新状态
      setApplication({
        ...application,
        status: 1,
        institutionId: institution.id,
        reviewedAt: new Date().toISOString(),
      });
    } catch (error) {
      console.error('审核通过失败', error);
      toast.error('审核操作失败，请重试');
    } finally {
      setIsApproving(false);
    }
  };

  // 拒绝申请
  const handleReject = async () => {
    if (!application || !rejectReason.trim()) return;
    
    setIsRejecting(true);
    try {
      await reviewerInstitutionService.rejectApplication(id, rejectReason);
      toast.success('审核拒绝成功');
      
      // 更新状态
      setApplication({
        ...application,
        status: 2,
        reviewComment: rejectReason,
        reviewedAt: new Date().toISOString(),
      });
      
      // 关闭弹窗
      setShowRejectDialog(false);
    } catch (error) {
      console.error('审核拒绝失败', error);
      toast.error('审核操作失败，请重试');
    } finally {
      setIsRejecting(false);
    }
  };

  // 状态显示
  const getStatusDisplay = (status: number) => {
    const statusInfo = statusMap[status as keyof typeof statusMap];
    return (
      <Badge className={`${statusInfo.color} border`}>
        {statusInfo.label}
      </Badge>
    );
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" disabled>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <Skeleton className="h-8 w-60" />
        </div>
        
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-40" />
            <Skeleton className="h-4 w-60" />
          </CardHeader>
          <CardContent className="space-y-6">
            {Array.from({ length: 6 }).map((_, index) => (
              <div key={index} className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-6 w-full" />
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!application) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <h2 className="text-3xl font-bold tracking-tight">申请详情</h2>
        </div>
        
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16">
            <Building2 className="h-16 w-16 mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">未找到申请记录</h3>
            <p className="text-muted-foreground mb-4">
              无法获取ID为 {id} 的申请详情
            </p>
            <Button onClick={() => router.push('/dashboard/institutions')}>
              返回申请列表
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="icon"
          onClick={() => router.push('/dashboard/institutions')}
        >
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <h2 className="text-3xl font-bold tracking-tight">申请详情</h2>
      </div>

      <Card>
        <CardHeader className="flex flex-col md:flex-row justify-between md:items-center space-y-2 md:space-y-0">
          <div>
            <CardTitle className="text-2xl">{application.name}</CardTitle>
            <CardDescription>
              申请ID：{application.applicationId}
            </CardDescription>
          </div>
          {getStatusDisplay(application.status)}
        </CardHeader>
        
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-1">
              <h3 className="text-sm font-medium text-muted-foreground">联系人</h3>
              <p>{application.contactPerson}</p>
            </div>
            
            <div className="space-y-1">
              <h3 className="text-sm font-medium text-muted-foreground">联系电话</h3>
              <p>{application.contactPhone || '未提供'}</p>
            </div>
            
            <div className="space-y-1">
              <h3 className="text-sm font-medium text-muted-foreground">联系邮箱</h3>
              <p>{application.contactEmail}</p>
            </div>
            
            <div className="space-y-1">
              <h3 className="text-sm font-medium text-muted-foreground">地址</h3>
              <p>{application.address || '未提供'}</p>
            </div>
            
            <div className="space-y-1">
              <h3 className="text-sm font-medium text-muted-foreground">申请时间</h3>
              <p>{new Date(application.createdAt).toLocaleString()}</p>
            </div>
            
            {application.reviewedAt && (
              <div className="space-y-1">
                <h3 className="text-sm font-medium text-muted-foreground">审核时间</h3>
                <p>{new Date(application.reviewedAt).toLocaleString()}</p>
              </div>
            )}
          </div>

          {application.logo && (
            <div className="space-y-2">
              <h3 className="text-sm font-medium text-muted-foreground">机构LOGO</h3>
              <div className="h-40 w-40 rounded-md border overflow-hidden flex items-center justify-center">
                <img
                  src={application.logo}
                  alt={`${application.name} 的LOGO`}
                  className="max-h-full max-w-full object-contain"
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.src = 'https://via.placeholder.com/150?text=无图片';
                  }}
                />
              </div>
            </div>
          )}

          <div className="space-y-2">
            <h3 className="text-sm font-medium text-muted-foreground">机构描述</h3>
            <div className="rounded-md border p-4 bg-muted/30">
              <p className="whitespace-pre-wrap">
                {application.description || '未提供描述'}
              </p>
            </div>
          </div>

          {application.status === 2 && application.reviewComment && (
            <div className="space-y-2">
              <h3 className="text-sm font-medium text-muted-foreground">拒绝原因</h3>
              <div className="rounded-md border border-red-200 p-4 bg-red-50 text-red-800">
                <p className="whitespace-pre-wrap">{application.reviewComment}</p>
              </div>
            </div>
          )}
        </CardContent>

        <Separator />

        <CardFooter className="flex justify-between p-6">
          <Button
            variant="outline"
            onClick={() => router.push('/dashboard/institutions')}
          >
            返回列表
          </Button>

          {application.status === 0 && (
            <div className="flex gap-2">
              <Button
                variant="destructive"
                onClick={() => setShowRejectDialog(true)}
                disabled={isApproving || isRejecting}
              >
                <X className="mr-2 h-4 w-4" />
                拒绝申请
              </Button>
              <Button
                variant="default"
                onClick={handleApprove}
                disabled={isApproving || isRejecting}
              >
                {isApproving ? (
                  <span className="flex items-center">
                    <span className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                    处理中...
                  </span>
                ) : (
                  <>
                    <Check className="mr-2 h-4 w-4" />
                    通过申请
                  </>
                )}
              </Button>
            </div>
          )}
        </CardFooter>
      </Card>

      {/* 拒绝理由弹窗 */}
      <Dialog open={showRejectDialog} onOpenChange={setShowRejectDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>拒绝申请</DialogTitle>
            <DialogDescription>
              请输入拒绝原因，该信息将发送给申请人
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="rejectReason">拒绝原因</Label>
              <Textarea
                id="rejectReason"
                placeholder="请输入拒绝原因..."
                rows={5}
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
              />
            </div>
          </div>
          
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setShowRejectDialog(false)}
              disabled={isRejecting}
            >
              取消
            </Button>
            <Button
              variant="destructive"
              onClick={handleReject}
              disabled={isRejecting || !rejectReason.trim()}
            >
              {isRejecting ? (
                <span className="flex items-center">
                  <span className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                  处理中...
                </span>
              ) : (
                '确认拒绝'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 