'use client';

import { useEffect, useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Loader2, Search, X } from 'lucide-react';
import { Input } from '@/components/ui/input';

import { useRoleStore } from '@/stores/role-store';
import { usePermissionStore } from '@/stores/permission-store';
import { Permission } from '@/types/permission';

export function PermissionDialog() {
  const { 
    currentRole, 
    permissionDialogVisible, 
    setPermissionDialogVisible, 
    assignPermissions, 
    isLoading 
  } = useRoleStore();
  
  const { permissions, fetchPermissions } = usePermissionStore();
  
  // 选中的权限ID列表
  const [selectedPermissionIds, setSelectedPermissionIds] = useState<number[]>([]);
  // 搜索关键词
  const [searchTerm, setSearchTerm] = useState('');
  
  // 初始加载权限列表
  useEffect(() => {
    fetchPermissions();
  }, [fetchPermissions]);
  
  // 当对话框打开时，初始化选中的权限
  useEffect(() => {
    if (currentRole && currentRole.permissions) {
      setSelectedPermissionIds(currentRole.permissions.map(p => p.id));
    } else {
      setSelectedPermissionIds([]);
    }
  }, [currentRole, permissionDialogVisible]);

  // 处理权限选择
  const handlePermissionToggle = (id: number) => {
    setSelectedPermissionIds(prev => {
      if (prev.includes(id)) {
        return prev.filter(permId => permId !== id);
      } else {
        return [...prev, id];
      }
    });
  };

  // 处理全选/取消全选
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedPermissionIds(filteredPermissions.map(p => p.id));
    } else {
      setSelectedPermissionIds([]);
    }
  };

  // 提交权限分配
  const handleSubmit = async () => {
    if (currentRole) {
      await assignPermissions(currentRole.id, selectedPermissionIds);
    }
  };

  // 过滤权限列表
  const filteredPermissions = permissions.filter(permission => {
    return (
      permission.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      permission.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (permission.description && permission.description.toLowerCase().includes(searchTerm.toLowerCase()))
    );
  });

  return (
    <Dialog open={permissionDialogVisible} onOpenChange={setPermissionDialogVisible}>
      <DialogContent className="sm:max-w-[550px]">
        <DialogHeader>
          <DialogTitle>
            {currentRole ? `为角色 "${currentRole.name}" 分配权限` : '分配权限'}
          </DialogTitle>
        </DialogHeader>
        
        {/* 搜索框 */}
        <div className="flex items-center space-x-2 mb-4">
          <div className="relative flex-1">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="搜索权限..."
              className="pl-8"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            {searchTerm && (
              <button
                type="button"
                onClick={() => setSearchTerm('')}
                className="absolute right-2.5 top-2.5 text-muted-foreground hover:text-foreground"
              >
                <X className="h-4 w-4" />
                <span className="sr-only">清除搜索</span>
              </button>
            )}
          </div>
        </div>
        
        {/* 全选复选框 */}
        <div className="flex items-center space-x-2 mb-2">
          <Checkbox
            id="select-all"
            checked={
              filteredPermissions.length > 0 &&
              filteredPermissions.every(p => selectedPermissionIds.includes(p.id))
            }
            onCheckedChange={handleSelectAll}
          />
          <Label htmlFor="select-all">全选</Label>
        </div>
        
        {/* 权限列表 */}
        <ScrollArea className="h-[300px] pr-4">
          <div className="space-y-2">
            {filteredPermissions.length === 0 ? (
              <div className="py-6 text-center text-muted-foreground">
                {searchTerm ? '未找到匹配的权限' : '没有可用的权限'}
              </div>
            ) : (
              filteredPermissions.map((permission) => (
                <div key={permission.id} className="flex items-center space-x-2 py-1">
                  <Checkbox
                    id={`permission-${permission.id}`}
                    checked={selectedPermissionIds.includes(permission.id)}
                    onCheckedChange={() => handlePermissionToggle(permission.id)}
                  />
                  <div className="grid gap-1.5 leading-none">
                    <Label htmlFor={`permission-${permission.id}`} className="font-medium">
                      {permission.name} <span className="text-muted-foreground">({permission.code})</span>
                    </Label>
                    {permission.description && (
                      <p className="text-xs text-muted-foreground">
                        {permission.description}
                      </p>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </ScrollArea>
        
        {/* 操作按钮 */}
        <div className="flex justify-end space-x-2 pt-4">
          <Button
            variant="outline"
            onClick={() => setPermissionDialogVisible(false)}
            disabled={isLoading}
          >
            取消
          </Button>
          <Button onClick={handleSubmit} disabled={isLoading}>
            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            保存
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
} 