'use client';

import { create } from 'zustand';
import { Permission, PermissionDTO } from '@/types/permission';
import permissionService from '@/services/permission';
import { toast } from 'sonner';

interface PermissionState {
  // 权限列表
  permissions: Permission[];
  // 当前编辑的权限
  currentPermission: Permission | null;
  // 加载状态
  isLoading: boolean;
  // 错误信息
  error: string | null;
  // 表单可见性
  formVisible: boolean;
  // 确认对话框可见性
  confirmDialogVisible: boolean;
  // 待删除的权限ID列表
  selectedIds: number[];
  
  // 获取权限列表
  fetchPermissions: () => Promise<void>;
  // 根据ID获取权限
  fetchPermissionById: (id: number) => Promise<void>;
  // 创建权限
  createPermission: (permission: PermissionDTO) => Promise<void>;
  // 更新权限
  updatePermission: (id: number, permission: PermissionDTO) => Promise<void>;
  // 删除权限
  deletePermission: (id: number) => Promise<void>;
  // 批量删除权限
  batchDeletePermissions: (ids: number[]) => Promise<void>;
  
  // 设置当前权限
  setCurrentPermission: (permission: Permission | null) => void;
  // 设置表单可见性
  setFormVisible: (visible: boolean) => void;
  // 设置确认对话框可见性
  setConfirmDialogVisible: (visible: boolean) => void;
  // 设置选中权限ID列表
  setSelectedIds: (ids: number[]) => void;
  // 添加选中权限ID
  addSelectedId: (id: number) => void;
  // 移除选中权限ID
  removeSelectedId: (id: number) => void;
  // 切换选中权限ID
  toggleSelectedId: (id: number) => void;
  // 清空选中权限ID
  clearSelectedIds: () => void;
  // 清除错误
  clearError: () => void;
}

export const usePermissionStore = create<PermissionState>()((set, get) => ({
  permissions: [],
  currentPermission: null,
  isLoading: false,
  error: null,
  formVisible: false,
  confirmDialogVisible: false,
  selectedIds: [],
  
  // 获取权限列表
  fetchPermissions: async () => {
    set({ isLoading: true, error: null });
    try {
      const permissions = await permissionService.getPermissionList();
      set({ permissions, isLoading: false });
    } catch (error) {
      console.error('获取权限列表失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '获取权限列表失败' 
      });
      toast.error('获取权限列表失败');
    }
  },
  
  // 根据ID获取权限
  fetchPermissionById: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      const permission = await permissionService.getPermissionById(id);
      set({ currentPermission: permission, isLoading: false });
    } catch (error) {
      console.error(`获取权限详情失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '获取权限详情失败'
      });
      toast.error('获取权限详情失败');
    }
  },
  
  // 创建权限
  createPermission: async (permission: PermissionDTO) => {
    set({ isLoading: true, error: null });
    try {
      const createdPermission = await permissionService.createPermission(permission);
      set(state => ({ 
        permissions: [...state.permissions, createdPermission],
        isLoading: false,
        formVisible: false,
        currentPermission: null
      }));
      toast.success('创建权限成功');
    } catch (error) {
      console.error('创建权限失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '创建权限失败'
      });
      toast.error('创建权限失败');
    }
  },
  
  // 更新权限
  updatePermission: async (id: number, permission: PermissionDTO) => {
    set({ isLoading: true, error: null });
    try {
      const updatedPermission = await permissionService.updatePermission(id, permission);
      set(state => ({
        permissions: state.permissions.map(p => p.id === id ? updatedPermission : p),
        isLoading: false,
        formVisible: false,
        currentPermission: null
      }));
      toast.success('更新权限成功');
    } catch (error) {
      console.error(`更新权限失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '更新权限失败'
      });
      toast.error('更新权限失败');
    }
  },
  
  // 删除权限
  deletePermission: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await permissionService.deletePermission(id);
      set(state => ({
        permissions: state.permissions.filter(p => p.id !== id),
        isLoading: false
      }));
      toast.success('删除权限成功');
    } catch (error) {
      console.error(`删除权限失败, ID: ${id}:`, error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '删除权限失败'
      });
      toast.error('删除权限失败');
    }
  },
  
  // 批量删除权限
  batchDeletePermissions: async (ids: number[]) => {
    set({ isLoading: true, error: null });
    try {
      await permissionService.batchDeletePermissions(ids);
      set(state => ({
        permissions: state.permissions.filter(p => !ids.includes(p.id)),
        isLoading: false,
        selectedIds: [],
        confirmDialogVisible: false
      }));
      toast.success('批量删除权限成功');
    } catch (error) {
      console.error('批量删除权限失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '批量删除权限失败'
      });
      toast.error('批量删除权限失败');
    }
  },
  
  // 设置当前权限
  setCurrentPermission: (permission) => {
    set({ currentPermission: permission });
  },
  
  // 设置表单可见性
  setFormVisible: (visible) => {
    // 如果关闭表单，清空当前权限
    if (!visible) {
      set({ currentPermission: null });
    }
    set({ formVisible: visible });
  },
  
  // 设置确认对话框可见性
  setConfirmDialogVisible: (visible) => {
    set({ confirmDialogVisible: visible });
  },
  
  // 设置选中权限ID列表
  setSelectedIds: (ids) => {
    set({ selectedIds: ids });
  },
  
  // 添加选中权限ID
  addSelectedId: (id) => {
    set(state => ({
      selectedIds: [...state.selectedIds, id]
    }));
  },
  
  // 移除选中权限ID
  removeSelectedId: (id) => {
    set(state => ({
      selectedIds: state.selectedIds.filter(itemId => itemId !== id)
    }));
  },
  
  // 切换选中权限ID
  toggleSelectedId: (id) => {
    set(state => {
      if (state.selectedIds.includes(id)) {
        return { selectedIds: state.selectedIds.filter(itemId => itemId !== id) };
      } else {
        return { selectedIds: [...state.selectedIds, id] };
      }
    });
  },
  
  // 清空选中权限ID
  clearSelectedIds: () => {
    set({ selectedIds: [] });
  },
  
  // 清除错误
  clearError: () => {
    set({ error: null });
  }
})); 