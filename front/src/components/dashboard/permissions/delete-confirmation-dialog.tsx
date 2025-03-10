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
import { usePermissionStore } from '@/stores/permission-store';

interface DeleteConfirmationDialogProps {
  mode: 'single' | 'batch';
  id?: number;
}

export function DeleteConfirmationDialog({ mode, id }: DeleteConfirmationDialogProps) {
  const { 
    confirmDialogVisible, 
    setConfirmDialogVisible,
    deletePermission,
    batchDeletePermissions,
    selectedIds,
    isLoading
  } = usePermissionStore();

  const handleConfirm = async () => {
    if (mode === 'single' && id) {
      await deletePermission(id);
    } else if (mode === 'batch' && selectedIds.length > 0) {
      await batchDeletePermissions(selectedIds);
    }
    setConfirmDialogVisible(false);
  };

  const title = mode === 'single' ? '删除权限' : '批量删除权限';
  const description = mode === 'single' 
    ? '确定要删除此权限吗？此操作无法撤销。' 
    : `确定要删除选中的 ${selectedIds.length} 个权限吗？此操作无法撤销。`;

  return (
    <AlertDialog open={confirmDialogVisible} onOpenChange={setConfirmDialogVisible}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isLoading}>取消</AlertDialogCancel>
          <AlertDialogAction 
            onClick={handleConfirm}
            disabled={isLoading}
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
          >
            {isLoading ? '删除中...' : '确认删除'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
} 