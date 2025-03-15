'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ArrowLeft, Save } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';

import { questionGroupService } from '@/services';
import { QuestionGroupDTO } from '@/types/question';
import { useAuthStore } from '@/stores/auth-store';

// 创建问题组页面
export default function CreateQuestionGroupPage() {
  const router = useRouter();
  const { user } = useAuthStore();
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // 问题组表单数据
  const [formData, setFormData] = useState<QuestionGroupDTO>({
    name: '',
    description: '',
    institutionId: user?.institutionId || 0
  });
  
  // 返回列表
  const handleBack = () => {
    router.back();
  };
  
  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
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
      await questionGroupService.createGroup(formData);
      toast.success('创建成功');
      router.push('/dashboard/question-groups');
    } catch (error) {
      console.error('创建题组失败:', error);
      toast.error('创建失败');
    } finally {
      setIsSubmitting(false);
    }
  };
  
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
            <CardTitle>创建题组</CardTitle>
            <CardDescription>
              创建一个新的题目组，用于组织和管理相关题目
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">
                题组名称 <span className="text-red-500">*</span>
              </Label>
              <Input
                id="name"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="输入题组名称"
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
              />
            </div>
          </CardContent>
          <CardFooter>
            <Button type="submit" disabled={isSubmitting}>
              <Save className="h-4 w-4 mr-2" />
              {isSubmitting ? '创建中...' : '创建'}
            </Button>
          </CardFooter>
        </Card>
      </form>
    </div>
  );
} 