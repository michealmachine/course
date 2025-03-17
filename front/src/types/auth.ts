// 用户角色枚举
export enum UserRole {
  ADMIN = 'ADMIN',           // 管理员
  REVIEWER = 'REVIEWER',     // 审核员
  USER = 'USER',             // 普通用户
  INSTITUTION = 'INSTITUTION' // 机构用户
}

// 角色对象接口
export interface Role {
  id: number;
  name: string;
  code: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  permissions?: any[];
}

// 用户类型定义
export interface User {
  id: number;
  username: string;
  email: string;
  nickname?: string;
  phone?: string;
  avatar?: string;
  status?: number;
  roles: Role[];  // 角色对象数组
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
  institutionId: number;
  role: string;
}

// 登录请求参数
export interface LoginRequest {
  username: string;
  password: string;
}

// 邮箱验证码请求参数
export interface EmailVerificationRequest {
  email: string;
  captchaCode: string;
  captchaKey: string;
}

// 注册请求参数
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  captchaCode: string;
  captchaKey: string;
  emailCode: string;        // 邮箱验证码
}

// 登录响应
export interface LoginResponse {
  accessToken: string;      // 访问令牌
  token?: string;           // 兼容旧代码
  refreshToken: string;     // 刷新令牌
  tokenType: string;        // 令牌类型
  expiresIn: number;        // 过期时间（毫秒）
  user?: User;              // 用户信息（可能需要单独获取）
}

// 验证码响应
export interface CaptchaResponse {
  captchaId: string;
  captchaImage: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
} 