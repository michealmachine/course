'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Loader2, Search, User, Users } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { UserVO } from '@/types/user';
import { ApiResponse, PaginationResult } from '@/types/api';

interface UserListDialogProps {
  institutionId: number;
  institutionName: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function UserListDialog({
  institutionId,
  institutionName,
  open,
  onOpenChange
}: UserListDialogProps) {
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [users, setUsers] = useState<UserVO[]>([]);
  const [totalUsers, setTotalUsers] = useState(0);

  useEffect(() => {
    if (open && institutionId) {
      fetchUsers();
    }
  }, [open, institutionId, currentPage, pageSize]);

  const fetchUsers = async () => {
    if (!institutionId) return;

    setIsLoading(true);
    try {
      const params: any = {
        page: currentPage,
        size: pageSize
      };

      if (searchQuery) {
        params.keyword = searchQuery;
      }

      const response = await request.get<PaginationResult<UserVO>>(
        `/admin/institutions/${institutionId}/users`,
        { params }
      );

      if (response.data.code === 200 && response.data.data) {
        setUsers(response.data.data.content);
        setTotalPages(response.data.data.totalPages);
        setTotalUsers(response.data.data.totalElements);
      } else {
        toast.error('获取用户列表失败');
      }
    } catch (error) {
      console.error('获取用户列表出错:', error);
      toast.error('获取用户列表出错');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchUsers();
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Users className="h-5 w-5" />
            {institutionName} 的用户
          </DialogTitle>
          <DialogDescription>
            查看机构的所有用户信息
          </DialogDescription>
        </DialogHeader>

        <div className="flex items-center justify-between py-4">
          <form onSubmit={handleSearch} className="flex gap-2">
            <Input
              placeholder="搜索用户名或邮箱..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="max-w-sm"
            />
            <Button type="submit" variant="outline">
              <Search className="h-4 w-4 mr-2" />
              搜索
            </Button>
          </form>
          <div className="text-sm text-muted-foreground">
            共 <span className="font-medium">{totalUsers}</span> 个用户
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2">加载中...</span>
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>用户</TableHead>
                    <TableHead>邮箱</TableHead>
                    <TableHead>角色</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead>注册时间</TableHead>
                    <TableHead>最后登录</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {users.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="h-24 text-center">
                        没有找到用户
                      </TableCell>
                    </TableRow>
                  ) : (
                    users.map((user) => (
                      <TableRow key={user.id}>
                        <TableCell>
                          <div className="flex items-center space-x-2">
                            <Avatar className="h-8 w-8">
                              <AvatarImage src={user.avatar} alt={user.username} />
                              <AvatarFallback>
                                <User className="h-4 w-4" />
                              </AvatarFallback>
                            </Avatar>
                            <span className="font-medium">{user.username}</span>
                          </div>
                        </TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>
                          <Badge variant="outline" className={
                            user.role === 'ADMIN' ? 'bg-blue-50 text-blue-700 border-blue-200' :
                            user.role === 'INSTITUTION_ADMIN' ? 'bg-purple-50 text-purple-700 border-purple-200' :
                            'bg-gray-50 text-gray-700 border-gray-200'
                          }>
                            {user.role === 'ADMIN' ? '管理员' :
                             user.role === 'INSTITUTION_ADMIN' ? '机构管理员' : '学员'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline" className={
                            user.status === 1 ? 'bg-green-50 text-green-700 border-green-200' :
                            'bg-red-50 text-red-700 border-red-200'
                          }>
                            {user.status === 1 ? '正常' : '禁用'}
                          </Badge>
                        </TableCell>
                        <TableCell>{formatDate(user.createdAt)}</TableCell>
                        <TableCell>{user.lastLoginTime ? formatDate(user.lastLoginTime) : '从未登录'}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-end space-x-2 py-4">
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                    className={currentPage === 0 ? 'pointer-events-none opacity-50' : ''}
                  />
                </PaginationItem>
                {Array.from({ length: Math.min(5, totalPages) }).map((_, index) => {
                  // 显示当前页附近的页码
                  let pageToShow = currentPage - 2 + index;
                  if (currentPage < 2) {
                    pageToShow = index;
                  } else if (currentPage > totalPages - 3) {
                    pageToShow = totalPages - 5 + index;
                  }

                  // 确保页码在有效范围内
                  if (pageToShow >= 0 && pageToShow < totalPages) {
                    return (
                      <PaginationItem key={pageToShow}>
                        <PaginationLink
                          onClick={() => setCurrentPage(pageToShow)}
                          isActive={currentPage === pageToShow}
                        >
                          {pageToShow + 1}
                        </PaginationLink>
                      </PaginationItem>
                    );
                  }
                  return null;
                })}
                <PaginationItem>
                  <PaginationNext
                    onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                    className={currentPage === totalPages - 1 ? 'pointer-events-none opacity-50' : ''}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            关闭
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
