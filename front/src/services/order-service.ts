'use client';

import { request } from './api';
import { ApiResponse, PaginationResult } from '@/types/api';
import { OrderVO, OrderCreateDTO, OrderRefundDTO, OrderSearchDTO, InstitutionIncomeVO } from '@/types/order';
import { AxiosResponse } from 'axios';

/**
 * 订单服务 - 处理订单相关API
 */
const orderService = {
  /**
   * 创建订单
   */
  createOrder: async (dto: OrderCreateDTO): Promise<OrderVO> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderVO>> = await request.post('/orders', dto);
      return response.data.data;
    } catch (error) {
      console.error('创建订单失败:', error);
      throw error;
    }
  },

  /**
   * 根据ID获取订单详情
   */
  getOrderById: async (id: number): Promise<OrderVO> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderVO>> = await request.get(`/orders/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取订单详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 根据订单号查询订单
   */
  getOrderByOrderNo: async (orderNo: string): Promise<OrderVO> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderVO>> = await request.get(`/orders/query?orderNo=${orderNo}`);
      return response.data.data;
    } catch (error) {
      console.error(`查询订单状态失败, 订单号: ${orderNo}:`, error);
      throw error;
    }
  },

  /**
   * 获取订单支付表单
   */
  getPaymentForm: async (orderNo: string): Promise<string> => {
    try {
      const response: AxiosResponse<ApiResponse<string>> = await request.get(`/orders/${orderNo}/payment-form`);
      return response.data.data;
    } catch (error) {
      console.error(`获取支付表单失败, 订单号: ${orderNo}:`, error);
      throw error;
    }
  },

  /**
   * 取消未支付订单
   */
  cancelOrder: async (id: number): Promise<OrderVO> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderVO>> = await request.post(`/orders/${id}/cancel`);
      return response.data.data;
    } catch (error) {
      console.error(`取消订单失败, 订单ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 申请退款
   */
  refundOrder: async (id: number, dto: OrderRefundDTO): Promise<OrderVO> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderVO>> = await request.post(`/orders/${id}/refund`, dto);
      return response.data.data;
    } catch (error) {
      console.error(`申请退款失败, 订单ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 获取机构收入统计
   */
  getInstitutionIncome: async (): Promise<InstitutionIncomeVO> => {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionIncomeVO>> = await request.get('/orders/institution/income');
      return response.data.data;
    } catch (error) {
      console.error('获取机构收入统计失败:', error);
      throw error;
    }
  },

  /**
   * 处理退款申请（机构管理员或平台管理员）
   */
  processRefund: async (id: number, approved: boolean): Promise<OrderVO> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderVO>> = await request.post(`/orders/admin/${id}/process-refund?approved=${approved}`);
      return response.data.data;
    } catch (error) {
      console.error(`处理退款申请失败, 订单ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 高级搜索用户订单
   */
  searchUserOrders: async (searchDTO: OrderSearchDTO): Promise<PaginationResult<OrderVO>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<OrderVO>>> = await request.post('/orders/my/search', searchDTO);
      return response.data.data;
    } catch (error) {
      console.error('高级搜索用户订单失败:', error);
      throw error;
    }
  },

  /**
   * 高级搜索机构订单（机构管理员）
   */
  searchInstitutionOrders: async (searchDTO: OrderSearchDTO): Promise<PaginationResult<OrderVO>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<OrderVO>>> = await request.post('/orders/institution/search', searchDTO);
      return response.data.data;
    } catch (error) {
      console.error('高级搜索机构订单失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构待处理退款申请（机构管理员）
   */
  getInstitutionPendingRefunds: async (): Promise<OrderVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderVO[]>> = await request.get('/orders/institution/pending-refunds');
      return response.data.data;
    } catch (error) {
      console.error('获取机构待处理退款申请失败:', error);
      throw error;
    }
  },

  /**
   * 高级搜索所有订单（平台管理员）
   */
  searchAllOrders: async (searchDTO: OrderSearchDTO): Promise<PaginationResult<OrderVO>> => {
    try {
      const response: AxiosResponse<ApiResponse<PaginationResult<OrderVO>>> = await request.post('/orders/admin/search', searchDTO);
      return response.data.data;
    } catch (error) {
      console.error('高级搜索所有订单失败:', error);
      throw error;
    }
  }
};

export default orderService; 