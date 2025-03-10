'use client';

import { create } from 'zustand';
import { User } from '@/types/auth';
import { UserDTO, UserQueryParams, UserPageResponse, UserStatusDTO } from '@/types/user';
import userService from '@/services/user';
import { toast } from 'sonner';

interface UserManagementState {
  // 用户列表
  users: User[];
  // 用户分页信息
  pagination: {
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
  };
  // 当前查询参数
  queryParams: UserQueryParams;
  // 当前编辑的用户
  currentUser: User | null;
  // 加载状态
  isLoading: boolean;
  // 错误信息
  error: string | null;
  // 表单可见性
  formVisible: boolean;
  // 确认对话框可见性
  confirmDialogVisible: boolean;
  // 角色分配对话框可见性
  roleDialogVisible: boolean;
  // 待删除的用户ID列表
  selectedIds: number[];
  
  // 查询用户列表
  fetchUsers: (params?: Partial<UserQueryParams>) => Promise<void>;
  // 根据ID获取用户
  fetchUserById: (id: number) => Promise<void>;
  // 创建用户
  createUser: (user: UserDTO) => Promise<void>;
  // 更新用户
  updateUser: (id: number, user: UserDTO) => Promise<void>;
  // 删除用户
  deleteUser: (id: number) => Promise<void>;
  // 批量删除用户
  batchDeleteUsers: (ids: number[]) => Promise<void>;
  // 更新用户状态
  updateUserStatus: (id: number, status: number) => Promise<void>;
  // 给用户分配角色
  assignRoles: (userId: number, roleIds: number[]) => Promise<void>;
  
  // 设置查询参数
  setQueryParams: (params: Partial<UserQueryParams>) => void;
  // 设置当前用户
  setCurrentUser: (user: User | null) => void;
  // 设置表单可见性
  setFormVisible: (visible: boolean) => void;
  // 设置确认对话框可见性
  setConfirmDialogVisible: (visible: boolean) => void;
  // 设置角色分配对话框可见性
  setRoleDialogVisible: (visible: boolean) => void;
  // 设置选中用户ID列表
  setSelectedIds: (ids: number[]) => void;
  // 添加选中用户ID
  addSelectedId: (id: number) => void;
  // 移除选中用户ID
  removeSelectedId: (id: number) => void;
  // 切换选中用户ID
  toggleSelectedId: (id: number) => void;
  // 清空选中用户ID
  clearSelectedIds: () => void;
  // 清除错误
  clearError: () => void;
}

export const useUserManagementStore = create<UserManagementState>()((set, get) => ({
  users: [],
  pagination: {
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
    first: true,
    last: true,
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  currentUser: null,
  isLoading: false,
  error: null,
  formVisible: false,
  confirmDialogVisible: false,
  roleDialogVisible: false,
  selectedIds: [],
  
  // 查询用户列表
  fetchUsers: async (params) => {
    set({ isLoading: true, error: null });
    try {
      // 合并查询参数
      const queryParams = params 
        ? { ...get().queryParams, ...params }
        : get().queryParams;
        
      set({ queryParams });
      
      const response = await userService.getUserList(queryParams);
      set({ 
        users: response.content,
        pagination: {
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          size: response.size,
          number: response.number,
          first: response.first,
          last: response.last,
        },
        isLoading: false
      });
    } catch (error) {
      console.error('获取用户列表失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '获取用户列表失败' 
      });
      toast.error('获取用户列表失败');
    }
  },
  
  // 根据ID获取用户
  fetchUserById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const user = await userService.getUserById(id);
      set({ currentUser: user, isLoading: false });
    } catch (error) {
      console.error(`获取用户详情失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '获取用户详情失败'
      });
      toast.error('获取用户详情失败');
    }
  },
  
  // 创建用户
  createUser: async (user) => {
    set({ isLoading: true, error: null });
    try {
      const createdUser = await userService.createUser(user);
      set(state => ({ 
        users: [createdUser, ...state.users],
        isLoading: false,
        formVisible: false,
        currentUser: null
      }));
      toast.success('创建用户成功');
    } catch (error) {
      console.error('创建用户失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '创建用户失败'
      });
      toast.error('创建用户失败');
    }
  },
  
  // 更新用户
  updateUser: async (id, user) => {
    set({ isLoading: true, error: null });
    try {
      const updatedUser = await userService.updateUser(id, user);
      set(state => ({
        users: state.users.map(u => u.id === id ? updatedUser : u),
        isLoading: false,
        formVisible: false,
        currentUser: null
      }));
      toast.success('更新用户成功');
    } catch (error) {
      console.error(`更新用户失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '更新用户失败'
      });
      toast.error('更新用户失败');
    }
  },
  
  // 删除用户
  deleteUser: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await userService.deleteUser(id);
      set(state => ({
        users: state.users.filter(u => u.id !== id),
        isLoading: false,
        confirmDialogVisible: false
      }));
      toast.success('删除用户成功');
    } catch (error) {
      console.error(`删除用户失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '删除用户失败'
      });
      toast.error('删除用户失败');
    }
  },
  
  // 批量删除用户
  batchDeleteUsers: async (ids) => {
    set({ isLoading: true, error: null });
    try {
      await userService.batchDeleteUsers(ids);
      set(state => ({
        users: state.users.filter(u => !ids.includes(u.id)),
        isLoading: false,
        selectedIds: [],
        confirmDialogVisible: false
      }));
      toast.success('批量删除用户成功');
    } catch (error) {
      console.error('批量删除用户失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '批量删除用户失败'
      });
      toast.error('批量删除用户失败');
    }
  },
  
  // 更新用户状态
  updateUserStatus: async (id, status) => {
    set({ isLoading: true, error: null });
    try {
      const updatedUser = await userService.updateUserStatus(id, { status });
      set(state => ({
        users: state.users.map(u => u.id === id ? updatedUser : u),
        isLoading: false
      }));
      toast.success('更新用户状态成功');
    } catch (error) {
      console.error(`更新用户状态失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '更新用户状态失败'
      });
      toast.error('更新用户状态失败');
    }
  },
  
  // 给用户分配角色
  assignRoles: async (userId, roleIds) => {
    set({ isLoading: true, error: null });
    try {
      const updatedUser = await userService.assignRoles(userId, roleIds);
      set(state => ({
        users: state.users.map(u => u.id === userId ? updatedUser : u),
        isLoading: false,
        roleDialogVisible: false,
        currentUser: null
      }));
      toast.success('角色分配成功');
    } catch (error) {
      console.error(`角色分配失败, userId: ${userId}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '角色分配失败'
      });
      toast.error('角色分配失败');
    }
  },
  
  // 设置查询参数
  setQueryParams: (params) => {
    set(state => ({ 
      queryParams: { ...state.queryParams, ...params } 
    }));
  },
  
  // 设置当前用户
  setCurrentUser: (user) => {
    set({ currentUser: user });
  },
  
  // 设置表单可见性
  setFormVisible: (visible) => {
    // 如果关闭表单，清空当前用户
    if (!visible) {
      set({ currentUser: null });
    }
    set({ formVisible: visible });
  },
  
  // 设置确认对话框可见性
  setConfirmDialogVisible: (visible) => {
    set({ confirmDialogVisible: visible });
  },
  
  // 设置角色分配对话框可见性
  setRoleDialogVisible: (visible) => {
    set({ roleDialogVisible: visible });
  },
  
  // 设置选中用户ID列表
  setSelectedIds: (ids) => {
    set({ selectedIds: ids });
  },
  
  // 添加选中用户ID
  addSelectedId: (id) => {
    set(state => ({
      selectedIds: [...state.selectedIds, id]
    }));
  },
  
  // 移除选中用户ID
  removeSelectedId: (id) => {
    set(state => ({
      selectedIds: state.selectedIds.filter(itemId => itemId !== id)
    }));
  },
  
  // 切换选中用户ID
  toggleSelectedId: (id) => {
    set(state => {
      if (state.selectedIds.includes(id)) {
        return { selectedIds: state.selectedIds.filter(itemId => itemId !== id) };
      } else {
        return { selectedIds: [...state.selectedIds, id] };
      }
    });
  },
  
  // 清空选中用户ID
  clearSelectedIds: () => {
    set({ selectedIds: [] });
  },
  
  // 清除错误
  clearError: () => {
    set({ error: null });
  }
})); 