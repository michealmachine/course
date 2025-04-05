'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  Database,
  HardDrive,
  PieChart as PieChartIcon,
  BarChart,
  Building,
  RefreshCw,
  FileText,
  Users,
  Gauge,
  Calendar,
  Image as ImageIcon,
  Film,
  Search,
  FileX,
  Loader2,
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { DatePicker } from '@/components/ui/date-picker';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

// 导入 recharts 组件
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  BarChart as RechartBarChart,
  Bar,
} from 'recharts';

// 导入 date-fns 用于日期处理
import { format, parseISO, addDays, subDays, isSameDay } from 'date-fns';

// 导入 shadcn 图表组件
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from "@/components/ui/chart";
import { type ChartConfig } from "@/components/ui/chart";

// 导入服务和类型
import adminQuotaService from '@/services/admin-quota-service';
import quotaApplicationService from '@/services/quota-application';
import { adminMediaActivityService } from '@/services/media-activity-service';
import { formatBytes, formatPercentage } from '@/lib/utils';
import { 
  InstitutionQuotaStatsVO, 
  InstitutionQuotaVO,
  InstitutionQuotaDistributionVO,
  TotalQuotaUsageVO,
  QuotaApplicationStatus,
  QuotaApplicationVO
} from '@/types/quota';
import { MediaActivityCalendarVO, MediaActivityDTO } from '@/types/media-activity';
import { MediaVO, MediaType, MediaTypeDistributionVO, TypeDistribution, AdminMediaVO } from '@/types/media';
import { AdvancedMediaQueryParams, Page } from '@/services/media-service';
import { mediaService } from '@/services/media-service';

// 获取饼图配置
const getPieChartConfig = (institutions: InstitutionQuotaDistributionVO[]): ChartConfig => {
  const chartConfig: ChartConfig = {};
  
  // 配色方案
  const colors = [
    "#4f46e5", // 靛蓝色
    "#10b981", // 翠绿色 
    "#f59e0b", // 琥珀色
    "#8b5cf6", // 紫色
    "#ef4444", // 红色
    "#0ea5e9", // 天蓝色
    "#ec4899", // 粉色
    "#14b8a6", // 蓝绿色
    "#f97316", // 橙色
    "#6366f1"  // 靛青色
  ];
  
  institutions.forEach((item, index) => {
    const key = `inst_${item.institutionId}`;
    chartConfig[key] = {
      label: item.institutionName,
      color: colors[index % colors.length],
    };
  });
  
  return chartConfig;
};

// 获取柱状图配置
const getBarChartConfig = (): ChartConfig => {
  return {
    usedQuota: {
      label: "已用配额",
      color: "hsl(var(--primary))"
    },
    totalQuota: {
      label: "总配额",
      color: "hsl(var(--muted-foreground))"
    }
  };
};

// 热图颜色计算
const getHeatColor = (count: number, maxCount: number): string => {
  if (count === 0) return 'bg-gray-100';
  
  const intensity = Math.min(Math.ceil((count / maxCount) * 5), 5);
  
  switch (intensity) {
    case 1: return 'bg-blue-100';
    case 2: return 'bg-blue-200';
    case 3: return 'bg-blue-300';
    case 4: return 'bg-blue-400';
    case 5: return 'bg-blue-500';
    default: return 'bg-gray-100';
  }
};

// 活动热图组件
const ActivityHeatmap = ({ 
  calendarData, 
  startDate, 
  endDate,
  selectedDate,
  onDateSelect
}: {
  calendarData: MediaActivityCalendarVO | null;
  startDate?: Date;
  endDate?: Date;
  selectedDate?: Date;
  onDateSelect: (date: Date) => void;
}) => {
  if (!calendarData || !calendarData.calendarData || calendarData.calendarData.length === 0) {
    return (
      <div className="p-4 flex flex-col items-center justify-center h-[300px] space-y-4">
        <Calendar className="h-12 w-12 text-muted-foreground opacity-20" />
        <p className="text-muted-foreground">该时间段内没有媒体活动数据</p>
      </div>
    );
  }

  // 创建日期映射
  const dateMap = new Map<string, MediaActivityDTO>();
  calendarData.calendarData.forEach(item => {
    dateMap.set(item.date, item);
  });

  // 生成日期区间内的所有日期
  const dateRange: Date[] = [];
  if (startDate && endDate) {
    let currentDate = new Date(startDate);
    while (currentDate <= endDate) {
      dateRange.push(new Date(currentDate));
      currentDate.setDate(currentDate.getDate() + 1);
    }
  }

  // 计算列和行
  const daysPerRow = 7;
  const totalRows = Math.ceil(dateRange.length / daysPerRow);
  const rows = Array.from({ length: totalRows }, (_, rowIndex) => {
    return dateRange.slice(rowIndex * daysPerRow, (rowIndex + 1) * daysPerRow);
  });

  // 获取一周的星期名称
  const weekDays = ["日", "一", "二", "三", "四", "五", "六"];

  return (
    <div className="p-4">
      <div className="mb-4">
        <p className="text-sm font-medium mb-2">活跃程度</p>
        <div className="flex items-center gap-1">
          {[0, 1, 2, 3, 4, 5].map(level => (
            <div 
              key={level} 
              className={`flex items-center justify-center w-8 h-8 rounded ${level === 0 ? 'bg-gray-100' : `bg-blue-${level * 100}`}`} 
              title={level === 0 ? '无活动' : `活动等级 ${level}`}
            >
              <span className="text-xs">{level === 0 ? '0' : level}</span>
            </div>
          ))}
        </div>
      </div>
      
      {/* 添加星期头部 */}
      <div className="flex gap-1 mb-1">
        {weekDays.map((day, index) => (
          <div 
            key={index} 
            className="w-10 h-6 flex items-center justify-center text-xs font-medium text-muted-foreground"
          >
            {day}
          </div>
        ))}
      </div>
      
      <div className="grid gap-1">
        {rows.map((week, weekIndex) => (
          <div key={weekIndex} className="flex gap-1">
            {week.map(date => {
              const dateStr = format(date, 'yyyy-MM-dd');
              const activity = dateMap.get(dateStr);
              const count = activity?.count || 0;
              const isSelectedDate = selectedDate && isSameDay(date, selectedDate);
              const isToday = isSameDay(date, new Date());
              const dayColor = getHeatColor(count, calendarData.peakCount);
              
              return (
                <div 
                  key={dateStr}
                  className={`
                    w-10 h-10 rounded flex flex-col items-center justify-center cursor-pointer
                    ${dayColor}
                    ${isSelectedDate ? 'ring-2 ring-primary' : ''}
                    ${isToday ? 'ring-1 ring-blue-400' : ''}
                    hover:opacity-80 transition-opacity relative
                  `}
                  onClick={() => onDateSelect(date)}
                  title={`${dateStr}: ${count} 个媒体活动${activity ? `, 总大小: ${formatBytes(activity.totalSize || 0)}` : ''}`}
                >
                  <span className={`text-xs ${isToday ? 'font-bold' : 'font-medium'}`}>
                    {date.getDate()}
                  </span>
                  {count > 0 && (
                    <span className="text-[10px]">{count}</span>
                  )}
                  {isToday && (
                    <span className="absolute -top-1 -right-1 w-2 h-2 bg-blue-500 rounded-full"></span>
                  )}
                </div>
              );
            })}
          </div>
        ))}
      </div>
      
      {/* 添加底部摘要 */}
      {selectedDate && (
        <div className="mt-4 p-3 bg-muted rounded-md">
          <h4 className="text-sm font-medium mb-2">选中日期: {format(selectedDate, 'yyyy-MM-dd')}</h4>
          <div className="grid grid-cols-2 gap-2 text-sm">
            {(() => {
              const selectedDateStr = format(selectedDate, 'yyyy-MM-dd');
              const selectedActivity = dateMap.get(selectedDateStr);
              
              if (!selectedActivity) {
                return <p className="text-muted-foreground col-span-2">该日期没有媒体活动</p>;
              }
              
              return (
                <>
                  <div>
                    <span className="text-muted-foreground">上传文件数:</span>{' '}
                    <span className="font-medium">{selectedActivity.count}</span>
                  </div>
                  <div>
                    <span className="text-muted-foreground">上传总大小:</span>{' '}
                    <span className="font-medium">{formatBytes(selectedActivity.totalSize || 0)}</span>
                  </div>
                </>
              );
            })()}
          </div>
        </div>
      )}
    </div>
  );
};

// 获取媒体类型图标
const getMediaTypeIcon = (mediaType: string) => {
  switch (mediaType) {
    case 'VIDEO':
      return <Film className="h-4 w-4" />;
    case 'AUDIO':
      return <HardDrive className="h-4 w-4" />;
    case 'IMAGE':
      return <ImageIcon className="h-4 w-4" />;
    case 'DOCUMENT':
      return <FileText className="h-4 w-4" />;
    default:
      return <Database className="h-4 w-4" />;
  }
};

// 媒体类型图表配置
const getMediaTypeChartConfig = (distribution: TypeDistribution[]): ChartConfig => {
  const colors = {
    [MediaType.VIDEO]: "#ef4444", // 红色
    [MediaType.AUDIO]: "#10b981", // 绿色
    [MediaType.IMAGE]: "#f59e0b", // 琥珀色
    [MediaType.DOCUMENT]: "#4f46e5", // 靛蓝色
  };
  
  const chartConfig: ChartConfig = {};
  
  distribution.forEach((item) => {
    chartConfig[item.type] = {
      label: item.typeName,
      color: colors[item.type] || "#8b5cf6", // 默认紫色
    };
  });
  
  return chartConfig;
};

export default function AdminQuotaPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [isLoading, setIsLoading] = useState(true);
  const [quotaStats, setQuotaStats] = useState<InstitutionQuotaStatsVO | null>(null);
  
  // 申请管理状态
  const [applications, setApplications] = useState<QuotaApplicationVO[]>([]);
  const [isLoadingApplications, setIsLoadingApplications] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize] = useState(10);
  
  // 详情弹窗状态
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedInstitution, setSelectedInstitution] = useState<InstitutionQuotaVO | null>(null);
  
  // 申请详情弹窗状态
  const [applicationDialogOpen, setApplicationDialogOpen] = useState(false);
  const [selectedApplication, setSelectedApplication] = useState<QuotaApplicationVO | null>(null);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  
  // 媒体活动状态
  const [startDate, setStartDate] = useState<Date | undefined>(subDays(new Date(), 30));
  const [endDate, setEndDate] = useState<Date | undefined>(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | undefined>();
  const [selectedInstitutionId, setSelectedInstitutionId] = useState<string>('');
  const [calendarData, setCalendarData] = useState<MediaActivityCalendarVO | null>(null);
  const [mediaList, setMediaList] = useState<MediaVO[]>([]);
  const [mediaPage, setMediaPage] = useState(1);
  const [mediaTotalPages, setMediaTotalPages] = useState(1);
  const [isMediaLoading, setIsMediaLoading] = useState(false);
  const [isCalendarLoading, setIsCalendarLoading] = useState(false);
  
  // 添加媒体类型分布状态
  const [mediaTypeDistribution, setMediaTypeDistribution] = useState<MediaTypeDistributionVO | null>(null);
  const [loadingMediaTypeDistribution, setLoadingMediaTypeDistribution] = useState(false);
  
  // 添加机构存储使用状态
  const [institutionStorageUsage, setInstitutionStorageUsage] = useState<Record<string, number>>({});
  const [loadingInstitutionStorageUsage, setLoadingInstitutionStorageUsage] = useState(false);
  
  // 在TabsContent value="advanced"之前添加状态变量
  const [advancedStartDate, setAdvancedStartDate] = useState<Date | undefined>(undefined);
  const [advancedEndDate, setAdvancedEndDate] = useState<Date | undefined>(undefined);
  
  // 在TabsContent value="advanced"之前添加状态管理变量
  // 添加高级搜索状态管理
  const [advancedSearchParams, setAdvancedSearchParams] = useState<AdvancedMediaQueryParams>({
    page: 0,
    size: 10
  });
  const [advancedSearchResults, setAdvancedSearchResults] = useState<Page<AdminMediaVO> | null>(null);
  const [isAdvancedSearching, setIsAdvancedSearching] = useState(false);
  const [mediaTypeFilter, setMediaTypeFilter] = useState<string>("ALL");
  const [institutionNameFilter, setInstitutionNameFilter] = useState<string>("");
  const [filenameFilter, setFilenameFilter] = useState<string>("");
  const [minSizeFilter, setMinSizeFilter] = useState<string>("");
  const [maxSizeFilter, setMaxSizeFilter] = useState<string>("");
  
  // 添加高级搜索函数
  const handleAdvancedSearch = async (pageNum: number = 0) => {
    setIsAdvancedSearching(true);
    
    try {
      // 构建查询参数
      const params: AdvancedMediaQueryParams = {
        page: pageNum,
        size: 10
      };
      
      // 只添加有值的参数
      if (mediaTypeFilter && mediaTypeFilter !== "ALL") {
        params.type = mediaTypeFilter as MediaType;
      }
      
      if (institutionNameFilter.trim()) {
        params.institutionName = institutionNameFilter.trim();
      }
      
      if (filenameFilter.trim()) {
        params.filename = filenameFilter.trim();
      }
      
      if (advancedStartDate) {
        params.uploadStartTime = format(advancedStartDate, "yyyy-MM-dd'T'HH:mm:ss");
      }
      
      if (advancedEndDate) {
        params.uploadEndTime = format(advancedEndDate, "yyyy-MM-dd'T'HH:mm:ss");
      }
      
      // 将MB转换为字节
      if (minSizeFilter.trim()) {
        // 将MB转换为字节 (1 MB = 1024 * 1024 字节)
        params.minSize = Math.floor(parseFloat(minSizeFilter.trim()) * 1024 * 1024);
      }
      
      if (maxSizeFilter.trim()) {
        // 将MB转换为字节 (1 MB = 1024 * 1024 字节)
        params.maxSize = Math.floor(parseFloat(maxSizeFilter.trim()) * 1024 * 1024);
      }
      
      console.log('高级搜索参数:', params);
      
      // 调用API
      const result = await mediaService.getAdvancedMediaList(params);
      if (result.code === 200 && result.data) {
        setAdvancedSearchResults(result.data);
      } else {
        toast.error('搜索失败: ' + result.message);
      }
    } catch (error) {
      console.error('高级搜索失败:', error);
      toast.error('搜索请求失败，请稍后重试');
    } finally {
      setIsAdvancedSearching(false);
    }
  };
  
  // 重置高级搜索条件
  const resetAdvancedSearch = () => {
    setMediaTypeFilter("ALL");
    setInstitutionNameFilter("");
    setFilenameFilter("");
    setAdvancedStartDate(undefined);
    setAdvancedEndDate(undefined);
    setMinSizeFilter("");
    setMaxSizeFilter("");
    setAdvancedSearchResults(null);
  };
  
  // 处理URL查询参数
  useEffect(() => {
    // 获取URL中的tab参数
    const searchParams = new URLSearchParams(window.location.search);
    const tabParam = searchParams.get('tab');
    
    // 如果存在有效的tab参数，则切换到对应标签页
    if (tabParam && ['overview', 'institutions', 'applications', 'media'].includes(tabParam)) {
      setActiveTab(tabParam);
    }
  }, []);
  
  // 获取配额统计数据
  useEffect(() => {
    fetchQuotaStats();
  }, []);
  
  // 标签页切换时加载对应数据
  useEffect(() => {
    if (activeTab === 'applications') {
      fetchApplications();
    } else if (activeTab === 'media') {
      fetchMediaActivityCalendar();
    }
  }, [activeTab, currentPage, selectedStatus]);
  
  // 日期或机构选择变化时重新加载媒体活动日历
  useEffect(() => {
    if (activeTab === 'media' && startDate && endDate) {
      fetchMediaActivityCalendar();
    }
  }, [startDate, endDate, selectedInstitutionId, activeTab]);
  
  // 选定日期变化时加载该日期的媒体列表
  useEffect(() => {
    if (selectedDate) {
      fetchMediaListByDate();
    }
  }, [selectedDate, mediaPage, selectedInstitutionId]);
  
  // 获取配额统计数据
  const fetchQuotaStats = async () => {
    setIsLoading(true);
    try {
      const response = await adminQuotaService.getAllInstitutionsQuotaStats();
      if (response && response.data) {
        setQuotaStats(response.data);
      }
      setIsLoading(false);
    } catch (error) {
      console.error('获取机构配额统计数据失败:', error);
      toast.error('获取机构配额统计数据失败');
      setIsLoading(false);
    }
  };
  
  // 获取配额申请列表
  const fetchApplications = async () => {
    setIsLoadingApplications(true);
    try {
      const status = selectedStatus ? parseInt(selectedStatus) : undefined;
      const response = await quotaApplicationService.getAllApplications({
        status,
        pageNum: currentPage,
        pageSize
      });
      
      setApplications(response.content);
      setTotalPages(response.totalPages);
      setIsLoadingApplications(false);
    } catch (error) {
      console.error('获取配额申请列表失败:', error);
      toast.error('获取配额申请列表失败');
      setIsLoadingApplications(false);
    }
  };
  
  // 获取媒体活动日历数据
  const fetchMediaActivityCalendar = async () => {
    if (!startDate || !endDate) return;
    
    setIsCalendarLoading(true);
    try {
      const formatDateStr = (date: Date) => format(date, 'yyyy-MM-dd');
      const data = await adminMediaActivityService.getAllMediaActivityCalendar(
        formatDateStr(startDate),
        formatDateStr(endDate)
      );
      
      setCalendarData(data);
      
      // 如果没有选择日期但有最活跃日期，则选择最活跃日期
      if (!selectedDate && data.mostActiveDate) {
        const activeDate = parseISO(data.mostActiveDate);
        setSelectedDate(activeDate);
      }
      
    } catch (error) {
      console.error('获取媒体活动日历数据失败:', error);
      toast.error('获取媒体活动日历数据失败');
    } finally {
      setIsCalendarLoading(false);
    }
  };
  
  // 获取指定日期的媒体列表
  const fetchMediaListByDate = async () => {
    if (!selectedDate) return;
    
    setIsMediaLoading(true);
    try {
      const formatDateStr = (date: Date) => format(date, 'yyyy-MM-dd');
      
      // 如果selectedInstitutionId为"all"或空字符串，则不传递机构ID
      const institutionId = (selectedInstitutionId && selectedInstitutionId !== "all") 
        ? parseInt(selectedInstitutionId) 
        : undefined;
      
      // 如果有AdminMediaVO接口可用，使用它
      try {
        const data = await adminMediaActivityService.getAllMediaListByDate(
          formatDateStr(selectedDate),
          institutionId,
          mediaPage,
          10
        );
        
        setMediaList(data.content);
        setMediaTotalPages(data.totalPages);
      } catch (error) {
        console.error('无法使用扩展接口获取媒体列表:', error);
        // 回退到基本接口
        const data = await adminMediaActivityService.getAllMediaListByDate(
          formatDateStr(selectedDate),
          institutionId,
          mediaPage,
          10
        );
        
        setMediaList(data.content);
        setMediaTotalPages(data.totalPages);
      }
      
    } catch (error) {
      console.error('获取媒体列表失败:', error);
      toast.error('获取媒体列表失败');
    } finally {
      setIsMediaLoading(false);
    }
  };
  
  // 处理日期选择
  const handleDateSelect = (date: Date) => {
    setSelectedDate(date);
    setMediaPage(1); // 重置页码
  };
  
  // 查看机构详情
  const handleViewInstitutionDetail = (institution: InstitutionQuotaVO) => {
    setSelectedInstitution(institution);
    setDetailDialogOpen(true);
  };
  
  // 查看申请详情
  const handleViewApplicationDetail = (application: QuotaApplicationVO) => {
    setSelectedApplication(application);
    setApplicationDialogOpen(true);
  };
  
  // 批准申请
  const handleApproveApplication = async () => {
    if (!selectedApplication) return;
    
    setIsProcessing(true);
    try {
      await quotaApplicationService.approveApplication(selectedApplication.id);
      toast.success('申请已批准');
      setApplicationDialogOpen(false);
      fetchApplications(); // 刷新列表
      setIsProcessing(false);
    } catch (error) {
      console.error('批准申请失败:', error);
      toast.error('批准申请失败');
      setIsProcessing(false);
    }
  };
  
  // 打开拒绝对话框
  const handleOpenRejectDialog = () => {
    setRejectDialogOpen(true);
  };
  
  // 拒绝申请
  const handleRejectApplication = async () => {
    if (!selectedApplication || !rejectReason.trim()) return;
    
    setIsProcessing(true);
    try {
      await quotaApplicationService.rejectApplication(selectedApplication.id, rejectReason);
      toast.success('申请已拒绝');
      setRejectDialogOpen(false);
      setApplicationDialogOpen(false);
      fetchApplications(); // 刷新列表
      setRejectReason('');
      setIsProcessing(false);
    } catch (error) {
      console.error('拒绝申请失败:', error);
      toast.error('拒绝申请失败');
      setIsProcessing(false);
    }
  };
  
  // 获取状态标签
  const getStatusBadge = (status: QuotaApplicationStatus) => {
    switch (status) {
      case QuotaApplicationStatus.PENDING:
        return <Badge variant="secondary">待审核</Badge>;
      case QuotaApplicationStatus.APPROVED:
        return <Badge variant="success">已通过</Badge>;
      case QuotaApplicationStatus.REJECTED:
        return <Badge variant="destructive">已拒绝</Badge>;
      default:
        return <Badge>未知</Badge>;
    }
  };
  
  // 获取媒体类型分布
  const fetchMediaTypeDistribution = async () => {
    setLoadingMediaTypeDistribution(true);
    try {
      const result = await adminQuotaService.getMediaTypeDistribution();
      setMediaTypeDistribution(result.data);
    } catch (error) {
      console.error('获取媒体类型分布失败:', error);
      toast.error('获取媒体类型分布失败');
    } finally {
      setLoadingMediaTypeDistribution(false);
    }
  };
  
  // 获取机构存储使用统计
  const fetchInstitutionStorageUsage = async () => {
    setLoadingInstitutionStorageUsage(true);
    try {
      const result = await adminQuotaService.getInstitutionStorageUsage();
      setInstitutionStorageUsage(result.data);
    } catch (error) {
      console.error('获取机构存储使用统计失败:', error);
      toast.error('获取机构存储使用统计失败');
    } finally {
      setLoadingInstitutionStorageUsage(false);
    }
  };
  
  // 在初始化时获取数据
  useEffect(() => {
    if (activeTab === 'overview') {
      fetchQuotaStats();
      fetchMediaTypeDistribution();
      fetchInstitutionStorageUsage();
    }
  }, [activeTab]);
  
  // 在handleRefresh函数中添加对新加入的数据刷新
  const handleRefresh = () => {
    toast('刷新数据中...');
    if (activeTab === 'overview') {
      fetchQuotaStats();
      fetchMediaTypeDistribution();
      fetchInstitutionStorageUsage();
    } else if (activeTab === 'applications') {
      fetchApplications();
    } else if (activeTab === 'activity') {
      fetchMediaActivityCalendar();
      if (selectedDate) {
        fetchMediaListByDate();
      }
    }
  };
  
  // 在 useEffect 中处理标签切换
  useEffect(() => {
    // ... 现有代码保持不变 ...
    
    // 在切换到高级筛选标签时，重置状态
    if (activeTab === 'advanced') {
      resetAdvancedSearch();
    }
  }, [activeTab]);
  
  // 添加处理文件点击的函数
  const handleViewMediaDetail = (media: AdminMediaVO) => {
    toast.info(`正在加载媒体详情: ${media.originalFilename}`);
    router.push(`/dashboard/media/${media.id}`);
  };
  
  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">配额管理</h1>
          <p className="text-muted-foreground">管理所有机构的存储配额和配额申请</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={handleRefresh}
            disabled={isLoading}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            刷新
          </Button>
        </div>
      </div>
      
      <Tabs
        defaultValue="overview"
        value={activeTab}
        onValueChange={setActiveTab}
        className="space-y-4"
      >
        <TabsList>
          <TabsTrigger value="overview">
            <Gauge className="h-4 w-4 mr-2" />
            总体概览
          </TabsTrigger>
          <TabsTrigger value="institutions">
            <Building className="h-4 w-4 mr-2" />
            机构配额
          </TabsTrigger>
          <TabsTrigger value="applications">
            <FileText className="h-4 w-4 mr-2" />
            配额申请
          </TabsTrigger>
          <TabsTrigger value="media">
            <Calendar className="h-4 w-4 mr-2" />
            媒体活动
          </TabsTrigger>
          <TabsTrigger value="advanced">
            <FileText className="h-4 w-4 mr-2" />
            高级筛选
          </TabsTrigger>
        </TabsList>
        
        {/* 总体概览内容 */}
        <TabsContent value="overview" className="space-y-4">
          {isLoading ? (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
              {Array.from({ length: 4 }).map((_, i) => (
                <Card key={i}>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">
                      <Skeleton className="h-4 w-40" />
                    </CardTitle>
                    <Skeleton className="h-4 w-4" />
                  </CardHeader>
                  <CardContent>
                    <Skeleton className="h-8 w-40" />
                    <Skeleton className="h-4 w-24 mt-2" />
                  </CardContent>
                </Card>
              ))}
              
              <Card className="col-span-4">
                <CardHeader>
                  <Skeleton className="h-6 w-48" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-12 w-full" />
                </CardContent>
              </Card>
              
              {/* 媒体类型分布加载状态 */}
              <Card className="col-span-2">
                <CardHeader>
                  <Skeleton className="h-6 w-48" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-[300px] w-full" />
                </CardContent>
              </Card>
              
              {/* 媒体类型表格加载状态 */}
              <Card className="col-span-2">
                <CardHeader>
                  <Skeleton className="h-6 w-48" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-[300px] w-full" />
                </CardContent>
              </Card>
              
              {/* 机构存储表格加载状态 */}
              <Card className="col-span-4">
                <CardHeader>
                  <Skeleton className="h-6 w-48" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-[300px] w-full" />
                </CardContent>
              </Card>
            </div>
          ) : (
            <>
              {quotaStats && (
                <>
                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                    {/* 总配额卡片 */}
                    <Card>
                      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                          总配额
                        </CardTitle>
                        <HardDrive className="h-4 w-4 text-muted-foreground" />
                      </CardHeader>
                      <CardContent>
                        <div className="text-2xl font-bold">
                          {formatBytes(quotaStats.totalUsage.totalQuota)}
                        </div>
                        <p className="text-xs text-muted-foreground">
                          系统总存储容量
                        </p>
                      </CardContent>
                    </Card>
                    
                    {/* 已用配额卡片 */}
                    <Card>
                      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                          已用配额
                        </CardTitle>
                        <Database className="h-4 w-4 text-muted-foreground" />
                      </CardHeader>
                      <CardContent>
                        <div className="text-2xl font-bold">
                          {formatBytes(quotaStats.totalUsage.usedQuota)}
                        </div>
                        <p className="text-xs text-muted-foreground">
                          当前已使用的存储空间
                        </p>
                      </CardContent>
                    </Card>
                    
                    {/* 可用配额卡片 */}
                    <Card>
                      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                          可用配额
                        </CardTitle>
                        <Database className="h-4 w-4 text-muted-foreground" />
                      </CardHeader>
                      <CardContent>
                        <div className="text-2xl font-bold">
                          {formatBytes(quotaStats.totalUsage.availableQuota)}
                        </div>
                        <p className="text-xs text-muted-foreground">
                          剩余可用存储空间
                        </p>
                      </CardContent>
                    </Card>
                    
                    {/* 机构数量卡片 */}
                    <Card>
                      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                          机构数量
                        </CardTitle>
                        <Building className="h-4 w-4 text-muted-foreground" />
                      </CardHeader>
                      <CardContent>
                        <div className="text-2xl font-bold">
                          {quotaStats.totalUsage.institutionCount}
                        </div>
                        <p className="text-xs text-muted-foreground">
                          系统中的机构总数
                        </p>
                      </CardContent>
                    </Card>
                    
                    {/* 总体配额使用进度条 */}
                    <Card className="col-span-4">
                      <CardHeader>
                        <CardTitle>系统存储使用率</CardTitle>
                        <CardDescription>
                          整个系统的存储配额使用情况
                        </CardDescription>
                      </CardHeader>
                      <CardContent className="space-y-2">
                        <div className="flex justify-between items-center">
                          <span className="text-sm">
                            {formatBytes(quotaStats.totalUsage.usedQuota)}已用 / {formatBytes(quotaStats.totalUsage.totalQuota)}总量
                          </span>
                          <span className="text-sm font-medium">
                            {quotaStats.totalUsage.usagePercentage.toFixed(2)}%
                          </span>
                        </div>
                        <Progress value={quotaStats.totalUsage.usagePercentage} className="h-2" />
                      </CardContent>
                    </Card>
                  </div>
                  
                  {/* 媒体类型分布和详细统计 */}
                  <div className="grid gap-4 md:grid-cols-2">
                    {/* 媒体类型分布 */}
                    <Card>
                      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <div>
                          <CardTitle>媒体类型分布</CardTitle>
                          <CardDescription>系统中各类型媒体资源占比</CardDescription>
                        </div>
                        {loadingMediaTypeDistribution ? (
                          <RefreshCw className="h-4 w-4 animate-spin" />
                        ) : (
                          <Button 
                            variant="ghost" 
                            size="icon" 
                            onClick={fetchMediaTypeDistribution}
                            className="h-8 w-8"
                          >
                            <RefreshCw className="h-4 w-4" />
                          </Button>
                        )}
                      </CardHeader>
                      <CardContent>
                        {loadingMediaTypeDistribution ? (
                          <div className="flex items-center justify-center h-[300px]">
                            <RefreshCw className="h-8 w-8 animate-spin text-muted-foreground" />
                          </div>
                        ) : mediaTypeDistribution ? (
                          <ChartContainer 
                            className="h-[300px]"
                            config={getMediaTypeChartConfig(mediaTypeDistribution.distribution)}
                          >
                            <PieChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
                              <Pie
                                data={mediaTypeDistribution.distribution}
                                dataKey="count"
                                nameKey="typeName"
                                cx="50%"
                                cy="50%"
                                outerRadius={100}
                                innerRadius={40}
                                paddingAngle={2}
                                labelLine={true}
                                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                              >
                                {mediaTypeDistribution.distribution.map((entry, index) => (
                                  <Cell 
                                    key={`cell-${index}`} 
                                    fill={`var(--color-${entry.type})`} 
                                    stroke="var(--background)"
                                    strokeWidth={2}
                                  />
                                ))}
                              </Pie>
                              <ChartTooltip
                                content={
                                  <ChartTooltipContent 
                                    formatter={(value, name, props) => {
                                      return [value, props.payload.typeName];
                                    }}
                                  />
                                }
                              />
                              <ChartLegend 
                                content={<ChartLegendContent />}
                                layout="vertical"
                                verticalAlign="middle" 
                                align="right"
                              />
                            </PieChart>
                          </ChartContainer>
                        ) : (
                          <div className="flex flex-col items-center justify-center h-[300px] space-y-4">
                            <PieChartIcon className="h-12 w-12 text-muted-foreground opacity-20" />
                            <p className="text-muted-foreground">暂无媒体类型数据</p>
                            <Button onClick={fetchMediaTypeDistribution} size="sm">
                              加载数据
                            </Button>
                          </div>
                        )}
                      </CardContent>
                    </Card>
                    
                    {/* 媒体类型分布表格 */}
                    <Card>
                      <CardHeader>
                        <CardTitle>媒体类型详细统计</CardTitle>
                        <CardDescription>各类型媒体文件数量和占比</CardDescription>
                      </CardHeader>
                      <CardContent>
                        {loadingMediaTypeDistribution ? (
                          <div className="space-y-4">
                            {Array.from({ length: 4 }).map((_, i) => (
                              <div key={i} className="flex items-center justify-between">
                                <Skeleton className="h-8 w-40" />
                                <Skeleton className="h-8 w-20" />
                              </div>
                            ))}
                          </div>
                        ) : mediaTypeDistribution ? (
                          <Table>
                            <TableHeader>
                              <TableRow>
                                <TableHead>类型</TableHead>
                                <TableHead>数量</TableHead>
                                <TableHead>占比</TableHead>
                              </TableRow>
                            </TableHeader>
                            <TableBody>
                              {mediaTypeDistribution.distribution.map((item) => (
                                <TableRow key={item.type}>
                                  <TableCell>
                                    <div className="flex items-center gap-2">
                                      <div className={`p-1 rounded-full bg-primary/10`}>
                                        {getMediaTypeIcon(item.type)}
                                      </div>
                                      <span>{item.typeName}</span>
                                    </div>
                                  </TableCell>
                                  <TableCell>{item.count}</TableCell>
                                  <TableCell>{formatPercentage(item.percentage)}</TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        ) : (
                          <div className="flex items-center justify-center h-[300px]">
                            <p className="text-muted-foreground">暂无媒体类型数据</p>
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  </div>
                  
                  {/* 机构存储使用排行表格 */}
                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                      <div>
                        <CardTitle>机构存储使用排行</CardTitle>
                        <CardDescription>按实际存储使用量排序的机构</CardDescription>
                      </div>
                      {loadingInstitutionStorageUsage ? (
                        <RefreshCw className="h-4 w-4 animate-spin" />
                      ) : (
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          onClick={fetchInstitutionStorageUsage}
                          className="h-8 w-8"
                        >
                          <RefreshCw className="h-4 w-4" />
                        </Button>
                      )}
                    </CardHeader>
                    <CardContent>
                      {loadingInstitutionStorageUsage ? (
                        <div className="space-y-4">
                          {Array.from({ length: 5 }).map((_, i) => (
                            <div key={i} className="flex items-center justify-between">
                              <Skeleton className="h-8 w-40" />
                              <Skeleton className="h-8 w-20" />
                            </div>
                          ))}
                        </div>
                      ) : Object.keys(institutionStorageUsage).length > 0 ? (
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead>排名</TableHead>
                              <TableHead>机构名称</TableHead>
                              <TableHead>存储使用量</TableHead>
                              <TableHead>占总存储比例</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {Object.entries(institutionStorageUsage)
                              .sort(([, a], [, b]) => b - a)
                              .map(([name, size], index) => (
                                <TableRow key={name}>
                                  <TableCell>#{index + 1}</TableCell>
                                  <TableCell className="font-medium">{name}</TableCell>
                                  <TableCell>{formatBytes(size)}</TableCell>
                                  <TableCell>
                                    {quotaStats.totalUsage.usedQuota ? 
                                      `${((size / quotaStats.totalUsage.usedQuota) * 100).toFixed(2)}%` : 
                                      '0%'
                                    }
                                  </TableCell>
                                </TableRow>
                              ))}
                          </TableBody>
                        </Table>
                      ) : (
                        <div className="flex flex-col items-center justify-center h-[200px] space-y-4">
                          <Database className="h-12 w-12 text-muted-foreground opacity-20" />
                          <p className="text-muted-foreground">暂无机构存储使用数据</p>
                          <Button onClick={fetchInstitutionStorageUsage} size="sm">
                            加载数据
                          </Button>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                </>
              )}
            </>
          )}
        </TabsContent>
        
        {/* 机构配额内容 */}
        <TabsContent value="institutions" className="space-y-4">
          {isLoading ? (
            <>
              <div className="grid gap-4 md:grid-cols-2">
                <Card>
                  <CardHeader>
                    <Skeleton className="h-6 w-48" />
                  </CardHeader>
                  <CardContent>
                    <Skeleton className="h-[300px] w-full" />
                  </CardContent>
                </Card>
                <Card>
                  <CardHeader>
                    <Skeleton className="h-6 w-48" />
                  </CardHeader>
                  <CardContent>
                    <Skeleton className="h-[300px] w-full" />
                  </CardContent>
                </Card>
              </div>
              <Card>
                <CardHeader>
                  <Skeleton className="h-6 w-48" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-[400px] w-full" />
                </CardContent>
              </Card>
            </>
          ) : (
            <>
              {quotaStats && (
                <>
                  <div className="grid gap-4 md:grid-cols-2">
                    {/* 机构配额分布饼图 */}
                    <Card>
                      <CardHeader>
                        <CardTitle>机构配额分布</CardTitle>
                        <CardDescription>各机构存储空间使用占比</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <ChartContainer 
                          className="h-[300px]"
                          config={getPieChartConfig(quotaStats.distribution)}
                        >
                          <PieChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
                            <Pie
                              data={quotaStats.distribution}
                              dataKey="usedQuota"
                              nameKey="institutionName"
                              cx="50%"
                              cy="50%"
                              outerRadius={100}
                              innerRadius={40}
                              paddingAngle={2}
                              labelLine={true}
                              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                            >
                              {quotaStats.distribution.map((entry, index) => (
                                <Cell 
                                  key={`cell-${index}`} 
                                  fill={`var(--color-inst_${entry.institutionId})`} 
                                  stroke="var(--background)"
                                  strokeWidth={2}
                                />
                              ))}
                            </Pie>
                            <ChartTooltip
                              content={
                                <ChartTooltipContent 
                                  formatter={(value, name, props) => {
                                    return [formatBytes(Number(value)), props.payload.institutionName];
                                  }}
                                />
                              }
                            />
                            <ChartLegend 
                              content={<ChartLegendContent />}
                              layout="vertical"
                              verticalAlign="middle" 
                              align="right"
                            />
                          </PieChart>
                        </ChartContainer>
                      </CardContent>
                    </Card>
                    
                    {/* 机构配额排行 */}
                    <Card>
                      <CardHeader>
                        <CardTitle>机构配额使用排行</CardTitle>
                        <CardDescription>按使用量排序的前10个机构</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <ChartContainer 
                          className="h-[300px]"
                          config={getBarChartConfig()}
                        >
                          <RechartBarChart
                            data={quotaStats.institutions
                              .sort((a, b) => b.usagePercentage - a.usagePercentage)
                              .slice(0, 10)
                              .map(inst => ({
                                name: inst.institutionName,
                                usedQuota: inst.usedQuota,
                                totalQuota: inst.totalQuota,
                                percentage: inst.usagePercentage
                              }))
                            }
                            layout="vertical"
                            margin={{ top: 20, right: 30, left: 70, bottom: 10 }}
                          >
                            <CartesianGrid strokeDasharray="3 3" horizontal={true} vertical={false} />
                            <XAxis type="number" tickFormatter={(value) => formatBytes(value)} />
                            <YAxis 
                              type="category" 
                              dataKey="name" 
                              width={65}
                              tickLine={false}
                              axisLine={false}
                              tickFormatter={(value) => {
                                return value.length > 8 ? `${value.slice(0, 8)}...` : value;
                              }}
                            />
                            <ChartTooltip
                              content={
                                <ChartTooltipContent 
                                  labelFormatter={(label) => `机构: ${label}`}
                                  formatter={(value, name, props) => {
                                    if (name === 'usedQuota') {
                                      return [formatBytes(Number(value)), '已用配额'];
                                    } else if (name === 'totalQuota') {
                                      return [formatBytes(Number(value)), '总配额'];
                                    }
                                    return [value, name];
                                  }}
                                />
                              }
                            />
                            <Bar 
                              dataKey="usedQuota" 
                              fill="var(--color-usedQuota)" 
                              maxBarSize={20}
                              radius={[0, 4, 4, 0]}
                            />
                          </RechartBarChart>
                        </ChartContainer>
                      </CardContent>
                    </Card>
                  </div>
                  
                  {/* 机构配额详情表格 */}
                  <Card>
                    <CardHeader>
                      <CardTitle>机构配额详情</CardTitle>
                      <CardDescription>所有机构的配额使用情况</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>机构名称</TableHead>
                            <TableHead>总配额</TableHead>
                            <TableHead>已用配额</TableHead>
                            <TableHead>可用配额</TableHead>
                            <TableHead>使用百分比</TableHead>
                            <TableHead>上次更新时间</TableHead>
                            <TableHead className="text-right">操作</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {quotaStats.institutions.map((inst) => (
                            <TableRow key={inst.institutionId}>
                              <TableCell className="font-medium">{inst.institutionName}</TableCell>
                              <TableCell>{formatBytes(inst.totalQuota)}</TableCell>
                              <TableCell>{formatBytes(inst.usedQuota)}</TableCell>
                              <TableCell>{formatBytes(inst.availableQuota)}</TableCell>
                              <TableCell>
                                <div className="flex items-center gap-2">
                                  <Progress value={inst.usagePercentage} className="h-2 w-[60px]" />
                                  <span>{inst.usagePercentage.toFixed(2)}%</span>
                                </div>
                              </TableCell>
                              <TableCell>{new Date(inst.lastUpdatedTime).toLocaleString()}</TableCell>
                              <TableCell className="text-right">
                                <Button 
                                  variant="ghost" 
                                  size="sm"
                                  onClick={() => handleViewInstitutionDetail(inst)}
                                >
                                  详情
                                </Button>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </CardContent>
                  </Card>
                </>
              )}
            </>
          )}
        </TabsContent>
        
        {/* 配额申请内容 */}
        <TabsContent value="applications" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>配额申请管理</CardTitle>
              <CardDescription>查看和处理机构用户的配额申请</CardDescription>
            </CardHeader>
            <CardContent>
              {/* 状态筛选器 */}
              <div className="mb-4 flex justify-between">
                <div className="flex gap-2">
                  <Button
                    variant={selectedStatus === '' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setSelectedStatus('')}
                  >
                    全部
                  </Button>
                  <Button
                    variant={selectedStatus === '0' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setSelectedStatus('0')}
                  >
                    待审核
                  </Button>
                  <Button
                    variant={selectedStatus === '1' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setSelectedStatus('1')}
                  >
                    已通过
                  </Button>
                  <Button
                    variant={selectedStatus === '2' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setSelectedStatus('2')}
                  >
                    已拒绝
                  </Button>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => fetchApplications()}
                  disabled={isLoadingApplications}
                >
                  <RefreshCw className={`h-4 w-4 mr-2 ${isLoadingApplications ? 'animate-spin' : ''}`} />
                  刷新
                </Button>
              </div>
              
              {/* 申请列表表格 */}
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>申请编号</TableHead>
                    <TableHead>机构</TableHead>
                    <TableHead>申请人</TableHead>
                    <TableHead>申请类型</TableHead>
                    <TableHead>申请容量</TableHead>
                    <TableHead>申请时间</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead className="text-right">操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {isLoadingApplications ? (
                    Array.from({ length: 5 }).map((_, i) => (
                      <TableRow key={i}>
                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-16" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-16" /></TableCell>
                        <TableCell className="text-right"><Skeleton className="h-8 w-16 ml-auto" /></TableCell>
                      </TableRow>
                    ))
                  ) : applications.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                        暂无配额申请记录
                      </TableCell>
                    </TableRow>
                  ) : (
                    applications.map((app) => (
                      <TableRow key={app.id}>
                        <TableCell className="font-medium">{app.applicationId}</TableCell>
                        <TableCell>{app.institutionName}</TableCell>
                        <TableCell>{app.applicantUsername}</TableCell>
                        <TableCell>
                          {app.quotaType === 0 ? '总配额' : 
                           app.quotaType === 1 ? '视频配额' : 
                           app.quotaType === 2 ? '文档配额' : '未知类型'}
                        </TableCell>
                        <TableCell>{formatBytes(app.requestedBytes)}</TableCell>
                        <TableCell>{new Date(app.createdAt).toLocaleString()}</TableCell>
                        <TableCell>{getStatusBadge(app.status)}</TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleViewApplicationDetail(app)}
                          >
                            {app.status === QuotaApplicationStatus.PENDING ? '审批' : '详情'}
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
              
              {/* 分页控件 */}
              {!isLoadingApplications && applications.length > 0 && (
                <div className="flex items-center justify-end space-x-2 py-4">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                    disabled={currentPage <= 1}
                  >
                    上一页
                  </Button>
                  <div className="text-sm">
                    第 {currentPage} 页，共 {totalPages} 页
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                    disabled={currentPage >= totalPages}
                  >
                    下一页
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
        
        {/* 媒体活动内容 */}
        <TabsContent value="media" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-[350px_1fr] gap-6">
            {/* 左侧热力图区域 */}
            <Card>
              <CardHeader>
                <CardTitle>上传活动热图</CardTitle>
                <CardDescription>按日期查看所有机构的媒体文件上传情况</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex flex-col space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="flex flex-col space-y-2">
                      <Label htmlFor="startDate">开始日期</Label>
                      <DatePicker
                        date={startDate}
                        setDate={setStartDate}
                      />
                    </div>
                    <div className="flex flex-col space-y-2">
                      <Label htmlFor="endDate">结束日期</Label>
                      <DatePicker
                        date={endDate}
                        setDate={setEndDate}
                      />
                    </div>
                  </div>
                  
                  <div className="flex flex-col space-y-2">
                    <Label htmlFor="institutionFilter">机构筛选</Label>
                    <Select
                      value={selectedInstitutionId}
                      onValueChange={setSelectedInstitutionId}
                    >
                      <SelectTrigger id="institutionFilter">
                        <SelectValue placeholder="所有机构" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">所有机构</SelectItem>
                        {quotaStats?.institutions.map(inst => (
                          <SelectItem key={inst.institutionId} value={inst.institutionId.toString()}>
                            {inst.institutionName}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  
                  {isCalendarLoading ? (
                    <div className="p-4">
                      <Skeleton className="h-[300px] w-full" />
                    </div>
                  ) : (
                    <ActivityHeatmap
                      calendarData={calendarData}
                      startDate={startDate}
                      endDate={endDate}
                      selectedDate={selectedDate}
                      onDateSelect={handleDateSelect}
                    />
                  )}
                  
                  {calendarData && (
                    <div className="bg-muted p-3 rounded-md text-sm">
                      <div className="grid grid-cols-2 gap-2">
                        <div>
                          <span className="text-muted-foreground">总活动数：</span>
                          <span className="font-medium">{calendarData.totalCount}</span>
                        </div>
                        <div>
                          <span className="text-muted-foreground">总文件大小：</span>
                          <span className="font-medium">{formatBytes(calendarData.totalSize)}</span>
                        </div>
                        <div>
                          <span className="text-muted-foreground">最活跃日期：</span>
                          <span className="font-medium">
                            {calendarData.mostActiveDate ? format(parseISO(calendarData.mostActiveDate), 'yyyy-MM-dd') : '-'}
                          </span>
                        </div>
                        <div>
                          <span className="text-muted-foreground">峰值活动数：</span>
                          <span className="font-medium">{calendarData.peakCount}</span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
            
            {/* 右侧媒体列表 */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <div>
                  <CardTitle>媒体文件列表</CardTitle>
                  <CardDescription>
                    {selectedDate ? (
                      <>日期: {format(selectedDate, 'yyyy-MM-dd')} 的上传文件</>
                    ) : (
                      <>选择日期查看上传的媒体文件</>
                    )}
                  </CardDescription>
                </div>
                {selectedDate && (
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => {
                      setMediaPage(1);
                      fetchMediaListByDate();
                    }}
                    disabled={isMediaLoading}
                  >
                    <RefreshCw className={`h-4 w-4 mr-2 ${isMediaLoading ? 'animate-spin' : ''}`} />
                    刷新
                  </Button>
                )}
              </CardHeader>
              <CardContent>
                {!selectedDate ? (
                  <div className="flex flex-col items-center justify-center h-[300px] text-muted-foreground">
                    <Calendar className="h-12 w-12 mb-4 opacity-20" />
                    <p>请从热力图中选择一个日期</p>
                  </div>
                ) : isMediaLoading ? (
                  <div className="space-y-4">
                    {Array.from({ length: 5 }).map((_, i) => (
                      <div key={i} className="flex items-center space-x-4">
                        <Skeleton className="h-12 w-12 rounded" />
                        <div className="space-y-2 flex-1">
                          <Skeleton className="h-4 w-48" />
                          <Skeleton className="h-4 w-24" />
                        </div>
                      </div>
                    ))}
                  </div>
                ) : mediaList.length === 0 ? (
                  <div className="flex flex-col items-center justify-center h-[300px] text-muted-foreground">
                    <FileText className="h-12 w-12 mb-4 opacity-20" />
                    <p>该日期没有媒体文件上传记录</p>
                  </div>
                ) : (
                  <>
                    <div className="space-y-4">
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>文件名</TableHead>
                            <TableHead>类型</TableHead>
                            <TableHead>大小</TableHead>
                            <TableHead>机构</TableHead>
                            <TableHead>上传者</TableHead>
                            <TableHead>上传时间</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {mediaList.map((media: any) => (
                            <TableRow key={media.id}>
                              <TableCell className="flex items-center gap-2">
                                {getMediaTypeIcon(media.type)}
                                <span 
                                  className="truncate max-w-[200px] cursor-pointer text-blue-500 hover:text-blue-700 hover:underline" 
                                  title={`查看 ${media.originalFilename} 详情`}
                                  onClick={() => handleViewMediaDetail(media)}
                                >
                                  {media.originalFilename}
                                </span>
                              </TableCell>
                              <TableCell>
                                <Badge variant="outline" className="font-normal">
                                  {media.type === 'VIDEO' ? '视频' : 
                                   media.type === 'AUDIO' ? '音频' : 
                                   media.type === 'IMAGE' ? '图片' : 
                                   media.type === 'DOCUMENT' ? '文档' : media.type}
                                </Badge>
                              </TableCell>
                              <TableCell>{formatBytes(media.size)}</TableCell>
                              <TableCell>
                                {(media as any).institutionName ? 
                                  (media as any).institutionName : 
                                  `机构 ID: ${media.institutionId}`
                                }
                              </TableCell>
                              <TableCell>
                                {(media as any).uploaderUsername ? 
                                  (media as any).uploaderUsername : 
                                  `上传者 ID: ${media.uploaderId}`
                                }
                              </TableCell>
                              <TableCell className="whitespace-nowrap">
                                {format(new Date(media.uploadTime), 'yyyy-MM-dd HH:mm')}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                      
                      {/* 分页控件 */}
                      <div className="flex items-center justify-end space-x-2 py-4">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setMediaPage(prev => Math.max(prev - 1, 1))}
                          disabled={mediaPage <= 1 || isMediaLoading}
                        >
                          上一页
                        </Button>
                        <div className="text-sm">
                          第 {mediaPage} 页，共 {mediaTotalPages} 页
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setMediaPage(prev => Math.min(prev + 1, mediaTotalPages))}
                          disabled={mediaPage >= mediaTotalPages || isMediaLoading}
                        >
                          下一页
                        </Button>
                      </div>
                    </div>
                  </>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>
        
        {/* 高级筛选内容 */}
        <TabsContent value="advanced" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>媒体高级筛选</CardTitle>
              <CardDescription>使用多种条件筛选全部媒体资源</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {/* 筛选表单 */}
                <div className="space-y-4 col-span-full md:col-span-2 lg:col-span-1">
                  <div className="space-y-2">
                    <Label htmlFor="mediaType">媒体类型</Label>
                    <Select 
                      value={mediaTypeFilter} 
                      onValueChange={setMediaTypeFilter}
                    >
                      <SelectTrigger id="mediaType">
                        <SelectValue placeholder="所有类型" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ALL">所有类型</SelectItem>
                        <SelectItem value="VIDEO">视频</SelectItem>
                        <SelectItem value="AUDIO">音频</SelectItem>
                        <SelectItem value="IMAGE">图片</SelectItem>
                        <SelectItem value="DOCUMENT">文档</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="institutionName">机构名称</Label>
                    <input
                      type="text"
                      id="institutionName"
                      placeholder="输入机构名称关键词"
                      className="w-full p-2 border rounded-md"
                      value={institutionNameFilter}
                      onChange={(e) => setInstitutionNameFilter(e.target.value)}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label>上传时间范围</Label>
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <Label htmlFor="startDate" className="text-xs">开始日期</Label>
                        <DatePicker
                          date={advancedStartDate}
                          setDate={setAdvancedStartDate}
                        />
                      </div>
                      <div>
                        <Label htmlFor="endDate" className="text-xs">结束日期</Label>
                        <DatePicker
                          date={advancedEndDate}
                          setDate={setAdvancedEndDate}
                        />
                      </div>
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <Label>文件大小范围 (MB)</Label>
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <Label htmlFor="minSize" className="text-xs">最小大小</Label>
                        <div className="relative">
                          <input
                            type="number"
                            id="minSize"
                            placeholder="最小大小"
                            className="w-full p-2 pr-8 border rounded-md"
                            value={minSizeFilter}
                            onChange={(e) => setMinSizeFilter(e.target.value)}
                            step="0.1"
                            min="0"
                          />
                          <span className="absolute right-3 top-2 text-muted-foreground text-sm">MB</span>
                        </div>
                      </div>
                      <div>
                        <Label htmlFor="maxSize" className="text-xs">最大大小</Label>
                        <div className="relative">
                          <input
                            type="number"
                            id="maxSize"
                            placeholder="最大大小"
                            className="w-full p-2 pr-8 border rounded-md"
                            value={maxSizeFilter}
                            onChange={(e) => setMaxSizeFilter(e.target.value)}
                            step="0.1"
                            min="0"
                          />
                          <span className="absolute right-3 top-2 text-muted-foreground text-sm">MB</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="filename">文件名</Label>
                    <input
                      type="text"
                      id="filename"
                      placeholder="输入文件名关键词"
                      className="w-full p-2 border rounded-md"
                      value={filenameFilter}
                      onChange={(e) => setFilenameFilter(e.target.value)}
                    />
                  </div>
                  
                  <div className="flex space-x-2 mt-4">
                    <Button className="flex-1" onClick={() => handleAdvancedSearch()} disabled={isAdvancedSearching}>
                      {isAdvancedSearching ? (
                        <>
                          <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                          搜索中...
                        </>
                      ) : (
                        <>
                          <Search className="h-4 w-4 mr-2" />
                          搜索
                        </>
                      )}
                    </Button>
                    <Button variant="outline" onClick={resetAdvancedSearch} disabled={isAdvancedSearching}>
                      重置
                    </Button>
                  </div>
                </div>
                
                {/* 搜索结果 */}
                <div className="md:col-span-2">
                  {!advancedSearchResults && !isAdvancedSearching ? (
                    <div className="flex flex-col items-center justify-center h-[400px] space-y-4 border border-dashed rounded-md p-8">
                      <Search className="h-12 w-12 text-muted-foreground opacity-20" />
                      <p className="text-muted-foreground text-center">
                        使用左侧筛选条件搜索媒体文件<br />
                        结果将显示在此处
                      </p>
                      <p className="text-xs text-muted-foreground text-center">
                        提示：高级筛选支持多条件组合查询，包括机构名称、上传时间段、文件大小范围等
                      </p>
                    </div>
                  ) : isAdvancedSearching ? (
                    <div className="flex flex-col items-center justify-center h-[400px] space-y-4">
                      <Loader2 className="h-12 w-12 text-primary animate-spin" />
                      <p className="text-muted-foreground">正在搜索，请稍候...</p>
                    </div>
                  ) : advancedSearchResults && advancedSearchResults.content.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-[400px] space-y-4 border border-dashed rounded-md p-8">
                      <FileX className="h-12 w-12 text-muted-foreground opacity-20" />
                      <p className="text-muted-foreground text-center">
                        没有找到符合条件的媒体文件<br />
                        请尝试修改筛选条件
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      <Card>
                        <CardContent className="p-0">
                          <Table>
                            <TableHeader>
                              <TableRow>
                                <TableHead>文件名</TableHead>
                                <TableHead>类型</TableHead>
                                <TableHead>大小</TableHead>
                                <TableHead>机构</TableHead>
                                <TableHead>上传者</TableHead>
                                <TableHead>上传时间</TableHead>
                              </TableRow>
                            </TableHeader>
                            <TableBody>
                              {advancedSearchResults?.content.map((media: AdminMediaVO) => (
                                <TableRow key={media.id}>
                                  <TableCell className="flex items-center gap-2">
                                    {getMediaTypeIcon(media.type)}
                                    <span 
                                      className="truncate max-w-[200px] cursor-pointer text-blue-500 hover:text-blue-700 hover:underline" 
                                      title={`查看 ${media.originalFilename} 详情`}
                                      onClick={() => handleViewMediaDetail(media)}
                                    >
                                      {media.originalFilename}
                                    </span>
                                  </TableCell>
                                  <TableCell>
                                    <Badge variant="outline" className="font-normal">
                                      {media.type === 'VIDEO' ? '视频' : 
                                       media.type === 'AUDIO' ? '音频' : 
                                       media.type === 'IMAGE' ? '图片' : 
                                       media.type === 'DOCUMENT' ? '文档' : media.type}
                                    </Badge>
                                  </TableCell>
                                  <TableCell>{media.formattedSize || formatBytes(media.size)}</TableCell>
                                  <TableCell>{media.institutionName || `机构 ID: ${media.institutionId}`}</TableCell>
                                  <TableCell>{media.uploaderUsername || `上传者 ID: ${media.uploaderId}`}</TableCell>
                                  <TableCell className="whitespace-nowrap">
                                    {format(new Date(media.uploadTime), 'yyyy-MM-dd HH:mm')}
                                  </TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </CardContent>
                      </Card>
                      
                      {/* 分页控件 */}
                      {advancedSearchResults && advancedSearchResults.totalPages > 1 && (
                        <div className="flex items-center justify-between">
                          <div className="text-sm text-muted-foreground">
                            共 {advancedSearchResults.totalElements} 条记录，第 {advancedSearchResults.number + 1}/{advancedSearchResults.totalPages} 页
                          </div>
                          <div className="flex items-center justify-end space-x-2 py-4">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleAdvancedSearch(0)}
                              disabled={advancedSearchResults.number === 0 || isAdvancedSearching}
                            >
                              首页
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleAdvancedSearch(advancedSearchResults.number - 1)}
                              disabled={advancedSearchResults.number === 0 || isAdvancedSearching}
                            >
                              上一页
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleAdvancedSearch(advancedSearchResults.number + 1)}
                              disabled={advancedSearchResults.number >= advancedSearchResults.totalPages - 1 || isAdvancedSearching}
                            >
                              下一页
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleAdvancedSearch(advancedSearchResults.totalPages - 1)}
                              disabled={advancedSearchResults.number >= advancedSearchResults.totalPages - 1 || isAdvancedSearching}
                            >
                              末页
                            </Button>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
      
      {/* 机构详情弹窗 */}
      {selectedInstitution && (
        <Dialog open={detailDialogOpen} onOpenChange={setDetailDialogOpen}>
          <DialogContent className="sm:max-w-[500px]">
            <DialogHeader>
              <DialogTitle>机构配额详情</DialogTitle>
              <DialogDescription>
                {selectedInstitution.institutionName} 的存储使用情况
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium">总配额</p>
                  <p className="text-2xl font-bold">{formatBytes(selectedInstitution.totalQuota)}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">使用率</p>
                  <p className="text-2xl font-bold">{selectedInstitution.usagePercentage.toFixed(2)}%</p>
                </div>
                <div>
                  <p className="text-sm font-medium">已用配额</p>
                  <p className="text-2xl font-bold">{formatBytes(selectedInstitution.usedQuota)}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">可用配额</p>
                  <p className="text-2xl font-bold">{formatBytes(selectedInstitution.availableQuota)}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <p className="text-sm font-medium">配额使用进度</p>
                <Progress value={selectedInstitution.usagePercentage} className="h-2" />
                <p className="text-xs text-muted-foreground text-right">
                  最后更新时间: {new Date(selectedInstitution.lastUpdatedTime).toLocaleString()}
                </p>
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setDetailDialogOpen(false)}
              >
                关闭
              </Button>
              <Button
                onClick={() => {
                  // 导航到机构详情页
                  router.push(`/dashboard/institutions/${selectedInstitution.institutionId}`);
                }}
              >
                查看机构详情
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
      
      {/* 申请详情弹窗 */}
      {selectedApplication && (
        <Dialog open={applicationDialogOpen} onOpenChange={setApplicationDialogOpen}>
          <DialogContent className="sm:max-w-[600px]">
            <DialogHeader>
              <DialogTitle>配额申请详情</DialogTitle>
              <DialogDescription>
                申请编号: {selectedApplication.applicationId}
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium">申请机构</p>
                  <p className="text-lg">{selectedApplication.institutionName}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">申请人</p>
                  <p className="text-lg">{selectedApplication.applicantUsername}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">申请类型</p>
                  <p className="text-lg">
                    {selectedApplication.quotaType === 0 ? '总配额' : 
                     selectedApplication.quotaType === 1 ? '视频配额' : 
                     selectedApplication.quotaType === 2 ? '文档配额' : '未知类型'}
                  </p>
                </div>
                <div>
                  <p className="text-sm font-medium">申请容量</p>
                  <p className="text-lg">{formatBytes(selectedApplication.requestedBytes)}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">申请时间</p>
                  <p className="text-lg">{new Date(selectedApplication.createdAt).toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">状态</p>
                  <div className="mt-1">
                    {getStatusBadge(selectedApplication.status)}
                  </div>
                </div>
              </div>
              
              <div>
                <p className="text-sm font-medium">申请原因</p>
                <div className="mt-1 p-3 bg-muted rounded-md">
                  <p className="text-sm whitespace-pre-wrap">{selectedApplication.reason}</p>
                </div>
              </div>
              
              {selectedApplication.status !== QuotaApplicationStatus.PENDING && (
                <div>
                  <p className="text-sm font-medium">审核结果</p>
                  <div className="mt-1 grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm">审核人</p>
                      <p className="text-lg">{selectedApplication.reviewerUsername || '-'}</p>
                    </div>
                    <div>
                      <p className="text-sm">审核时间</p>
                      <p className="text-lg">{selectedApplication.reviewedAt ? new Date(selectedApplication.reviewedAt).toLocaleString() : '-'}</p>
                    </div>
                  </div>
                  {selectedApplication.reviewComment && (
                    <div className="mt-2">
                      <p className="text-sm">审核意见</p>
                      <div className="mt-1 p-3 bg-muted rounded-md">
                        <p className="text-sm whitespace-pre-wrap">{selectedApplication.reviewComment}</p>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
            <DialogFooter>
              {selectedApplication.status === QuotaApplicationStatus.PENDING ? (
                <>
                  <Button
                    variant="outline"
                    onClick={() => setApplicationDialogOpen(false)}
                    disabled={isProcessing}
                  >
                    取消
                  </Button>
                  <Button
                    variant="destructive"
                    onClick={handleOpenRejectDialog}
                    disabled={isProcessing}
                  >
                    {isProcessing ? '处理中...' : '拒绝'}
                  </Button>
                  <Button
                    onClick={handleApproveApplication}
                    disabled={isProcessing}
                  >
                    {isProcessing ? '处理中...' : '批准'}
                  </Button>
                </>
              ) : (
                <Button
                  onClick={() => setApplicationDialogOpen(false)}
                >
                  关闭
                </Button>
              )}
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
      
      {/* 拒绝理由输入弹窗 */}
      {selectedApplication && (
        <Dialog open={rejectDialogOpen} onOpenChange={setRejectDialogOpen}>
          <DialogContent className="sm:max-w-[450px]">
            <DialogHeader>
              <DialogTitle>拒绝申请</DialogTitle>
              <DialogDescription>
                请输入拒绝理由，将通知给申请人
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="reason">拒绝理由</Label>
                <Textarea
                  id="reason"
                  placeholder="请输入拒绝理由..."
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  rows={4}
                  className="resize-none"
                />
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setRejectDialogOpen(false)}
                disabled={isProcessing}
              >
                取消
              </Button>
              <Button
                variant="destructive"
                onClick={handleRejectApplication}
                disabled={isProcessing || !rejectReason.trim()}
              >
                {isProcessing ? '处理中...' : '确认拒绝'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
} 