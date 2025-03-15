'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus, Search, Edit, Trash } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';

import { questionGroupService } from '@/services';
import { QuestionGroup } from '@/types/question';
import { useAuthStore } from '@/stores/auth-store';
import useDebounce from '@/hooks/use-debounce';

export default function QuestionGroupsPage() {
  const router = useRouter();
  const { user } = useAuthStore();
  const [isLoading, setIsLoading] = useState(true);
  const [groups, setGroups] = useState<QuestionGroup[]>([]);
  const [keyword, setKeyword] = useState('');
  const debouncedKeyword = useDebounce(keyword, 500);

  // 加载题组列表
  useEffect(() => {
    if (user?.institutionId) {
      fetchGroups();
    }
  }, [user?.institutionId, debouncedKeyword]);

  // 获取题组列表
  const fetchGroups = async () => {
    try {
      const data = await questionGroupService.getGroups({
        institutionId: user?.institutionId || 0,
        keyword: debouncedKeyword,
        page: 1,
        pageSize: 100
      });
      setGroups(data.content);
    } catch (error) {
      console.error('获取题组列表失败:', error);
      toast.error('获取题组列表失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 删除题组
  const handleDelete = async (id: number) => {
    if (confirm('确定要删除此题组吗？')) {
      try {
        await questionGroupService.deleteGroup(id);
        toast.success('删除成功');
        fetchGroups();
      } catch (error) {
        console.error('删除题组失败:', error);
        toast.error('删除失败');
      }
    }
  };

  // 创建题组
  const handleCreate = () => {
    router.push('/dashboard/question-groups/create');
  };

  // 编辑题组
  const handleEdit = (id: number) => {
    router.push(`/dashboard/question-groups/${id}?edit=true`);
  };

  // 查看题组详情
  const handleView = (id: number) => {
    router.push(`/dashboard/question-groups/${id}`);
  };

  // 格式化日期
  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString();
  };

  // 加载骨架屏
  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <Skeleton className="h-10 w-64" />
          <Skeleton className="h-10 w-32" />
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {[...Array(6)].map((_, i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-3/4" />
                <Skeleton className="h-4 w-1/2" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-4 w-full mb-2" />
                <Skeleton className="h-4 w-2/3" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <Input
            placeholder="搜索题组..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            className="w-64"
          />
          <Search className="h-4 w-4 text-muted-foreground" />
        </div>

        <Button onClick={handleCreate}>
          <Plus className="h-4 w-4 mr-2" />
          创建题组
        </Button>
      </div>

      {groups.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center h-64">
            <p className="text-muted-foreground mb-4">暂无题组</p>
            <Button onClick={handleCreate}>
              <Plus className="h-4 w-4 mr-2" />
              创建题组
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {groups.map((group) => (
            <Card
              key={group.id}
              className="cursor-pointer hover:shadow-md transition-shadow"
              onClick={() => handleView(group.id)}
            >
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span className="truncate">{group.name}</span>
                  <div className="flex items-center space-x-2" onClick={(e) => e.stopPropagation()}>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleEdit(group.id)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleDelete(group.id)}
                    >
                      <Trash className="h-4 w-4" />
                    </Button>
                  </div>
                </CardTitle>
                <CardDescription className="truncate">
                  {group.description || '暂无描述'}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center space-x-2">
                  <Badge variant="secondary">
                    {group.questionCount || 0} 个题目
                  </Badge>
                  <Badge variant="outline">
                    {formatDate(group.createdAt)}
                  </Badge>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
} 