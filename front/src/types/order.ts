// 订单状态枚举
export enum OrderStatus {
  PENDING = 0,       // 待支付
  PAID = 1,          // 已支付
  CLOSED = 2,        // 已关闭
  REFUNDING = 3,     // 申请退款
  REFUNDED = 4,      // 已退款
  REFUND_FAILED = 5  // 退款失败
}

// 订单值对象
export interface OrderVO {
  id: number;
  orderNo: string;
  title: string;
  description?: string;
  amount: number;
  userId: number;
  userName: string;
  courseId: number;
  courseTitle: string;
  courseCover?: string;
  institutionId: number;
  institutionName: string;
  tradeNo?: string;
  paidAt?: string;
  status: OrderStatus;
  refundedAt?: string;
  refundAmount?: number;
  refundReason?: string;
  refundTradeNo?: string;
  createdAt: string;
  updatedAt: string;
  remainingTime?: number;
}

// 订单创建数据传输对象
export interface OrderCreateDTO {
  courseId: number;
}

// 订单退款数据传输对象
export interface OrderRefundDTO {
  refundAmount?: number;
  refundReason: string;
}

// 订单搜索数据传输对象
export interface OrderSearchDTO {
  orderNo?: string;
  tradeNo?: string;
  status?: OrderStatus;
  createdTimeStart?: string;
  createdTimeEnd?: string;
  paidTimeStart?: string;
  paidTimeEnd?: string;
  refundTimeStart?: string;
  refundTimeEnd?: string;
  courseId?: number;
  userId?: number;
  courseTitle?: string;
  userName?: string;
  minAmount?: number;
  maxAmount?: number;
  pageSize?: number;
  pageNum?: number;
}

// 机构收入统计
export interface InstitutionIncomeVO {
  totalIncome: number;
  totalRefund: number;
  netIncome: number;
}

// 机构日收入统计
export interface InstitutionDailyIncomeVO {
  dailyIncome: number;
  dailyRefund: number;
  dailyNetIncome: number;
}

// 机构周收入统计
export interface InstitutionWeeklyIncomeVO {
  weeklyIncome: number;
  weeklyRefund: number;
  weeklyNetIncome: number;
}

// 机构月收入统计
export interface InstitutionMonthlyIncomeVO {
  monthlyIncome: number;
  monthlyRefund: number;
  monthlyNetIncome: number;
}

// 机构自定义时间范围收入统计
export interface InstitutionCustomIncomeVO {
  customIncome: number;
  customRefund: number;
  customNetIncome: number;
  startTime: number;
  endTime: number;
} 