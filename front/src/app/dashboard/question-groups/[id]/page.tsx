'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { toast } from 'sonner';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ArrowLeft, Save } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Skeleton } from '@/components/ui/skeleton';

import { questionGroupService } from '@/services';
import { QuestionGroup, QuestionGroupDTO } from '@/types/question';
import { useAuthStore } from '@/stores/auth-store';

interface PageProps {
  params: {
    id: string;
  };
}

export default function QuestionGroupPage({ params }: PageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const isEdit = searchParams.get('edit') === 'true';
  const { user } = useAuthStore();
  
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [group, setGroup] = useState<QuestionGroup | null>(null);
  const [formData, setFormData] = useState<QuestionGroupDTO>({
    name: '',
    description: '',
    institutionId: user?.institutionId || 0
  });

  // 加载题组数据
  useEffect(() => {
    fetchGroup();
  }, [params.id]);

  // 获取题组详情
  const fetchGroup = async () => {
    try {
      const data = await questionGroupService.getGroupById(parseInt(params.id));
      setGroup(data);
      setFormData({
        name: data.name,
        description: data.description || '',
        institutionId: data.institutionId
      });
    } catch (error) {
      console.error('获取题组详情失败:', error);
      toast.error('获取题组详情失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // 返回列表页
  const handleBack = () => {
    router.back();
  };

  // 提交表单
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      toast.error('请输入题组名称');
      return;
    }

    if (!formData.institutionId) {
      toast.error('机构ID无效');
      return;
    }

    setIsSubmitting(true);
    try {
      await questionGroupService.updateGroup(parseInt(params.id), formData);
      toast.success('更新成功');
      router.push('/dashboard/question-groups');
    } catch (error) {
      console.error('更新题组失败:', error);
      toast.error('更新失败');
    } finally {
      setIsSubmitting(false);
    }
  };

  // 加载骨架屏
  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <Skeleton className="h-10 w-20" />
        </div>

        <Card>
          <CardHeader>
            <Skeleton className="h-8 w-1/4" />
            <Skeleton className="h-4 w-1/2" />
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Skeleton className="h-4 w-20" />
              <Skeleton className="h-10 w-full" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-20" />
              <Skeleton className="h-32 w-full" />
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button variant="ghost" onClick={handleBack}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          返回
        </Button>
      </div>

      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader>
            <CardTitle>{isEdit ? '编辑题组' : '题组详情'}</CardTitle>
            <CardDescription>
              {isEdit ? '修改题组信息' : '查看题组详细信息'}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">
                题组名称 {isEdit && <span className="text-red-500">*</span>}
              </Label>
              <Input
                id="name"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="输入题组名称"
                readOnly={!isEdit}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">描述</Label>
              <Textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                placeholder="输入题组描述（可选）"
                rows={4}
                readOnly={!isEdit}
              />
            </div>
          </CardContent>
          {isEdit && (
            <CardFooter>
              <Button type="submit" disabled={isSubmitting}>
                <Save className="h-4 w-4 mr-2" />
                {isSubmitting ? '保存中...' : '保存'}
              </Button>
            </CardFooter>
          )}
        </Card>
      </form>
    </div>
  );
} 