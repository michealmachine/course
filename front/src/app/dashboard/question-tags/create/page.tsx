'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { ArrowLeft, Save } from 'lucide-react';
import { Separator } from '@/components/ui/separator';

import { questionTagService } from '@/services';
import { QuestionTagDTO } from '@/types/question';

// 创建问题标签页面
export default function CreateQuestionTagPage() {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 问题标签表单数据
  const [formData, setFormData] = useState<QuestionTagDTO>({
    name: '',
    institutionId: 1 // 这里应该根据实际情况获取机构ID
  });

  // 返回列表
  const handleBack = () => {
    router.push('/dashboard/questions?tab=tags');
  };

  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // 创建问题标签
  const handleSubmit = async () => {
    // 表单验证
    if (!formData.name.trim()) {
      toast.error('请输入标签名称');
      return;
    }

    setIsSubmitting(true);
    try {
      await questionTagService.createQuestionTag(formData);
      toast.success('创建标签成功');
      router.push('/dashboard/questions?tab=tags');
    } catch (error) {
      console.error('创建标签失败:', error);
      toast.error('创建标签失败');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button
          variant="outline"
          size="sm"
          onClick={handleBack}
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          返回列表
        </Button>

        <Button
          size="sm"
          onClick={handleSubmit}
          disabled={isSubmitting}
        >
          <Save className="h-4 w-4 mr-2" />
          保存
        </Button>
      </div>

      <Separator />

      <Card>
        <CardHeader>
          <CardTitle>创建标签</CardTitle>
          <CardDescription>
            创建一个新的问题标签，用于给题目分类和检索
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">
                标签名称 <span className="text-red-500">*</span>
              </Label>
              <Input
                id="name"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="输入标签名称"
              />
            </div>

            {/* 后端DTO中没有description字段，暂时移除 */}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}