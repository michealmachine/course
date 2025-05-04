'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Copy, Users, BookOpen, User } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { InstitutionVO } from '@/types/institution';
import { UserVO } from '@/types/user';
import { CourseVO } from '@/types/course';
import { ApiResponse, PaginationResult } from '@/types/api';
import { UserListDialog } from './user-list-dialog';
import { CourseListDialog } from './course-list-dialog';

interface InstitutionExpandedRowProps {
  institution: InstitutionVO;
}

export function InstitutionExpandedRow({ institution }: InstitutionExpandedRowProps) {
  const [users, setUsers] = useState<UserVO[]>([]);
  const [courses, setCourses] = useState<CourseVO[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(false);
  const [isLoadingCourses, setIsLoadingCourses] = useState(false);
  const [isUserDialogOpen, setIsUserDialogOpen] = useState(false);
  const [isCourseDialogOpen, setIsCourseDialogOpen] = useState(false);

  useEffect(() => {
    fetchTopUsers();
    fetchTopCourses();
  }, [institution.id]);

  const fetchTopUsers = async () => {
    if (!institution.id) return;

    setIsLoadingUsers(true);
    try {
      const response = await request.get<PaginationResult<UserVO>>(
        `/admin/institutions/${institution.id}/users`,
        {
          params: {
            page: 0,
            size: 5
          }
        }
      );

      if (response.data.code === 200 && response.data.data) {
        setUsers(response.data.data.content);
      }
    } catch (error) {
      console.error('获取机构用户出错:', error);
    } finally {
      setIsLoadingUsers(false);
    }
  };

  const fetchTopCourses = async () => {
    if (!institution.id) return;

    setIsLoadingCourses(true);
    try {
      const response = await request.get<PaginationResult<CourseVO>>(
        `/admin/courses/institutions/${institution.id}`,
        {
          params: {
            page: 0,
            size: 5
          }
        }
      );

      if (response.data.code === 200 && response.data.data) {
        setCourses(response.data.data.content);
      }
    } catch (error) {
      console.error('获取机构课程出错:', error);
    } finally {
      setIsLoadingCourses(false);
    }
  };

  const copyRegisterCode = () => {
    if (institution.registerCode) {
      navigator.clipboard.writeText(institution.registerCode);
      toast.success('注册码已复制到剪贴板');
    }
  };

  // 格式化日期
  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  // 格式化金额
  const formatMoney = (cents: number) => {
    if (!cents) return '¥0';
    return `¥${(cents / 100).toFixed(2)}`;
  };

  return (
    <div className="p-4 bg-muted/30">
      {/* 统计卡片 */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-muted-foreground">用户数</p>
                <p className="text-lg font-bold">{institution.userCount || 0}</p>
                <p className="text-xs text-muted-foreground">学习人数: {institution.totalLearners !== undefined ? institution.totalLearners : '加载中...'}</p>
              </div>
              <Users className="h-4 w-4 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-muted-foreground">课程数</p>
                <p className="text-lg font-bold">{institution.courseCount || 0}</p>
                <p className="text-xs text-muted-foreground">已发布: {institution.publishedCourseCount || 0}</p>
              </div>
              <BookOpen className="h-4 w-4 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-muted-foreground">创建时间</p>
                <p className="text-sm font-medium">{formatDate(institution.createdAt)}</p>
              </div>
              <span className="text-xs px-2 py-1 rounded-full bg-gray-100">
                {institution.status === 1 ? '正常' : '禁用'}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* 基本信息区 */}
        <div className="space-y-2">
          <h3 className="text-sm font-medium">基本信息</h3>
          <div className="text-sm">
            <div className="grid grid-cols-3 gap-2 mb-1">
              <span className="text-muted-foreground">机构描述:</span>
              <span className="col-span-2">{institution.description || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-1">
              <span className="text-muted-foreground">联系人:</span>
              <span className="col-span-2">{institution.contactPerson || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-1">
              <span className="text-muted-foreground">联系电话:</span>
              <span className="col-span-2">{institution.contactPhone || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-1">
              <span className="text-muted-foreground">联系邮箱:</span>
              <span className="col-span-2">{institution.contactEmail || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-1">
              <span className="text-muted-foreground">地址:</span>
              <span className="col-span-2">{institution.address || '-'}</span>
            </div>
            <div className="grid grid-cols-3 gap-2 mb-1">
              <span className="text-muted-foreground">注册码:</span>
              <div className="col-span-2 flex items-center">
                <code className="bg-muted px-1 py-0.5 rounded text-xs">
                  {institution.registerCode || '-'}
                </code>
                {institution.registerCode && (
                  <Button variant="ghost" size="icon" className="h-6 w-6 ml-1" onClick={copyRegisterCode}>
                    <Copy className="h-3 w-3" />
                  </Button>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* 用户概览区 */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-medium">用户概览</h3>
            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs"
              onClick={() => setIsUserDialogOpen(true)}
            >
              <Users className="h-3 w-3 mr-1" />
              查看全部
            </Button>
          </div>

          <div className="space-y-2">
            {isLoadingUsers ? (
              <div className="text-sm text-muted-foreground">加载中...</div>
            ) : users.length === 0 ? (
              <div className="text-sm text-muted-foreground">暂无用户</div>
            ) : (
              users.map((user) => (
                <div key={user.id} className="flex items-center space-x-2">
                  <Avatar className="h-6 w-6">
                    <AvatarImage src={user.avatar} alt={user.username} />
                    <AvatarFallback>
                      <User className="h-3 w-3" />
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{user.username}</p>
                    <p className="text-xs text-muted-foreground truncate">{user.email}</p>
                  </div>
                  <Badge variant="outline" className="text-xs">
                    {user.role === 'ADMIN' ? '管理员' : '学员'}
                  </Badge>
                </div>
              ))
            )}
          </div>
        </div>

        {/* 课程概览区 */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-medium">课程概览</h3>
            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs"
              onClick={() => setIsCourseDialogOpen(true)}
            >
              <BookOpen className="h-3 w-3 mr-1" />
              查看全部
            </Button>
          </div>

          <div className="space-y-2">
            {isLoadingCourses ? (
              <div className="text-sm text-muted-foreground">加载中...</div>
            ) : courses.length === 0 ? (
              <div className="text-sm text-muted-foreground">暂无课程</div>
            ) : (
              courses.map((course) => (
                <div key={course.id} className="flex items-center space-x-2">
                  <div className="h-8 w-12 bg-muted rounded overflow-hidden flex-shrink-0">
                    {course.coverUrl ? (
                      <img
                        src={course.coverUrl}
                        alt={course.title}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <div className="h-full w-full flex items-center justify-center">
                        <BookOpen className="h-3 w-3 text-muted-foreground" />
                      </div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{course.title}</p>
                    <div className="flex items-center space-x-2">
                      <Badge variant="outline" className={`text-xs ${
                        course.status === 4 ? 'bg-green-50 text-green-700 border-green-200' : 'bg-gray-50 text-gray-700 border-gray-200'
                      }`}>
                        {course.status === 4 ? '已发布' : '未发布'}
                      </Badge>
                      <p className="text-xs text-muted-foreground">
                        学习: {course.studentCount || 0}
                      </p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* 用户列表弹窗 */}
      <UserListDialog
        institutionId={institution.id}
        institutionName={institution.name}
        open={isUserDialogOpen}
        onOpenChange={setIsUserDialogOpen}
      />

      {/* 课程列表弹窗 */}
      <CourseListDialog
        institutionId={institution.id}
        institutionName={institution.name}
        open={isCourseDialogOpen}
        onOpenChange={setIsCourseDialogOpen}
      />
    </div>
  );
}
