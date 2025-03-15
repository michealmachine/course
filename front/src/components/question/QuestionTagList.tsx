'use client';

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Card, CardContent } from '@/components/ui/card';
import { 
  MoreHorizontal, 
  Search,
  Plus,
  Edit,
  Trash2,
  Tag
} from 'lucide-react';

import { questionTagService } from '@/services';
import { QuestionTag, QuestionTagDTO } from '@/types/question';
import useQuestionStore from '@/stores/question-store';

interface QuestionTagListProps {
  institutionId: number;
}

export function QuestionTagList({ institutionId }: QuestionTagListProps) {
  const [isLoading, setIsLoading] = useState(true);
  const [tags, setTags] = useState<QuestionTag[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [searchName, setSearchName] = useState('');
  
  // 弹窗状态
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
  const [currentTag, setCurrentTag] = useState<QuestionTag | null>(null);
  const [formData, setFormData] = useState<QuestionTagDTO>({
    name: '',
    description: '',
    institutionId: institutionId
  });
  
  // 初始加载
  useEffect(() => {
    if (institutionId) {
      fetchTags();
    }
  }, [institutionId, page, pageSize]);
  
  // 获取问题标签列表
  const fetchTags = async () => {
    setIsLoading(true);
    try {
      const response = await questionTagService.getQuestionTagList({
        page,
        pageSize,
        name: searchName || undefined,
        institutionId
      });
      
      setTags(response.content);
      setTotalCount(response.totalElements);
    } catch (error) {
      console.error('获取问题标签列表失败:', error);
      toast.error('获取问题标签列表失败');
    } finally {
      setIsLoading(false);
    }
  };
  
  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    fetchTags();
  };
  
  // 打开创建对话框
  const handleOpenCreateDialog = () => {
    setCurrentTag(null);
    setFormData({
      name: '',
      description: '',
      institutionId
    });
    setIsDialogOpen(true);
  };
  
  // 打开编辑对话框
  const handleOpenEditDialog = (tag: QuestionTag) => {
    setCurrentTag(tag);
    setFormData({
      name: tag.name,
      description: tag.description || '',
      institutionId
    });
    setIsDialogOpen(true);
  };
  
  // 表单输入变更
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // 保存问题标签
  const handleSaveTag = async () => {
    try {
      if (!formData.name.trim()) {
        toast.error('标签名不能为空');
        return;
      }
      
      if (currentTag) {
        // 更新已有问题标签
        await questionTagService.updateQuestionTag(currentTag.id, formData);
        toast.success('更新成功');
      } else {
        // 创建新问题标签
        await questionTagService.createQuestionTag(formData);
        toast.success('创建成功');
      }
      
      setIsDialogOpen(false);
      fetchTags();
    } catch (error) {
      console.error('保存问题标签失败:', error);
      toast.error('保存失败，请重试');
    }
  };
  
  // 打开删除确认
  const handleOpenDeleteConfirm = (tag: QuestionTag) => {
    setCurrentTag(tag);
    setIsDeleteConfirmOpen(true);
  };
  
  // 删除问题标签
  const handleDeleteTag = async () => {
    if (!currentTag) return;
    
    try {
      await questionTagService.deleteQuestionTag(currentTag.id);
      toast.success('删除成功');
      setIsDeleteConfirmOpen(false);
      fetchTags();
    } catch (error) {
      console.error('删除问题标签失败:', error);
      toast.error('删除失败，请重试');
    }
  };
  
  // 格式化日期
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };
  
  // 加载骨架屏
  if (isLoading && tags.length === 0) {
    return (
      <div className="space-y-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex justify-between items-center">
              <Skeleton className="h-10 w-1/3" />
              <Skeleton className="h-10 w-20" />
            </div>
          </CardContent>
        </Card>
        
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>
                  <Skeleton className="h-5 w-32" />
                </TableHead>
                <TableHead>
                  <Skeleton className="h-5 w-40" />
                </TableHead>
                <TableHead>
                  <Skeleton className="h-5 w-20" />
                </TableHead>
                <TableHead className="text-right">
                  <Skeleton className="h-5 w-20 ml-auto" />
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[...Array(5)].map((_, i) => (
                <TableRow key={i}>
                  <TableCell>
                    <Skeleton className="h-5 w-32" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-5 w-full max-w-md" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-5 w-32" />
                  </TableCell>
                  <TableCell className="text-right">
                    <Skeleton className="h-8 w-8 ml-auto" />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </div>
    );
  }
  
  return (
    <div className="space-y-4">
      {/* 搜索栏 */}
      <Card>
        <CardContent className="p-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center space-x-2 w-full max-w-sm">
              <Input
                placeholder="搜索标签名称..."
                value={searchName}
                onChange={(e) => setSearchName(e.target.value)}
                className="flex-1"
              />
              <Button onClick={handleSearch} variant="outline" size="icon">
                <Search className="h-4 w-4" />
              </Button>
            </div>
            
            <Button onClick={handleOpenCreateDialog} size="sm">
              <Plus className="h-4 w-4 mr-2" />
              新建标签
            </Button>
          </div>
        </CardContent>
      </Card>
      
      {/* 问题标签列表 */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>标签名称</TableHead>
              <TableHead>描述</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {tags.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                  暂无数据
                </TableCell>
              </TableRow>
            ) : (
              tags.map((tag) => (
                <TableRow key={tag.id}>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Tag className="h-4 w-4 text-muted-foreground" />
                      <span className="font-medium">{tag.name}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="truncate max-w-md" title={tag.description || ''}>
                      {tag.description || <span className="text-muted-foreground text-xs">无描述</span>}
                    </div>
                  </TableCell>
                  <TableCell>{formatDate(tag.createdAt)}</TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => handleOpenEditDialog(tag)}>
                          <Edit className="h-4 w-4 mr-2" />
                          编辑
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleOpenDeleteConfirm(tag)}>
                          <Trash2 className="h-4 w-4 mr-2" />
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
      
      {/* 创建/编辑对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {currentTag ? '编辑标签' : '创建新标签'}
            </DialogTitle>
            <DialogDescription>
              {currentTag ? '修改标签信息' : '创建一个新的题目标签'}
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <label htmlFor="name" className="text-sm font-medium">
                标签名称 <span className="text-red-500">*</span>
              </label>
              <Input
                id="name"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="输入标签名称"
              />
            </div>
            
            <div className="space-y-2">
              <label htmlFor="description" className="text-sm font-medium">
                描述
              </label>
              <Input
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                placeholder="输入标签描述（可选）"
              />
            </div>
          </div>
          
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
              取消
            </Button>
            <Button onClick={handleSaveTag}>保存</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      {/* 删除确认对话框 */}
      <Dialog open={isDeleteConfirmOpen} onOpenChange={setIsDeleteConfirmOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              您确定要删除标签 "{currentTag?.name}" 吗？删除后不可恢复。
              删除标签不会影响已标记的题目，但这些题目将不再与此标签关联。
            </DialogDescription>
          </DialogHeader>
          
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteConfirmOpen(false)}>
              取消
            </Button>
            <Button variant="destructive" onClick={handleDeleteTag}>
              删除
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 