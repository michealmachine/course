'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Copy, Building2, Users, BarChart, BookOpen } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { InstitutionVO } from '@/types/institution';
import { ApiResponse } from '@/types/api';
import { InstitutionBasicInfo } from './institution-basic-info';
import { InstitutionUserList } from './institution-user-list';
import { InstitutionStatistics } from './institution-statistics';
import { InstitutionCourseList } from './institution-course-list';

interface InstitutionDetailDialogProps {
  institution: InstitutionVO;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function InstitutionDetailDialog({
  institution,
  open,
  onOpenChange
}: InstitutionDetailDialogProps) {
  const [activeTab, setActiveTab] = useState('basic');
  const [detailedInstitution, setDetailedInstitution] = useState<InstitutionVO | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [stats, setStats] = useState<any>(null);

  useEffect(() => {
    if (open && institution.id) {
      fetchInstitutionDetail();
      fetchInstitutionStats();
    }
  }, [open, institution.id]);

  const fetchInstitutionDetail = async () => {
    if (!institution.id) return;

    setIsLoading(true);
    try {
      const response = await request.get<InstitutionVO>(`/admin/institutions/${institution.id}`);

      if (response.data.code === 200 && response.data.data) {
        setDetailedInstitution(response.data.data);
      } else {
        toast.error('获取机构详情失败');
      }
    } catch (error) {
      console.error('获取机构详情出错:', error);
      toast.error('获取机构详情出错');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchInstitutionStats = async () => {
    if (!institution.id) return;

    try {
      console.log('正在获取机构统计数据，机构ID:', institution.id);
      const response = await request.get<any>(`/admin/institutions/${institution.id}/stats`);

      if (response.data.code === 200 && response.data.data) {
        console.log('获取到机构统计数据:', response.data.data);
        setStats(response.data.data);
      } else {
        console.warn('获取机构统计数据失败，响应:', response.data);
      }
    } catch (error) {
      console.error('获取机构统计数据出错:', error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="!w-[1200px] !max-w-[1200px] max-h-[90vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Building2 className="h-5 w-5" />
            {institution.name}
          </DialogTitle>
          <DialogDescription>
            查看机构详细信息、用户、课程和统计数据
          </DialogDescription>
        </DialogHeader>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 overflow-hidden flex flex-col">
          <TabsList className="grid grid-cols-4">
            <TabsTrigger value="basic">基本信息</TabsTrigger>
            <TabsTrigger value="users">用户管理</TabsTrigger>
            <TabsTrigger value="courses">课程管理</TabsTrigger>
            <TabsTrigger value="statistics">统计数据</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-auto mt-4">
            <TabsContent value="basic" className="h-full">
              <InstitutionBasicInfo
                institution={detailedInstitution || institution}
                isLoading={isLoading}
              />
            </TabsContent>

            <TabsContent value="users" className="h-full">
              <InstitutionUserList
                institutionId={institution.id}
                institutionName={institution.name}
              />
            </TabsContent>

            <TabsContent value="courses" className="h-full">
              <InstitutionCourseList
                institutionId={institution.id}
                institutionName={institution.name}
              />
            </TabsContent>

            <TabsContent value="statistics" className="h-full">
              <InstitutionStatistics
                institutionId={institution.id}
                stats={stats}
              />
            </TabsContent>
          </div>
        </Tabs>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            关闭
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
