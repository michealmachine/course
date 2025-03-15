/**
 * 题目服务辅助函数
 */
import { QuestionType, QuestionDifficulty } from '@/types/question';
import questionService from './question';
import { toast } from 'sonner';

/**
 * 执行题目搜索
 * @param params - 搜索参数
 * @param callbacks - 回调函数
 */
export const executeQuestionSearch = async (
  params: {
    institutionId: number;
    page?: number;
    pageSize?: number;
    keyword?: string;
    type?: number | null;
    difficulty?: number | null;
    tagIds?: number[] | null;
  },
  callbacks: {
    onLoading?: (loading: boolean) => void;
    onSuccess?: (content: any[], total: number) => void;
    onError?: (error: any) => void;
  }
) => {
  // 设置加载状态
  if (callbacks.onLoading) {
    callbacks.onLoading(true);
  }

  try {
    // 构建API参数
    const apiParams: Record<string, any> = {
      page: params.page !== undefined ? params.page : 0,
      pageSize: params.pageSize || 10,
      institutionId: params.institutionId
    };

    // 添加可选参数
    if (params.keyword) apiParams.keyword = params.keyword;
    if (params.type !== undefined) apiParams.type = params.type;
    if (params.difficulty !== undefined) apiParams.difficulty = params.difficulty;
    if (params.tagIds && params.tagIds.length > 0) apiParams.tagIds = params.tagIds;

    // 打印请求参数
    console.log('搜索请求参数:', apiParams);

    // 发送请求
    const response = await questionService.getQuestionList(apiParams);
    
    // 处理成功回调
    if (callbacks.onSuccess) {
      callbacks.onSuccess(response.content, response.totalElements);
    }
  } catch (error) {
    console.error('搜索题目失败:', error);
    toast.error('搜索题目失败');
    
    // 处理错误回调
    if (callbacks.onError) {
      callbacks.onError(error);
    }
  } finally {
    // 重置加载状态
    if (callbacks.onLoading) {
      callbacks.onLoading(false);
    }
  }
}; 