/**
 * 角色工具函数
 */

import { UserRole } from '@/types/auth';

/**
 * 角色代码到显示名称的映射
 */
export const ROLE_DISPLAY_NAMES: Record<string, string> = {
  [UserRole.ADMIN]: '管理员',
  [UserRole.REVIEWER]: '审核员',
  [UserRole.USER]: '普通用户',
  [UserRole.INSTITUTION]: '机构用户',
  'ROLE_ADMIN': '管理员',
  'ROLE_REVIEWER': '审核员',
  'ROLE_USER': '普通用户',
  'ROLE_INSTITUTION': '机构用户',
};

/**
 * 获取角色显示名称
 * @param roleCode 角色代码
 * @returns 角色显示名称
 */
export function getRoleDisplayName(roleCode: string): string {
  // 处理带有ROLE_前缀的情况
  const code = roleCode.replace('ROLE_', '');
  
  // 尝试从映射中获取显示名称
  return ROLE_DISPLAY_NAMES[roleCode] || 
         ROLE_DISPLAY_NAMES[code] || 
         ROLE_DISPLAY_NAMES[code.toUpperCase()] || 
         code;
}

/**
 * 从用户对象中获取角色显示名称列表
 * @param user 用户对象
 * @returns 角色显示名称列表
 */
export function getUserRoleDisplayNames(user: any): string[] {
  if (!user) return [];
  
  // 如果有roles数组，使用它
  if (user.roles && Array.isArray(user.roles)) {
    return user.roles.map(role => {
      const roleCode = typeof role === 'string' ? role : role.code || role.name;
      return getRoleDisplayName(roleCode);
    });
  }
  
  // 如果有单个role字段，使用它
  if (user.role) {
    return [getRoleDisplayName(user.role)];
  }
  
  return [];
}
