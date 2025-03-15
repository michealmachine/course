import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Edit, ChevronDown, ChevronUp, Trash } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { MultiSelect } from '@/components/ui/multi-select';
import { QuestionDetailForm, Question } from './QuestionDetailForm';

// 问题类型定义
export type QuestionType = 
  | "SINGLE_CHOICE" 
  | "MULTIPLE_CHOICE" 
  | "TRUE_FALSE" 
  | "FILL_BLANK" 
  | "SHORT_ANSWER";

// 临时定义 questionTypeNames 对象
const questionTypeNames: Record<QuestionType, string> = {
  "SINGLE_CHOICE": "单选题",
  "MULTIPLE_CHOICE": "多选题",
  "TRUE_FALSE": "判断题",
  "FILL_BLANK": "填空题",
  "SHORT_ANSWER": "简答题"
};

// 组件属性类型定义
interface QuestionListProps {
  questions: Question[]; // 问题列表
  onDeleteQuestion?: (id: string) => Promise<void>; // 删除问题的回调
  onUpdateQuestion?: (data: any) => Promise<void>; // 更新问题的回调
  tags?: Array<{ id: number; name: string }>; // 可用标签列表
  onRefresh?: () => void; // 刷新数据的回调
  institutionId?: string; // 机构ID
  courseId?: string; // 课程ID
  // 分页相关
  currentPage?: number;
  totalPages?: number;
  onPageChange?: (page: number) => void;
}

export function QuestionList({
  questions = [],
  onDeleteQuestion,
  onUpdateQuestion,
  tags = [],
  onRefresh,
  institutionId,
  courseId,
  currentPage = 1,
  totalPages = 1,
  onPageChange,
}: QuestionListProps) {
  const router = useRouter();
  // 展开状态记录
  const [expandedQuestionId, setExpandedQuestionId] = useState<string | null>(null);
  // 删除对话框状态
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [questionToDelete, setQuestionToDelete] = useState<string | null>(null);
  // 调试信息
  const [debugInfo, setDebugInfo] = useState<any>(null);
  // 标签过滤
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);
  // 记录更新中的问题ID
  const [updatingQuestionId, setUpdatingQuestionId] = useState<string | null>(null);

  // 转换标签为选项格式
  const tagOptions = tags.map(tag => ({
    label: tag.name,
    value: tag.id,
  }));

  // 切换问题展开状态
  const toggleExpanded = (questionId: string) => {
    if (expandedQuestionId === questionId) {
      setExpandedQuestionId(null);
    } else {
      setExpandedQuestionId(questionId);
    }
  };

  // 处理删除问题
  const handleDelete = async (id: string) => {
    try {
      if (onDeleteQuestion) {
        await onDeleteQuestion(id);
        toast.success("问题删除成功");
        // 如果删除的是当前展开的问题，重置展开状态
        if (expandedQuestionId === id) {
          setExpandedQuestionId(null);
        }
        // 刷新数据
        if (onRefresh) {
          onRefresh();
        }
      }
    } catch (error) {
      toast.error("删除问题失败");
      console.error("删除问题时出错:", error);
    } finally {
      setDeleteDialogOpen(false);
      setQuestionToDelete(null);
    }
  };

  // 确认删除前设置要删除的问题ID
  const confirmDelete = (id: string) => {
    setQuestionToDelete(id);
    setDeleteDialogOpen(true);
  };

  // 处理问题更新
  const handleUpdateQuestion = async (data: any) => {
    try {
      if (onUpdateQuestion) {
        setUpdatingQuestionId(data.id); // 设置更新中的问题ID
        
        // 确保包含机构ID
        const dataWithInstitutionId = {
          ...data,
          institutionId: institutionId || data.institutionId,
        };
        
        console.log("准备更新问题：", dataWithInstitutionId);
        await onUpdateQuestion(dataWithInstitutionId);
        console.log("问题更新成功");
        toast.success("问题更新成功");
        // 更新后折叠表单
        setExpandedQuestionId(null);
        // 刷新数据
        if (onRefresh) {
          onRefresh();
        }
      } else {
        console.error("未提供问题更新回调函数");
        toast.error("更新功能未实现");
      }
    } catch (error) {
      console.error("更新问题时出错:", error);
      toast.error("更新问题失败");
    } finally {
      setUpdatingQuestionId(null); // 重置更新中的问题ID
    }
  };
  
  // 处理标签选择变更，避免无限循环渲染
  const handleTagChange = (selected: (number | string)[]) => {
    // 确保所有值都是数字类型
    const numericValues = selected.map(val => typeof val === 'string' ? parseInt(val, 10) : val) as number[];
    setSelectedTagIds(numericValues);
  };

  // 根据选中的标签过滤问题
  const filteredQuestions = selectedTagIds.length > 0
    ? questions.filter(question => 
        question.tagIds && 
        question.tagIds.some((tagId: number) => selectedTagIds.includes(tagId))
      )
    : questions;

  // 获取问题对应的标签名称
  const getTagNames = (question: Question) => {
    if (!question.tagIds || !Array.isArray(question.tagIds)) return [] as string[];
    
    return question.tagIds
      .map((tagId: number) => tags.find(tag => tag.id === tagId)?.name)
      .filter((name): name is string => name !== undefined);
  };

  // 渲染分页控件
  const renderPagination = () => {
    if (!onPageChange || totalPages <= 1) return null;

    const pageItems = [];
    // 决定显示哪些页码
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    // 添加首页
    if (startPage > 1) {
      pageItems.push(
        <PaginationItem key="first">
          <PaginationLink onClick={() => onPageChange(1)}>
            1
          </PaginationLink>
        </PaginationItem>
      );
      
      // 添加省略号
      if (startPage > 2) {
        pageItems.push(
          <PaginationItem key="ellipsis-start">
            <PaginationEllipsis />
          </PaginationItem>
        );
      }
    }

    // 添加中间页码
    for (let i = startPage; i <= endPage; i++) {
      pageItems.push(
        <PaginationItem key={i}>
          <PaginationLink 
            isActive={currentPage === i}
            onClick={() => onPageChange(i)}
          >
            {i}
          </PaginationLink>
        </PaginationItem>
      );
    }

    // 添加末页
    if (endPage < totalPages) {
      // 添加省略号
      if (endPage < totalPages - 1) {
        pageItems.push(
          <PaginationItem key="ellipsis-end">
            <PaginationEllipsis />
          </PaginationItem>
        );
      }
      
      pageItems.push(
        <PaginationItem key="last">
          <PaginationLink onClick={() => onPageChange(totalPages)}>
            {totalPages}
          </PaginationLink>
        </PaginationItem>
      );
    }

    return (
      <Pagination className="mt-4">
        <PaginationContent>
          {currentPage > 1 ? (
            <PaginationItem>
              <PaginationPrevious onClick={() => onPageChange(currentPage - 1)} />
            </PaginationItem>
          ) : (
            <PaginationItem className="opacity-50 pointer-events-none">
              <span className="flex h-9 items-center justify-center whitespace-nowrap rounded-md border border-input px-3">
                上一页
              </span>
            </PaginationItem>
          )}
          
          {pageItems}
          
          {currentPage < totalPages ? (
            <PaginationItem>
              <PaginationNext onClick={() => onPageChange(currentPage + 1)} />
            </PaginationItem>
          ) : (
            <PaginationItem className="opacity-50 pointer-events-none">
              <span className="flex h-9 items-center justify-center whitespace-nowrap rounded-md border border-input px-3">
                下一页
              </span>
            </PaginationItem>
          )}
        </PaginationContent>
      </Pagination>
    );
  };

  // 主渲染部分
  return (
    <div className="space-y-4">
      {/* 标签过滤器 */}
      {tags.length > 0 && (
        <div className="mb-4">
          <label className="text-sm font-medium mb-1 block">按标签过滤:</label>
          <MultiSelect
            options={tagOptions}
            selected={selectedTagIds}
            onValueChange={handleTagChange}
            placeholder="选择标签进行过滤"
          />
        </div>
      )}
      
      {/* 问题列表 */}
      {filteredQuestions.length > 0 ? (
        <div className="space-y-4">
          {filteredQuestions.map((question) => (
            <div
              key={question.id}
              className="border rounded-lg p-4 shadow-sm bg-card hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center space-x-2 mb-2">
                    <Badge variant="outline">{questionTypeNames[question.type]}</Badge>
                    <Badge variant={
                      question.level === "EASY" ? "secondary" :
                      question.level === "MEDIUM" ? "default" : "destructive"
                    }>
                      {question.level === "EASY" ? "简单" :
                       question.level === "MEDIUM" ? "中等" : "困难"}
                    </Badge>
                    
                    {/* 显示问题标签 */}
                    {getTagNames(question).map((tagName, index) => (
                      <Badge key={`${question.id}-tag-${index}`} variant="outline">
                        {tagName}
                      </Badge>
                    ))}
                  </div>
                  
                  <h3 className="text-lg font-semibold mb-2 line-clamp-2">
                    {question.content}
                  </h3>
                </div>
                
                {/* 操作按钮 */}
                <div className="flex items-center space-x-2 ml-4">
                  {/* 编辑按钮 - 切换展开状态 */}
                  <Button
                    onClick={() => toggleExpanded(question.id)}
                    variant="outline"
                    size="icon"
                    className="hover:bg-secondary"
                  >
                    {expandedQuestionId === question.id ? (
                      <ChevronUp className="h-4 w-4" />
                    ) : (
                      <ChevronDown className="h-4 w-4" />
                    )}
                  </Button>
                  
                  {/* 编辑页面链接 */}
                  <Button
                    variant="outline"
                    size="icon"
                    asChild
                    className="hover:bg-secondary"
                  >
                    <Link href={`/questions/${question.id}/edit${courseId ? `?courseId=${courseId}` : ''}`}>
                      <Edit className="h-4 w-4" />
                    </Link>
                  </Button>
                  
                  {/* 删除按钮 */}
                  {onDeleteQuestion && (
                    <Button
                      onClick={() => confirmDelete(question.id)}
                      variant="outline"
                      size="icon"
                      className="hover:bg-destructive hover:text-destructive-foreground"
                    >
                      <Trash className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              </div>
              
              {/* 展开的编辑表单 */}
              {expandedQuestionId === question.id && (
                <div className="mt-4 border-t pt-4">
                  <QuestionDetailForm
                    question={question}
                    onSubmit={handleUpdateQuestion}
                    isLoading={updatingQuestionId === question.id}
                    tags={tags}
                    setDebugInfo={setDebugInfo}
                    institutionId={institutionId}
                    isExpanded={true}
                  />
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-10 text-muted-foreground">
          没有找到匹配的问题
        </div>
      )}
      
      {/* 分页控件 */}
      {renderPagination()}

      {/* 删除确认对话框 */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              您确定要删除这个问题吗？此操作无法撤销。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => questionToDelete && handleDelete(questionToDelete)}
            >
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
} 