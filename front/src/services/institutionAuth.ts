import { request } from './api';
import { InstitutionRegisterRequest } from '@/types/institution';

/**
 * 机构认证服务 - 处理机构用户注册相关接口
 */
export const institutionAuthService = {
  /**
   * 使用机构注册码注册机构用户
   * @param data 注册数据
   */
  async register(data: InstitutionRegisterRequest): Promise<void> {
    try {
      console.log("发送机构用户注册请求到: /auth/institution/register");
      const response = await request.post('/auth/institution/register', data);
      console.log("注册API响应:", response.data);
      
      // 检查响应状态
      if (response.data.code !== 200 && response.data.code !== 201) {
        throw {
          code: response.data.code,
          message: response.data.message || "注册失败",
          errors: response.data.errors
        };
      }
    } catch (error) {
      console.error("机构注册服务捕获到错误:", error);
      throw error;
    }
  }
};

export default institutionAuthService; 