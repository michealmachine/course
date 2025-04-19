import { AxiosResponse } from 'axios';
import request from '@/utils/request';
import { ApiResponse } from '@/types/api';
import { 
  InstitutionLearningStatisticsVO, 
  DailyLearningStatVO,
  ActivityTypeStatVO,
  LearningHeatmapVO,
  LearningProgressTrendVO
} from '@/types/learning-stats';

/**
 * 管理员学习统计服务接口
 */
export interface AdminLearningStatisticsService {
  /**
   * 获取平台学习时长统计
   * @returns 平台学习时长统计
   */
  getLearningDuration(): Promise<{
    todayLearningDuration: number;
    totalLearningDuration: number;
    totalLearners: number;
  }>;

  /**
   * 获取平台每日学习统计
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 每日学习统计列表
   */
  getDailyStatistics(startDate?: string, endDate?: string): Promise<DailyLearningStatVO[]>;

  /**
   * 获取平台活动类型统计
   * @returns 活动类型统计列表
   */
  getActivityTypeStatistics(): Promise<ActivityTypeStatVO[]>;

  /**
   * 获取所有课程学习统计
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  getAllCourseStatistics(page?: number, size?: number): Promise<{
    content: InstitutionLearningStatisticsVO.CourseStatisticsVO[];
    totalElements: number;
    totalPages: number;
  }>;

  /**
   * 获取机构课程学习统计
   * @param institutionId 机构ID
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  getInstitutionCourseStatistics(
    institutionId: number,
    page?: number,
    size?: number
  ): Promise<{
    content: InstitutionLearningStatisticsVO.CourseStatisticsVO[];
    totalElements: number;
    totalPages: number;
  }>;

  /**
   * 获取课程学习统计概览
   * @param courseId 课程ID
   * @returns 课程学习统计概览
   */
  getCourseStatisticsOverview(
    courseId: number
  ): Promise<InstitutionLearningStatisticsVO.CourseStatisticsVO>;

  /**
   * 获取课程每日学习统计
   * @param courseId 课程ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 每日学习统计列表
   */
  getCourseDailyStatistics(
    courseId: number,
    startDate?: string,
    endDate?: string
  ): Promise<DailyLearningStatVO[]>;

  /**
   * 获取课程活动类型统计
   * @param courseId 课程ID
   * @returns 活动类型统计列表
   */
  getCourseActivityTypeStatistics(courseId: number): Promise<ActivityTypeStatVO[]>;

  /**
   * 获取课程学生学习统计
   * @param courseId 课程ID
   * @param page 页码
   * @param size 每页数量
   * @returns 学生学习统计分页
   */
  getCourseStudentStatistics(
    courseId: number,
    page?: number,
    size?: number
  ): Promise<{
    content: InstitutionLearningStatisticsVO.StudentLearningVO[];
    totalElements: number;
    totalPages: number;
  }>;

  /**
   * 获取课程学习热力图数据
   * @param courseId 课程ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 学习热力图数据
   */
  getCourseLearningHeatmap(
    courseId: number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningHeatmapVO>;

  /**
   * 获取课程学习进度趋势
   * @param courseId 课程ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 学习进度趋势数据
   */
  getCourseLearningProgressTrend(
    courseId: number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningProgressTrendVO>;

  /**
   * 获取机构学习统计排行
   * @param sortBy 排序字段
   * @param limit 数量限制
   * @returns 机构学习统计排行列表
   */
  getInstitutionRanking(
    sortBy?: string,
    limit?: number
  ): Promise<InstitutionLearningStatisticsVO.InstitutionStatisticsVO[]>;

  /**
   * 获取课程学习统计排行
   * @param sortBy 排序字段
   * @param institutionId 机构ID（可选）
   * @param limit 数量限制
   * @returns 课程学习统计排行列表
   */
  getCourseRanking(
    sortBy?: string,
    institutionId?: number,
    limit?: number
  ): Promise<InstitutionLearningStatisticsVO.CourseStatisticsVO[]>;

  /**
   * 获取机构课程占比统计
   * @returns 机构课程占比统计
   */
  getInstitutionCourseDistribution(): Promise<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO[]>;
}

/**
 * 管理员学习统计服务实现
 */
class AdminLearningStatisticsServiceImpl implements AdminLearningStatisticsService {
  /**
   * 获取平台学习时长统计
   * @returns 平台学习时长统计
   */
  async getLearningDuration(): Promise<{
    todayLearningDuration: number;
    totalLearningDuration: number;
    totalLearners: number;
  }> {
    try {
      const response: AxiosResponse<ApiResponse<{
        todayLearningDuration: number;
        totalLearningDuration: number;
        totalLearners: number;
      }>> = await request.get('/admin/learning-statistics/duration');
      return response.data.data;
    } catch (error) {
      console.error('获取平台学习时长统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取平台每日学习统计
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 每日学习统计列表
   */
  async getDailyStatistics(startDate?: string, endDate?: string): Promise<DailyLearningStatVO[]> {
    try {
      let url = '/admin/learning-statistics/daily';
      const params: Record<string, string> = {};
      
      if (startDate) {
        params.startDate = startDate;
      }
      
      if (endDate) {
        params.endDate = endDate;
      }
      
      const response: AxiosResponse<ApiResponse<DailyLearningStatVO[]>> = 
        await request.get(url, { params });
      return response.data.data;
    } catch (error) {
      console.error('获取平台每日学习统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取平台活动类型统计
   * @returns 活动类型统计列表
   */
  async getActivityTypeStatistics(): Promise<ActivityTypeStatVO[]> {
    try {
      const response: AxiosResponse<ApiResponse<ActivityTypeStatVO[]>> = 
        await request.get('/admin/learning-statistics/activity-types');
      return response.data.data;
    } catch (error) {
      console.error('获取平台活动类型统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取所有课程学习统计
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  async getAllCourseStatistics(page: number = 0, size: number = 10): Promise<{
    content: InstitutionLearningStatisticsVO.CourseStatisticsVO[];
    totalElements: number;
    totalPages: number;
  }> {
    try {
      const response: AxiosResponse<ApiResponse<{
        content: InstitutionLearningStatisticsVO.CourseStatisticsVO[];
        totalElements: number;
        totalPages: number;
      }>> = await request.get('/admin/learning-statistics/courses', {
        params: { page, size }
      });
      return response.data.data;
    } catch (error) {
      console.error('获取所有课程学习统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取机构课程学习统计
   * @param institutionId 机构ID
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  async getInstitutionCourseStatistics(
    institutionId: number,
    page: number = 0,
    size: number = 10
  ): Promise<{
    content: InstitutionLearningStatisticsVO.CourseStatisticsVO[];
    totalElements: number;
    totalPages: number;
  }> {
    try {
      const response: AxiosResponse<ApiResponse<{
        content: InstitutionLearningStatisticsVO.CourseStatisticsVO[];
        totalElements: number;
        totalPages: number;
      }>> = await request.get(`/admin/learning-statistics/institutions/${institutionId}/courses`, {
        params: { page, size }
      });
      return response.data.data;
    } catch (error) {
      console.error('获取机构课程学习统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取课程学习统计概览
   * @param courseId 课程ID
   * @returns 课程学习统计概览
   */
  async getCourseStatisticsOverview(
    courseId: number
  ): Promise<InstitutionLearningStatisticsVO.CourseStatisticsVO> {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionLearningStatisticsVO.CourseStatisticsVO>> = 
        await request.get(`/admin/learning-statistics/courses/${courseId}/overview`);
      return response.data.data;
    } catch (error) {
      console.error('获取课程学习统计概览失败:', error);
      throw error;
    }
  }

  /**
   * 获取课程每日学习统计
   * @param courseId 课程ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 每日学习统计列表
   */
  async getCourseDailyStatistics(
    courseId: number,
    startDate?: string,
    endDate?: string
  ): Promise<DailyLearningStatVO[]> {
    try {
      let url = `/admin/learning-statistics/courses/${courseId}/daily`;
      const params: Record<string, string> = {};
      
      if (startDate) {
        params.startDate = startDate;
      }
      
      if (endDate) {
        params.endDate = endDate;
      }
      
      const response: AxiosResponse<ApiResponse<DailyLearningStatVO[]>> = 
        await request.get(url, { params });
      return response.data.data;
    } catch (error) {
      console.error('获取课程每日学习统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取课程活动类型统计
   * @param courseId 课程ID
   * @returns 活动类型统计列表
   */
  async getCourseActivityTypeStatistics(courseId: number): Promise<ActivityTypeStatVO[]> {
    try {
      const response: AxiosResponse<ApiResponse<ActivityTypeStatVO[]>> = 
        await request.get(`/admin/learning-statistics/courses/${courseId}/activity-types`);
      return response.data.data;
    } catch (error) {
      console.error('获取课程活动类型统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取课程学生学习统计
   * @param courseId 课程ID
   * @param page 页码
   * @param size 每页数量
   * @returns 学生学习统计分页
   */
  async getCourseStudentStatistics(
    courseId: number,
    page: number = 0,
    size: number = 10
  ): Promise<{
    content: InstitutionLearningStatisticsVO.StudentLearningVO[];
    totalElements: number;
    totalPages: number;
  }> {
    try {
      const response: AxiosResponse<ApiResponse<{
        content: InstitutionLearningStatisticsVO.StudentLearningVO[];
        totalElements: number;
        totalPages: number;
      }>> = await request.get(`/admin/learning-statistics/courses/${courseId}/students`, {
        params: { page, size }
      });
      return response.data.data;
    } catch (error) {
      console.error('获取课程学生学习统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取课程学习热力图数据
   * @param courseId 课程ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 学习热力图数据
   */
  async getCourseLearningHeatmap(
    courseId: number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningHeatmapVO> {
    try {
      let url = `/admin/learning-statistics/courses/${courseId}/heatmap`;
      const params: Record<string, string> = {};
      
      if (startDate) {
        params.startDate = startDate;
      }
      
      if (endDate) {
        params.endDate = endDate;
      }
      
      const response: AxiosResponse<ApiResponse<LearningHeatmapVO>> = 
        await request.get(url, { params });
      return response.data.data;
    } catch (error) {
      console.error('获取课程学习热力图数据失败:', error);
      throw error;
    }
  }

  /**
   * 获取课程学习进度趋势
   * @param courseId 课程ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 学习进度趋势数据
   */
  async getCourseLearningProgressTrend(
    courseId: number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningProgressTrendVO> {
    try {
      let url = `/admin/learning-statistics/courses/${courseId}/progress-trend`;
      const params: Record<string, string> = {};
      
      if (startDate) {
        params.startDate = startDate;
      }
      
      if (endDate) {
        params.endDate = endDate;
      }
      
      const response: AxiosResponse<ApiResponse<LearningProgressTrendVO>> = 
        await request.get(url, { params });
      return response.data.data;
    } catch (error) {
      console.error('获取课程学习进度趋势失败:', error);
      throw error;
    }
  }

  /**
   * 获取机构学习统计排行
   * @param sortBy 排序字段
   * @param limit 数量限制
   * @returns 机构学习统计排行列表
   */
  async getInstitutionRanking(
    sortBy: string = 'totalDuration',
    limit: number = 10
  ): Promise<InstitutionLearningStatisticsVO.InstitutionStatisticsVO[]> {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionLearningStatisticsVO.InstitutionStatisticsVO[]>> = 
        await request.get('/admin/learning-statistics/institutions/ranking', {
          params: { sortBy, limit }
        });
      return response.data.data;
    } catch (error) {
      console.error('获取机构学习统计排行失败:', error);
      throw error;
    }
  }

  /**
   * 获取课程学习统计排行
   * @param sortBy 排序字段
   * @param institutionId 机构ID（可选）
   * @param limit 数量限制
   * @returns 课程学习统计排行列表
   */
  async getCourseRanking(
    sortBy: string = 'totalDuration',
    institutionId?: number,
    limit: number = 10
  ): Promise<InstitutionLearningStatisticsVO.CourseStatisticsVO[]> {
    try {
      const params: Record<string, string | number> = { sortBy, limit };
      
      if (institutionId !== undefined) {
        params.institutionId = institutionId;
      }
      
      const response: AxiosResponse<ApiResponse<InstitutionLearningStatisticsVO.CourseStatisticsVO[]>> = 
        await request.get('/admin/learning-statistics/courses/ranking', { params });
      return response.data.data;
    } catch (error) {
      console.error('获取课程学习统计排行失败:', error);
      throw error;
    }
  }

  /**
   * 获取机构课程占比统计
   * @returns 机构课程占比统计
   */
  async getInstitutionCourseDistribution(): Promise<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO[]> {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO[]>> = 
        await request.get('/admin/learning-statistics/institutions/course-distribution');
      return response.data.data;
    } catch (error) {
      console.error('获取机构课程占比统计失败:', error);
      throw error;
    }
  }
}

// 导出服务实例
export const adminLearningStatisticsService = new AdminLearningStatisticsServiceImpl();
