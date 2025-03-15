'use client';

import { useState, useEffect, useCallback } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { Plus, Upload, Download } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import useQuestionStore from '@/stores/question-store';
import { useRouter, useSearchParams } from 'next/navigation';
import { getCreateGroupUrl } from '@/utils/navigationUtils';

// 引入组件
import { 
  QuestionList,
  QuestionGroupList,
  QuestionTagList,
  QuestionImportModal
} from '@/components/question';

export default function QuestionsPage() {
  const { user } = useAuthStore();
  const router = useRouter();
  const searchParams = useSearchParams();
  
  // 从URL参数中获取激活的标签页
  const tabParam = searchParams.get('tab');
  const [activeTab, setActiveTab] = useState<string>(
    tabParam && ['questions', 'groups', 'tags'].includes(tabParam) ? tabParam : 'questions'
  );
  
  // 添加刷新计数器，用于触发组件重新渲染
  const [refreshCounter, setRefreshCounter] = useState(0);
  const [importModalOpen, setImportModalOpen] = useState(false);
  const institutionId = user?.institutionId || 0;

  // 触发刷新
  const triggerRefresh = useCallback(() => {
    setRefreshCounter(prev => prev + 1);
  }, []);

  // 处理标签页变化
  const handleTabChange = (value: string) => {
    setActiveTab(value);
    // 更新URL，但不刷新页面
    router.replace(`/dashboard/questions?tab=${value}`, { scroll: false });
    
    // 当切换到题组标签页时，触发刷新
    if (value === 'groups') {
      triggerRefresh();
    }
  };

  // 处理创建题组
  const handleCreateGroup = () => {
    router.push(getCreateGroupUrl());
  };
  
  // 创建新题目
  const handleCreateQuestion = () => {
    router.push('/dashboard/questions/create');
  };
  
  // 题目导入成功回调
  const handleImportSuccess = () => {
    // 刷新题目列表
    // TODO: 实现刷新列表逻辑
  };
  
  // 创建新标签
  const handleCreateQuestionTag = () => {
    router.push('/dashboard/question-tags/create');
  };
  
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">题库管理</h2>
          <p className="text-muted-foreground">
            管理教学题库，包括题目、分组和标签
          </p>
        </div>
        
        {activeTab === 'questions' && (
          <div className="flex space-x-2">
            <Button
              onClick={() => setImportModalOpen(true)}
              variant="outline"
              size="sm"
            >
              <Upload className="h-4 w-4 mr-2" />
              批量导入
            </Button>
            <Button
              onClick={handleCreateQuestion}
              size="sm"
            >
              <Plus className="h-4 w-4 mr-2" />
              创建题目
            </Button>
          </div>
        )}
        
        {activeTab === 'groups' && (
          <Button
            onClick={handleCreateGroup}
            size="sm"
          >
            <Plus className="h-4 w-4 mr-2" />
            创建题目组
          </Button>
        )}
        
        {activeTab === 'tags' && (
          <Button
            onClick={handleCreateQuestionTag}
            size="sm"
          >
            <Plus className="h-4 w-4 mr-2" />
            创建标签
          </Button>
        )}
      </div>
      
      <Separator />
      
      <Tabs
        defaultValue="questions"
        value={activeTab}
        onValueChange={handleTabChange}
        className="space-y-4"
      >
        <TabsList>
          <TabsTrigger value="questions">题目管理</TabsTrigger>
          <TabsTrigger value="groups">题目分组</TabsTrigger>
          <TabsTrigger value="tags">题目标签</TabsTrigger>
        </TabsList>
        
        <TabsContent value="questions" className="space-y-4">
          <QuestionList institutionId={institutionId} />
        </TabsContent>
        
        <TabsContent value="groups" className="space-y-4">
          <QuestionGroupList 
            institutionId={institutionId} 
            key={`group-list-${refreshCounter}`} 
            onDataChange={triggerRefresh} 
          />
        </TabsContent>
        
        <TabsContent value="tags" className="space-y-4">
          <QuestionTagList institutionId={institutionId} />
        </TabsContent>
      </Tabs>
      
      {/* 导入模态框 */}
      <QuestionImportModal
        institutionId={institutionId}
        isOpen={importModalOpen}
        onClose={() => setImportModalOpen(false)}
        onSuccess={handleImportSuccess}
      />
    </div>
  );
} 