'use client';

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import {
  Building2,
  Settings,
  RefreshCw,
  Save,
  Upload,
  Copy,
  Eye,
  EyeOff,
  Users,
  UserPlus,
  Search,
  Trash2,
  User,
  PieChart,
  TrendingUp
} from 'lucide-react';
import { 
  LineChart,
  Line, 
  PieChart as RechartsPieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid
} from 'recharts';

import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Textarea } from '@/components/ui/textarea';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/components/ui/alert';
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui/table';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger
} from '@/components/ui/tabs';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig
} from '@/components/ui/chart';

import institutionManagementService, { InstitutionInfo, InstitutionUpdateRequest } from '@/services/institution-management';
import { institutionMemberService } from '@/services';
import { InstitutionMemberVO, InstitutionMemberStats } from '@/types/institution-member';
import { Badge } from '@/components/ui/badge';

export default function InstitutionManagementPage() {
  // 标签页状态
  const [activeTab, setActiveTab] = useState("info");
  
  // 机构信息状态
  const [institution, setInstitution] = useState<InstitutionInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isResetting, setIsResetting] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [showRegisterCode, setShowRegisterCode] = useState(false);
  const [resetConfirmOpen, setResetConfirmOpen] = useState(false);
  
  // 成员管理状态
  const [members, setMembers] = useState<InstitutionMemberVO[]>([]);
  const [memberStats, setMemberStats] = useState<InstitutionMemberStats>({
    total: 0,
    limit: 0,
    available: 0
  });
  const [isLoadingMembers, setIsLoadingMembers] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize] = useState(10);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [memberToDelete, setMemberToDelete] = useState<InstitutionMemberVO | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [shareCodeOpen, setShareCodeOpen] = useState(false);
  
  // 表单字段
  const [formData, setFormData] = useState<InstitutionUpdateRequest>({
    name: '',
    description: '',
    contactPerson: '',
    contactPhone: '',
    address: '',
  });
  
  // 添加成员角色和状态分布数据状态
  const [statusDistribution, setStatusDistribution] = useState<Array<{name: string, value: number}>>([]);
  const [registrationTrend, setRegistrationTrend] = useState<Array<{date: string, count: number}>>([]);
  
  // 加载机构信息
  useEffect(() => {
    fetchInstitutionDetail();
  }, []);

  // 切换标签页时加载成员数据
  useEffect(() => {
    if (activeTab === 'members') {
      fetchMembers();
      fetchMemberStats();
    }
  }, [activeTab]);
  
  const fetchInstitutionDetail = async () => {
    setIsLoading(true);
    try {
      const data = await institutionManagementService.getInstitutionDetail();
      setInstitution(data);
      
      // 初始化表单数据
      setFormData({
        name: data.name || '',
        description: data.description || '',
        contactPerson: data.contactPerson || '',
        contactPhone: data.contactPhone || '',
        address: data.address || '',
      });
      
      setIsLoading(false);
    } catch (error) {
      console.error('获取机构详情失败:', error);
      toast.error('获取机构信息失败');
      setIsLoading(false);
    }
  };
  
  // 加载成员列表
  const fetchMembers = async (page = currentPage, keyword = searchKeyword) => {
    setIsLoadingMembers(true);
    try {
      const response = await institutionMemberService.getMembers({
        pageNum: page,
        pageSize,
        keyword: keyword || undefined
      });
      
      setMembers(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(response.number + 1);
      
      // 计算状态分布
      if (response.content.length > 0) {
        setStatusDistribution(institutionMemberService.generateStatusDistribution(response.content));
        
        // 计算注册时间趋势
        setRegistrationTrend(generateRegistrationTrend(response.content));
      }
      
      setIsLoadingMembers(false);
    } catch (error) {
      console.error('获取机构成员列表失败:', error);
      toast.error('获取机构成员列表失败');
      setIsLoadingMembers(false);
    }
  };
  
  // 生成注册时间趋势数据
  const generateRegistrationTrend = (members: InstitutionMemberVO[]): Array<{date: string, count: number}> => {
    // 按日期分组统计注册用户数量
    const dateMap = new Map<string, number>();
    
    // 首先对日期进行排序
    const sortedMembers = [...members].sort((a, b) => 
      new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
    
    sortedMembers.forEach(member => {
      // 格式化日期为 YYYY-MM-DD
      const registerDate = new Date(member.createdAt).toISOString().split('T')[0];
      dateMap.set(registerDate, (dateMap.get(registerDate) || 0) + 1);
    });
    
    // 累计用户数量
    let cumulativeCount = 0;
    const trend = Array.from(dateMap.entries()).map(([date, count]) => {
      cumulativeCount += count;
      return {
        date,
        count: cumulativeCount
      };
    });
    
    return trend;
  };
  
  // 加载成员统计
  const fetchMemberStats = async () => {
    try {
      const stats = await institutionMemberService.getMemberStats();
      setMemberStats(stats);
    } catch (error) {
      console.error('获取机构成员统计信息失败:', error);
      toast.error('获取机构成员统计信息失败');
    }
  };
  
  // 处理表单变更
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // 保存机构信息
  const handleSave = async () => {
    setIsSaving(true);
    try {
      const data = await institutionManagementService.updateInstitution(formData);
      setInstitution(data);
      toast.success('机构信息更新成功');
      setIsSaving(false);
    } catch (error) {
      console.error('更新机构信息失败:', error);
      toast.error('更新机构信息失败');
      setIsSaving(false);
    }
  };
  
  // 上传Logo
  const handleLogoUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    
    // 简单的文件类型验证
    if (!file.type.startsWith('image/')) {
      toast.error('请上传图片文件');
      return;
    }
    
    // 大小限制（2MB）
    if (file.size > 2 * 1024 * 1024) {
      toast.error('图片大小不能超过2MB');
      return;
    }
    
    setIsUploading(true);
    try {
      const logoUrl = await institutionManagementService.uploadLogo(file);
      
      // 更新本地状态
      if (institution) {
        setInstitution({
          ...institution,
          logo: logoUrl
        });
      }
      
      toast.success('Logo上传成功');
      setIsUploading(false);
    } catch (error) {
      console.error('上传Logo失败:', error);
      toast.error('上传Logo失败');
      setIsUploading(false);
    }
  };
  
  // 重置注册码
  const handleResetRegisterCode = async () => {
    setIsResetting(true);
    try {
      const newCode = await institutionManagementService.resetRegisterCode();
      
      // 更新本地状态
      toast.success('注册码重置成功');
      setResetConfirmOpen(false);
      
      // 简单的方式更新显示的注册码，实际可能需要完整刷新机构信息
      fetchInstitutionDetail();
      
      setIsResetting(false);
    } catch (error) {
      console.error('重置注册码失败:', error);
      toast.error('重置注册码失败');
      setIsResetting(false);
      setResetConfirmOpen(false);
    }
  };
  
  // 复制注册码
  const handleCopyRegisterCode = (code?: string) => {
    if (!code && !institution?.registerCode) return;
    
    navigator.clipboard.writeText(code || institution?.registerCode || '')
      .then(() => toast.success('注册码已复制到剪贴板'))
      .catch(() => toast.error('复制失败，请手动复制'));
  };
  
  // 搜索成员
  const handleSearch = () => {
    setCurrentPage(1);
    fetchMembers(1, searchKeyword);
  };
  
  // 切换页码
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    fetchMembers(page, searchKeyword);
  };
  
  // 显示删除确认
  const handleDeleteClick = (member: InstitutionMemberVO) => {
    setMemberToDelete(member);
    setDeleteConfirmOpen(true);
  };
  
  // 执行删除
  const handleDeleteConfirm = async () => {
    if (!memberToDelete) return;
    
    setIsDeleting(true);
    try {
      await institutionMemberService.removeMember(memberToDelete.id);
      toast.success(`成员 ${memberToDelete.username} 已成功移除`);
      setDeleteConfirmOpen(false);
      setMemberToDelete(null);
      
      // 刷新数据
      fetchMembers();
      fetchMemberStats();
    } catch (error) {
      console.error('移除成员失败:', error);
      toast.error('移除成员失败，请稍后重试');
    } finally {
      setIsDeleting(false);
    }
  };
  
  // 打开分享注册码对话框
  const handleOpenShareDialog = async () => {
    setShareCodeOpen(true);
  };
  
  // 获取成员状态标签
  const getMemberStatusBadge = (status: number) => {
    if (status === 1) {
      return <Badge variant="success">正常</Badge>;
    } else {
      return <Badge variant="destructive">禁用</Badge>;
    }
  };
  
  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">机构管理</h1>
          <p className="text-muted-foreground">管理您的机构信息和成员</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={activeTab === 'info' ? fetchInstitutionDetail : () => fetchMembers()}
            disabled={isLoading || isLoadingMembers}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${(isLoading || isLoadingMembers) ? 'animate-spin' : ''}`} />
            刷新
          </Button>
        </div>
      </div>
      
      <Tabs defaultValue="info" value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-4">
          <TabsTrigger value="info" className="flex items-center">
            <Building2 className="mr-2 h-4 w-4" />
            机构信息
          </TabsTrigger>
          <TabsTrigger value="members" className="flex items-center">
            <Users className="mr-2 h-4 w-4" />
            成员管理
          </TabsTrigger>
        </TabsList>
        
        {/* 机构信息标签内容 */}
        <TabsContent value="info">
          {isLoading ? (
            <Card>
              <CardHeader>
                <Skeleton className="h-6 w-32 mb-2" />
                <Skeleton className="h-4 w-64" />
              </CardHeader>
              <CardContent className="space-y-4">
                {Array(5).fill(0).map((_, i) => (
                  <div key={i} className="space-y-2">
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-10 w-full" />
                  </div>
                ))}
              </CardContent>
            </Card>
          ) : (
            <>
              <Card>
                <CardHeader>
                  <CardTitle>基本信息</CardTitle>
                  <CardDescription>管理您的机构基本信息</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">机构名称</Label>
                    <Input 
                      id="name" 
                      name="name" 
                      value={formData.name} 
                      onChange={handleChange} 
                      placeholder="请输入机构名称" 
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="description">机构描述</Label>
                    <Textarea 
                      id="description" 
                      name="description" 
                      value={formData.description || ''} 
                      onChange={handleChange} 
                      placeholder="请输入机构描述" 
                      className="min-h-32"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="contactPerson">联系人</Label>
                    <Input 
                      id="contactPerson" 
                      name="contactPerson" 
                      value={formData.contactPerson} 
                      onChange={handleChange} 
                      placeholder="请输入联系人姓名"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="contactPhone">联系电话</Label>
                    <Input 
                      id="contactPhone" 
                      name="contactPhone" 
                      value={formData.contactPhone || ''} 
                      onChange={handleChange} 
                      placeholder="请输入联系电话"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="address">地址</Label>
                    <Input 
                      id="address" 
                      name="address" 
                      value={formData.address || ''} 
                      onChange={handleChange} 
                      placeholder="请输入地址"
                    />
                  </div>
                </CardContent>
                <CardFooter className="flex justify-end">
                  <Button onClick={handleSave} disabled={isSaving}>
                    {isSaving ? (
                      <>
                        <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                        保存中...
                      </>
                    ) : (
                      <>
                        <Save className="h-4 w-4 mr-2" />
                        保存信息
                      </>
                    )}
                  </Button>
                </CardFooter>
              </Card>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                {/* Logo管理 */}
                <Card>
                  <CardHeader>
                    <CardTitle>机构Logo</CardTitle>
                    <CardDescription>上传或更新您的机构Logo</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex justify-center p-4 border rounded-md">
                      {institution?.logo ? (
                        <img 
                          src={institution.logo} 
                          alt={institution.name} 
                          className="max-h-40 object-contain"
                        />
                      ) : (
                        <div className="flex flex-col items-center justify-center text-muted-foreground h-40">
                          <Building2 className="h-12 w-12 mb-2" />
                          <p>暂无Logo</p>
                        </div>
                      )}
                    </div>
                    
                    <div className="flex justify-center">
                      <Label 
                        htmlFor="logo-upload" 
                        className="cursor-pointer flex items-center justify-center px-4 py-2 rounded-md bg-primary text-primary-foreground hover:bg-primary/90"
                      >
                        <Upload className="h-4 w-4 mr-2" />
                        {isUploading ? '上传中...' : '上传新Logo'}
                        <Input 
                          id="logo-upload" 
                          type="file" 
                          accept="image/*" 
                          onChange={handleLogoUpload}
                          disabled={isUploading}
                          className="sr-only"
                        />
                      </Label>
                    </div>
                    
                    <div className="text-xs text-muted-foreground text-center">
                      支持JPG, PNG, SVG格式，建议尺寸200x200px，大小不超过2MB
                    </div>
                  </CardContent>
                </Card>
                
                {/* 注册码管理 */}
                <Card>
                  <CardHeader>
                    <CardTitle>机构注册码</CardTitle>
                    <CardDescription>管理您的机构注册码，用于新成员注册</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <Alert>
                      <AlertTitle>重要提示</AlertTitle>
                      <AlertDescription>
                        机构注册码用于新成员注册时验证身份，请妥善保管。重置后，旧注册码将失效。
                      </AlertDescription>
                    </Alert>
                    
                    <div className="space-y-2">
                      <Label>当前注册码</Label>
                      <div className="flex items-center">
                        <Input 
                          value={institution?.registerCode || '尚未生成'} 
                          type={showRegisterCode ? 'text' : 'password'} 
                          readOnly 
                          className="font-mono"
                        />
                        <Button 
                          variant="ghost" 
                          size="icon"
                          onClick={() => setShowRegisterCode(!showRegisterCode)}
                          className="ml-2"
                        >
                          {showRegisterCode ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon"
                          onClick={() => handleCopyRegisterCode()}
                          className="ml-2"
                        >
                          <Copy className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                  <CardFooter>
                    <Dialog open={resetConfirmOpen} onOpenChange={setResetConfirmOpen}>
                      <DialogTrigger asChild>
                        <Button variant="outline" className="w-full">
                          <RefreshCw className="h-4 w-4 mr-2" />
                          重置注册码
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>确认重置注册码</DialogTitle>
                          <DialogDescription>
                            重置注册码后，旧注册码将立即失效。已注册的用户不受影响，但使用旧注册码的邀请链接将无法使用。
                          </DialogDescription>
                        </DialogHeader>
                        <div className="py-4">
                          <p className="text-sm text-destructive font-medium">此操作不可撤销，请确认您要继续。</p>
                        </div>
                        <DialogFooter>
                          <Button 
                            variant="outline" 
                            onClick={() => setResetConfirmOpen(false)}
                          >
                            取消
                          </Button>
                          <Button 
                            variant="destructive" 
                            onClick={handleResetRegisterCode}
                            disabled={isResetting}
                          >
                            {isResetting ? '重置中...' : '确认重置'}
                          </Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                  </CardFooter>
                </Card>
              </div>
            </>
          )}
        </TabsContent>
        
        {/* 成员管理标签内容 */}
        <TabsContent value="members">
          <div className="space-y-6">
            {/* 成员统计卡片 */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">当前成员数</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{memberStats.total}</div>
                  <p className="text-xs text-muted-foreground mt-1">
                    机构内已注册成员总数
                  </p>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">最大成员限制</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{memberStats.limit}</div>
                  <p className="text-xs text-muted-foreground mt-1">
                    机构可容纳的最大成员数
                  </p>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">剩余可用名额</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{memberStats.available}</div>
                  <p className="text-xs text-muted-foreground mt-1">
                    机构还可添加的成员数量
                  </p>
                </CardContent>
              </Card>
            </div>
            
            {/* 成员分布图表 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* 用户注册趋势图 */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-sm font-medium">成员注册趋势</CardTitle>
                  <CardDescription>机构内成员的注册时间分布情况</CardDescription>
                </CardHeader>
                <CardContent>
                  {registrationTrend.length > 0 ? (
                    <ChartContainer 
                      className="min-h-[300px] w-full"
                      config={{
                        count: {
                          label: '累计人数',
                          color: 'hsl(var(--primary))'
                        }
                      }}
                    >
                      <LineChart 
                        accessibilityLayer
                        data={registrationTrend}
                        margin={{ top: 5, right: 30, left: 20, bottom: 20 }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis 
                          dataKey="date" 
                          tickLine={false}
                          axisLine={false}
                          tickMargin={10}
                          tickFormatter={(value) => value.split('-').slice(1).join('/')}
                        />
                        <YAxis
                          tickLine={false}
                          axisLine={false}
                          tickMargin={10}
                        />
                        <ChartTooltip
                          content={
                            <ChartTooltipContent 
                              labelFormatter={(label) => `注册日期：${label}`}
                              formatter={(value) => [`${value} 人`, '累计注册人数']}
                            />
                          }
                        />
                        <Line
                          type="monotone"
                          dataKey="count"
                          stroke="var(--color-count)"
                          strokeWidth={2}
                          dot={{ r: 4, fill: "var(--color-count)" }}
                          activeDot={{ r: 6, fill: "var(--color-count)" }}
                        />
                      </LineChart>
                    </ChartContainer>
                  ) : (
                    <div className="h-[300px] w-full flex flex-col items-center justify-center text-muted-foreground">
                      <TrendingUp className="h-12 w-12 mb-4 opacity-20" />
                      <p>暂无注册趋势数据</p>
                    </div>
                  )}
                </CardContent>
              </Card>
              
              {/* 状态分布图表 */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-sm font-medium">成员状态分布</CardTitle>
                  <CardDescription>机构内成员的状态分布情况</CardDescription>
                </CardHeader>
                <CardContent>
                  {statusDistribution.length > 0 ? (
                    <ChartContainer 
                      className="min-h-[300px] w-full"
                      config={{
                        normal: {
                          label: '正常',
                          color: 'hsl(var(--success))'
                        },
                        disabled: {
                          label: '禁用',
                          color: 'hsl(var(--destructive))'
                        }
                      }}
                    >
                      <RechartsPieChart
                        accessibilityLayer
                        margin={{ top: 0, right: 0, bottom: 0, left: 0 }}
                      >
                        <Pie
                          data={[
                            { name: '正常', value: statusDistribution[0].value, fill: 'var(--color-normal)' },
                            { name: '禁用', value: statusDistribution[1].value, fill: 'var(--color-disabled)' }
                          ]}
                          cx="50%"
                          cy="50%"
                          outerRadius={80}
                          innerRadius={0}
                          paddingAngle={2}
                          dataKey="value"
                          labelLine={false}
                          label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                        />
                        <ChartTooltip
                          content={
                            <ChartTooltipContent 
                              formatter={(value, name) => [`${value} 人`, name]}
                            />
                          }
                        />
                      </RechartsPieChart>
                    </ChartContainer>
                  ) : (
                    <div className="h-[300px] w-full flex flex-col items-center justify-center text-muted-foreground">
                      <PieChart className="h-12 w-12 mb-4 opacity-20" />
                      <p>暂无状态分布数据</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
            
            {/* 功能按钮和搜索栏 */}
            <div className="flex flex-col sm:flex-row justify-between gap-4">
              <div className="flex gap-2">
                <Button onClick={handleOpenShareDialog}>
                  <UserPlus className="h-4 w-4 mr-2" />
                  邀请新成员
                </Button>
              </div>
              
              <div className="flex gap-2">
                <Input
                  placeholder="搜索成员..."
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  className="min-w-[200px]"
                />
                <Button variant="outline" onClick={handleSearch}>
                  <Search className="h-4 w-4 mr-2" />
                  搜索
                </Button>
              </div>
            </div>
            
            {/* 成员列表表格 */}
            <Card>
              <CardContent className="pt-6">
                {isLoadingMembers ? (
                  <div className="flex justify-center py-12">
                    <RefreshCw className="h-8 w-8 animate-spin text-muted-foreground" />
                  </div>
                ) : members.length === 0 ? (
                  <div className="text-center py-12 text-muted-foreground">
                    <Users className="h-12 w-12 mx-auto mb-4 opacity-20" />
                    <p>暂无成员数据</p>
                    {searchKeyword && (
                      <p className="text-sm mt-2">尝试使用其他关键字搜索</p>
                    )}
                  </div>
                ) : (
                  <Table>
                    <TableCaption>
                      共 {members.length} 名成员
                    </TableCaption>
                    <TableHeader>
                      <TableRow>
                        <TableHead>用户名</TableHead>
                        <TableHead>邮箱</TableHead>
                        <TableHead>注册时间</TableHead>
                        <TableHead>状态</TableHead>
                        <TableHead className="text-right">操作</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {members.map((member) => (
                        <TableRow key={member.id}>
                          <TableCell className="font-medium flex items-center gap-2">
                            {member.avatar ? (
                              <img 
                                src={member.avatar} 
                                alt={member.username} 
                                className="w-8 h-8 rounded-full" 
                              />
                            ) : (
                              <div className="w-8 h-8 bg-muted rounded-full flex items-center justify-center">
                                <User className="h-4 w-4" />
                              </div>
                            )}
                            {member.username}
                          </TableCell>
                          <TableCell>{member.email}</TableCell>
                          <TableCell>
                            {new Date(member.createdAt).toLocaleDateString('zh-CN', {
                              year: 'numeric',
                              month: 'long',
                              day: 'numeric'
                            })}
                          </TableCell>
                          <TableCell>
                            {getMemberStatusBadge(member.status)}
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleDeleteClick(member)}
                            >
                              <Trash2 className="h-4 w-4 text-destructive" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
                
                {/* 分页控件 */}
                {totalPages > 1 && (
                  <div className="flex justify-center mt-4 gap-1">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handlePageChange(1)}
                      disabled={currentPage === 1}
                    >
                      首页
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handlePageChange(currentPage - 1)}
                      disabled={currentPage === 1}
                    >
                      上一页
                    </Button>
                    
                    <span className="flex items-center px-2 text-sm">
                      第 {currentPage} 页 / 共 {totalPages} 页
                    </span>
                    
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handlePageChange(currentPage + 1)}
                      disabled={currentPage === totalPages}
                    >
                      下一页
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handlePageChange(totalPages)}
                      disabled={currentPage === totalPages}
                    >
                      末页
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
      
      {/* 移除成员确认对话框 */}
      <Dialog open={deleteConfirmOpen} onOpenChange={setDeleteConfirmOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认移除成员</DialogTitle>
            <DialogDescription>
              您确定要将成员"{memberToDelete?.username}"移出机构吗？此操作无法撤销。
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <Alert variant="destructive">
              <AlertTitle>警告</AlertTitle>
              <AlertDescription>
                移除后，该成员将失去对机构资源的访问权限，并且必须使用邀请码重新加入。
              </AlertDescription>
            </Alert>
          </div>
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => setDeleteConfirmOpen(false)}
              disabled={isDeleting}
            >
              取消
            </Button>
            <Button 
              variant="destructive" 
              onClick={handleDeleteConfirm}
              disabled={isDeleting}
            >
              {isDeleting ? (
                <>
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                  移除中...
                </>
              ) : (
                '确认移除'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      {/* 分享注册码对话框 */}
      <Dialog open={shareCodeOpen} onOpenChange={setShareCodeOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>邀请新成员</DialogTitle>
            <DialogDescription>
              分享以下注册码，让新成员加入您的机构。
            </DialogDescription>
          </DialogHeader>
          <div className="py-4 space-y-4">
            <div className="space-y-2">
              <Label>机构注册码</Label>
              <div className="flex">
                <Input 
                  value={institution?.registerCode || ''} 
                  readOnly 
                  className="font-mono"
                />
                <Button 
                  variant="outline" 
                  className="ml-2"
                  onClick={() => handleCopyRegisterCode(institution?.registerCode)}
                >
                  <Copy className="h-4 w-4 mr-2" />
                  复制
                </Button>
              </div>
            </div>
            
            <div className="space-y-2">
              <Label>邀请链接</Label>
              <div className="flex">
                <Input 
                  value={`${window.location.origin}/register?type=institution`} 
                  readOnly 
                />
                <Button 
                  variant="outline" 
                  className="ml-2"
                  onClick={() => handleCopyRegisterCode(`${window.location.origin}/register?type=institution`)}
                >
                  <Copy className="h-4 w-4 mr-2" />
                  复制
                </Button>
              </div>
            </div>
            
            <Alert>
              <AlertTitle>使用说明</AlertTitle>
              <AlertDescription>
                新用户访问邀请链接后，需要在注册页面输入注册码完成机构成员注册。
              </AlertDescription>
            </Alert>
          </div>
          <DialogFooter>
            <Button onClick={() => setShareCodeOpen(false)}>
              关闭
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 