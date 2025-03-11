import { request } from './api';

/**
 * 机构成员服务 - 处理机构成员管理相关接口
 */
export const institutionMemberService = {
  /**
   * 获取机构注册码（机构用户）
   * @returns 机构注册码
   */
  async getRegisterCode(): Promise<string> {
    const response = await request.get<string>('/institutions/members/register-code');
    return response.data.data;
  }
};

export default institutionMemberService; 