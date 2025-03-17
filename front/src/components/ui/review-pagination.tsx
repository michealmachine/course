import { ChevronLeft, ChevronRight, MoreHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";

interface ReviewPaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  siblings?: number;
}

export function ReviewPagination({
  currentPage,
  totalPages,
  onPageChange,
  siblings = 1
}: ReviewPaginationProps) {
  // 生成页码数组
  const generatePagination = () => {
    // 如果总页数小于7，直接显示所有页码
    if (totalPages <= 7) {
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    }

    // 计算左右边界
    const leftSiblingIndex = Math.max(currentPage - siblings, 1);
    const rightSiblingIndex = Math.min(currentPage + siblings, totalPages);

    // 是否显示边界省略号
    const shouldShowLeftDots = leftSiblingIndex > 2;
    const shouldShowRightDots = rightSiblingIndex < totalPages - 1;

    // 始终显示第一页和最后一页
    const firstPageIndex = 1;
    const lastPageIndex = totalPages;

    // 只显示左边省略号
    if (!shouldShowLeftDots && shouldShowRightDots) {
      const leftItemCount = 3 + 2 * siblings;
      const leftRange = Array.from(
        { length: leftItemCount },
        (_, i) => i + 1
      );

      return [...leftRange, -1, lastPageIndex];
    }

    // 只显示右边省略号
    if (shouldShowLeftDots && !shouldShowRightDots) {
      const rightItemCount = 3 + 2 * siblings;
      const rightRange = Array.from(
        { length: rightItemCount },
        (_, i) => totalPages - rightItemCount + i + 1
      );

      return [firstPageIndex, -1, ...rightRange];
    }

    // 显示两边省略号
    if (shouldShowLeftDots && shouldShowRightDots) {
      const middleRange = Array.from(
        { length: rightSiblingIndex - leftSiblingIndex + 1 },
        (_, i) => leftSiblingIndex + i
      );

      return [firstPageIndex, -1, ...middleRange, -1, lastPageIndex];
    }

    return [];
  };

  const pages = generatePagination();

  // 处理页面切换
  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      onPageChange(page);
    }
  };

  // 渲染省略号或页码按钮
  const renderPaginationItem = (page: number, index: number) => {
    // 渲染省略号
    if (page === -1) {
      return (
        <span
          key={`ellipsis-${index}`}
          className="mx-1 flex h-9 w-9 items-center justify-center text-sm text-muted-foreground"
        >
          <MoreHorizontal className="h-4 w-4" />
        </span>
      );
    }

    // 渲染页码按钮
    return (
      <Button
        key={page}
        variant={currentPage === page ? "default" : "outline"}
        size="icon"
        className="h-9 w-9"
        onClick={() => handlePageChange(page)}
        aria-current={currentPage === page ? "page" : undefined}
      >
        {page}
      </Button>
    );
  };

  // 如果只有一页则不显示
  if (totalPages <= 1) {
    return null;
  }

  return (
    <nav className="flex items-center space-x-1" aria-label="Pagination">
      <Button
        variant="outline"
        size="icon"
        className="h-9 w-9"
        onClick={() => handlePageChange(currentPage - 1)}
        disabled={currentPage === 1}
      >
        <ChevronLeft className="h-4 w-4" />
        <span className="sr-only">上一页</span>
      </Button>

      <div className="flex items-center">
        {pages.map(renderPaginationItem)}
      </div>

      <Button
        variant="outline"
        size="icon"
        className="h-9 w-9"
        onClick={() => handlePageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
      >
        <ChevronRight className="h-4 w-4" />
        <span className="sr-only">下一页</span>
      </Button>
    </nav>
  );
} 