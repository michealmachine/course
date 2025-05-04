import { Course } from '@/types/course';
import { Page } from '@/types/common';
import { request } from './api';

/**
 * 元数据服务
 * 提供标签和分类相关的扩展功能
 */
class MetadataService {
  /**
   * 获取标签关联的所有课程（不限状态）
   * @param tagId 标签ID
   * @param page 页码
   * @param size 每页大小
   * @returns 课程分页
   */
  async getTagCourses(tagId: number, page: number = 0, size: number = 10): Promise<Page<Course>> {
    const response = await request.get<Page<Course>>(`/metadata/tags/${tagId}/courses`, {
      params: { page, size }
    });
    return response.data.data;
  }

  /**
   * 获取分类关联的所有课程（不限状态）
   * @param categoryId 分类ID
   * @param page 页码
   * @param size 每页大小
   * @returns 课程分页
   */
  async getCategoryCourses(categoryId: number, page: number = 0, size: number = 10): Promise<Page<Course>> {
    const response = await request.get<Page<Course>>(`/metadata/categories/${categoryId}/courses`, {
      params: { page, size }
    });
    return response.data.data;
  }
}

export default new MetadataService();
