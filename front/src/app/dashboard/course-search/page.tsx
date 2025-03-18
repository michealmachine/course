'use client';

import { useState, useEffect, useCallback } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Slider } from '@/components/ui/slider';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Empty } from '@/components/ui/empty';
import { Skeleton } from '@/components/ui/skeleton';
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
  HeartOff
} from 'lucide-react';
import { Course, CoursePaymentType, CourseDifficulty } from '@/types/course';
import { Category } from '@/types/course';
import { Tag } from '@/types/course';
import courseService from '@/services/course-service';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';
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
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [favoriteStates, setFavoriteStates] = useState<Record<number, boolean>>({});
  const [favoritesLoading, setFavoritesLoading] = useState<Record<number, boolean>>({});

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

  return (
    <div className="flex gap-6">
      {/* 左侧过滤器 */}
      <Card className="w-72 shrink-0 h-[calc(100vh-6rem)] sticky top-20">
        <CardHeader className="border-b">
          <CardTitle className="flex items-center gap-2 text-lg">
            <Filter className="w-5 h-5" />
            过滤器
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <ScrollArea className="h-[calc(100vh-12rem)]">
            <div className="space-y-6 p-6">
              {/* 分类选择 */}
              <div className="space-y-2">
                <label className="text-sm font-medium flex items-center gap-2">
                  <GraduationCap className="w-4 h-4" />
                  课程分类
                </label>
                <Select
                  value={filters.categoryId?.toString()}
                  onValueChange={(value) => handleFilterChange('categoryId', parseInt(value))}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="选择分类" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((category) => (
                      <SelectItem key={category.id} value={category.id.toString()}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* 难度选择 */}
              <div className="space-y-2">
                <label className="text-sm font-medium flex items-center gap-2">
                  <Sparkles className="w-4 h-4" />
                  课程难度
                </label>
                <Select
                  value={filters.difficulty?.toString()}
                  onValueChange={(value) => handleFilterChange('difficulty', parseInt(value))}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="课程难度" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={CourseDifficulty.BEGINNER.toString()}>初级</SelectItem>
                    <SelectItem value={CourseDifficulty.INTERMEDIATE.toString()}>中级</SelectItem>
                    <SelectItem value={CourseDifficulty.ADVANCED.toString()}>高级</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* 付费类型 */}
              <div className="space-y-2">
                <label className="text-sm font-medium">付费类型</label>
                <Select
                  value={filters.paymentType?.toString()}
                  onValueChange={(value) => handleFilterChange('paymentType', parseInt(value))}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="付费类型" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={CoursePaymentType.FREE.toString()}>免费</SelectItem>
                    <SelectItem value={CoursePaymentType.PAID.toString()}>付费</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* 价格范围滑块 */}
              <div className="space-y-2">
                <label className="text-sm font-medium">价格范围</label>
                <div className="flex justify-between text-sm text-muted-foreground">
                  <span>¥{filters.priceRange[0]}</span>
                  <span>¥{filters.priceRange[1]}</span>
                </div>
                <Slider
                  value={filters.priceRange}
                  min={0}
                  max={1000}
                  step={10}
                  onValueChange={(value: [number, number]) => handleFilterChange('priceRange', value)}
                  className="mt-2"
                />
              </div>

              {/* 标签选择 */}
              <div className="space-y-2">
                <label className="text-sm font-medium">课程标签</label>
                <div className="flex flex-wrap gap-2">
                  {tags.map((tag) => (
                    <Badge
                      key={tag.id}
                      variant={filters.tagIds.includes(tag.id) ? 'default' : 'outline'}
                      className="cursor-pointer hover:bg-primary/90 transition-colors"
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
                </div>
              </div>

              {/* 清除按钮 */}
              <Button
                variant="outline"
                className="w-full hover:bg-destructive hover:text-destructive-foreground transition-colors"
                onClick={clearFilters}
              >
                <X className="w-4 h-4 mr-2" />
                清除过滤器
              </Button>
            </div>
          </ScrollArea>
        </CardContent>
      </Card>

      {/* 右侧内容 */}
      <div className="flex-1 space-y-6">
        {/* 搜索和工具栏 */}
        <div className="flex items-center gap-4 sticky top-20 z-10 bg-background pb-4 border-b">
          <div className="flex-1 flex gap-2">
            <div className="relative flex-1 max-w-md">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索课程..."
                value={filters.keyword}
                onChange={(e) => handleFilterChange('keyword', e.target.value)}
                className="pl-9"
              />
            </div>
          </div>

          <div className="flex items-center gap-2">
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
                className="rounded-r-none"
              >
                <LayoutGrid className="w-4 h-4" />
              </Button>
              <Button
                variant={viewMode === 'list' ? 'default' : 'ghost'}
                size="icon"
                onClick={() => setViewMode('list')}
                className="rounded-l-none"
              >
                <List className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </div>

        {/* 课程列表 */}
        {loading ? (
          // 加载状态
          <div className={cn(
            "grid gap-6",
            viewMode === 'grid' ? 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'
          )}>
            {Array(6).fill(0).map((_, index) => (
              <Card key={index} className={cn(
                "overflow-hidden hover:shadow-lg transition-shadow",
                viewMode === 'list' && "flex"
              )}>
                <div className={cn(
                  "bg-muted animate-pulse",
                  viewMode === 'grid' ? 'h-48' : 'h-32 w-48'
                )} />
                <CardContent className={cn(
                  "space-y-3",
                  viewMode === 'grid' ? 'p-4' : 'flex-1 p-4'
                )}>
                  <div className="h-4 bg-muted rounded w-3/4 animate-pulse" />
                  <div className="h-4 bg-muted rounded w-1/2 animate-pulse" />
                  <div className="flex justify-between">
                    <div className="h-4 bg-muted rounded w-1/4 animate-pulse" />
                    <div className="h-4 bg-muted rounded w-1/4 animate-pulse" />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : courses.length > 0 ? (
          // 课程卡片
          <div className={cn(
            "grid gap-6",
            viewMode === 'grid' ? 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'
          )}>
            {courses.map((course) => (
              <Card 
                key={course.id} 
                className={cn(
                  "overflow-hidden hover:shadow-lg transition-shadow cursor-pointer group",
                  viewMode === 'list' && "flex"
                )}
                onClick={() => handleCourseClick(course.id)}
              >
                {course.coverUrl && (
                  <div className={cn(
                    "relative overflow-hidden",
                    viewMode === 'grid' ? 'h-48' : 'h-32 w-48'
                  )}>
                    <img
                      src={course.coverUrl}
                      alt={course.title}
                      className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                    />
                    {/* 收藏按钮 */}
                    <Button
                      variant="ghost"
                      size="icon"
                      className="absolute top-2 right-2 bg-white/80 hover:bg-white/90 rounded-full w-8 h-8 p-1.5"
                      onClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        handleToggleFavorite(course.id, e);
                      }}
                      disabled={favoritesLoading[course.id]}
                    >
                      {favoritesLoading[course.id] ? (
                        <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-solid border-current border-r-transparent" />
                      ) : favoriteStates[course.id] ? (
                        <Heart className="h-4 w-4 text-red-500 fill-red-500" />
                      ) : (
                        <Heart className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                )}
                <CardContent className={cn(
                  "space-y-3",
                  viewMode === 'grid' ? 'p-4' : 'flex-1 p-4'
                )}>
                  <h3 className="font-semibold text-lg line-clamp-1 group-hover:text-primary transition-colors">
                    {course.title}
                  </h3>
                  <p className="text-sm text-muted-foreground line-clamp-2">
                    {course.description}
                  </p>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-1">
                      <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                      <span className="font-medium">{course.averageRating?.toFixed(1) || '暂无评分'}</span>
                      <span className="text-sm text-muted-foreground">
                        ({course.ratingCount || 0})
                      </span>
                    </div>
                    <div>
                      {course.paymentType === CoursePaymentType.FREE ? (
                        <span className="text-green-600 font-medium">免费</span>
                      ) : (
                        <div className="text-right">
                          <span className="text-primary font-medium text-lg">
                            ¥{course.discountPrice || course.price}
                          </span>
                          {course.discountPrice && course.price && (
                            <div className="text-sm text-muted-foreground line-through">
                              ¥{course.price}
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2 flex-wrap">
                    <Badge variant="secondary" className="text-xs">
                      <Users className="w-3 h-3 mr-1" />
                      {course.studentCount || 0} 人学习
                    </Badge>
                    {course.difficulty && (
                      <Badge variant="outline" className="text-xs">
                        <Sparkles className="w-3 h-3 mr-1" />
                        {course.difficulty === CourseDifficulty.BEGINNER
                          ? '初级'
                          : course.difficulty === CourseDifficulty.INTERMEDIATE
                          ? '中级'
                          : '高级'}
                      </Badge>
                    )}
                    {course.category && (
                      <Badge variant="outline" className="text-xs">
                        <GraduationCap className="w-3 h-3 mr-1" />
                        {course.category.name}
                      </Badge>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          // 空状态
          <Empty
            icon={<BookOpen className="w-12 h-12" />}
            title="暂无课程"
            description="没有找到符合条件的课程，请尝试调整搜索条件"
          />
        )}
      </div>
    </div>
  );
} 