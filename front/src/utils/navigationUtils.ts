/**
 * 导航工具函数
 */

/**
 * 获取题目管理页面URL，可以指定激活的标签页
 * @param tab 要激活的标签页
 * @returns 题目管理页面URL
 */
export function getQuestionsPageUrl(tab: 'questions' | 'groups' | 'tags' = 'questions'): string {
  return `/dashboard/questions?tab=${tab}`;
}

/**
 * 获取创建题目组的URL
 * @returns 创建题目组页面URL
 */
export function getCreateGroupUrl(): string {
  return '/dashboard/question-groups/create';
}

/**
 * 获取编辑题目组的URL
 * @param id 题目组ID
 * @returns 编辑题目组页面URL
 */
export function getEditGroupUrl(id: number): string {
  return `/dashboard/question-groups/${id}?edit=true`;
}

/**
 * 获取题目组详情页URL
 * @param id 题目组ID
 * @returns 题目组详情页URL
 */
export function getGroupDetailUrl(id: number): string {
  return `/dashboard/question-groups/${id}`;
} 