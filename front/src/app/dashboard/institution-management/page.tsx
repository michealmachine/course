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
  EyeOff
} from 'lucide-react';

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

import institutionManagementService, { InstitutionInfo, InstitutionUpdateRequest } from '@/services/institution-management';

export default function InstitutionManagementPage() {
  const [institution, setInstitution] = useState<InstitutionInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isResetting, setIsResetting] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [showRegisterCode, setShowRegisterCode] = useState(false);
  const [resetConfirmOpen, setResetConfirmOpen] = useState(false);
  
  // 表单字段
  const [formData, setFormData] = useState<InstitutionUpdateRequest>({
    name: '',
    description: '',
    contactPerson: '',
    contactPhone: '',
    address: '',
  });
  
  // 加载机构信息
  useEffect(() => {
    fetchInstitutionDetail();
  }, []);
  
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
  const handleCopyRegisterCode = () => {
    if (!institution) return;
    
    navigator.clipboard.writeText(institution.registerCode || '')
      .then(() => toast.success('注册码已复制到剪贴板'))
      .catch(() => toast.error('复制失败，请手动复制'));
  };
  
  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">机构管理</h1>
          <p className="text-muted-foreground">管理您的机构信息</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchInstitutionDetail}
            disabled={isLoading}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            刷新
          </Button>
        </div>
      </div>
      
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
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
                      onClick={handleCopyRegisterCode}
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
    </div>
  );
} 