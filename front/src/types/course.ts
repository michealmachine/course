// 标签类型定义
export interface Tag {
  id: number;
  name: string;
  description?: string;
  useCount?: number;
  courseCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

// 标签创建/更新请求参数
export interface TagDTO {
  id?: number;
  name: string;
  description?: string;
}

// 分类类型定义
export interface Category {
  id: number;
  name: string;
  code: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  level?: number;
  orderIndex?: number;
  enabled?: boolean;
  icon?: string;
  courseCount?: number;
  childrenCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

// 分类创建/更新请求参数
export interface CategoryDTO {
  id?: number;
  name: string;
  code: string;
  description?: string;
  parentId?: number;
  orderIndex?: number;
  enabled?: boolean;
  icon?: string;
}

// 分类树结构
export interface CategoryTree {
  id: number;
  name: string;
  code: string;
  description?: string;
  level?: number;
  orderIndex?: number;
  enabled?: boolean;
  icon?: string;
  courseCount?: number;
  children?: CategoryTree[];
  fullPath?: string;
} 