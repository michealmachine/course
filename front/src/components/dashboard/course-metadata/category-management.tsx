'use client';

import { useState, useEffect } from 'react';
import { Category, CategoryDTO, CategoryTree } from '@/types/course';
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
import { Switch } from '@/components/ui/switch';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Loader2, Plus, Search, Trash2, Edit, Check, X } from 'lucide-react';
import { toast } from 'sonner';
import categoryService from '@/services/category';
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

// 表单验证模式
const categoryFormSchema = z.object({
  name: z.string().min(2, '分类名称至少需要2个字符').max(100, '分类名称不能超过100个字符'),
  code: z.string()
    .min(2, '分类编码至少需要2个字符')
    .max(50, '分类编码不能超过50个字符')
    .regex(/^[a-zA-Z0-9_-]+$/, '分类编码只能包含字母、数字、下划线和连字符'),
  description: z.string().max(500, '分类描述不能超过500个字符').optional(),
  parentId: z.string().optional(),
  orderIndex: z.coerce.number().int().optional(),
  enabled: z.boolean().default(true),
  icon: z.string().max(255, '图标路径不能超过255个字符').optional(),
});

export function CategoryManagement() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [parentCategories, setParentCategories] = useState<Category[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [categoryToDelete, setCategoryToDelete] = useState<Category | null>(null);
  const [categoryTree, setCategoryTree] = useState<CategoryTree[]>([]);
  const [viewMode, setViewMode] = useState<'list' | 'tree'>('list');

  // 表单初始化
  const form = useForm<z.infer<typeof categoryFormSchema>>({
    resolver: zodResolver(categoryFormSchema),
    defaultValues: {
      name: '',
      code: '',
      description: '',
      parentId: 'none',
      orderIndex: 0,
      enabled: true,
      icon: '',
    },
  });

  // 加载分类列表
  const loadCategories = async (page = 0) => {
    setIsLoading(true);
    try {
      const result = await categoryService.getCategoryList(searchKeyword, page);
      setCategories(result.content);
      setTotalPages(result.totalPages);
      setTotalItems(result.totalElements);
      setCurrentPage(result.number);
    } catch (error) {
      toast.error('加载分类列表失败');
      console.error('加载分类列表失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 加载父分类
  const loadParentCategories = async () => {
    try {
      const result = await categoryService.getRootCategories();
      setParentCategories(result);
    } catch (error) {
      toast.error('加载父分类列表失败');
      console.error('加载父分类列表失败:', error);
    }
  };

  // 加载分类树
  const loadCategoryTree = async () => {
    try {
      const result = await categoryService.getCategoryTree();
      setCategoryTree(result);
    } catch (error) {
      toast.error('加载分类树失败');
      console.error('加载分类树失败:', error);
    }
  };

  // 初始加载
  useEffect(() => {
    loadCategories();
    loadParentCategories();
    loadCategoryTree();
  }, []);

  // 搜索分类
  const handleSearch = () => {
    loadCategories(0); // 重置到第一页
  };

  // 切换页面
  const handlePageChange = (page: number) => {
    loadCategories(page);
  };

  // 打开创建分类对话框
  const handleOpenCreateDialog = () => {
    form.reset({
      name: '',
      code: '',
      description: '',
      parentId: "none",
      orderIndex: 0,
      enabled: true,
      icon: '',
    });
    setEditingCategory(null);
    setIsDialogOpen(true);
  };

  // 打开编辑分类对话框
  const handleOpenEditDialog = (category: Category) => {
    form.reset({
      name: category.name,
      code: category.code,
      description: category.description || '',
      parentId: category.parentId ? String(category.parentId) : "none",
      orderIndex: category.orderIndex || 0,
      enabled: category.enabled !== false, // 默认为true
      icon: category.icon || '',
    });
    setEditingCategory(category);
    setIsDialogOpen(true);
  };

  // 确认删除分类
  const handleDeleteConfirm = async () => {
    if (!categoryToDelete) return;
    
    setIsLoading(true);
    try {
      await categoryService.deleteCategory(categoryToDelete.id);
      toast.success('分类删除成功');
      loadCategories(currentPage);
    } catch (error) {
      toast.error('删除分类失败');
      console.error('删除分类失败:', error);
    } finally {
      setIsLoading(false);
      setIsDeleteDialogOpen(false);
      setCategoryToDelete(null);
    }
  };

  // 保存分类
  const onSubmit = async (data: z.infer<typeof categoryFormSchema>) => {
    setIsLoading(true);
    
    // 转换表单数据为DTO
    const categoryDTO: CategoryDTO = {
      name: data.name,
      code: data.code,
      description: data.description,
      parentId: data.parentId && data.parentId !== "none" ? parseInt(data.parentId) : undefined,
      orderIndex: data.orderIndex,
      enabled: data.enabled,
      icon: data.icon,
    };
    
    try {
      // 检查编码是否可用
      const excludeId = editingCategory?.id;
      const isAvailable = await categoryService.isCodeAvailable(data.code, excludeId);
      
      if (!isAvailable) {
        form.setError('code', {
          type: 'manual',
          message: '此分类编码已存在，请使用其他编码'
        });
        setIsLoading(false);
        return;
      }
      
      if (editingCategory) {
        // 更新分类
        await categoryService.updateCategory(editingCategory.id, categoryDTO);
        toast.success('分类更新成功');
      } else {
        // 创建分类
        await categoryService.createCategory(categoryDTO);
        toast.success('分类创建成功');
      }
      
      // 刷新列表
      loadCategories(currentPage);
      setIsDialogOpen(false);
    } catch (error) {
      toast.error(editingCategory ? '更新分类失败' : '创建分类失败');
      console.error(editingCategory ? '更新分类失败:' : '创建分类失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 更新分类状态
  const handleToggleStatus = async (category: Category) => {
    const newStatus = !category.enabled;
    setIsLoading(true);
    try {
      await categoryService.updateCategoryStatus(category.id, newStatus);
      toast.success(`分类${newStatus ? '启用' : '禁用'}成功`);
      
      // 更新本地状态，避免重新加载整个列表
      setCategories(categories.map(c => 
        c.id === category.id ? { ...c, enabled: newStatus } : c
      ));
    } catch (error) {
      toast.error(`分类${newStatus ? '启用' : '禁用'}失败`);
      console.error(`分类${newStatus ? '启用' : '禁用'}失败:`, error);
    } finally {
      setIsLoading(false);
    }
  };

  // 更新分类排序
  const handleUpdateOrder = async (id: number, orderIndex: number) => {
    setIsLoading(true);
    try {
      await categoryService.updateCategoryOrder(id, orderIndex);
      toast.success('分类排序更新成功');
      if (viewMode === 'list') {
        loadCategories(currentPage);
      } else {
        loadCategoryTree();
      }
    } catch (error) {
      toast.error('更新分类排序失败');
      console.error('更新分类排序失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      {/* 搜索和添加区域 */}
      <div className="flex justify-between items-center">
        <div className="flex space-x-2">
          {viewMode === 'list' && (
            <>
              <Input
                placeholder="搜索分类名称或编码"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="w-[300px]"
              />
              <Button variant="outline" onClick={handleSearch}>
                <Search className="h-4 w-4 mr-2" />
                搜索
              </Button>
            </>
          )}
        </div>
        <div className="flex space-x-2">
          <Tabs value={viewMode} onValueChange={(v) => setViewMode(v as 'list' | 'tree')}>
            <TabsList>
              <TabsTrigger value="list">列表视图</TabsTrigger>
              <TabsTrigger value="tree">树形视图</TabsTrigger>
            </TabsList>
          </Tabs>
          <Button onClick={handleOpenCreateDialog}>
            <Plus className="h-4 w-4 mr-2" />
            添加分类
          </Button>
        </div>
      </div>

      {/* 视图内容 */}
      {viewMode === 'list' ? (
        <>
          {/* 分类列表表格 */}
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>名称</TableHead>
                  <TableHead>编码</TableHead>
                  <TableHead>父分类</TableHead>
                  <TableHead>排序</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>创建时间</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading && (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center p-4">
                      <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                    </TableCell>
                  </TableRow>
                )}
                {!isLoading && categories.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center p-4">
                      未找到分类数据
                    </TableCell>
                  </TableRow>
                )}
                {!isLoading && categories.map((category) => (
                  <TableRow key={category.id}>
                    <TableCell>{category.id}</TableCell>
                    <TableCell>{category.name}</TableCell>
                    <TableCell>{category.code}</TableCell>
                    <TableCell>{category.parentName || '-'}</TableCell>
                    <TableCell>{category.orderIndex || 0}</TableCell>
                    <TableCell>
                      <div className="flex items-center space-x-2">
                        <Switch
                          checked={category.enabled !== false}
                          onCheckedChange={() => handleToggleStatus(category)}
                        />
                        <span>{category.enabled !== false ? '启用' : '禁用'}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      {category.createdAt ? new Date(category.createdAt).toLocaleString() : '-'}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end space-x-2">
                        <Button variant="outline" size="sm" onClick={() => handleOpenEditDialog(category)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="destructive" 
                          size="sm" 
                          onClick={() => {
                            setCategoryToDelete(category);
                            setIsDeleteDialogOpen(true);
                          }}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
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
        </>
      ) : (
        /* 分类树视图 */
        <div className="rounded-md border p-4">
          {isLoading ? (
            <div className="flex justify-center p-8">
              <Loader2 className="h-8 w-8 animate-spin" />
            </div>
          ) : categoryTree.length === 0 ? (
            <div className="text-center p-8 text-muted-foreground">
              未找到分类数据
            </div>
          ) : (
            <CategoryTreeView 
              categoryTree={categoryTree} 
              onEdit={handleOpenEditDialog}
              onDelete={(category) => {
                setCategoryToDelete(category);
                setIsDeleteDialogOpen(true);
              }}
              onUpdateOrder={handleUpdateOrder}
            />
          )}
        </div>
      )}

      {/* 分类表单对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[550px]">
          <DialogHeader>
            <DialogTitle>{editingCategory ? '编辑分类' : '创建分类'}</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>分类名称 *</FormLabel>
                    <FormControl>
                      <Input placeholder="输入分类名称" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="code"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>分类编码 *</FormLabel>
                    <FormControl>
                      <Input 
                        placeholder="输入分类编码，只能包含字母、数字、下划线和连字符" 
                        {...field} 
                      />
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
                    <FormLabel>分类描述</FormLabel>
                    <FormControl>
                      <Textarea 
                        placeholder="输入分类描述" 
                        {...field} 
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="parentId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>父分类</FormLabel>
                      <Select 
                        onValueChange={field.onChange} 
                        value={field.value}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择父分类" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="none">无父分类</SelectItem>
                          {parentCategories.map((category) => (
                            <SelectItem 
                              key={category.id} 
                              value={String(category.id)}
                              disabled={editingCategory?.id === category.id}
                            >
                              {category.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="orderIndex"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>排序索引</FormLabel>
                      <FormControl>
                        <Input 
                          type="number" 
                          placeholder="输入排序索引" 
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="enabled"
                  render={({ field }) => (
                    <FormItem className="flex flex-row items-start space-x-3 space-y-0 rounded-md border p-4">
                      <FormControl>
                        <Switch
                          checked={field.value}
                          onCheckedChange={field.onChange}
                        />
                      </FormControl>
                      <div className="space-y-1 leading-none">
                        <FormLabel>是否启用</FormLabel>
                      </div>
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="icon"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>图标路径</FormLabel>
                      <FormControl>
                        <Input placeholder="输入图标路径" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              
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
                  {editingCategory ? '更新' : '创建'}
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
            <AlertDialogTitle>确认删除分类</AlertDialogTitle>
            <AlertDialogDescription>
              您确定要删除分类 "{categoryToDelete?.name}" 吗？该操作不可撤销，如果该分类下有子分类或关联课程，可能导致数据异常。
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

// 分类树视图组件
function CategoryTreeView({ 
  categoryTree, 
  onEdit, 
  onDelete,
  onUpdateOrder
}: { 
  categoryTree: CategoryTree[], 
  onEdit: (category: Category) => void,
  onDelete: (category: Category) => void,
  onUpdateOrder: (id: number, orderIndex: number) => void
}) {
  
  const renderTreeNode = (node: CategoryTree, level = 0) => (
    <div key={node.id} className={`border-l-2 ${level > 0 ? 'ml-6 pl-2 border-gray-200' : ''}`}>
      <div className="flex items-center justify-between py-2 hover:bg-muted/20 px-2 rounded-sm">
        <div className="flex items-center">
          <div className={`w-3 h-3 rounded-full mr-2 ${node.enabled ? 'bg-green-500' : 'bg-red-500'}`} />
          <span className="font-medium">{node.name}</span>
          <span className="text-muted-foreground ml-2 text-xs">({node.code})</span>
          {node.children && node.children.length > 0 && (
            <span className="ml-2 text-xs text-muted-foreground">
              {node.children.length} 个子分类
            </span>
          )}
          {node.courseCount && node.courseCount > 0 && (
            <span className="ml-2 text-xs text-muted-foreground">
              {node.courseCount} 个课程
            </span>
          )}
        </div>
        <div className="flex space-x-2">
          <Button variant="ghost" size="sm" onClick={() => {
            const newIndex = (node.orderIndex || 0) - 1;
            if (newIndex >= 0) {
              onUpdateOrder(node.id, newIndex);
            }
          }} disabled={(node.orderIndex || 0) <= 0}>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-arrow-up"><path d="m5 12 7-7 7 7"/><path d="M12 19V5"/></svg>
          </Button>
          <Button variant="ghost" size="sm" onClick={() => {
            onUpdateOrder(node.id, (node.orderIndex || 0) + 1);
          }}>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-arrow-down"><path d="M12 5v14"/><path d="m19 12-7 7-7-7"/></svg>
          </Button>
          <Button variant="outline" size="sm" onClick={() => onEdit(node)}>
            <Edit className="h-4 w-4" />
          </Button>
          <Button variant="destructive" size="sm" onClick={() => onDelete(node)}>
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </div>
      {node.children && node.children.length > 0 && (
        <div>
          {node.children.map((child: CategoryTree) => renderTreeNode(child, level + 1))}
        </div>
      )}
    </div>
  );
  
  return (
    <div className="space-y-2">
      {categoryTree.map(item => renderTreeNode(item))}
    </div>
  );
} 