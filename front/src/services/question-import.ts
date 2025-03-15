'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import axios, { AxiosResponse } from 'axios';
import { QuestionImportResultVO } from '@/types/question';

/**
 * 试题导入服务
 */
const questionImportService = {
  /**
   * 下载试题导入模板
   */
  downloadTemplate: async (): Promise<void> => {
    try {
      // 由于这个请求直接返回文件，不是JSON，使用axios直接请求
      const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';
      const token = localStorage.getItem('token');
      
      const response = await axios.get(`${baseUrl}/questions/import/template`, {
        responseType: 'blob',
        headers: {
          Authorization: token ? `Bearer ${token}` : '',
        }
      });
      
      // 创建下载链接
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', '试题导入模板.xlsx');
      document.body.appendChild(link);
      link.click();
      
      // 清理
      window.URL.revokeObjectURL(url);
      document.body.removeChild(link);
    } catch (error) {
      console.error('下载试题导入模板失败:', error);
      throw error;
    }
  },

  /**
   * 导入试题
   * @param file Excel文件
   * @param institutionId 机构ID
   * @param batchSize 批处理大小（可选）
   */
  importQuestions: async (
    file: File,
    institutionId: number,
    batchSize?: number
  ): Promise<QuestionImportResultVO> => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('institutionId', institutionId.toString());
      if (batchSize) {
        formData.append('batchSize', batchSize.toString());
      }

      const response: AxiosResponse<ApiResponse<QuestionImportResultVO>> = await request.post(
        '/questions/import',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      return response.data.data;
    } catch (error) {
      console.error('导入试题失败:', error);
      throw error;
    }
  },
};

export default questionImportService; 