'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { 
  Card, 
  CardContent,
  CardHeader,
  CardTitle 
} from '@/components/ui/card';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table';
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue, 
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import { 
  Pagination, 
  PaginationContent, 
  PaginationEllipsis, 
  PaginationItem, 
  PaginationLink, 
  PaginationNext, 
  PaginationPrevious 
} from '@/components/ui/pagination';
import { toast } from 'sonner';
import { 
  Check, 
  X, 
  Trash, 
  Edit, 
  ChevronRight, 
  AlertCircle,
  BookOpen
} from 'lucide-react';
import { formatDate } from '@/lib/utils';
import { wrongQuestionService } from '@/services';
import { UserWrongQuestionVO, WrongQuestionStatus } from '@/types/wrongQuestion';

interface WrongQuestionListProps {
  courseId?: number; // 如果提供courseId，则只显示该课程的错题
  showCourseColumn?: boolean; // 是否显示课程列
}

export default function WrongQuestionList({ 
  courseId, 
  showCourseColumn = true 
}: WrongQuestionListProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [currentTab, setCurrentTab] = useState('all'); // 'all' | 'unresolved'
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [wrongQuestions, setWrongQuestions] = useState<UserWrongQuestionVO[]>([]);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  // 加载错题数据
  const loadWrongQuestions = async () => {
    setLoading(true);
    try {
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        direction: sortDirection
      };

      let result;
      if (currentTab === 'all') {
        if (courseId) {
          result = await wrongQuestionService.getCourseWrongQuestions(courseId, params);
        } else {
          result = await wrongQuestionService.getWrongQuestions(params);
        }
      } else {
        result = await wrongQuestionService.getUnresolvedWrongQuestions(params);
      }

      setWrongQuestions(result.content);
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
    } catch (error) {
      toast.error('加载错题列表失败');
      console.error('加载错题列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 当依赖项变化时重新加载数据
  useEffect(() => {
    loadWrongQuestions();
  }, [currentTab, currentPage, pageSize, sortBy, sortDirection, courseId]);

  // 标记为已解决
  const handleResolve = async (wrongQuestionId: number) => {
    try {
      await wrongQuestionService.resolveWrongQuestion(wrongQuestionId);
      toast.success('已标记为已解决');
      loadWrongQuestions();
    } catch (error) {
      toast.error('操作失败');
      console.error('标记为已解决失败:', error);
    }
  };

  // 删除错题
  const handleDelete = async (wrongQuestionId: number) => {
    if (!window.confirm('确定要删除这道错题吗？')) return;
    
    try {
      await wrongQuestionService.deleteWrongQuestion(wrongQuestionId);
      toast.success('错题已删除');
      loadWrongQuestions();
    } catch (error) {
      toast.error('删除失败');
      console.error('删除错题失败:', error);
    }
  };

  // 清空所有错题
  const handleClearAll = async () => {
    if (!window.confirm('确定要清空所有错题吗？这个操作不可撤销。')) return;
    
    try {
      if (courseId) {
        await wrongQuestionService.deleteAllCourseWrongQuestions(courseId);
      } else {
        await wrongQuestionService.deleteAllWrongQuestions();
      }
      toast.success('所有错题已清空');
      loadWrongQuestions();
    } catch (error) {
      toast.error('清空错题失败');
      console.error('清空错题失败:', error);
    }
  };

  // 生成分页组件
  const renderPagination = () => {
    const pages = [];
    const maxVisiblePages = 5;
    
    // 显示当前页码附近的页码
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
    
    // 如果结束页码小于总页数，调整开始页码
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <PaginationItem key={i}>
          <PaginationLink
            isActive={i === currentPage}
            onClick={() => setCurrentPage(i)}
          >
            {i + 1}
          </PaginationLink>
        </PaginationItem>
      );
    }
    
    return (
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            {currentPage === 0 ? (
              <PaginationPrevious className="pointer-events-none opacity-50" />
            ) : (
              <PaginationPrevious onClick={() => setCurrentPage(Math.max(0, currentPage - 1))} />
            )}
          </PaginationItem>
          
          {startPage > 0 && (
            <>
              <PaginationItem>
                <PaginationLink onClick={() => setCurrentPage(0)}>1</PaginationLink>
              </PaginationItem>
              {startPage > 1 && <PaginationEllipsis />}
            </>
          )}
          
          {pages}
          
          {endPage < totalPages - 1 && (
            <>
              {endPage < totalPages - 2 && <PaginationEllipsis />}
              <PaginationItem>
                <PaginationLink onClick={() => setCurrentPage(totalPages - 1)}>
                  {totalPages}
                </PaginationLink>
              </PaginationItem>
            </>
          )}
          
          <PaginationItem>
            {currentPage === totalPages - 1 || totalPages === 0 ? (
              <PaginationNext className="pointer-events-none opacity-50" />
            ) : (
              <PaginationNext onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))} />
            )}
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );
  };

  // 查看题目详情
  const handleViewQuestion = (sectionId: number, questionId: number) => {
    // 导航到题目页面
    router.push(`/dashboard/learn/section/${sectionId}/question/${questionId}`);
  };

  if (loading && wrongQuestions.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>错题本</CardTitle>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">排序:</span>
            <Select 
              value={sortBy} 
              onValueChange={setSortBy}
            >
              <SelectTrigger className="w-[140px]">
                <SelectValue placeholder="排序方式" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="createdAt">错误时间</SelectItem>
                <SelectItem value="updatedAt">更新时间</SelectItem>
                <SelectItem value="questionTitle">题目名称</SelectItem>
              </SelectContent>
            </Select>
            <Select 
              value={sortDirection} 
              onValueChange={(value) => setSortDirection(value as 'asc' | 'desc')}
            >
              <SelectTrigger className="w-[80px]">
                <SelectValue placeholder="排序" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="asc">升序</SelectItem>
                <SelectItem value="desc">降序</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <Button 
            variant="destructive" 
            size="sm"
            onClick={handleClearAll}
          >
            清空错题
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <Tabs 
          defaultValue="all" 
          className="w-full"
          value={currentTab}
          onValueChange={setCurrentTab}
        >
          <TabsList className="mb-4">
            <TabsTrigger value="all">全部错题</TabsTrigger>
            <TabsTrigger value="unresolved">未解决</TabsTrigger>
          </TabsList>
          
          <TabsContent value="all" className="mt-0">
            {wrongQuestions.length === 0 ? (
              <div className="p-8 text-center text-muted-foreground">
                暂无错题记录
              </div>
            ) : (
              <div className="overflow-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      {showCourseColumn && <TableHead>课程</TableHead>}
                      <TableHead>题目</TableHead>
                      <TableHead>类型</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>错误时间</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {wrongQuestions.map((question) => (
                      <TableRow key={question.id}>
                        {showCourseColumn && (
                          <TableCell>
                            <div className="font-medium max-w-[200px] truncate">
                              {question.courseTitle}
                            </div>
                          </TableCell>
                        )}
                        <TableCell>
                          <div className="font-medium max-w-[300px] truncate">
                            {question.questionTitle}
                          </div>
                        </TableCell>
                        <TableCell>
                          {question.questionType === 'SINGLE_CHOICE' && '单选题'}
                          {question.questionType === 'MULTIPLE_CHOICE' && '多选题'}
                          {question.questionType === 'TRUE_FALSE' && '判断题'}
                          {question.questionType === 'FILL_BLANK' && '填空题'}
                          {question.questionType === 'SHORT_ANSWER' && '简答题'}
                        </TableCell>
                        <TableCell>
                          {question.status === WrongQuestionStatus.RESOLVED ? (
                            <span className="flex items-center text-success">
                              <Check className="mr-1 h-4 w-4" /> 已解决
                            </span>
                          ) : (
                            <span className="flex items-center text-destructive">
                              <AlertCircle className="mr-1 h-4 w-4" /> 未解决
                            </span>
                          )}
                        </TableCell>
                        <TableCell>
                          {formatDate(question.createdAt)}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end space-x-2">
                            <Button 
                              variant="ghost" 
                              size="icon"
                              onClick={() => handleViewQuestion(question.sectionId, question.questionId)}
                              title="查看题目"
                            >
                              <BookOpen className="h-4 w-4" />
                            </Button>
                            
                            {question.status === WrongQuestionStatus.UNRESOLVED && (
                              <Button 
                                variant="ghost" 
                                size="icon"
                                onClick={() => handleResolve(question.id)}
                                title="标记为已解决"
                              >
                                <Check className="h-4 w-4" />
                              </Button>
                            )}
                            
                            <Button 
                              variant="ghost" 
                              size="icon"
                              onClick={() => handleDelete(question.id)}
                              title="删除"
                            >
                              <Trash className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                
                <div className="flex items-center justify-between mt-4">
                  <div className="text-sm text-muted-foreground">
                    共 {totalElements} 条记录，每页 
                    <Select 
                      value={pageSize.toString()} 
                      onValueChange={(value) => {
                        setPageSize(parseInt(value));
                        setCurrentPage(0);
                      }}
                    >
                      <SelectTrigger className="w-[60px] h-8 mx-1">
                        <SelectValue placeholder="10" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="5">5</SelectItem>
                        <SelectItem value="10">10</SelectItem>
                        <SelectItem value="20">20</SelectItem>
                        <SelectItem value="50">50</SelectItem>
                      </SelectContent>
                    </Select>
                    条
                  </div>
                  
                  {renderPagination()}
                </div>
              </div>
            )}
          </TabsContent>
          
          <TabsContent value="unresolved" className="mt-0">
            {wrongQuestions.length === 0 ? (
              <div className="p-8 text-center text-muted-foreground">
                暂无未解决的错题
              </div>
            ) : (
              <div className="overflow-auto">
                {/* 与全部错题相同的表格结构，但这里只展示未解决的错题 */}
                <Table>
                  <TableHeader>
                    <TableRow>
                      {showCourseColumn && <TableHead>课程</TableHead>}
                      <TableHead>题目</TableHead>
                      <TableHead>类型</TableHead>
                      <TableHead>错误时间</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {wrongQuestions.map((question) => (
                      <TableRow key={question.id}>
                        {showCourseColumn && (
                          <TableCell>
                            <div className="font-medium max-w-[200px] truncate">
                              {question.courseTitle}
                            </div>
                          </TableCell>
                        )}
                        <TableCell>
                          <div className="font-medium max-w-[300px] truncate">
                            {question.questionTitle}
                          </div>
                        </TableCell>
                        <TableCell>
                          {question.questionType === 'SINGLE_CHOICE' && '单选题'}
                          {question.questionType === 'MULTIPLE_CHOICE' && '多选题'}
                          {question.questionType === 'TRUE_FALSE' && '判断题'}
                          {question.questionType === 'FILL_BLANK' && '填空题'}
                          {question.questionType === 'SHORT_ANSWER' && '简答题'}
                        </TableCell>
                        <TableCell>
                          {formatDate(question.createdAt)}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end space-x-2">
                            <Button 
                              variant="ghost" 
                              size="icon"
                              onClick={() => handleViewQuestion(question.sectionId, question.questionId)}
                              title="查看题目"
                            >
                              <BookOpen className="h-4 w-4" />
                            </Button>
                            
                            <Button 
                              variant="ghost" 
                              size="icon"
                              onClick={() => handleResolve(question.id)}
                              title="标记为已解决"
                            >
                              <Check className="h-4 w-4" />
                            </Button>
                            
                            <Button 
                              variant="ghost" 
                              size="icon"
                              onClick={() => handleDelete(question.id)}
                              title="删除"
                            >
                              <Trash className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                
                <div className="flex items-center justify-between mt-4">
                  <div className="text-sm text-muted-foreground">
                    共 {totalElements} 条记录，每页 
                    <Select 
                      value={pageSize.toString()} 
                      onValueChange={(value) => {
                        setPageSize(parseInt(value));
                        setCurrentPage(0);
                      }}
                    >
                      <SelectTrigger className="w-[60px] h-8 mx-1">
                        <SelectValue placeholder="10" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="5">5</SelectItem>
                        <SelectItem value="10">10</SelectItem>
                        <SelectItem value="20">20</SelectItem>
                        <SelectItem value="50">50</SelectItem>
                      </SelectContent>
                    </Select>
                    条
                  </div>
                  
                  {renderPagination()}
                </div>
              </div>
            )}
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
} 