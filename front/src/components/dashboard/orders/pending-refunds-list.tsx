'use client';

import { useEffect, useState } from 'react';
import { orderService } from '@/services';
import { OrderVO, OrderStatus } from '@/types/order';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Loader2, RefreshCw, Check, X } from 'lucide-react';
import { formatPrice, formatDate } from '@/lib/utils';
import { toast } from 'sonner';

export function PendingRefundsList() {
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [pendingRefunds, setPendingRefunds] = useState<OrderVO[]>([]);
  
  // 加载待处理退款数据
  const loadPendingRefunds = async () => {
    setLoading(true);
    try {
      const data = await orderService.getInstitutionPendingRefunds();
      setPendingRefunds(data);
    } catch (error) {
      console.error('获取待处理退款失败:', error);
      toast.error('获取待处理退款失败');
    } finally {
      setLoading(false);
    }
  };
  
  // 初始加载数据
  useEffect(() => {
    loadPendingRefunds();
  }, []);
  
  // 处理退款申请
  const handleProcessRefund = async (orderId: number, approved: boolean) => {
    setProcessing(true);
    try {
      await orderService.processRefund(orderId, approved);
      toast.success(approved ? '已批准退款申请' : '已拒绝退款申请');
      // 移除已处理的订单
      setPendingRefunds((prev) => prev.filter(order => order.id !== orderId));
    } catch (error) {
      console.error('处理退款失败:', error);
      toast.error('处理退款失败，请稍后重试');
    } finally {
      setProcessing(false);
    }
  };
  
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>待处理退款申请</CardTitle>
        <Button 
          variant="outline"
          size="sm"
          onClick={loadPendingRefunds}
          disabled={loading}
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
          刷新
        </Button>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex justify-center py-6">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        ) : pendingRefunds.length === 0 ? (
          <div className="text-center py-6 text-muted-foreground">
            没有待处理的退款申请
          </div>
        ) : (
          <div className="space-y-4">
            {pendingRefunds.map((order) => (
              <div key={order.id} className="p-4 border rounded-md">
                <div className="flex flex-col md:flex-row justify-between mb-2">
                  <div>
                    <h3 className="font-medium">{order.courseTitle}</h3>
                    <p className="text-sm text-muted-foreground">订单号: {order.orderNo}</p>
                  </div>
                  <div className="mt-2 md:mt-0">
                    <Badge>申请退款</Badge>
                  </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                  <div>
                    <p className="text-sm">用户: {order.userName}</p>
                    <p className="text-sm">订单金额: {formatPrice(order.amount)}</p>
                    <p className="text-sm">创建时间: {formatDate(order.createdAt)}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">退款原因:</p>
                    <p className="text-sm mt-1 p-2 bg-muted rounded-md">{order.refundReason || '未提供退款原因'}</p>
                  </div>
                </div>
                
                <Separator className="my-4" />
                
                <div className="flex justify-end gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleProcessRefund(order.id, false)}
                    disabled={processing}
                  >
                    <X className="h-4 w-4 mr-2" />
                    拒绝
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleProcessRefund(order.id, true)}
                    disabled={processing}
                  >
                    <Check className="h-4 w-4 mr-2" />
                    批准
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
} 