'use client';

import { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Checkbox } from '@/components/ui/checkbox';
import { useUserManagementStore } from '@/stores/user-management-store';
import { useRoleStore } from '@/stores/role-store';
import { Role } from '@/types/auth';
import { getRoleDisplayName } from '@/utils/roleUtils';

export function RoleAssignmentDialog() {
  const {
    roleDialogVisible,
    setRoleDialogVisible,
    currentUser,
    assignRoles,
    isLoading
  } = useUserManagementStore();

  const { roles, fetchRoles } = useRoleStore();

  // 选中的角色ID列表
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);

  // 加载角色列表
  useEffect(() => {
    fetchRoles();
  }, [fetchRoles]);

  // 当currentUser变化时，更新选中的角色
  useEffect(() => {
    console.log('当前编辑用户:', currentUser);
    console.log('当前用户角色:', currentUser?.roles);

    // 如果后端没有返回角色信息，我们需要从API获取
    if (currentUser && currentUser.id) {
      // 如果用户有角色信息，使用它
      if (currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.length > 0) {
        const roleIds = currentUser.roles.map(role => role.id);
        console.log('提取的角色IDs:', roleIds);
        setSelectedRoleIds(roleIds);
      } else {
        // 如果没有角色信息，默认为空数组
        console.log('用户没有角色信息，设置为空数组');
        setSelectedRoleIds([]);
      }
    } else {
      setSelectedRoleIds([]);
    }
  }, [currentUser]);

  // 处理角色选择变化
  const handleRoleToggle = (roleId: number) => {
    setSelectedRoleIds(prev => {
      if (prev.includes(roleId)) {
        return prev.filter(id => id !== roleId);
      } else {
        return [...prev, roleId];
      }
    });
  };

  // 提交角色分配
  const handleSubmit = async () => {
    if (currentUser) {
      await assignRoles(currentUser.id, selectedRoleIds);
    }
  };

  return (
    <Dialog open={roleDialogVisible} onOpenChange={setRoleDialogVisible}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>分配角色</DialogTitle>
          <DialogDescription>
            {currentUser ? `为用户 ${currentUser.username} 分配角色` : '选择要分配的角色'}
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          <ScrollArea className="h-[300px] pr-4">
            <div className="space-y-4">
              {roles.map((role) => (
                <div key={role.id} className="flex items-center space-x-2">
                  <Checkbox
                    id={`role-${role.id}`}
                    checked={selectedRoleIds.includes(role.id)}
                    onCheckedChange={() => handleRoleToggle(role.id)}
                  />
                  <label
                    htmlFor={`role-${role.id}`}
                    className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                  >
                    <div>{getRoleDisplayName(role.code) || role.name}</div>
                    {role.description && (
                      <p className="text-xs text-muted-foreground mt-1">
                        {role.description}
                      </p>
                    )}
                    <p className="text-xs text-muted-foreground mt-1">
                      角色代码: {role.code}
                    </p>
                  </label>
                </div>
              ))}
            </div>
          </ScrollArea>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => setRoleDialogVisible(false)}
            disabled={isLoading}
          >
            取消
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={isLoading || selectedRoleIds.length === 0}
          >
            {isLoading ? '处理中...' : '保存'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}