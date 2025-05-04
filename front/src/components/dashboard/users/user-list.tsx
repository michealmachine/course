'use client';

import { useEffect, useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { Badge } from '@/components/ui/badge';
import { useUserManagementStore } from '@/stores/user-management-store';
import { User } from '@/types/auth';
import { UserQueryParams } from '@/types/user';
import { MoreHorizontal, Plus, Search, RefreshCw } from 'lucide-react';
import { formatDate } from '@/lib/utils';
import { getUserRoleDisplayNames, getRoleDisplayName } from '@/utils/roleUtils';

export function UserList() {
  const {
    users,
    pagination,
    queryParams,
    fetchUsers,
    fetchUserById,
    setQueryParams,
    setCurrentUser,
    setFormVisible,
    setConfirmDialogVisible,
    setRoleDialogVisible,
    selectedIds,
    toggleSelectedId,
    clearSelectedIds,
    updateUserStatus,
    isLoading,
  } = useUserManagementStore();

  // 搜索关键词
  const [searchKeyword, setSearchKeyword] = useState('');

  // 初始加载用户列表
  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // 调试输出用户数据
  useEffect(() => {
    if (users.length > 0) {
      console.log('用户列表数据:', users);
      console.log('第一个用户的角色数据:', users[0].roles);
    }
  }, [users]);

  // 处理搜索
  const handleSearch = () => {
    const newParams: Partial<UserQueryParams> = {
      pageNum: 1, // 重置到第一页
    };

    // 根据搜索关键词设置查询参数
    if (searchKeyword) {
      if (searchKeyword.includes('@')) {
        newParams.email = searchKeyword;
      } else if (/^1[3-9]\d{9}$/.test(searchKeyword)) {
        newParams.phone = searchKeyword;
      } else {
        newParams.username = searchKeyword;
      }
    } else {
      // 清空搜索条件
      newParams.username = undefined;
      newParams.email = undefined;
      newParams.phone = undefined;
    }

    fetchUsers(newParams);
  };

  // 处理页码变化
  const handlePageChange = (page: number) => {
    fetchUsers({ pageNum: page });
  };

  // 处理每页数量变化
  const handlePageSizeChange = (size: number) => {
    fetchUsers({ pageSize: size, pageNum: 1 });
  };

  // 处理编辑用户
  const handleEdit = (user: User) => {
    // 先获取用户详情，确保有完整的角色信息
    fetchUserById(user.id).then(() => {
      setFormVisible(true);
    });
  };

  // 处理删除用户
  const handleDelete = (user: User) => {
    setCurrentUser(user);
    setConfirmDialogVisible(true);
  };

  // 处理分配角色
  const handleAssignRoles = (user: User) => {
    // 先获取用户详情，确保有完整的角色信息
    fetchUserById(user.id).then(() => {
      setRoleDialogVisible(true);
    });
  };

  // 处理批量删除
  const handleBatchDelete = () => {
    setCurrentUser(null);
    setConfirmDialogVisible(true);
  };

  // 处理状态切换
  const handleStatusToggle = async (user: User) => {
    await updateUserStatus(user.id, user.status === 1 ? 0 : 1);
  };

  // 处理全选/取消全选
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      const allIds = users.map(user => user.id);
      useUserManagementStore.getState().setSelectedIds(allIds);
    } else {
      clearSelectedIds();
    }
  };

  // 判断是否全选
  const isAllSelected = users.length > 0 && selectedIds.length === users.length;

  // 生成分页项
  const renderPaginationItems = () => {
    const { totalPages, number } = pagination;
    const currentPage = number + 1; // API返回的页码从0开始，UI从1开始
    const items = [];

    // 添加首页
    if (totalPages > 5 && currentPage > 3) {
      items.push(
        <PaginationItem key="first">
          <PaginationLink onClick={() => handlePageChange(1)}>1</PaginationLink>
        </PaginationItem>
      );

      if (currentPage > 4) {
        items.push(
          <PaginationItem key="ellipsis-start">
            <PaginationEllipsis />
          </PaginationItem>
        );
      }
    }

    // 添加当前页附近的页码
    for (let i = Math.max(1, currentPage - 2); i <= Math.min(totalPages, currentPage + 2); i++) {
      items.push(
        <PaginationItem key={i}>
          <PaginationLink
            isActive={currentPage === i}
            onClick={() => handlePageChange(i)}
          >
            {i}
          </PaginationLink>
        </PaginationItem>
      );
    }

    // 添加末页
    if (totalPages > 5 && currentPage < totalPages - 2) {
      if (currentPage < totalPages - 3) {
        items.push(
          <PaginationItem key="ellipsis-end">
            <PaginationEllipsis />
          </PaginationItem>
        );
      }

      items.push(
        <PaginationItem key="last">
          <PaginationLink onClick={() => handlePageChange(totalPages)}>
            {totalPages}
          </PaginationLink>
        </PaginationItem>
      );
    }

    return items;
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Input
            placeholder="搜索用户名/邮箱/手机号"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="w-[300px]"
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />
          <Button variant="outline" size="icon" onClick={handleSearch}>
            <Search className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            size="icon"
            onClick={() => {
              setSearchKeyword('');
              fetchUsers({
                username: undefined,
                email: undefined,
                phone: undefined,
                pageNum: 1,
              });
            }}
          >
            <RefreshCw className="h-4 w-4" />
          </Button>
        </div>
        <div className="flex items-center gap-2">
          {selectedIds.length > 0 && (
            <Button
              variant="destructive"
              size="sm"
              onClick={handleBatchDelete}
              disabled={isLoading}
            >
              删除选中 ({selectedIds.length})
            </Button>
          )}
          <Button
            onClick={() => {
              setCurrentUser(null);
              setFormVisible(true);
            }}
            disabled={isLoading}
          >
            <Plus className="h-4 w-4 mr-2" /> 新建用户
          </Button>
        </div>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[50px]">
                <Checkbox
                  checked={isAllSelected}
                  onCheckedChange={handleSelectAll}
                  disabled={users.length === 0}
                />
              </TableHead>
              <TableHead>用户名</TableHead>
              <TableHead>昵称</TableHead>
              <TableHead>邮箱</TableHead>
              <TableHead>手机号</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="h-24 text-center">
                  {isLoading ? '加载中...' : '暂无数据'}
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>
                    <Checkbox
                      checked={selectedIds.includes(user.id)}
                      onCheckedChange={() => toggleSelectedId(user.id)}
                    />
                  </TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.nickname}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{user.phone || '-'}</TableCell>
                  <TableCell>
                    <Badge variant={user.status === 1 ? "success" : "destructive"}>
                      {user.status === 1 ? '启用' : '禁用'}
                    </Badge>
                  </TableCell>
                  <TableCell>{formatDate(user.createdAt)}</TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreHorizontal className="h-4 w-4" />
                          <span className="sr-only">操作</span>
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuLabel>操作</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem onClick={() => handleEdit(user)}>
                          编辑
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleAssignRoles(user)}>
                          分配角色
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleStatusToggle(user)}>
                          {user.status === 1 ? '禁用' : '启用'}
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          className="text-destructive focus:text-destructive"
                          onClick={() => handleDelete(user)}
                        >
                          删除
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {pagination.totalElements > 0 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            共 {pagination.totalElements} 条记录，每页
            <select
              className="mx-1 bg-background"
              value={pagination.size}
              onChange={(e) => handlePageSizeChange(Number(e.target.value))}
              disabled={isLoading}
            >
              <option value="10">10</option>
              <option value="20">20</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
            条
          </div>

          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => !pagination.first && handlePageChange(pagination.number)}
                  className={pagination.first ? "pointer-events-none opacity-50" : ""}
                />
              </PaginationItem>

              {renderPaginationItems()}

              <PaginationItem>
                <PaginationNext
                  onClick={() => !pagination.last && handlePageChange(pagination.number + 2)}
                  className={pagination.last ? "pointer-events-none opacity-50" : ""}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}
    </div>
  );
}