'use client';

import { useState, useEffect } from 'react';
import { Tag, TagDTO, Course } from '@/types/course';
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Badge } from "@/components/ui/badge";
import { Textarea } from "@/components/ui/textarea";
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Loader2, Plus, Search, Trash2, Edit, ChevronDown, ChevronUp } from 'lucide-react';
import { toast } from 'sonner';
import tagService from '@/services/tag';
import courseService from '@/services/course-service';
import {
  Pagination,
  PaginationContent,
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
} from "@/components/ui/alert-dialog";
import { Card, CardContent } from "@/components/ui/card";
import { CourseListView } from './course-list-view';
import { useRouter } from 'next/navigation';

// 表单验证模式
const tagFormSchema = z.object({
  name: z.string().min(2, '标签名称至少需要2个字符').max(50, '标签名称不能超过50个字符'),
  description: z.string().max(255, '标签描述不能超过255个字符').optional(),
});

export function TagManagement() {
  const router = useRouter();
  const [tags, setTags] = useState<Tag[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingTag, setEditingTag] = useState<Tag | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [tagToDelete, setTagToDelete] = useState<Tag | null>(null);
  const [expandedTag, setExpandedTag] = useState<number | null>(null);
  const [tagCourses, setTagCourses] = useState<Course[]>([]);
  const [loadingCourses, setLoadingCourses] = useState(false);

  // 表单初始化
  const form = useForm<z.infer<typeof tagFormSchema>>({
    resolver: zodResolver(tagFormSchema),
    defaultValues: {
      name: '',
      description: '',
    },
  });

  // 加载标签列表
  const loadTags = async (page = 0) => {
    setIsLoading(true);
    try {
      const result = await tagService.getTagList(searchKeyword, page);
      setTags(result.content || []);
      setTotalPages(result.totalPages || 0);
      setTotalItems(result.totalElements || 0);
      setCurrentPage(result.pageable?.pageNumber || 0);
    } catch (error) {
      toast.error('加载标签列表失败');
      console.error('加载标签列表失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 加载标签课程
  const loadTagCourses = async (tagId: number) => {
    setLoadingCourses(true);
    try {
      // 使用现有的课程搜索API
      const result = await courseService.searchCourses({
        tagIds: [tagId],
        page: 0,
        pageSize: 6
      });
      setTagCourses(result.content || []);
    } catch (error) {
      console.error('加载标签课程失败:', error);
      toast.error('加载标签课程失败');
      setTagCourses([]);
    } finally {
      setLoadingCourses(false);
    }
  };

  // 切换标签展开/折叠
  const toggleTag = async (tagId: number, e?: React.MouseEvent) => {
    // 如果是从操作按钮点击，阻止事件冒泡
    if (e) {
      e.stopPropagation();
    }

    if (expandedTag === tagId) {
      setExpandedTag(null);
      setTagCourses([]);
    } else {
      setExpandedTag(tagId);
      await loadTagCourses(tagId);
    }
  };

  // 处理课程点击
  const handleCourseClick = (courseId: number) => {
    router.push(`/dashboard/courses/${courseId}`);
  };

  // 初始加载
  useEffect(() => {
    loadTags();
  }, []);

  // 搜索标签
  const handleSearch = () => {
    loadTags(0); // 重置到第一页
  };

  // 切换页面
  const handlePageChange = (page: number) => {
    loadTags(page);
  };

  // 打开创建标签对话框
  const handleOpenCreateDialog = () => {
    form.reset({
      name: '',
      description: '',
    });
    setEditingTag(null);
    setIsDialogOpen(true);
  };

  // 打开编辑标签对话框
  const handleOpenEditDialog = (tag: Tag) => {
    form.reset({
      name: tag.name,
      description: tag.description || '',
    });
    setEditingTag(tag);
    setIsDialogOpen(true);
  };

  // 确认删除标签
  const handleDeleteConfirm = async () => {
    if (!tagToDelete) return;

    setIsLoading(true);
    try {
      await tagService.deleteTag(tagToDelete.id);
      toast.success('标签删除成功');
      loadTags(currentPage);
    } catch (error) {
      toast.error('删除标签失败');
      console.error('删除标签失败:', error);
    } finally {
      setIsLoading(false);
      setIsDeleteDialogOpen(false);
      setTagToDelete(null);
    }
  };

  // 保存标签
  const onSubmit = async (data: z.infer<typeof tagFormSchema>) => {
    setIsLoading(true);

    // 转换表单数据为DTO
    const tagDTO: TagDTO = {
      name: data.name,
      description: data.description,
    };

    try {
      // 检查名称是否可用
      const excludeId = editingTag?.id;
      const isAvailable = await tagService.isNameAvailable(data.name, excludeId);

      if (!isAvailable) {
        form.setError('name', {
          type: 'manual',
          message: '此标签名称已存在，请使用其他名称'
        });
        setIsLoading(false);
        return;
      }

      // 先关闭对话框，避免状态更新冲突
      setIsDialogOpen(false);

      if (editingTag) {
        // 更新标签
        await tagService.updateTag(editingTag.id, tagDTO);
        toast.success('标签更新成功');
      } else {
        // 创建标签
        await tagService.createTag(tagDTO);
        toast.success('标签创建成功');
      }

      // 使用setTimeout延迟加载，避免组件状态更新冲突
      setTimeout(() => {
        if (document.getElementById('tag-management-container')) {
          // 确保组件仍然挂载
          loadTags(currentPage);
        }
      }, 100);
    } catch (error) {
      toast.error(editingTag ? '更新标签失败' : '创建标签失败');
      console.error(editingTag ? '更新标签失败:' : '创建标签失败:', error);
      setIsDialogOpen(false);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div id="tag-management-container" className="space-y-4">
      {/* 搜索和添加区域 */}
      <div className="flex justify-between items-center">
        <div className="flex space-x-2">
          <Input
            placeholder="搜索标签名称"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="w-[300px]"
          />
          <Button variant="outline" onClick={handleSearch}>
            <Search className="h-4 w-4 mr-2" />
            搜索
          </Button>
        </div>
        <Button onClick={handleOpenCreateDialog}>
          <Plus className="h-4 w-4 mr-2" />
          添加标签
        </Button>
      </div>

      {/* 标签列表表格 */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>名称</TableHead>
              <TableHead>描述</TableHead>
              <TableHead>使用次数</TableHead>
              <TableHead>关联课程</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading && (
              <TableRow>
                <TableCell colSpan={7} className="text-center p-4">
                  <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                </TableCell>
              </TableRow>
            )}
            {!isLoading && tags.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} className="text-center p-4">
                  未找到标签数据
                </TableCell>
              </TableRow>
            )}
            {!isLoading && tags.map((tag) => (
              <>
                <TableRow
                  key={tag.id}
                  className="cursor-pointer hover:bg-muted/50"
                  onClick={() => toggleTag(tag.id)}
                >
                  <TableCell>{tag.id}</TableCell>
                  <TableCell>
                    <div className="flex items-center">
                      <Badge variant="outline">{tag.name}</Badge>
                      {expandedTag === tag.id ? (
                        <ChevronUp className="ml-2 h-4 w-4" />
                      ) : (
                        <ChevronDown className="ml-2 h-4 w-4" />
                      )}
                    </div>
                  </TableCell>
                  <TableCell>{tag.description || '-'}</TableCell>
                  <TableCell>{tag.useCount || 0}</TableCell>
                  <TableCell>{tag.courseCount || 0}</TableCell>
                  <TableCell>
                    {tag.createdAt ? new Date(tag.createdAt).toLocaleString() : '-'}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end space-x-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleOpenEditDialog(tag);
                        }}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          setTagToDelete(tag);
                          setIsDeleteDialogOpen(true);
                        }}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>

                {/* 展开的课程列表 */}
                {expandedTag === tag.id && (
                  <TableRow>
                    <TableCell colSpan={7} className="p-0 border-t-0">
                      <div className="bg-muted/20 p-4">
                        <h4 className="font-medium mb-3">关联课程</h4>
                        <CourseListView
                          courses={tagCourses}
                          loading={loadingCourses}
                          onCourseClick={handleCourseClick}
                        />
                      </div>
                    </TableCell>
                  </TableRow>
                )}
              </>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* 分页 */}
      {!isLoading && totalPages > 0 && (
        <div className="flex justify-between items-center">
          <div className="text-sm text-muted-foreground">
            共 {totalItems} 条数据，当前显示第 {currentPage + 1} 页
          </div>
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    handlePageChange(Math.max(0, currentPage - 1));
                  }}
                  className={currentPage === 0 ? "pointer-events-none opacity-50" : ""}
                />
              </PaginationItem>

              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                // 显示当前页附近的页码
                let pageToShow;
                if (totalPages <= 5) {
                  pageToShow = i;
                } else if (currentPage < 2) {
                  pageToShow = i;
                } else if (currentPage > totalPages - 3) {
                  pageToShow = totalPages - 5 + i;
                } else {
                  pageToShow = currentPage - 2 + i;
                }

                if (pageToShow >= 0 && pageToShow < totalPages) {
                  return (
                    <PaginationItem key={pageToShow}>
                      <PaginationLink
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          handlePageChange(pageToShow);
                        }}
                        isActive={pageToShow === currentPage}
                      >
                        {pageToShow + 1}
                      </PaginationLink>
                    </PaginationItem>
                  )
                }
                return null;
              })}

              <PaginationItem>
                <PaginationNext
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    handlePageChange(Math.min(totalPages - 1, currentPage + 1));
                  }}
                  className={currentPage === totalPages - 1 ? "pointer-events-none opacity-50" : ""}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}

      {/* 标签表单对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editingTag ? '编辑标签' : '创建标签'}</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>标签名称 *</FormLabel>
                    <FormControl>
                      <Input placeholder="输入标签名称" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>标签描述</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="输入标签描述"
                        {...field}
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <DialogFooter>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setIsDialogOpen(false)}
                >
                  取消
                </Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  {editingTag ? '更新' : '创建'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* 删除确认对话框 */}
      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除标签</AlertDialogTitle>
            <AlertDialogDescription>
              您确定要删除标签 "{tagToDelete?.name}" 吗？该操作不可撤销，如果该标签已被课程使用，可能导致关联数据丢失。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteConfirm} disabled={isLoading}>
              {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              确认删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}