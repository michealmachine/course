'use client';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { useUserManagementStore } from '@/stores/user-management-store';

export function DeleteConfirmationDialog() {
  const { 
    confirmDialogVisible, 
    setConfirmDialogVisible, 
    currentUser, 
    selectedIds,
    deleteUser,
    batchDeleteUsers,
    isLoading 
  } = useUserManagementStore();
  
  // 判断是单个删除还是批量删除
  const isBatchDelete = selectedIds.length > 0 && !currentUser;
  
  // 获取要删除的用户名或数量
  const getDeleteTarget = () => {
    if (currentUser) {
      return `用户 "${currentUser.username}"`;
    } else if (isBatchDelete) {
      return `${selectedIds.length} 个用户`;
    }
    return '选中的用户';
  };
  
  // 处理删除操作
  const handleDelete = async () => {
    if (currentUser) {
      await deleteUser(currentUser.id);
    } else if (isBatchDelete) {
      await batchDeleteUsers(selectedIds);
    }
  };
  
  return (
    <Dialog open={confirmDialogVisible} onOpenChange={setConfirmDialogVisible}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>确认删除</DialogTitle>
          <DialogDescription>
            您确定要删除 {getDeleteTarget()} 吗？此操作无法撤销。
          </DialogDescription>
        </DialogHeader>
        
        <DialogFooter className="mt-4">
          <Button 
            type="button" 
            variant="outline" 
            onClick={() => setConfirmDialogVisible(false)}
            disabled={isLoading}
          >
            取消
          </Button>
          <Button 
            variant="destructive" 
            onClick={handleDelete}
            disabled={isLoading}
          >
            {isLoading ? '处理中...' : '删除'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
} 