'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import * as z from 'zod';
import { courseService, categoryService, tagService } from '@/services';
import { 
  Course, 
  CourseCreateDTO, 
  CoursePaymentType, 
  CourseDifficulty, 
  CourseStatus,
  Category,
  Tag
} from '@/types/course';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle, Loader2, Upload } from 'lucide-react';

// 表单验证模式
const formSchema = z.object({
  title: z.string().min(2, '标题至少2个字符').max(100, '标题不能超过100个字符'),
  description: z.string().max(1000, '描述不能超过1000个字符').optional(),
  categoryId: z.number().optional(),
  tagIds: z.array(z.number()).optional(),
  paymentType: z.nativeEnum(CoursePaymentType),
  price: z.number().min(0, '价格不能为负').optional(),
  discountPrice: z.number().min(0, '折扣价格不能为负').optional(),
  difficulty: z.nativeEnum(CourseDifficulty).optional(),
  targetAudience: z.string().max(500, '目标受众不能超过500个字符').optional(),
  learningObjectives: z.string().max(500, '学习目标不能超过500个字符').optional(),
});

interface CourseFormProps {
  course?: Course;
  onSuccess?: (course: Course) => void;
}

export default function CourseForm({ course, onSuccess }: CourseFormProps) {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = useState(false);
  const [isLoadingTags, setIsLoadingTags] = useState(false);
  const [categoryPage, setCategoryPage] = useState(0);
  const [tagPage, setTagPage] = useState(0);
  const [hasMoreCategories, setHasMoreCategories] = useState(true);
  const [hasMoreTags, setHasMoreTags] = useState(true);
  const [searchingCategory, setSearchingCategory] = useState('');
  const [searchingTag, setSearchingTag] = useState('');
  const [categorySearchTimeout, setCategorySearchTimeout] = useState<NodeJS.Timeout | null>(null);
  const [tagSearchTimeout, setTagSearchTimeout] = useState<NodeJS.Timeout | null>(null);
  // 添加isMounted引用以跟踪组件挂载状态
  const isMounted = useRef(true);
  
  // 表单默认值
  const defaultValues: Partial<CourseCreateDTO> = {
    title: '',
    description: '',
    categoryId: undefined,
    tagIds: [],
    paymentType: CoursePaymentType.FREE,
    price: 0,
    discountPrice: 0,
    difficulty: CourseDifficulty.BEGINNER,
    targetAudience: '',
    learningObjectives: '',
  };

  // 初始化表单
  const form = useForm<CourseCreateDTO>({
    resolver: zodResolver(formSchema),
    defaultValues: course ? {
      title: course.title,
      description: course.description || '',
      categoryId: course.category?.id,
      tagIds: course.tags?.map(tag => tag.id) || [],
      paymentType: course.paymentType,
      price: course.price || 0,
      discountPrice: course.discountPrice || 0,
      difficulty: course.difficulty || CourseDifficulty.BEGINNER,
      targetAudience: course.targetAudience || '',
      learningObjectives: course.learningObjectives || '',
    } : defaultValues,
  });

  // 加载分类数据，使用分页方式，并支持搜索
  const loadCategories = useCallback(async (page = 0, keyword = '') => {
    if (!hasMoreCategories && page > 0) return;
    
    setIsLoadingCategories(true);
    try {
      // 使用分页API获取数据
      const pageSize = 20; // 合理的页面大小
      const result = await categoryService.getCategoryList(keyword, page, pageSize);
      
      if (page === 0) {
        // 重置数据，确保没有重复ID
        setCategories(result.content.filter((cat, index, self) => 
          self.findIndex(c => c.id === cat.id) === index
        ));
      } else {
        // 追加数据，过滤掉已存在的ID
        setCategories(prev => {
          const existingIds = new Set(prev.map(c => c.id));
          const newItems = result.content.filter(cat => !existingIds.has(cat.id));
          return [...prev, ...newItems];
        });
      }
      
      // 判断是否还有更多数据
      setHasMoreCategories(page < result.totalPages - 1);
      setCategoryPage(page);
    } catch (err) {
      console.error('加载分类失败:', err);
    } finally {
      setIsLoadingCategories(false);
    }
  }, [hasMoreCategories]);
  
  // 加载标签数据，使用分页方式，并支持搜索
  const loadTags = useCallback(async (page = 0, keyword = '') => {
    if (!hasMoreTags && page > 0) return;
    
    setIsLoadingTags(true);
    try {
      const pageSize = 50; // 标签可以一次加载多一些
      const result = await tagService.getTagList(keyword, page, pageSize);
      
      if (page === 0) {
        // 重置数据，确保没有重复ID
        setTags(result.content.filter((tag, index, self) => 
          self.findIndex(t => t.id === tag.id) === index
        ));
      } else {
        // 追加数据，过滤掉已存在的ID
        setTags(prev => {
          const existingIds = new Set(prev.map(t => t.id));
          const newItems = result.content.filter(tag => !existingIds.has(tag.id));
          return [...prev, ...newItems];
        });
      }
      
      // 判断是否还有更多数据
      setHasMoreTags(page < result.totalPages - 1);
      setTagPage(page);
    } catch (err) {
      console.error('加载标签失败:', err);
    } finally {
      setIsLoadingTags(false);
    }
  }, [hasMoreTags]);
  
  // 处理分类搜索
  const handleCategorySearch = (keyword: string) => {
    setSearchingCategory(keyword);
    
    // 防抖处理
    if (categorySearchTimeout) {
      clearTimeout(categorySearchTimeout);
    }
    
    setCategorySearchTimeout(setTimeout(() => {
      loadCategories(0, keyword);
    }, 300));
  };
  
  // 处理标签搜索
  const handleTagSearch = (keyword: string) => {
    setSearchingTag(keyword);
    
    // 防抖处理
    if (tagSearchTimeout) {
      clearTimeout(tagSearchTimeout);
    }
    
    setTagSearchTimeout(setTimeout(() => {
      loadTags(0, keyword);
    }, 300));
  };
  
  // 加载更多分类
  const handleLoadMoreCategories = () => {
    loadCategories(categoryPage + 1, searchingCategory);
  };
  
  // 加载更多标签
  const handleLoadMoreTags = () => {
    loadTags(tagPage + 1, searchingTag);
  };

  // 初始加载
  useEffect(() => {
    loadCategories();
    loadTags();
    
    // 组件卸载时清除定时器和更新挂载状态
    return () => {
      isMounted.current = false;
      if (categorySearchTimeout) clearTimeout(categorySearchTimeout);
      if (tagSearchTimeout) clearTimeout(tagSearchTimeout);
    };
  }, [loadCategories, loadTags]);

  // 编辑模式下，确保加载完整的分类和标签数据
  useEffect(() => {
    if (course) {
      // 如果是编辑模式，加载分类详情
      if (course.category && course.category.id) {
        const loadCategory = async () => {
          try {
            setIsLoadingCategories(true);
            const categoryDetail = await categoryService.getCategoryById(course.category!.id);
            if (categoryDetail && !categories.some(c => c.id === categoryDetail.id)) {
              setCategories(prev => {
                // 确保不添加重复ID的分类
                if (prev.some(c => c.id === categoryDetail.id)) {
                  return prev;
                }
                return [categoryDetail, ...prev];
              });
            }
          } catch (err) {
            console.error('加载分类详情失败:', err);
          } finally {
            setIsLoadingCategories(false);
          }
        };
        loadCategory();
      }
      
      // 如果有tags数组，则确保添加到标签列表中
      if (course.tags && course.tags.length > 0) {
        const validTags = course.tags.filter(tag => tag && tag.id && tag.name);
        if (validTags.length > 0) {
          setTags(prev => {
            const existingIds = new Set(prev.map(t => t.id));
            // 过滤出prev中不存在的标签
            const newTags = validTags.filter(tag => !existingIds.has(tag.id));
            // 确保没有重复
            return [...newTags, ...prev].filter((tag, index, self) => 
              self.findIndex(t => t.id === tag.id) === index
            );
          });
          
          // 确保表单的tagIds值是正确的
          const tagIds = validTags.map(tag => tag.id);
          form.setValue('tagIds', tagIds);
        }
      }
    }
  }, [course, categories, form]);

  // 添加封面上传预览功能
  const [previewImage, setPreviewImage] = useState<string | null>(course?.coverUrl || null);
  const [coverFile, setCoverFile] = useState<File | null>(null);

  // 处理封面文件选择
  const handleCoverChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setCoverFile(file);
      
      // 创建预览URL
      const reader = new FileReader();
      reader.onload = (event) => {
        setPreviewImage(event.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // 上传封面图片
  const handleCoverUpload = async () => {
    if (!coverFile || !course) return;
    
    try {
      setIsSubmitting(true);
      const updatedCourse = await courseService.updateCourseCover(course.id, coverFile);
      
      // 检查组件是否仍挂载
      if (isMounted.current) {
        // 更新预览图片
        setPreviewImage(updatedCourse.coverUrl || null);
        // 通知上层组件
        if (onSuccess) {
          onSuccess(updatedCourse);
        }
        setCoverFile(null);
        
        // 重置文件输入
        const fileInput = document.getElementById('cover-upload') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
      }
    } catch (err: any) {
      if (isMounted.current) {
        setError(err.message || '上传封面失败');
      }
    } finally {
      if (isMounted.current) {
        setIsSubmitting(false);
      }
    }
  };

  // 提交表单
  const onSubmit = async (data: CourseCreateDTO) => {
    setIsSubmitting(true);
    setError(null);
    
    try {
      let result: Course;
      
      if (course) {
        // 更新课程
        result = await courseService.updateCourse(course.id, data);
      } else {
        // 创建课程
        result = await courseService.createCourse(data);
      }
      
      // 移除setTimeout，直接执行回调或导航，但首先检查组件是否仍然挂载
      if (isMounted.current) {
        if (onSuccess) {
          onSuccess(result);
        } else {
          // 默认跳转到课程编辑页
          router.push(`/dashboard/courses/${result.id}`);
        }
      }
    } catch (err: any) {
      if (isMounted.current) {
        setError(err.message || '提交失败，请稍后重试');
      }
    } finally {
      if (isMounted.current) {
        setIsSubmitting(false);
      }
    }
  };

  return (
    <Card className="shadow-sm">
      <CardHeader className="bg-muted/30">
        <CardTitle className="text-2xl font-bold">{course ? '编辑课程' : '创建新课程'}</CardTitle>
        <CardDescription>
          {course ? '修改课程信息' : '填写课程基本信息，创建完成后可以继续添加内容和封面'}
        </CardDescription>
      </CardHeader>
      
      <CardContent className="pt-6">
        {error && (
          <Alert variant="destructive" className="mb-6">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>错误</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
            {/* 基本信息区域 */}
            <div className="space-y-6">
              <h3 className="text-lg font-medium border-b pb-2">基本信息</h3>
              
              {/* 课程标题 */}
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-base">课程标题</FormLabel>
                    <FormControl>
                      <Input placeholder="输入课程标题" {...field} className="h-10" />
                    </FormControl>
                    <FormDescription>
                      给课程起一个吸引人的标题
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              {/* 课程描述 */}
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-base">课程描述</FormLabel>
                    <FormControl>
                      <Textarea 
                        placeholder="详细描述课程内容" 
                        {...field} 
                        rows={5}
                        className="resize-y"
                      />
                    </FormControl>
                    <FormDescription>
                      详细介绍课程内容和特点，支持多行文本
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            
            {/* 分类与标签区域 */}
            <div className="space-y-6">
              <h3 className="text-lg font-medium border-b pb-2">分类与标签</h3>
              
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* 课程分类 */}
                <FormField
                  control={form.control}
                  name="categoryId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-base">课程分类</FormLabel>
                      <div className="space-y-2">
                        <div className="flex mb-2">
                          <Input
                            placeholder="搜索分类"
                            value={searchingCategory}
                            onChange={(e) => handleCategorySearch(e.target.value)}
                            className="w-full"
                          />
                        </div>
                        <Select
                          onValueChange={(value) => field.onChange(parseInt(value))}
                          value={field.value?.toString()}
                        >
                          <FormControl>
                            <SelectTrigger className="h-10">
                              <SelectValue placeholder="选择课程分类" />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            {isLoadingCategories && categoryPage === 0 ? (
                              <div className="flex items-center justify-center py-2">
                                <span className="text-sm text-muted-foreground">加载中...</span>
                              </div>
                            ) : categories.length > 0 ? (
                              <>
                                {categories.map((category, index) => (
                                  <SelectItem 
                                    key={`category-${category.id}-${index}-${Math.random().toString(36).substr(2, 9)}`} 
                                    value={category.id.toString()}
                                  >
                                    {category.name}
                                  </SelectItem>
                                ))}
                                {hasMoreCategories && (
                                  <div 
                                    className="py-2 px-2 cursor-pointer hover:bg-muted text-center text-sm"
                                    onClick={(e) => {
                                      e.preventDefault();
                                      e.stopPropagation();
                                      handleLoadMoreCategories();
                                    }}
                                  >
                                    {isLoadingCategories ? '加载中...' : '加载更多'}
                                  </div>
                                )}
                              </>
                            ) : (
                              <div className="flex items-center justify-center py-2">
                                <span className="text-sm text-muted-foreground">暂无分类</span>
                              </div>
                            )}
                          </SelectContent>
                        </Select>
                      </div>
                      <FormDescription>
                        选择课程所属的分类
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                {/* 课程难度 */}
                <FormField
                  control={form.control}
                  name="difficulty"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-base">课程难度</FormLabel>
                      <Select
                        onValueChange={(value) => field.onChange(parseInt(value))}
                        defaultValue={field.value?.toString()}
                      >
                        <FormControl>
                          <SelectTrigger className="h-10">
                            <SelectValue placeholder="选择课程难度" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem key="difficulty-beginner" value={CourseDifficulty.BEGINNER.toString()}>初级</SelectItem>
                          <SelectItem key="difficulty-intermediate" value={CourseDifficulty.INTERMEDIATE.toString()}>中级</SelectItem>
                          <SelectItem key="difficulty-advanced" value={CourseDifficulty.ADVANCED.toString()}>高级</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormDescription>
                        选择适合的课程难度级别
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              
              {/* 课程标签 */}
              <FormField
                control={form.control}
                name="tagIds"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-base">课程标签</FormLabel>
                    <div className="space-y-2">
                      <div className="flex mb-2">
                        <Input
                          placeholder="搜索标签"
                          value={searchingTag}
                          onChange={(e) => handleTagSearch(e.target.value)}
                          className="w-full"
                        />
                      </div>
                      <div className="border rounded-md p-4 min-h-[120px]">
                        {isLoadingTags && tagPage === 0 ? (
                          <div className="flex items-center justify-center h-full">
                            <span className="text-sm text-muted-foreground">加载中...</span>
                          </div>
                        ) : (
                          <div className="flex flex-wrap gap-2">
                            {tags.length > 0 ? (
                              <>
                                {tags.map((tag, index) => (
                                  <div
                                    key={`tag-${tag.id}-${index}-${Math.random().toString(36).substr(2, 9)}`}
                                    className={`px-3 py-1 rounded-full text-sm cursor-pointer transition-colors 
                                      ${Array.isArray(field.value) && field.value.includes(tag.id) 
                                        ? 'bg-primary text-primary-foreground' 
                                        : 'bg-secondary text-secondary-foreground hover:bg-secondary/80'
                                      }`}
                                    onClick={() => {
                                      const currentTags = Array.isArray(field.value) ? field.value : [];
                                      if (currentTags.includes(tag.id)) {
                                        field.onChange(currentTags.filter(id => id !== tag.id));
                                      } else {
                                        field.onChange([...currentTags, tag.id]);
                                      }
                                    }}
                                  >
                                    {tag.name}
                                  </div>
                                ))}
                                {hasMoreTags && (
                                  <Button 
                                    variant="outline" 
                                    size="sm" 
                                    onClick={handleLoadMoreTags}
                                    disabled={isLoadingTags}
                                    className="ml-2"
                                  >
                                    {isLoadingTags ? '加载中...' : '加载更多'}
                                  </Button>
                                )}
                              </>
                            ) : (
                              <div className="w-full text-center py-2">
                                <span className="text-sm text-muted-foreground">暂无标签</span>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        已选择 {field.value?.length || 0} 个标签
                      </div>
                    </div>
                    <FormDescription>
                      选择课程相关的标签，点击标签进行选择
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            
            {/* 价格设置区域 */}
            <div className="space-y-6">
              <h3 className="text-lg font-medium border-b pb-2">价格设置</h3>
              
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* 付费类型 */}
                <FormField
                  control={form.control}
                  name="paymentType"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-base">付费类型</FormLabel>
                      <Select
                        onValueChange={(value) => field.onChange(parseInt(value))}
                        defaultValue={field.value?.toString()}
                      >
                        <FormControl>
                          <SelectTrigger className="h-10">
                            <SelectValue placeholder="选择付费类型" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem key="payment-free" value={CoursePaymentType.FREE.toString()}>免费</SelectItem>
                          <SelectItem key="payment-paid" value={CoursePaymentType.PAID.toString()}>付费</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormDescription>
                        选择课程的付费类型
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                {form.watch('paymentType') === CoursePaymentType.PAID && (
                  <div className="space-y-6 lg:col-span-1">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {/* 价格 */}
                      <FormField
                        control={form.control}
                        name="price"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel className="text-base">课程价格</FormLabel>
                            <FormControl>
                              <Input 
                                type="number" 
                                min="0"
                                step="0.01"
                                placeholder="设置课程价格" 
                                {...field}
                                onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                                className="h-10"
                              />
                            </FormControl>
                            <FormDescription>
                              设置课程的价格（元）
                            </FormDescription>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      
                      {/* 优惠价格 */}
                      <FormField
                        control={form.control}
                        name="discountPrice"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel className="text-base">优惠价格</FormLabel>
                            <FormControl>
                              <Input 
                                type="number" 
                                min="0"
                                step="0.01"
                                placeholder="设置优惠价格" 
                                {...field}
                                onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                                className="h-10"
                              />
                            </FormControl>
                            <FormDescription>
                              设置课程的优惠价格
                            </FormDescription>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>
            
            {/* 目标与收益区域 */}
            <div className="space-y-6">
              <h3 className="text-lg font-medium border-b pb-2">学习目标与受众</h3>
              
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* 目标受众 */}
                <FormField
                  control={form.control}
                  name="targetAudience"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-base">目标受众</FormLabel>
                      <FormControl>
                        <Textarea 
                          placeholder="描述适合学习的人群" 
                          {...field} 
                          rows={3}
                          className="resize-y"
                        />
                      </FormControl>
                      <FormDescription>
                        描述这门课程适合什么样的学习者
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                {/* 学习目标 */}
                <FormField
                  control={form.control}
                  name="learningObjectives"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-base">学习目标</FormLabel>
                      <FormControl>
                        <Textarea 
                          placeholder="学完课程后能掌握的技能" 
                          {...field} 
                          rows={3}
                          className="resize-y"
                        />
                      </FormControl>
                      <FormDescription>
                        描述学完课程后能获得哪些能力或技能
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            </div>
            
            {/* 封面上传区域 - 新增 */}
            {course && (
              <div className="space-y-6">
                <h3 className="text-lg font-medium border-b pb-2">课程封面</h3>
                
                <div className="flex justify-center mb-6">
                  {previewImage ? (
                    <div className="relative w-full max-w-md h-64 rounded-md overflow-hidden">
                      <img
                        src={previewImage}
                        alt="课程封面预览"
                        className="object-cover w-full h-full"
                      />
                    </div>
                  ) : (
                    <div className="border border-dashed border-gray-300 rounded-md p-12 text-center w-full max-w-md">
                      <Upload className="mx-auto h-12 w-12 text-gray-400" />
                      <p className="mt-2 text-sm text-gray-500">尚未上传封面图片</p>
                    </div>
                  )}
                </div>
                
                <div className="space-y-4">
                  <div className="flex items-center space-x-4">
                    <input
                      id="cover-upload"
                      type="file"
                      accept="image/*"
                      onChange={handleCoverChange}
                      className="block w-full text-sm text-gray-500
                        file:mr-4 file:py-2 file:px-4
                        file:rounded-md file:border-0
                        file:text-sm file:font-semibold
                        file:bg-gray-50 file:text-gray-700
                        hover:file:bg-gray-100"
                    />
                    <Button 
                      type="button"
                      onClick={handleCoverUpload} 
                      disabled={!coverFile || isSubmitting}
                    >
                      {isSubmitting ? '上传中...' : '上传封面'}
                    </Button>
                  </div>
                  
                  {coverFile && (
                    <p className="text-sm text-green-600">
                      已选择文件: {coverFile.name}
                    </p>
                  )}
                </div>
              </div>
            )}
            
            <div className="flex justify-end space-x-4 pt-4 border-t">
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => router.back()}
                disabled={isSubmitting}
                className="min-w-[100px]"
              >
                取消
              </Button>
              <Button type="submit" disabled={isSubmitting} className="min-w-[100px]">
                {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {course ? '保存更改' : '创建课程'}
              </Button>
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
} 