'use client';

import { useEffect, useState } from 'react';
import { Pencil, Trash2, Plus, Shield, Search, X, Settings } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import { Skeleton } from '@/components/ui/skeleton';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination';

import { useRoleStore } from '@/stores/role-store';
import { Role } from '@/types/role';
import { RoleForm } from '@/components/dashboard/roles/role-form';
import { DeleteConfirmationDialog } from '@/components/dashboard/roles/delete-confirmation-dialog';
import { PermissionDialog } from '@/components/dashboard/roles/permission-dialog';

export default function RolesPage() {
  // 使用角色状态
  const {
    roles,
    isLoading,
    error,
    formVisible,
    confirmDialogVisible,
    permissionDialogVisible,
    selectedIds,
    fetchRoles,
    setFormVisible,
    setConfirmDialogVisible,
    setPermissionDialogVisible,
    setCurrentRole,
    toggleSelectedId,
    clearSelectedIds,
  } = useRoleStore();

  // 搜索关键字
  const [searchTerm, setSearchTerm] = useState('');
  // 是否显示搜索栏
  const [showSearch, setShowSearch] = useState(false);
  // 删除模式：单个或批量
  const [deleteMode, setDeleteMode] = useState<'single' | 'batch'>('single');
  // 当前要删除的角色ID
  const [currentDeleteId, setCurrentDeleteId] = useState<number | undefined>(undefined);
  // 当前页码
  const [currentPage, setCurrentPage] = useState(1);
  // 每页条数
  const [pageSize] = useState(10);

  // 组件加载时获取角色列表
  useEffect(() => {
    fetchRoles();
  }, [fetchRoles]);

  // 筛选角色列表
  const filteredRoles = roles?.filter((role) =>
    role.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    role.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (role.description && role.description.toLowerCase().includes(searchTerm.toLowerCase()))
  ) || [];

  // 分页角色列表
  const paginatedRoles = filteredRoles.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  // 总页数
  const totalPages = Math.ceil(filteredRoles.length / pageSize);

  // 处理创建角色
  const handleCreateRole = () => {
    setCurrentRole(null);
    setFormVisible(true);
  };

  // 处理编辑角色
  const handleEditRole = (role: Role) => {
    setCurrentRole(role);
    setFormVisible(true);
  };

  // 处理分配权限
  const handleAssignPermissions = (role: Role) => {
    setCurrentRole(role);
    setPermissionDialogVisible(true);
  };

  // 处理删除角色
  const handleDeleteRole = (id: number) => {
    setDeleteMode('single');
    // 设置要删除的角色ID
    useRoleStore.setState({ selectedIds: [id] });
    setConfirmDialogVisible(true);
  };

  // 处理批量删除角色
  const handleBatchDelete = () => {
    if (selectedIds.length === 0) return;
    setDeleteMode('batch');
    setConfirmDialogVisible(true);
  };

  // 处理全选/取消全选
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      const ids = paginatedRoles.map(r => r.id);
      useRoleStore.setState({ selectedIds: ids });
    } else {
      clearSelectedIds();
    }
  };

  // 渲染角色列表表格
  const renderRolesTable = () => {
    if (isLoading) {
      return (
        <div className="space-y-3">
          {[...Array(5)].map((_, index) => (
            <Skeleton key={index} className="w-full h-12" />
          ))}
        </div>
      );
    }

    if (paginatedRoles.length === 0) {
      return (
        <div className="py-12 text-center">
          <Shield className="mx-auto h-12 w-12 text-muted-foreground" />
          <h3 className="mt-4 text-lg font-semibold">没有角色数据</h3>
          <p className="mt-2 text-sm text-muted-foreground">
            {searchTerm ? '没有匹配的搜索结果，请尝试其他关键词' : '系统中还没有角色数据，请点击"创建角色"按钮添加'}
          </p>
        </div>
      );
    }

    return (
      <>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[50px]">
                <Checkbox
                  checked={
                    paginatedRoles.length > 0 &&
                    paginatedRoles.every(r => selectedIds.includes(r.id))
                  }
                  onCheckedChange={handleSelectAll}
                  aria-label="全选"
                />
              </TableHead>
              <TableHead className="w-[200px]">角色名称</TableHead>
              <TableHead className="w-[200px]">角色编码</TableHead>
              <TableHead className="hidden md:table-cell">描述</TableHead>
              <TableHead className="w-[150px] hidden md:table-cell">创建时间</TableHead>
              <TableHead className="w-[150px] text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {paginatedRoles.map((role) => (
              <TableRow key={role.id}>
                <TableCell>
                  <Checkbox
                    checked={selectedIds.includes(role.id)}
                    onCheckedChange={() => toggleSelectedId(role.id)}
                    aria-label={`选择${role.name}`}
                  />
                </TableCell>
                <TableCell className="font-medium">{role.name}</TableCell>
                <TableCell>
                  <Badge variant="outline">{role.code}</Badge>
                </TableCell>
                <TableCell className="hidden md:table-cell">
                  {role.description || '-'}
                </TableCell>
                <TableCell className="hidden md:table-cell">
                  {role.createdAt
                    ? formatDistanceToNow(new Date(role.createdAt), { addSuffix: true, locale: zhCN })
                    : '-'}
                </TableCell>
                <TableCell className="text-right">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleAssignPermissions(role)}
                    className="mr-1"
                    title="分配权限"
                  >
                    <Settings className="h-4 w-4" />
                    <span className="sr-only">分配权限</span>
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleEditRole(role)}
                    className="mr-1"
                    title="编辑角色"
                  >
                    <Pencil className="h-4 w-4" />
                    <span className="sr-only">编辑</span>
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleDeleteRole(role.id)}
                    className="text-destructive hover:text-destructive hover:bg-destructive/10"
                    title="删除角色"
                  >
                    <Trash2 className="h-4 w-4" />
                    <span className="sr-only">删除</span>
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {/* 分页组件 */}
        {totalPages > 1 && (
          <Pagination className="mt-4">
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious 
                  href="#" 
                  onClick={(e) => {
                    e.preventDefault();
                    if (currentPage > 1) setCurrentPage(currentPage - 1);
                  }}
                  className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>
              
              {[...Array(totalPages)].map((_, i) => (
                <PaginationItem key={i}>
                  <PaginationLink 
                    href="#" 
                    onClick={(e) => {
                      e.preventDefault();
                      setCurrentPage(i + 1);
                    }}
                    isActive={currentPage === i + 1}
                  >
                    {i + 1}
                  </PaginationLink>
                </PaginationItem>
              ))}
              
              <PaginationItem>
                <PaginationNext 
                  href="#" 
                  onClick={(e) => {
                    e.preventDefault();
                    if (currentPage < totalPages) setCurrentPage(currentPage + 1);
                  }}
                  className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        )}
      </>
    );
  };

  return (
    <div className="container mx-auto py-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">角色管理</h1>
        <div className="flex space-x-2">
          {showSearch ? (
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="text"
                placeholder="搜索角色..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-[200px] pl-8 pr-8"
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
          ) : (
            <Button
              variant="outline"
              size="icon"
              onClick={() => setShowSearch(true)}
            >
              <Search className="h-4 w-4" />
              <span className="sr-only">搜索</span>
            </Button>
          )}
          {selectedIds.length > 0 && (
            <Button
              variant="destructive"
              size="sm"
              onClick={handleBatchDelete}
            >
              批量删除 ({selectedIds.length})
            </Button>
          )}
          <Button onClick={handleCreateRole}>
            <Plus className="mr-2 h-4 w-4" />
            创建角色
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>角色列表</CardTitle>
          <CardDescription>管理系统中的角色，包括创建、编辑、删除和分配权限等操作。</CardDescription>
        </CardHeader>
        <CardContent>
          {renderRolesTable()}
        </CardContent>
      </Card>

      {/* 角色表单对话框 */}
      <RoleForm />

      {/* 权限分配对话框 */}
      <PermissionDialog />

      {/* 删除确认对话框 */}
      <DeleteConfirmationDialog />
    </div>
  );
} 