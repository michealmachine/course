'use client';

import { useState, useEffect, useCallback } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Slider } from '@/components/ui/slider';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Empty } from '@/components/ui/empty';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';
import { 
  Search, 
  Filter, 
  X, 
  LayoutGrid, 
  List,
  BookOpen,
  Users,
  Star,
  Clock,
  Sparkles,
  GraduationCap,
  Heart,
  HeartOff,
  ChevronRight,
  Tag,
  DollarSign,
  ArrowUpDown
} from 'lucide-react';
import { Course, CoursePaymentType, CourseDifficulty } from '@/types/course';
import { Category } from '@/types/course';
import { Tag as TagType } from '@/types/course';
import courseService from '@/services/course-service';
import { toast } from 'sonner';
import { categoryService, tagService } from '@/services';
import useDebounce from '@/hooks/useDebounce';
import favoriteService from '@/services/favorite-service';

// 搜索过滤器接口
interface SearchFilters {
  keyword: string;
  categoryId?: number;
  tagIds: number[];
  difficulty?: CourseDifficulty;
  paymentType?: CoursePaymentType;
  priceRange: [number, number];
  sortBy: string;
  page: number;
}

// 搜索参数接口
interface CourseSearchParams {
  keyword?: string;
  categoryId?: number;
  tagIds?: number[];
  difficulty?: CourseDifficulty;
  paymentType?: CoursePaymentType;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: string;
  page?: number;
  pageSize?: number;
}

export default function CourseSearchPage() {
  const router = useRouter();
  // 状态管理
  const [filters, setFilters] = useState<SearchFilters>({
    keyword: '',
    tagIds: [],
    priceRange: [0, 1000],
    sortBy: 'rating',
    page: 1
  });
  const [courses, setCourses] = useState<Course[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<TagType[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [favoriteStates, setFavoriteStates] = useState<Record<number, boolean>>({});
  const [favoritesLoading, setFavoritesLoading] = useState<Record<number, boolean>>({});
  const [filterOpen, setFilterOpen] = useState(true);

  // 使用防抖处理搜索
  const debouncedFilters = useDebounce(filters, 300);

  // 搜索课程
  const searchCourses = useCallback(async () => {
    setLoading(true);
    try {
      const searchParams: CourseSearchParams = {
        keyword: debouncedFilters.keyword || undefined,
        categoryId: debouncedFilters.categoryId,
        tagIds: debouncedFilters.tagIds.length > 0 ? debouncedFilters.tagIds : undefined,
        difficulty: debouncedFilters.difficulty,
        paymentType: debouncedFilters.paymentType,
        minPrice: debouncedFilters.priceRange[0],
        maxPrice: debouncedFilters.priceRange[1],
        sortBy: debouncedFilters.sortBy,
        page: debouncedFilters.page - 1,
        pageSize: 12
      };
      const response = await courseService.searchCourses(searchParams);
      setCourses(response.content || []);
      setTotalElements(response.totalElements || 0);
    } catch (error) {
      console.error('搜索课程失败:', error);
      toast.error('搜索课程失败，请重试');
      setCourses([]);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  }, [debouncedFilters]);

  // 监听过滤器变化自动搜索
  useEffect(() => {
    searchCourses();
  }, [debouncedFilters, searchCourses]);

  // 加载分类和标签数据
  useEffect(() => {
    const loadMetadata = async () => {
      try {
        const [categoriesResult, tagsResult] = await Promise.all([
          categoryService.getCategoryList('', 0, 100),
          tagService.getTagList('', 0, 100)
        ]);
        setCategories(categoriesResult.content || []);
        setTags(tagsResult.content || []);
      } catch (error) {
        console.error('加载元数据失败:', error);
        toast.error('加载分类和标签数据失败');
      }
    };
    loadMetadata();
  }, []);

  // 检查课程收藏状态
  const checkFavoriteStatus = async (courseId: number) => {
    try {
      setFavoritesLoading(prev => ({ ...prev, [courseId]: true }));
      const isFavorite = await favoriteService.checkFavorite(courseId);
      setFavoriteStates(prev => ({ ...prev, [courseId]: isFavorite }));
    } catch (error) {
      console.error('检查收藏状态失败:', error);
    } finally {
      setFavoritesLoading(prev => ({ ...prev, [courseId]: false }));
    }
  };

  // 处理收藏/取消收藏
  const handleToggleFavorite = async (courseId: number, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    try {
      setFavoritesLoading(prev => ({ ...prev, [courseId]: true }));
      
      if (favoriteStates[courseId]) {
        await favoriteService.removeFavorite(courseId);
        toast.success('已取消收藏');
      } else {
        await favoriteService.addFavorite(courseId);
        toast.success('收藏成功');
      }
      
      setFavoriteStates(prev => ({ ...prev, [courseId]: !prev[courseId] }));
    } catch (err: any) {
      console.error('操作收藏失败:', err);
      toast.error(favoriteStates[courseId] ? '取消收藏失败' : '收藏失败');
    } finally {
      setFavoritesLoading(prev => ({ ...prev, [courseId]: false }));
    }
  };

  // 在课程列表加载完成后检查收藏状态
  useEffect(() => {
    if (courses.length > 0) {
      courses.forEach(course => {
        checkFavoriteStatus(course.id);
      });
    }
  }, [courses]);

  // 处理过滤器变化
  const handleFilterChange = (key: keyof SearchFilters, value: any) => {
    setFilters(prev => ({ ...prev, [key]: value, page: 1 }));
  };

  // 清除所有过滤器
  const clearFilters = () => {
    setFilters({
      keyword: '',
      tagIds: [],
      priceRange: [0, 1000],
      sortBy: 'rating',
      page: 1
    });
  };

  // 处理课程卡片点击
  const handleCourseClick = (courseId: number) => {
    router.push(`/dashboard/course-detail/${courseId}`);
  };

  // 计算激活过滤器数量
  const activeFilterCount = [
    filters.categoryId !== undefined,
    filters.difficulty !== undefined,
    filters.paymentType !== undefined,
    filters.tagIds.length > 0,
    filters.priceRange[0] > 0 || filters.priceRange[1] < 1000
  ].filter(Boolean).length;

  return (
    <div className="w-full px-2 py-4">
      {/* 页面标题 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight">课程探索</h1>
        <p className="text-muted-foreground mt-1">发现适合你的优质课程，开启学习之旅</p>
      </div>

      {/* 搜索栏 - 固定在顶部 */}
      <div className="sticky top-0 z-30 pt-2 pb-4 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
          <div className="relative flex-1 w-full">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="搜索课程、技能或关键词..."
              value={filters.keyword}
              onChange={(e) => handleFilterChange('keyword', e.target.value)}
              className="pl-9 w-full"
            />
          </div>
          
          <div className="flex items-center gap-2 self-end sm:self-auto">
            <Button 
              variant="outline" 
              size="sm"
              className="whitespace-nowrap"
              onClick={() => setFilterOpen(!filterOpen)}
            >
              <Filter className="w-4 h-4 mr-2" />
              筛选
              {activeFilterCount > 0 && (
                <Badge variant="secondary" className="ml-2">{activeFilterCount}</Badge>
              )}
            </Button>
            
            <Select
              value={filters.sortBy}
              onValueChange={(value) => handleFilterChange('sortBy', value)}
            >
              <SelectTrigger className="w-40">
                <SelectValue placeholder="排序方式" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="rating">
                  <div className="flex items-center">
                    <Star className="w-4 h-4 mr-2" />
                    评分最高
                  </div>
                </SelectItem>
                <SelectItem value="students">
                  <div className="flex items-center">
                    <Users className="w-4 h-4 mr-2" />
                    学习人数最多
                  </div>
                </SelectItem>
                <SelectItem value="newest">
                  <div className="flex items-center">
                    <Clock className="w-4 h-4 mr-2" />
                    最新发布
                  </div>
                </SelectItem>
                <SelectItem value="price-asc">价格从低到高</SelectItem>
                <SelectItem value="price-desc">价格从高到低</SelectItem>
              </SelectContent>
            </Select>

            <div className="flex border rounded-md">
              <Button
                variant={viewMode === 'grid' ? 'default' : 'ghost'}
                size="icon"
                onClick={() => setViewMode('grid')}
                className="rounded-r-none h-9 w-9"
              >
                <LayoutGrid className="w-4 h-4" />
              </Button>
              <Button
                variant={viewMode === 'list' ? 'default' : 'ghost'}
                size="icon"
                onClick={() => setViewMode('list')}
                className="rounded-l-none h-9 w-9"
              >
                <List className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-[250px_1fr]">
        {/* 左侧过滤器面板 */}
        {filterOpen && (
          <div className="md:sticky top-[80px] self-start rounded-lg border bg-card text-card-foreground shadow-sm overflow-hidden h-[calc(100vh-180px)]">
            <div className="p-3 border-b flex justify-between items-center">
              <h3 className="font-semibold flex items-center text-sm">
                <Filter className="w-4 h-4 mr-2" />
                筛选选项
              </h3>
              <Button
                variant="ghost"
                size="sm"
                onClick={clearFilters}
                className="h-7 px-2"
              >
                <X className="w-3 h-3 mr-1" />
                清除
              </Button>
            </div>
            
            <ScrollArea className="h-[calc(100vh-220px)]">
              <div className="p-3 space-y-5">
                {/* 课程分类 */}
                <div className="space-y-1.5">
                  <div className="flex items-center text-xs font-medium mb-1">
                    <GraduationCap className="w-3.5 h-3.5 mr-1.5 text-muted-foreground" />
                    课程分类
                  </div>
                  <Select
                    value={filters.categoryId?.toString()}
                    onValueChange={(value) => handleFilterChange('categoryId', parseInt(value))}
                  >
                    <SelectTrigger className="h-8 text-sm">
                      <SelectValue placeholder="选择分类" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">全部分类</SelectItem>
                      {categories.map((category) => (
                        <SelectItem key={category.id} value={category.id.toString()}>
                          {category.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <Separator className="my-3" />

                {/* 难度选择 */}
                <div className="space-y-1.5">
                  <div className="flex items-center text-xs font-medium mb-1">
                    <Sparkles className="w-3.5 h-3.5 mr-1.5 text-muted-foreground" />
                    课程难度
                  </div>
                  <Tabs 
                    defaultValue="all"
                    value={filters.difficulty?.toString() || "all"}
                    onValueChange={(value) => handleFilterChange('difficulty', value === "all" ? undefined : parseInt(value))}
                    className="w-full"
                  >
                    <TabsList className="grid grid-cols-4 w-full h-7 text-xs">
                      <TabsTrigger value="all">全部</TabsTrigger>
                      <TabsTrigger value={CourseDifficulty.BEGINNER.toString()}>初级</TabsTrigger>
                      <TabsTrigger value={CourseDifficulty.INTERMEDIATE.toString()}>中级</TabsTrigger>
                      <TabsTrigger value={CourseDifficulty.ADVANCED.toString()}>高级</TabsTrigger>
                    </TabsList>
                  </Tabs>
                </div>

                <Separator className="my-3" />

                {/* 付费类型 */}
                <div className="space-y-1.5">
                  <div className="flex items-center text-xs font-medium mb-1">
                    <DollarSign className="w-3.5 h-3.5 mr-1.5 text-muted-foreground" />
                    付费类型
                  </div>
                  <Tabs 
                    defaultValue="all"
                    value={filters.paymentType?.toString() || "all"}
                    onValueChange={(value) => handleFilterChange('paymentType', value === "all" ? undefined : parseInt(value))}
                    className="w-full"
                  >
                    <TabsList className="grid grid-cols-3 w-full h-7 text-xs">
                      <TabsTrigger value="all">全部</TabsTrigger>
                      <TabsTrigger value={CoursePaymentType.FREE.toString()}>免费</TabsTrigger>
                      <TabsTrigger value={CoursePaymentType.PAID.toString()}>付费</TabsTrigger>
                    </TabsList>
                  </Tabs>
                </div>

                <Separator className="my-3" />

                {/* 价格范围滑块 */}
                <div className="space-y-2">
                  <div className="flex items-center text-xs font-medium">
                    <ArrowUpDown className="w-3.5 h-3.5 mr-1.5 text-muted-foreground" />
                    价格范围
                  </div>
                  <div className="px-1">
                    <div className="flex justify-between text-xs mb-1.5">
                      <span className="text-muted-foreground">¥{filters.priceRange[0]}</span>
                      <span className="text-muted-foreground">¥{filters.priceRange[1]}</span>
                    </div>
                    <Slider
                      value={filters.priceRange}
                      min={0}
                      max={1000}
                      step={10}
                      onValueChange={(value: [number, number]) => handleFilterChange('priceRange', value)}
                    />
                  </div>
                </div>

                <Separator className="my-3" />

                {/* 标签选择 */}
                <div className="space-y-2">
                  <div className="flex items-center text-xs font-medium">
                    <Tag className="w-3.5 h-3.5 mr-1.5 text-muted-foreground" />
                    课程标签
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {tags.slice(0, 16).map((tag) => (
                      <Badge
                        key={tag.id}
                        variant={filters.tagIds.includes(tag.id) ? 'default' : 'outline'}
                        className={cn(
                          "cursor-pointer transition-all duration-200 text-xs py-0 h-5",
                          filters.tagIds.includes(tag.id) 
                            ? "hover:bg-primary/80" 
                            : "hover:bg-secondary/80"
                        )}
                        onClick={() => {
                          const newTagIds = filters.tagIds.includes(tag.id)
                            ? filters.tagIds.filter(id => id !== tag.id)
                            : [...filters.tagIds, tag.id];
                          handleFilterChange('tagIds', newTagIds);
                        }}
                      >
                        {tag.name}
                      </Badge>
                    ))}
                    {tags.length > 16 && (
                      <Badge variant="outline" className="cursor-default text-xs py-0 h-5">
                        +{tags.length - 16} 更多
                      </Badge>
                    )}
                  </div>
                </div>
              </div>
            </ScrollArea>
          </div>
        )}

        {/* 右侧课程列表 */}
        <div className="space-y-4">
          {/* 结果统计 */}
          <div className="text-sm text-muted-foreground">
            {loading ? (
              <Skeleton className="h-4 w-40" />
            ) : (
              <>找到 <span className="font-medium text-foreground">{totalElements}</span> 个课程</>
            )}
          </div>

          {/* 课程列表 */}
          {loading ? (
            // 加载状态
            <div className={cn(
              "grid gap-4",
              viewMode === 'grid' ? 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'
            )}>
              {Array(6).fill(0).map((_, index) => (
                <Card key={index} className="overflow-hidden animate-pulse">
                  {/* 骨架屏 */}
                  <div className={cn(
                    "bg-muted",
                    viewMode === 'grid' 
                      ? 'h-36' 
                      : viewMode === 'list'
                      ? 'h-24 w-40 flex-shrink-0'
                      : 'aspect-video h-32 sm:aspect-auto sm:h-full sm:w-40 flex-shrink-0'
                  )} />
                  <CardContent className="p-3 space-y-2">
                    <div className="h-4 bg-muted rounded-md w-3/4" />
                    <div className="h-3 bg-muted rounded-md w-1/2" />
                    <div className="flex justify-between">
                      <div className="h-4 bg-muted rounded-md w-1/4" />
                      <div className="h-4 bg-muted rounded-md w-1/4" />
                    </div>
                    <div className="flex gap-2">
                      <div className="h-5 bg-muted rounded-full w-16" />
                      <div className="h-5 bg-muted rounded-full w-16" />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : courses.length > 0 ? (
            // 课程卡片列表
            <div className={cn(
              "grid gap-4",
              viewMode === 'grid' ? 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'
            )}>
              {courses.map((course) => (
                <Card 
                  key={course.id} 
                  className={cn(
                    "overflow-hidden hover:shadow-md transition-all duration-300 cursor-pointer group",
                    viewMode === 'list' ? "flex" : ""
                  )}
                  onClick={() => handleCourseClick(course.id)}
                >
                  <div className={cn(
                    "relative bg-muted overflow-hidden",
                    viewMode === 'grid' 
                      ? 'aspect-video' 
                      : 'h-24 w-40 flex-shrink-0'
                  )}>
                    {course.coverUrl ? (
                      <img
                        src={course.coverUrl}
                        alt={course.title}
                        className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-500"
                      />
                    ) : (
                      <div className="flex items-center justify-center w-full h-full">
                        <BookOpen className="w-8 h-8 text-muted-foreground/50" />
                      </div>
                    )}
                    
                    {/* 收藏按钮 */}
                    <Button
                      variant="secondary"
                      size="icon"
                      className={cn(
                        "absolute top-1 right-1 rounded-full size-6",
                        "bg-white/90 hover:bg-white shadow-sm",
                        "transition-transform duration-200 group-hover:scale-110"
                      )}
                      onClick={(e) => handleToggleFavorite(course.id, e)}
                      disabled={favoritesLoading[course.id]}
                    >
                      {favoritesLoading[course.id] ? (
                        <span className="inline-block size-3 animate-spin rounded-full border-2 border-solid border-current border-r-transparent" />
                      ) : favoriteStates[course.id] ? (
                        <Heart className="size-3 text-red-500 fill-red-500" />
                      ) : (
                        <Heart className="size-3" />
                      )}
                    </Button>
                    
                    {/* 难度标签 */}
                    {course.difficulty !== undefined && (
                      <Badge 
                        variant="secondary" 
                        className="absolute bottom-1 left-1 bg-black/60 text-white text-xs py-0 px-1.5"
                      >
                        <Sparkles className="w-3 h-3 mr-1" />
                        {course.difficulty === CourseDifficulty.BEGINNER
                          ? '初级'
                          : course.difficulty === CourseDifficulty.INTERMEDIATE
                          ? '中级'
                          : '高级'}
                      </Badge>
                    )}
                  </div>
                  
                  <div className="flex flex-col flex-1">
                    <CardContent className={cn(
                      "p-3 flex-1",
                      viewMode === 'list' ? "flex flex-row items-center gap-4" : "space-y-2"
                    )}>
                      <div className={cn(
                        viewMode === 'list' ? "flex-1 min-w-0" : ""
                      )}>
                        <h3 className="font-semibold text-base line-clamp-1 group-hover:text-primary transition-colors duration-200">
                          {course.title}
                        </h3>
                        <p className={cn(
                          "text-xs text-muted-foreground",
                          viewMode === 'list' ? "line-clamp-1 mt-0.5" : "line-clamp-2 mt-1"
                        )}>
                          {course.description || "暂无描述"}
                        </p>
                        
                        {/* 元数据标签 - 列表模式不显示 */}
                        {viewMode !== 'list' && (
                          <div className="flex items-center gap-1.5 flex-wrap mt-2">
                            {course.category && (
                              <Badge variant="outline" className="text-xs py-0 h-5">
                                <GraduationCap className="w-2.5 h-2.5 mr-1" />
                                {course.category.name}
                              </Badge>
                            )}
                            <Badge variant="outline" className="text-xs py-0 h-5">
                              <Users className="w-2.5 h-2.5 mr-1" />
                              {course.studentCount || 0}
                            </Badge>
                            <Badge variant="outline" className="text-xs py-0 h-5">
                              <Heart className="w-2.5 h-2.5 mr-1" />
                              {course.favoriteCount || 0}
                            </Badge>
                          </div>
                        )}
                      </div>
                      
                      {/* 列表模式的评分和价格 */}
                      {viewMode === 'list' && (
                        <div className="flex items-center gap-6">
                          <div className="flex items-center">
                            <Badge variant="outline" className="text-xs py-0 h-5 mr-2">
                              <Users className="w-2.5 h-2.5 mr-1" />
                              {course.studentCount || 0}
                            </Badge>
                            <Star className="w-3.5 h-3.5 text-yellow-500 fill-yellow-500" />
                            <span className="ml-1 font-medium text-sm">{course.averageRating?.toFixed(1) || '暂无'}</span>
                          </div>
                          
                          <div>
                            {course.paymentType === CoursePaymentType.FREE ? (
                              <span className="text-green-600 font-medium text-sm">免费</span>
                            ) : (
                              <div className="text-right whitespace-nowrap">
                                <span className="text-primary font-medium text-sm">
                                  ¥{course.discountPrice || course.price}
                                </span>
                                {course.discountPrice && course.price && course.discountPrice < course.price && (
                                  <span className="text-xs text-muted-foreground line-through ml-1">
                                    ¥{course.price}
                                  </span>
                                )}
                              </div>
                            )}
                          </div>
                        </div>
                      )}
                    </CardContent>
                    
                    {/* 非列表模式时显示卡片底部 */}
                    {viewMode !== 'list' && (
                      <CardFooter className="p-3 pt-0 border-t mt-auto flex justify-between items-center">
                        <div className="flex items-center">
                          <Star className="w-3.5 h-3.5 text-yellow-500 fill-yellow-500" />
                          <span className="ml-1 font-medium text-sm">{course.averageRating?.toFixed(1) || '暂无'}</span>
                          {course.ratingCount && course.ratingCount > 0 && (
                            <span className="text-xs text-muted-foreground ml-1">
                              ({course.ratingCount})
                            </span>
                          )}
                        </div>
                        
                        <div>
                          {course.paymentType === CoursePaymentType.FREE ? (
                            <span className="text-green-600 font-medium text-sm">免费</span>
                          ) : (
                            <div className="text-right">
                              <span className="text-primary font-medium text-sm">
                                ¥{course.discountPrice || course.price}
                              </span>
                              {course.discountPrice && course.price && course.discountPrice < course.price && (
                                <div className="text-xs text-muted-foreground line-through">
                                  ¥{course.price}
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      </CardFooter>
                    )}
                  </div>
                </Card>
              ))}
            </div>
          ) : (
            // 空状态
            <Card className="p-6">
              <Empty
                icon={<BookOpen className="w-10 h-10" />}
                title="未找到课程"
                description="没有找到符合条件的课程，请尝试调整搜索条件或检查筛选选项"
                action={
                  <Button onClick={clearFilters} variant="outline" className="mt-3">
                    <X className="w-3.5 h-3.5 mr-1.5" />
                    清除所有筛选
                  </Button>
                }
              />
            </Card>
          )}
          
          {/* 分页 - 将在未来添加 */}
          {!loading && courses.length > 0 && totalElements > 12 && (
            <div className="flex justify-center mt-6">
              <Button variant="outline" size="sm" disabled>
                加载更多
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
} 