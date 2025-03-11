/**
 * 机构入驻申请请求参数
 */
export interface InstitutionApplyRequest {
  name: string;                  // 机构名称
  logo?: string;                 // 机构Logo URL
  description?: string;          // 机构描述
  contactPerson: string;         // 联系人
  contactPhone?: string;         // 联系电话
  contactEmail: string;          // 联系邮箱
  address?: string;              // 地址
  captchaKey: string;            // 验证码Key
  captchaCode: string;           // 验证码
}

/**
 * 机构用户注册请求参数
 */
export interface InstitutionRegisterRequest {
  username: string;              // 用户名
  password: string;              // 密码
  email: string;                 // 邮箱
  phone?: string;                // 手机号
  institutionCode: string;       // 机构注册码
  captchaKey: string;            // 验证码Key
  captchaCode: string;           // 验证码
  emailCode: string;             // 邮箱验证码
}

/**
 * 机构信息响应
 */
export interface InstitutionResponse {
  id: number;                    // ID
  name: string;                  // 机构名称
  logo?: string;                 // 机构Logo
  description?: string;          // 机构描述
  status: number;                // 状态：0-待审核，1-正常，2-禁用
  contactPerson: string;         // 联系人
  contactPhone?: string;         // 联系电话
  contactEmail: string;          // 联系邮箱
  address?: string;              // 地址
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
}

/**
 * 机构申请信息响应
 */
export interface InstitutionApplicationResponse {
  id: number;                    // ID
  applicationId: string;         // 申请ID
  name: string;                  // 机构名称
  logo?: string;                 // 机构Logo
  description?: string;          // 机构描述
  contactPerson: string;         // 联系人
  contactPhone?: string;         // 联系电话
  contactEmail: string;          // 联系邮箱
  address?: string;              // 地址
  status: number;                // 状态：0-待审核，1-已通过，2-已拒绝
  reviewComment?: string;        // 审核结果备注
  reviewerId?: number;           // 审核人ID
  reviewedAt?: string;           // 审核时间
  institutionId?: number;        // 关联的机构ID
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
}

/**
 * 机构申请查询参数
 */
export interface InstitutionApplicationQueryParams {
  applicationId?: string;        // 申请ID
  name?: string;                 // 机构名称
  contactPerson?: string;        // 联系人
  contactEmail?: string;         // 联系邮箱
  status?: number;               // 状态：0-待审核，1-已通过，2-已拒绝
  page?: number;                 // 页码
  size?: number;                 // 每页条数
}

/**
 * 通用分页响应接口
 */
export interface Page<T> {
  content: T[];                  // 数据内容
  totalElements: number;         // 总记录数
  totalPages: number;            // 总页数
  size: number;                  // 每页条数
  number: number;                // 当前页码
  numberOfElements: number;      // 当前页记录数
  first: boolean;                // 是否第一页
  last: boolean;                 // 是否最后一页
  empty: boolean;                // 是否为空
} 