'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { ArrowLeft, Save, Edit, Eye } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { 
  Question, 
  QuestionDTO, 
  QuestionType, 
  QuestionDifficulty, 
  QuestionTag 
} from '@/types/question';
import questionService from '@/services/question';
import questionTagService from '@/services/question-tag';
import { QuestionDetailForm } from '@/components/question/QuestionDetailForm';

/**
 * 题目详情/编辑页面
 */
export default function QuestionDetailPage() {
  const router = useRouter();
  const { id } = useParams();
  const searchParams = useSearchParams();
  const isEditMode = searchParams.get('edit') === 'true';
  
  const [loading, setLoading] = useState(true);
  const [question, setQuestion] = useState<Question | null>(null);
  const [tags, setTags] = useState<QuestionTag[]>([]);
  const [editMode, setEditMode] = useState(isEditMode);
  const [submitting, setSubmitting] = useState(false);
  
  // 加载数据
  useEffect(() => {
    fetchQuestionDetail();
  }, [id]);
  
  // 当题目加载完成后加载标签
  useEffect(() => {
    if (question) {
      fetchTags();
    }
  }, [question]);
  
  // 获取题目详情
  const fetchQuestionDetail = async () => {
    try {
      setLoading(true);
      const data = await questionService.getQuestionById(Number(id));
      
      // 确保题目类型为单选或多选
      if (data.type !== QuestionType.SINGLE_CHOICE && data.type !== QuestionType.MULTIPLE_CHOICE) {
        data.type = QuestionType.SINGLE_CHOICE;
      }
      
      // 确保难度有值
      if (!data.difficulty) {
        data.difficulty = QuestionDifficulty.MEDIUM;
      }
      
      setQuestion(data);
    } catch (error) {
      console.error('获取题目详情失败:', error);
      toast.error('获取题目详情失败');
    } finally {
      setLoading(false);
    }
  };
  
  // 获取所有标签
  const fetchTags = async () => {
    if (!question) return;
    
    try {
      const institutionId = question.institutionId || 1; // 默认值，实际应用中应从认证信息获取
      const data = await questionTagService.getAllQuestionTags(institutionId);
      setTags(data);
    } catch (error) {
      console.error('获取标签列表失败:', error);
      toast.error('获取标签列表失败');
    }
  };
  
  // 切换编辑模式
  const toggleEditMode = () => {
    setEditMode(!editMode);
    // 更新URL参数
    const url = new URL(window.location.href);
    if (!editMode) {
      url.searchParams.set('edit', 'true');
    } else {
      url.searchParams.delete('edit');
    }
    window.history.pushState({}, '', url);
  };
  
  // 返回列表页
  const handleBackToList = () => {
    router.push('/dashboard/questions');
  };
  
  // 保存更新
  const handleSubmit = async (formData: QuestionDTO) => {
    if (!question) return;
    
    try {
      setSubmitting(true);
      await questionService.updateQuestion(question.id, formData);
      toast.success('题目更新成功');
      
      // 更新本地数据
      await fetchQuestionDetail();
      
      // 切换回查看模式
      setEditMode(false);
      const url = new URL(window.location.href);
      url.searchParams.delete('edit');
      window.history.pushState({}, '', url);
    } catch (error) {
      console.error('更新题目失败:', error);
      toast.error('更新题目失败');
    } finally {
      setSubmitting(false);
    }
  };
  
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <Button variant="outline" size="icon" onClick={handleBackToList}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h2 className="text-2xl font-bold tracking-tight">
              {loading ? <Skeleton className="h-8 w-48" /> : question?.title || '题目详情'}
            </h2>
            <p className="text-muted-foreground">
              {editMode ? '编辑题目' : '查看题目详情'}
            </p>
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          {!editMode ? (
            <Button onClick={toggleEditMode}>
              <Edit className="h-4 w-4 mr-2" />
              编辑题目
            </Button>
          ) : (
            <Button variant="outline" onClick={toggleEditMode}>
              <Eye className="h-4 w-4 mr-2" />
              查看模式
            </Button>
          )}
        </div>
      </div>
      
      <Separator />
      
      <Card>
        <CardContent className="p-6">
          {loading ? (
            <div className="space-y-4">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-24 w-full" />
              <Skeleton className="h-8 w-1/2" />
              <div className="space-y-2">
                <Skeleton className="h-6 w-full" />
                <Skeleton className="h-6 w-full" />
                <Skeleton className="h-6 w-full" />
              </div>
            </div>
          ) : question ? (
            <QuestionDetailForm 
              question={question}
              tags={tags}
              readOnly={!editMode}
              onSubmit={handleSubmit}
              isSubmitting={submitting}
            />
          ) : (
            <div className="text-center py-8">
              <p>未找到题目信息</p>
              <Button 
                variant="outline" 
                onClick={handleBackToList} 
                className="mt-4"
              >
                返回列表
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
} 