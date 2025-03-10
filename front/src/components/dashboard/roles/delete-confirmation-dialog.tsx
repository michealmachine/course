'use client';

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Loader2 } from 'lucide-react';

import { useRoleStore } from '@/stores/role-store';

export function DeleteConfirmationDialog() {
  const {
    confirmDialogVisible,
    setConfirmDialogVisible,
    selectedIds,
    deleteRole,
    batchDeleteRoles,
    isLoading,
  } = useRoleStore();

  // 判断是单个删除还是批量删除
  const isBatchDelete = selectedIds.length > 1;

  // 处理删除操作
  const handleDelete = async () => {
    if (isBatchDelete) {
      await batchDeleteRoles(selectedIds);
    } else if (selectedIds.length === 1) {
      await deleteRole(selectedIds[0]);
    }
  };

  return (
    <AlertDialog open={confirmDialogVisible} onOpenChange={setConfirmDialogVisible}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            {isBatchDelete
              ? `确认删除 ${selectedIds.length} 个角色`
              : '确认删除角色'}
          </AlertDialogTitle>
          <AlertDialogDescription>
            {isBatchDelete
              ? '此操作将删除选中的所有角色，删除后无法恢复。'
              : '此操作将删除该角色，删除后无法恢复。'}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isLoading}>取消</AlertDialogCancel>
          <AlertDialogAction onClick={handleDelete} disabled={isLoading}>
            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            确认删除
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
} 