'use client';

import { create } from 'zustand';
import { Role, RoleDTO } from '@/types/role';
import roleService from '@/services/role';
import { toast } from 'sonner';

interface RoleState {
  // 角色列表
  roles: Role[];
  // 当前编辑的角色
  currentRole: Role | null;
  // 加载状态
  isLoading: boolean;
  // 错误信息
  error: string | null;
  // 表单可见性
  formVisible: boolean;
  // 确认对话框可见性
  confirmDialogVisible: boolean;
  // 权限分配对话框可见性
  permissionDialogVisible: boolean;
  // 待删除的角色ID列表
  selectedIds: number[];
  
  // 获取角色列表
  fetchRoles: () => Promise<void>;
  // 根据ID获取角色
  fetchRoleById: (id: number) => Promise<void>;
  // 创建角色
  createRole: (role: RoleDTO) => Promise<void>;
  // 更新角色
  updateRole: (id: number, role: RoleDTO) => Promise<void>;
  // 删除角色
  deleteRole: (id: number) => Promise<void>;
  // 批量删除角色
  batchDeleteRoles: (ids: number[]) => Promise<void>;
  // 给角色分配权限
  assignPermissions: (roleId: number, permissionIds: number[]) => Promise<void>;
  
  // 设置当前角色
  setCurrentRole: (role: Role | null) => void;
  // 设置表单可见性
  setFormVisible: (visible: boolean) => void;
  // 设置确认对话框可见性
  setConfirmDialogVisible: (visible: boolean) => void;
  // 设置权限分配对话框可见性
  setPermissionDialogVisible: (visible: boolean) => void;
  // 设置选中角色ID列表
  setSelectedIds: (ids: number[]) => void;
  // 添加选中角色ID
  addSelectedId: (id: number) => void;
  // 移除选中角色ID
  removeSelectedId: (id: number) => void;
  // 切换选中角色ID
  toggleSelectedId: (id: number) => void;
  // 清空选中角色ID
  clearSelectedIds: () => void;
  // 清除错误
  clearError: () => void;
}

export const useRoleStore = create<RoleState>()((set, get) => ({
  roles: [],
  currentRole: null,
  isLoading: false,
  error: null,
  formVisible: false,
  confirmDialogVisible: false,
  permissionDialogVisible: false,
  selectedIds: [],
  
  // 获取角色列表
  fetchRoles: async () => {
    set({ isLoading: true, error: null });
    try {
      const roles = await roleService.getRoleList();
      set({ roles, isLoading: false });
    } catch (error) {
      console.error('获取角色列表失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '获取角色列表失败' 
      });
      toast.error('获取角色列表失败');
    }
  },
  
  // 根据ID获取角色
  fetchRoleById: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const role = await roleService.getRoleById(id);
      set({ currentRole: role, isLoading: false });
    } catch (error) {
      console.error(`获取角色详情失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '获取角色详情失败'
      });
      toast.error('获取角色详情失败');
    }
  },
  
  // 创建角色
  createRole: async (role: RoleDTO) => {
    set({ isLoading: true, error: null });
    try {
      const createdRole = await roleService.createRole(role);
      set(state => ({ 
        roles: [...state.roles, createdRole],
        isLoading: false,
        formVisible: false,
        currentRole: null
      }));
      toast.success('创建角色成功');
    } catch (error) {
      console.error('创建角色失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '创建角色失败'
      });
      toast.error('创建角色失败');
    }
  },
  
  // 更新角色
  updateRole: async (id: number, role: RoleDTO) => {
    set({ isLoading: true, error: null });
    try {
      const updatedRole = await roleService.updateRole(id, role);
      set(state => ({
        roles: state.roles.map(r => r.id === id ? updatedRole : r),
        isLoading: false,
        formVisible: false,
        currentRole: null
      }));
      toast.success('更新角色成功');
    } catch (error) {
      console.error(`更新角色失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '更新角色失败'
      });
      toast.error('更新角色失败');
    }
  },
  
  // 删除角色
  deleteRole: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await roleService.deleteRole(id);
      set(state => ({
        roles: state.roles.filter(r => r.id !== id),
        isLoading: false
      }));
      toast.success('删除角色成功');
    } catch (error) {
      console.error(`删除角色失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '删除角色失败'
      });
      toast.error('删除角色失败');
    }
  },
  
  // 批量删除角色
  batchDeleteRoles: async (ids: number[]) => {
    set({ isLoading: true, error: null });
    try {
      await roleService.batchDeleteRoles(ids);
      set(state => ({
        roles: state.roles.filter(r => !ids.includes(r.id)),
        isLoading: false,
        selectedIds: [],
        confirmDialogVisible: false
      }));
      toast.success('批量删除角色成功');
    } catch (error) {
      console.error('批量删除角色失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '批量删除角色失败'
      });
      toast.error('批量删除角色失败');
    }
  },
  
  // 给角色分配权限
  assignPermissions: async (roleId: number, permissionIds: number[]) => {
    set({ isLoading: true, error: null });
    try {
      const updatedRole = await roleService.assignPermissions(roleId, permissionIds);
      set(state => ({
        roles: state.roles.map(r => r.id === roleId ? updatedRole : r),
        isLoading: false,
        permissionDialogVisible: false,
        currentRole: null
      }));
      toast.success('权限分配成功');
    } catch (error) {
      console.error(`权限分配失败, roleId: ${roleId}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '权限分配失败'
      });
      toast.error('权限分配失败');
    }
  },
  
  // 设置当前角色
  setCurrentRole: (role) => {
    set({ currentRole: role });
  },
  
  // 设置表单可见性
  setFormVisible: (visible) => {
    // 如果关闭表单，清空当前角色
    if (!visible) {
      set({ currentRole: null });
    }
    set({ formVisible: visible });
  },
  
  // 设置确认对话框可见性
  setConfirmDialogVisible: (visible) => {
    set({ confirmDialogVisible: visible });
  },
  
  // 设置权限分配对话框可见性
  setPermissionDialogVisible: (visible) => {
    set({ permissionDialogVisible: visible });
  },
  
  // 设置选中角色ID列表
  setSelectedIds: (ids) => {
    set({ selectedIds: ids });
  },
  
  // 添加选中角色ID
  addSelectedId: (id) => {
    set(state => ({
      selectedIds: [...state.selectedIds, id]
    }));
  },
  
  // 移除选中角色ID
  removeSelectedId: (id) => {
    set(state => ({
      selectedIds: state.selectedIds.filter(itemId => itemId !== id)
    }));
  },
  
  // 切换选中角色ID
  toggleSelectedId: (id) => {
    set(state => {
      if (state.selectedIds.includes(id)) {
        return { selectedIds: state.selectedIds.filter(itemId => itemId !== id) };
      } else {
        return { selectedIds: [...state.selectedIds, id] };
      }
    });
  },
  
  // 清空选中角色ID
  clearSelectedIds: () => {
    set({ selectedIds: [] });
  },
  
  // 清除错误
  clearError: () => {
    set({ error: null });
  }
})); 