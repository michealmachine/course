'use client';

import { useEffect, useState } from 'react';
import { Pencil, Trash2, Plus, Shield, Search, X } from 'lucide-react';
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

import { usePermissionStore } from '@/stores/permission-store';
import { Permission } from '@/types/permission';
import { PermissionForm } from '@/components/dashboard/permissions/permission-form';
import { DeleteConfirmationDialog } from '@/components/dashboard/permissions/delete-confirmation-dialog';

// HTTP方法的对应颜色
const methodColors = {
  GET: 'bg-green-500',
  POST: 'bg-blue-500',
  PUT: 'bg-amber-500',
  DELETE: 'bg-red-500',
  PATCH: 'bg-purple-500',
};

export default function PermissionsPage() {
  // 使用权限状态
  const {
    permissions,
    isLoading,
    error,
    formVisible,
    confirmDialogVisible,
    selectedIds,
    fetchPermissions,
    setFormVisible,
    setConfirmDialogVisible,
    setCurrentPermission,
    toggleSelectedId,
    clearSelectedIds,
  } = usePermissionStore();

  // 搜索关键字
  const [searchTerm, setSearchTerm] = useState('');
  // 是否显示搜索栏
  const [showSearch, setShowSearch] = useState(false);
  // 删除模式：单个或批量
  const [deleteMode, setDeleteMode] = useState<'single' | 'batch'>('single');
  // 当前要删除的权限ID
  const [currentDeleteId, setCurrentDeleteId] = useState<number | undefined>(undefined);
  // 当前页码
  const [currentPage, setCurrentPage] = useState(1);
  // 每页条数
  const [pageSize] = useState(10);

  // 组件加载时获取权限列表
  useEffect(() => {
    fetchPermissions();
  }, [fetchPermissions]);

  // 筛选权限列表
  const filteredPermissions = permissions?.filter((permission) =>
    permission.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    permission.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (permission.description && permission.description.toLowerCase().includes(searchTerm.toLowerCase()))
  ) || [];

  // 分页权限列表
  const paginatedPermissions = filteredPermissions.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  // 总页数
  const totalPages = Math.ceil(filteredPermissions.length / pageSize);

  // 处理创建权限
  const handleCreatePermission = () => {
    setCurrentPermission(null);
    setFormVisible(true);
  };

  // 处理编辑权限
  const handleEditPermission = (permission: Permission) => {
    setCurrentPermission(permission);
    setFormVisible(true);
  };

  // 处理删除权限
  const handleDeletePermission = (id: number) => {
    setDeleteMode('single');
    setCurrentDeleteId(id);
    setConfirmDialogVisible(true);
  };

  // 处理批量删除权限
  const handleBatchDelete = () => {
    if (selectedIds.length === 0) return;
    setDeleteMode('batch');
    setConfirmDialogVisible(true);
  };

  // 处理全选/取消全选
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      const ids = paginatedPermissions.map(p => p.id);
      usePermissionStore.setState({ selectedIds: ids });
    } else {
      clearSelectedIds();
    }
  };

  // 渲染权限列表表格
  const renderPermissionsTable = () => {
    if (isLoading) {
      return (
        <div className="space-y-3">
          {[...Array(5)].map((_, index) => (
            <Skeleton key={index} className="w-full h-12" />
          ))}
        </div>
      );
    }

    if (paginatedPermissions.length === 0) {
      return (
        <div className="py-12 text-center">
          <Shield className="mx-auto h-12 w-12 text-muted-foreground" />
          <h3 className="mt-4 text-lg font-semibold">没有权限数据</h3>
          <p className="mt-2 text-sm text-muted-foreground">
            {searchTerm ? '没有匹配的搜索结果，请尝试其他关键词' : '系统中还没有权限数据，请点击"创建权限"按钮添加'}
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
                    paginatedPermissions.length > 0 &&
                    paginatedPermissions.every(p => selectedIds.includes(p.id))
                  }
                  onCheckedChange={handleSelectAll}
                  aria-label="全选"
                />
              </TableHead>
              <TableHead className="w-[200px]">权限名称</TableHead>
              <TableHead className="w-[200px]">权限编码</TableHead>
              <TableHead className="hidden md:table-cell">资源路径</TableHead>
              <TableHead className="w-[100px] hidden md:table-cell">HTTP方法</TableHead>
              <TableHead className="w-[150px] hidden md:table-cell">创建时间</TableHead>
              <TableHead className="w-[120px] text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {paginatedPermissions.map((permission) => (
              <TableRow key={permission.id}>
                <TableCell>
                  <Checkbox
                    checked={selectedIds.includes(permission.id)}
                    onCheckedChange={() => toggleSelectedId(permission.id)}
                    aria-label={`选择${permission.name}`}
                  />
                </TableCell>
                <TableCell className="font-medium">{permission.name}</TableCell>
                <TableCell>{permission.code}</TableCell>
                <TableCell className="hidden md:table-cell">
                  {permission.url || '-'}
                </TableCell>
                <TableCell className="hidden md:table-cell">
                  {permission.method ? (
                    <Badge 
                      className={methodColors[permission.method as keyof typeof methodColors] || 'bg-gray-500'}
                    >
                      {permission.method}
                    </Badge>
                  ) : (
                    '-'
                  )}
                </TableCell>
                <TableCell className="hidden md:table-cell">
                  {permission.createdAt
                    ? formatDistanceToNow(new Date(permission.createdAt), { addSuffix: true, locale: zhCN })
                    : '-'}
                </TableCell>
                <TableCell className="text-right">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleEditPermission(permission)}
                    className="mr-1"
                  >
                    <Pencil className="h-4 w-4" />
                    <span className="sr-only">编辑</span>
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleDeletePermission(permission.id)}
                    className="text-destructive hover:text-destructive hover:bg-destructive/10"
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
    <div className="space-y-4">
      <Card>
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center justify-between space-y-2 sm:space-y-0 py-5">
          <div>
            <CardTitle>权限管理</CardTitle>
            <CardDescription>
              管理系统权限，控制资源访问
            </CardDescription>
          </div>
          <div className="flex items-center gap-2 w-full sm:w-auto">
            {showSearch ? (
              <div className="flex items-center w-full sm:w-auto">
                <Input
                  placeholder="搜索权限..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="mr-2"
                />
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => {
                    setSearchTerm('');
                    setShowSearch(false);
                  }}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ) : (
              <>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setShowSearch(true)}
                >
                  <Search className="h-4 w-4 mr-2" />
                  搜索
                </Button>
                {selectedIds.length > 0 && (
                  <Button
                    variant="destructive"
                    size="sm"
                    onClick={handleBatchDelete}
                  >
                    <Trash2 className="h-4 w-4 mr-2" />
                    删除所选 ({selectedIds.length})
                  </Button>
                )}
                <Button size="sm" onClick={handleCreatePermission}>
                  <Plus className="h-4 w-4 mr-2" />
                  创建权限
                </Button>
              </>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {renderPermissionsTable()}
        </CardContent>
      </Card>

      {/* 权限表单对话框 */}
      <PermissionForm />

      {/* 删除确认对话框 */}
      <DeleteConfirmationDialog 
        mode={deleteMode} 
        id={currentDeleteId} 
      />
    </div>
  );
} 