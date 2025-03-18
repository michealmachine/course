'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Separator } from '@/components/ui/separator';
import { Star, Heart, HeartOff, BookOpen, Building, Tag, Clock, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';
import favoriteService, { UserFavoriteVO } from '@/services/favorite-service';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { Page } from '@/types/api';

export default function FavoritesPage() {
  const router = useRouter();
  const [favorites, setFavorites] = useState<UserFavoriteVO[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(9);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 加载收藏列表
  useEffect(() => {
    const loadFavorites = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const data = await favoriteService.getFavorites(currentPage, pageSize);
        
        // 添加空值检查
        if (data && data.content) {
          setFavorites(data.content);
          setTotalPages(data.totalPages);
          setTotalElements(data.totalElements);
        } else {
          // 处理空数据情况
          setFavorites([]);
          setTotalPages(0);
          setTotalElements(0);
          console.warn('获取到的收藏数据为空');
        }
      } catch (err: any) {
        console.error('加载收藏列表失败:', err);
        setError(err.message || '加载收藏列表失败');
        toast.error('加载收藏列表失败');
        
        // 重置数据
        setFavorites([]);
        setTotalPages(0);
        setTotalElements(0);
      } finally {
        setIsLoading(false);
      }
    };

    loadFavorites();
  }, [currentPage, pageSize]);

  // 处理页码变更
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  // 处理取消收藏
  const handleRemoveFavorite = async (courseId: number, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    try {
      await favoriteService.removeFavorite(courseId);
      setFavorites(prevFavorites => prevFavorites.filter(f => f.courseId !== courseId));
      toast.success('已取消收藏');
      
      // 如果当前页数据为空且不是第一页，则返回上一页
      if (favorites.length === 1 && currentPage > 0) {
        setCurrentPage(currentPage - 1);
      }
    } catch (err: any) {
      console.error('取消收藏失败:', err);
      toast.error('取消收藏失败');
    }
  };

  // 处理课程点击
  const handleCourseClick = (courseId: number) => {
    router.push(`/dashboard/course-detail/${courseId}`);
  };

  // 格式化日期
  const formatDateTime = (dateString: string) => {
    try {
      return format(new Date(dateString), 'yyyy-MM-dd HH:mm', { locale: zhCN });
    } catch (e) {
      return dateString;
    }
  };

  // 课程卡片骨架屏
  const renderSkeletons = () => {
    return Array(9)
      .fill(0)
      .map((_, i) => (
        <Card key={i} className="overflow-hidden">
          <div className="relative h-48">
            <Skeleton className="h-full w-full" />
          </div>
          <CardContent className="p-4 space-y-3">
            <Skeleton className="h-6 w-3/4" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-2/3" />
            <div className="flex justify-between">
              <Skeleton className="h-4 w-16" />
              <Skeleton className="h-4 w-12" />
            </div>
          </CardContent>
        </Card>
      ));
  };

  // 渲染收藏列表
  const renderFavorites = () => {
    if (isLoading) {
      return renderSkeletons();
    }

    if (favorites.length === 0) {
      return (
        <div className="col-span-full flex flex-col items-center justify-center py-12">
          <Heart className="h-12 w-12 text-muted-foreground mb-4 stroke-[1.25px]" />
          <h3 className="text-xl font-semibold mb-2">暂无收藏课程</h3>
          <p className="text-muted-foreground mb-4">您还没有收藏任何课程</p>
          <Button onClick={() => router.push('/dashboard/course-search')}>
            浏览课程
          </Button>
        </div>
      );
    }

    return favorites.map((favorite) => (
      <Card
        key={favorite.id}
        className="overflow-hidden hover:shadow-lg transition-shadow cursor-pointer group"
        onClick={() => handleCourseClick(favorite.courseId)}
      >
        <div className="relative h-48 bg-slate-100">
          {favorite.courseCoverImage ? (
            <img
              src={favorite.courseCoverImage}
              alt={favorite.courseTitle}
              className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
          ) : (
            <div className="absolute inset-0 flex items-center justify-center bg-slate-200">
              <BookOpen className="h-12 w-12 text-slate-400" />
            </div>
          )}
          <Button
            variant="ghost"
            size="icon"
            className="absolute top-2 right-2 bg-white/80 hover:bg-white/90 rounded-full w-8 h-8 p-1.5"
            onClick={(e) => handleRemoveFavorite(favorite.courseId, e)}
          >
            <HeartOff className="h-4 w-4 text-red-500" />
          </Button>
        </div>
        <CardContent className="p-4 space-y-3">
          <h3 className="font-semibold text-lg line-clamp-1 group-hover:text-primary transition-colors">
            {favorite.courseTitle}
          </h3>
          
          <div className="flex flex-wrap gap-2 text-xs">
            {favorite.categoryName && (
              <Badge variant="outline" className="flex items-center gap-1 font-normal">
                <Tag className="h-3 w-3" />
                {favorite.categoryName}
              </Badge>
            )}
            
            {favorite.institutionName && (
              <Badge variant="outline" className="flex items-center gap-1 font-normal">
                <Building className="h-3 w-3" />
                {favorite.institutionName}
              </Badge>
            )}
            
            <Badge variant="outline" className="flex items-center gap-1 font-normal">
              <Clock className="h-3 w-3" />
              {formatDateTime(favorite.favoriteTime)}
            </Badge>
          </div>
          
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <Heart className="h-4 w-4 text-red-500 fill-red-500 mr-1" />
              <span className="text-sm">已收藏</span>
            </div>
            
            <div>
              <span className={`${favorite.coursePrice === '免费' ? 'text-green-600' : 'text-primary'} font-medium`}>
                {favorite.coursePrice}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>
    ));
  };

  // 渲染分页
  const renderPagination = () => {
    if (totalPages <= 1) return null;

    const getPaginationItems = () => {
      let items = [];
      
      // 前一页按钮
      items.push(
        <PaginationItem key="prev">
          <PaginationPrevious
            onClick={() => currentPage > 0 && handlePageChange(currentPage - 1)}
            className={currentPage === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
          />
        </PaginationItem>
      );
      
      // 页码按钮
      const maxVisiblePages = 5;
      const halfVisible = Math.floor(maxVisiblePages / 2);
      
      let startPage = Math.max(0, currentPage - halfVisible);
      let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
      
      if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
      }
      
      // 第一页
      if (startPage > 0) {
        items.push(
          <PaginationItem key={0}>
            <PaginationLink
              onClick={() => handlePageChange(0)}
              isActive={currentPage === 0}
            >
              1
            </PaginationLink>
          </PaginationItem>
        );
        
        if (startPage > 1) {
          items.push(
            <PaginationItem key="ellipsis-start">
              <PaginationEllipsis />
            </PaginationItem>
          );
        }
      }
      
      // 中间页码
      for (let i = startPage; i <= endPage; i++) {
        items.push(
          <PaginationItem key={i}>
            <PaginationLink
              onClick={() => handlePageChange(i)}
              isActive={currentPage === i}
            >
              {i + 1}
            </PaginationLink>
          </PaginationItem>
        );
      }
      
      // 最后一页
      if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
          items.push(
            <PaginationItem key="ellipsis-end">
              <PaginationEllipsis />
            </PaginationItem>
          );
        }
        
        items.push(
          <PaginationItem key={totalPages - 1}>
            <PaginationLink
              onClick={() => handlePageChange(totalPages - 1)}
              isActive={currentPage === totalPages - 1}
            >
              {totalPages}
            </PaginationLink>
          </PaginationItem>
        );
      }
      
      // 下一页按钮
      items.push(
        <PaginationItem key="next">
          <PaginationNext
            onClick={() => currentPage < totalPages - 1 && handlePageChange(currentPage + 1)}
            className={currentPage === totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
          />
        </PaginationItem>
      );
      
      return items;
    };

    return (
      <Pagination>
        <PaginationContent>{getPaginationItems()}</PaginationContent>
      </Pagination>
    );
  };

  if (error) {
    return (
      <div className="container py-6">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>加载失败</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="container py-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">我的收藏</h1>
          <p className="text-muted-foreground mt-1">
            共 {totalElements} 个收藏课程
          </p>
        </div>
        
        <Button
          onClick={() => router.push('/dashboard/course-search')}
          variant="outline"
        >
          浏览更多课程
        </Button>
      </div>
      
      <Separator />
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {renderFavorites()}
      </div>
      
      <div className="mt-8 flex justify-center">
        {renderPagination()}
      </div>
    </div>
  );
} 