'use client';

import { useState, useEffect, useRef } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { orderService } from '@/services';
import { OrderVO, OrderStatus, OrderSearchDTO } from '@/types/order';
import { PaginationResult } from '@/types/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Loader2, RefreshCw, Search, X, Filter, Clock } from 'lucide-react';
import { formatDate, formatPrice } from '@/lib/utils';
import { toast } from 'sonner';

// 订单状态映射
const orderStatusMap = {
  [OrderStatus.PENDING]: { label: '待支付', color: 'bg-yellow-100 text-yellow-800' },
  [OrderStatus.PAID]: { label: '已支付', color: 'bg-green-100 text-green-800' },
  [OrderStatus.CLOSED]: { label: '已关闭', color: 'bg-gray-100 text-gray-800' },
  [OrderStatus.REFUNDING]: { label: '申请退款', color: 'bg-blue-100 text-blue-800' },
  [OrderStatus.REFUNDED]: { label: '已退款', color: 'bg-purple-100 text-purple-800' },
  [OrderStatus.REFUND_FAILED]: { label: '退款失败', color: 'bg-red-100 text-red-800' },
};

// 组件属性
interface OrderListProps {
  isAdmin: boolean;
  isInstitution: boolean;
}

export function OrderList({ isAdmin, isInstitution }: OrderListProps) {
  // 搜索条件
  const [searchParams, setSearchParams] = useState<OrderSearchDTO>({
    pageNum: 1,
    pageSize: 10,
  });
  
  // 高级搜索面板状态
  const [showAdvancedSearch, setShowAdvancedSearch] = useState(false);
  
  // 订单数据
  const [orders, setOrders] = useState<OrderVO[]>([]);
  const [pagination, setPagination] = useState({
    currentPage: 1,
    totalPages: 0,
    totalElements: 0,
  });
  
  // 加载状态
  const [loading, setLoading] = useState(false);
  
  // 当前选中的订单（用于详情和退款）
  const [selectedOrder, setSelectedOrder] = useState<OrderVO | null>(null);
  
  // 对话框状态
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [refundOpen, setRefundOpen] = useState(false);
  
  // 退款信息
  const [refundReason, setRefundReason] = useState('');
  const [refundLoading, setRefundLoading] = useState(false);
  
  // 处理退款状态
  const [processingRefund, setProcessingRefund] = useState(false);
  
  // 支付相关状态
  const [paymentProcessing, setPaymentProcessing] = useState(false);
  const [currentOrderNo, setCurrentOrderNo] = useState<string | null>(null);
  const [isLoadingPaymentForm, setIsLoadingPaymentForm] = useState(false);
  const [paymentTimer, setPaymentTimer] = useState<NodeJS.Timeout | null>(null);
  
  // 倒计时相关
  const [countdowns, setCountdowns] = useState<{[orderNo: string]: number}>({});
  const countdownTimerRef = useRef<NodeJS.Timeout | null>(null);
  
  // 获取用户信息
  const { user } = useAuthStore();
  
  // 根据角色加载订单数据
  const loadOrders = async () => {
    setLoading(true);
    try {
      let result: PaginationResult<OrderVO>;
      
      if (isAdmin) {
        // 管理员查看所有订单
        result = await orderService.searchAllOrders(searchParams);
      } else if (isInstitution) {
        // 机构查看自己的订单
        result = await orderService.searchInstitutionOrders(searchParams);
      } else {
        // 普通用户查看自己的订单
        result = await orderService.searchUserOrders(searchParams);
      }
      
      setOrders(result.content || []);
      
      // 初始化待支付订单的倒计时
      const initialCountdowns: {[orderNo: string]: number} = {};
      result.content?.forEach(order => {
        if (order.status === OrderStatus.PENDING && order.remainingTime) {
          initialCountdowns[order.orderNo] = order.remainingTime;
        }
      });
      setCountdowns(initialCountdowns);
      
      // 启动倒计时
      startCountdownTimer();
      
      setPagination({
        currentPage: result.number + 1 || 1,
        totalPages: result.totalPages || 0,
        totalElements: result.totalElements || 0,
      });
    } catch (error) {
      console.error('加载订单失败:', error);
      toast.error('加载订单失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };
  
  // 倒计时计时器
  const startCountdownTimer = () => {
    // 清除已有计时器
    if (countdownTimerRef.current) {
      clearInterval(countdownTimerRef.current);
    }
    
    // 启动新的计时器，每秒更新一次倒计时
    countdownTimerRef.current = setInterval(() => {
      setCountdowns(prev => {
        const newCountdowns = { ...prev };
        let hasChanges = false;
        
        // 更新每个订单的倒计时
        Object.keys(newCountdowns).forEach(orderNo => {
          if (newCountdowns[orderNo] > 0) {
            newCountdowns[orderNo] -= 1;
            hasChanges = true;
          }
        });
        
        // 如果没有变化，不触发重新渲染
        return hasChanges ? newCountdowns : prev;
      });
    }, 1000);
  };
  
  // 清理计时器
  useEffect(() => {
    return () => {
      if (countdownTimerRef.current) {
        clearInterval(countdownTimerRef.current);
      }
      if (paymentTimer) {
        clearInterval(paymentTimer);
      }
    };
  }, [paymentTimer]);
  
  // 首次加载和搜索条件变化时加载数据
  useEffect(() => {
    loadOrders();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, isAdmin, isInstitution]);
  
  // 处理分页
  const handlePageChange = (page: number) => {
    setSearchParams((prev) => ({ ...prev, pageNum: page }));
  };
  
  // 处理搜索
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // 重置到第一页
    setSearchParams((prev) => ({ ...prev, pageNum: 1 }));
  };
  
  // 重置搜索
  const handleResetSearch = () => {
    setSearchParams({ pageNum: 1, pageSize: 10 });
    setShowAdvancedSearch(false);
  };
  
  // 打开订单详情
  const handleViewDetails = (order: OrderVO) => {
    setSelectedOrder(order);
    setDetailsOpen(true);
  };
  
  // 打开退款申请框
  const handleRefundRequest = (order: OrderVO) => {
    setSelectedOrder(order);
    setRefundOpen(true);
  };
  
  // 处理支付
  const handlePayOrder = async (order: OrderVO) => {
    if (isLoadingPaymentForm && currentOrderNo === order.orderNo) return;
    
    setCurrentOrderNo(order.orderNo);
    setIsLoadingPaymentForm(true);
    
    try {
      // 获取支付表单
      const paymentFormHtml = await orderService.getPaymentForm(order.orderNo);
      
      // 创建一个临时div来保存支付宝返回的表单HTML
      const div = document.createElement('div');
      div.innerHTML = paymentFormHtml;
      document.body.appendChild(div);
      
      // 获取表单并提交
      const form = div.getElementsByTagName('form')[0];
      if (form) {
        form.setAttribute('target', '_blank');
        form.submit();
        // 提交后移除临时div
        setTimeout(() => {
          document.body.removeChild(div);
        }, 100);
        
        // 开始轮询订单状态
        setPaymentProcessing(true);
        toast.info('支付页面已打开，请在新窗口完成支付');
        startPollingOrderStatus(order.orderNo);
      } else {
        console.error('支付表单不存在');
        toast.error('打开支付页面失败');
      }
    } catch (error) {
      console.error('获取支付表单失败:', error);
      toast.error('获取支付表单失败，请重试');
    } finally {
      setIsLoadingPaymentForm(false);
    }
  };
  
  // 轮询订单状态
  const startPollingOrderStatus = (orderNo: string) => {
    // 清除之前的计时器
    if (paymentTimer) {
      clearInterval(paymentTimer);
    }
    
    // 设置最大轮询时间（5分钟）
    const maxPollTime = 5 * 60 * 1000;
    const startTime = Date.now();
    
    // 设置轮询间隔（3秒）
    const interval = 3000;
    
    // 创建轮询计时器
    const timer = setInterval(async () => {
      try {
        // 检查是否超过最大轮询时间
        if (Date.now() - startTime > maxPollTime) {
          clearInterval(timer);
          setPaymentTimer(null);
          setPaymentProcessing(false);
          toast.info('支付等待超时，如已完成支付，请刷新页面查看订单状态');
          return;
        }
        
        // 查询订单状态
        const order = await orderService.getOrderByOrderNo(orderNo);
        
        // 如果订单已支付，结束轮询
        if (order.status === OrderStatus.PAID) {
          clearInterval(timer);
          setPaymentTimer(null);
          setPaymentProcessing(false);
          toast.success('支付成功！');
          // 重新加载订单列表
          loadOrders();
          return;
        }
      } catch (error) {
        console.error('轮询订单状态失败:', error);
      }
    }, interval);
    
    setPaymentTimer(timer);
  };
  
  // 提交退款申请
  const handleSubmitRefund = async () => {
    if (!selectedOrder) return;
    
    setRefundLoading(true);
    try {
      await orderService.refundOrder(selectedOrder.id, { refundReason });
      toast.success('退款申请已提交');
      setRefundOpen(false);
      setRefundReason('');
      loadOrders(); // 重新加载数据
    } catch (error) {
      console.error('申请退款失败:', error);
      toast.error('申请退款失败，请稍后重试');
    } finally {
      setRefundLoading(false);
    }
  };
  
  // 处理退款申请（机构或管理员）
  const handleProcessRefund = async (orderId: number, approved: boolean) => {
    setProcessingRefund(true);
    try {
      await orderService.processRefund(orderId, approved);
      toast.success(approved ? '已批准退款申请' : '已拒绝退款申请');
      loadOrders(); // 重新加载数据
    } catch (error) {
      console.error('处理退款失败:', error);
      toast.error('处理退款失败，请稍后重试');
    } finally {
      setProcessingRefund(false);
    }
  };
  
  // 格式化剩余时间显示
  const formatRemainingTime = (seconds: number) => {
    if (seconds <= 0) return '已超时';
    
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    
    // 当剩余时间少于5分钟时使用红色字体
    const isUrgent = minutes < 5;
    
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
  };
  
  // 渲染高级搜索面板
  const renderAdvancedSearch = () => (
    <Card className="mb-6">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>高级搜索</CardTitle>
          <Button 
            variant="ghost" 
            size="icon" 
            onClick={() => setShowAdvancedSearch(false)}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSearch} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">订单号</label>
              <Input 
                placeholder="输入订单号" 
                value={searchParams.orderNo || ''} 
                onChange={(e) => setSearchParams((prev) => ({ ...prev, orderNo: e.target.value }))}
              />
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">交易号</label>
              <Input 
                placeholder="输入交易号" 
                value={searchParams.tradeNo || ''} 
                onChange={(e) => setSearchParams((prev) => ({ ...prev, tradeNo: e.target.value }))}
              />
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">订单状态</label>
              <Select 
                value={searchParams.status?.toString() || 'ALL'} 
                onValueChange={(value) => setSearchParams((prev) => ({ ...prev, status: value === 'ALL' ? undefined : parseInt(value) }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="所有状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">所有状态</SelectItem>
                  <SelectItem value={OrderStatus.PENDING.toString()}>待支付</SelectItem>
                  <SelectItem value={OrderStatus.PAID.toString()}>已支付</SelectItem>
                  <SelectItem value={OrderStatus.CLOSED.toString()}>已关闭</SelectItem>
                  <SelectItem value={OrderStatus.REFUNDING.toString()}>申请退款</SelectItem>
                  <SelectItem value={OrderStatus.REFUNDED.toString()}>已退款</SelectItem>
                  <SelectItem value={OrderStatus.REFUND_FAILED.toString()}>退款失败</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">课程名称</label>
              <Input 
                placeholder="课程名称" 
                value={searchParams.courseTitle || ''} 
                onChange={(e) => setSearchParams((prev) => ({ ...prev, courseTitle: e.target.value }))}
              />
            </div>
            
            {isAdmin && (
              <div className="space-y-2">
                <label className="text-sm font-medium">用户名称</label>
                <Input 
                  placeholder="用户名称" 
                  value={searchParams.userName || ''} 
                  onChange={(e) => setSearchParams((prev) => ({ ...prev, userName: e.target.value }))}
                />
              </div>
            )}
            
            <div className="space-y-2">
              <label className="text-sm font-medium">最小金额</label>
              <Input 
                type="number"
                placeholder="最小金额" 
                value={searchParams.minAmount?.toString() || ''} 
                onChange={(e) => setSearchParams((prev) => ({ ...prev, minAmount: e.target.value ? parseInt(e.target.value) : undefined }))}
              />
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">最大金额</label>
              <Input 
                type="number"
                placeholder="最大金额" 
                value={searchParams.maxAmount?.toString() || ''} 
                onChange={(e) => setSearchParams((prev) => ({ ...prev, maxAmount: e.target.value ? parseInt(e.target.value) : undefined }))}
              />
            </div>
          </div>
          
          <div className="flex justify-end space-x-2">
            <Button variant="outline" type="button" onClick={handleResetSearch}>
              重置
            </Button>
            <Button type="submit">
              搜索
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
  
  // 订单状态标签
  const OrderStatusBadge = ({ status }: { status: OrderStatus }) => {
    const { label, color } = orderStatusMap[status] || { label: '未知状态', color: 'bg-gray-100 text-gray-800' };
    return (
      <Badge className={color} variant="outline">
        {label}
      </Badge>
    );
  };
  
  return (
    <div className="space-y-4">
      {/* 搜索工具栏 */}
      <div className="flex flex-col sm:flex-row justify-between gap-4">
        <div className="flex gap-2">
          <div className="relative flex-1 min-w-[200px]">
            <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="搜索订单号或课程名称"
              className="pl-8"
              value={searchParams.orderNo || ''}
              onChange={(e) => setSearchParams((prev) => ({ ...prev, orderNo: e.target.value }))}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch(e)}
            />
          </div>
          <Button variant="outline" onClick={() => setShowAdvancedSearch(!showAdvancedSearch)}>
            <Filter className="h-4 w-4 mr-2" />
            筛选
          </Button>
        </div>
        
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => loadOrders()} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
            刷新
          </Button>
        </div>
      </div>
      
      {/* 高级搜索面板 */}
      {showAdvancedSearch && renderAdvancedSearch()}
      
      {/* 订单列表 */}
      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>订单号</TableHead>
                <TableHead>课程名称</TableHead>
                {isAdmin && <TableHead>用户</TableHead>}
                <TableHead>金额</TableHead>
                <TableHead>状态</TableHead>
                <TableHead>创建时间</TableHead>
                <TableHead className="text-right">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={isAdmin ? 7 : 6} className="text-center py-8">
                    <div className="flex justify-center items-center">
                      <Loader2 className="h-6 w-6 animate-spin mr-2" />
                      <span>加载中...</span>
                    </div>
                  </TableCell>
                </TableRow>
              ) : orders.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={isAdmin ? 7 : 6} className="text-center py-8">
                    没有找到符合条件的订单
                  </TableCell>
                </TableRow>
              ) : (
                orders.map((order) => (
                  <TableRow key={order.id}>
                    <TableCell className="font-medium">{order.orderNo}</TableCell>
                    <TableCell>{order.courseTitle}</TableCell>
                    {isAdmin && <TableCell>{order.userName}</TableCell>}
                    <TableCell>{formatPrice(order.amount)}</TableCell>
                    <TableCell>
                      <div className="flex flex-col gap-1">
                        <OrderStatusBadge status={order.status} />
                        {order.status === OrderStatus.PENDING && countdowns[order.orderNo] !== undefined && (
                          <div className={`flex items-center text-xs mt-1 font-bold ${countdowns[order.orderNo] < 300 ? 'text-red-500' : 'text-orange-500'}`}>
                            <Clock className="h-3 w-3 mr-1" />
                            剩余 {formatRemainingTime(countdowns[order.orderNo])}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{formatDate(order.createdAt)}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        {/* 待支付状态显示去支付按钮 - 仅对普通用户显示 */}
                        {!isAdmin && !isInstitution && order.status === OrderStatus.PENDING && countdowns[order.orderNo] > 0 && (
                          <Button 
                            variant="default" 
                            size="sm" 
                            className="bg-primary hover:bg-primary/90"
                            onClick={() => handlePayOrder(order)}
                            disabled={isLoadingPaymentForm && currentOrderNo === order.orderNo}
                          >
                            {isLoadingPaymentForm && currentOrderNo === order.orderNo ? (
                              <>
                                <Loader2 className="h-3 w-3 mr-1 animate-spin" />
                                处理中
                              </>
                            ) : '去支付'}
                          </Button>
                        )}
                        
                        <Button variant="ghost" size="sm" onClick={() => handleViewDetails(order)}>
                          详情
                        </Button>
                        
                        {/* 当订单状态为已支付时显示退款按钮 */}
                        {order.status === OrderStatus.PAID && (
                          <Button variant="ghost" size="sm" onClick={() => handleRefundRequest(order)}>
                            申请退款
                          </Button>
                        )}
                        
                        {/* 机构或管理员处理退款申请 */}
                        {(isAdmin || isInstitution) && order.status === OrderStatus.REFUNDING && (
                          <>
                            <Button 
                              variant="outline" 
                              size="sm" 
                              onClick={() => handleProcessRefund(order.id, true)}
                              disabled={processingRefund}
                            >
                              批准
                            </Button>
                            <Button 
                              variant="outline" 
                              size="sm" 
                              onClick={() => handleProcessRefund(order.id, false)}
                              disabled={processingRefund}
                            >
                              拒绝
                            </Button>
                          </>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
      
      {/* 分页控件 */}
      {pagination.totalPages > 0 && (
        <div className="flex justify-between items-center">
          <div className="text-sm text-muted-foreground">
            共 {pagination.totalElements} 条记录，第 {pagination.currentPage}/{pagination.totalPages} 页
          </div>
          <div className="flex gap-1">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(1)}
              disabled={pagination.currentPage === 1 || loading}
            >
              首页
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(pagination.currentPage - 1)}
              disabled={pagination.currentPage === 1 || loading}
            >
              上一页
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(pagination.currentPage + 1)}
              disabled={pagination.currentPage === pagination.totalPages || loading}
            >
              下一页
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(pagination.totalPages)}
              disabled={pagination.currentPage === pagination.totalPages || loading}
            >
              末页
            </Button>
          </div>
        </div>
      )}
      
      {/* 订单详情对话框 */}
      <Dialog open={detailsOpen} onOpenChange={setDetailsOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>订单详情</DialogTitle>
            <DialogDescription>
              订单号: {selectedOrder?.orderNo}
            </DialogDescription>
          </DialogHeader>
          
          {selectedOrder && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <h4 className="text-sm font-medium text-muted-foreground">基本信息</h4>
                  <Separator className="my-2" />
                  <dl className="space-y-2">
                    <div className="flex justify-between">
                      <dt className="text-sm font-medium">课程名称:</dt>
                      <dd>{selectedOrder.courseTitle}</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt className="text-sm font-medium">订单金额:</dt>
                      <dd>{formatPrice(selectedOrder.amount)}</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt className="text-sm font-medium">订单状态:</dt>
                      <dd><OrderStatusBadge status={selectedOrder.status} /></dd>
                    </div>
                    <div className="flex justify-between">
                      <dt className="text-sm font-medium">创建时间:</dt>
                      <dd>{formatDate(selectedOrder.createdAt)}</dd>
                    </div>
                    {/* 待支付订单显示剩余支付时间 */}
                    {selectedOrder.status === OrderStatus.PENDING && countdowns[selectedOrder.orderNo] !== undefined && (
                      <div className="flex justify-between text-orange-500">
                        <dt className="text-sm font-medium">剩余支付时间:</dt>
                        <dd className="flex items-center font-bold">
                          <Clock className="h-4 w-4 mr-1" />
                          {countdowns[selectedOrder.orderNo] < 300 ? 
                            <span className="text-red-500">剩余 {formatRemainingTime(countdowns[selectedOrder.orderNo])}</span> : 
                            <span>剩余 {formatRemainingTime(countdowns[selectedOrder.orderNo])}</span>
                          }
                        </dd>
                      </div>
                    )}
                  </dl>
                </div>
                
                <div>
                  <h4 className="text-sm font-medium text-muted-foreground">用户和机构信息</h4>
                  <Separator className="my-2" />
                  <dl className="space-y-2">
                    <div className="flex justify-between">
                      <dt className="text-sm font-medium">用户名:</dt>
                      <dd>{selectedOrder.userName}</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt className="text-sm font-medium">机构名称:</dt>
                      <dd>{selectedOrder.institutionName}</dd>
                    </div>
                  </dl>
                </div>
              </div>
              
              {/* 支付信息 */}
              {(selectedOrder.status === OrderStatus.PAID || 
                selectedOrder.status === OrderStatus.REFUNDING || 
                selectedOrder.status === OrderStatus.REFUNDED) && (
                <>
                  <h4 className="text-sm font-medium text-muted-foreground">支付信息</h4>
                  <Separator className="my-2" />
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <dt className="text-sm font-medium">支付时间:</dt>
                      <dd>{formatDate(selectedOrder.paidAt)}</dd>
                    </div>
                    {selectedOrder.tradeNo && (
                      <div className="flex justify-between">
                        <dt className="text-sm font-medium">交易号:</dt>
                        <dd>{selectedOrder.tradeNo}</dd>
                      </div>
                    )}
                  </div>
                </>
              )}
              
              {/* 退款信息 */}
              {(selectedOrder.status === OrderStatus.REFUNDING || 
                selectedOrder.status === OrderStatus.REFUNDED || 
                selectedOrder.status === OrderStatus.REFUND_FAILED) && (
                <>
                  <h4 className="text-sm font-medium text-muted-foreground">退款信息</h4>
                  <Separator className="my-2" />
                  <div className="space-y-2">
                    {selectedOrder.refundReason && (
                      <div className="flex flex-col">
                        <dt className="text-sm font-medium">退款原因:</dt>
                        <dd className="mt-1">{selectedOrder.refundReason}</dd>
                      </div>
                    )}
                    {selectedOrder.refundAmount && (
                      <div className="flex justify-between">
                        <dt className="text-sm font-medium">退款金额:</dt>
                        <dd>{formatPrice(selectedOrder.refundAmount)}</dd>
                      </div>
                    )}
                    {selectedOrder.refundedAt && (
                      <div className="flex justify-between">
                        <dt className="text-sm font-medium">退款时间:</dt>
                        <dd>{formatDate(selectedOrder.refundedAt)}</dd>
                      </div>
                    )}
                    {selectedOrder.refundTradeNo && (
                      <div className="flex justify-between">
                        <dt className="text-sm font-medium">退款交易号:</dt>
                        <dd>{selectedOrder.refundTradeNo}</dd>
                      </div>
                    )}
                  </div>
                </>
              )}
            </div>
          )}
          
          <DialogFooter className="flex justify-between items-center">
            <Button variant="outline" onClick={() => setDetailsOpen(false)}>
              关闭
            </Button>
            
            {/* 添加待支付订单的支付按钮 - 仅对普通用户显示 */}
            {!isAdmin && !isInstitution && selectedOrder && selectedOrder.status === OrderStatus.PENDING && countdowns[selectedOrder.orderNo] > 0 && (
              <Button 
                variant="default"
                className="bg-primary hover:bg-primary/90"
                onClick={() => {
                  setDetailsOpen(false);
                  handlePayOrder(selectedOrder);
                }}
                disabled={isLoadingPaymentForm && currentOrderNo === selectedOrder.orderNo}
              >
                {isLoadingPaymentForm && currentOrderNo === selectedOrder.orderNo ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    处理中...
                  </>
                ) : '去支付'}
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      {/* 退款申请对话框 */}
      <Dialog open={refundOpen} onOpenChange={setRefundOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>申请退款</DialogTitle>
            <DialogDescription>
              请填写退款原因，提交后将等待审核。
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">退款原因</label>
              <textarea
                className="w-full min-h-[100px] p-2 border rounded-md"
                placeholder="请详细说明退款原因..."
                value={refundReason}
                onChange={(e) => setRefundReason(e.target.value)}
              />
            </div>
          </div>
          
          <DialogFooter>
            <Button variant="outline" onClick={() => setRefundOpen(false)} disabled={refundLoading}>
              取消
            </Button>
            <Button 
              onClick={handleSubmitRefund} 
              disabled={!refundReason.trim() || refundLoading}
            >
              {refundLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              提交
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      {/* 支付处理中对话框 */}
      <Dialog open={paymentProcessing} onOpenChange={(open) => {
        if (!open && paymentTimer) {
          clearInterval(paymentTimer);
          setPaymentTimer(null);
        }
        setPaymentProcessing(open);
      }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>支付处理中</DialogTitle>
            <DialogDescription>
              请在新窗口中完成支付。系统正在等待支付结果，支付完成后会自动处理。
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col items-center py-6 space-y-4">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <p className="text-sm text-muted-foreground">订单号: {currentOrderNo}</p>
          </div>
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => {
                if (paymentTimer) {
                  clearInterval(paymentTimer);
                  setPaymentTimer(null);
                }
                setPaymentProcessing(false);
              }}
            >
              取消
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 