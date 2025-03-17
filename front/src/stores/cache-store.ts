import { create } from 'zustand';
import { Tag, Category } from '@/types/course';

interface CacheState {
  // 标签缓存
  tags: Tag[] | null;
  tagMap: Map<number, Tag>;
  lastTagsFetchTime: number;
  
  // 分类缓存
  categories: Category[] | null;
  categoryMap: Map<number, Category>;
  lastCategoriesFetchTime: number;
  
  // 缓存时间设置
  CACHE_TTL: number; // 缓存有效期（毫秒）

  // 标签相关方法
  setTags: (tags: Tag[]) => void;
  getTagById: (id: number) => Tag | undefined;
  getTagsByIds: (ids: number[]) => Tag[];
  clearTagsCache: () => void;
  isTagsCacheValid: () => boolean;

  // 分类相关方法
  setCategories: (categories: Category[]) => void;
  getCategoryById: (id: number) => Category | undefined;
  getCategoriesByIds: (ids: number[]) => Category[];
  clearCategoriesCache: () => void;
  isCategoriesCacheValid: () => boolean;

  // 通用方法
  clearAllCache: () => void;
}

export const useCacheStore = create<CacheState>((set, get) => ({
  // 初始状态
  tags: null,
  tagMap: new Map(),
  lastTagsFetchTime: 0,
  
  categories: null,
  categoryMap: new Map(),
  lastCategoriesFetchTime: 0,
  
  CACHE_TTL: 5 * 60 * 1000, // 5分钟缓存

  // 标签相关方法
  setTags: (tags: Tag[]) => {
    const tagMap = new Map(tags.map(tag => [tag.id, tag]));
    set({ 
      tags, 
      tagMap,
      lastTagsFetchTime: Date.now() 
    });
  },

  getTagById: (id: number) => {
    return get().tagMap.get(id);
  },

  getTagsByIds: (ids: number[]) => {
    const { tagMap } = get();
    return ids.map(id => tagMap.get(id)).filter((tag): tag is Tag => tag !== undefined);
  },

  clearTagsCache: () => {
    set({ 
      tags: null, 
      tagMap: new Map(),
      lastTagsFetchTime: 0 
    });
  },

  isTagsCacheValid: () => {
    const { lastTagsFetchTime, CACHE_TTL, tags } = get();
    return tags !== null && (Date.now() - lastTagsFetchTime < CACHE_TTL);
  },

  // 分类相关方法
  setCategories: (categories: Category[]) => {
    const categoryMap = new Map(categories.map(category => [category.id, category]));
    set({ 
      categories, 
      categoryMap,
      lastCategoriesFetchTime: Date.now() 
    });
  },

  getCategoryById: (id: number) => {
    return get().categoryMap.get(id);
  },

  getCategoriesByIds: (ids: number[]) => {
    const { categoryMap } = get();
    return ids.map(id => categoryMap.get(id)).filter((category): category is Category => category !== undefined);
  },

  clearCategoriesCache: () => {
    set({ 
      categories: null, 
      categoryMap: new Map(),
      lastCategoriesFetchTime: 0 
    });
  },

  isCategoriesCacheValid: () => {
    const { lastCategoriesFetchTime, CACHE_TTL, categories } = get();
    return categories !== null && (Date.now() - lastCategoriesFetchTime < CACHE_TTL);
  },

  // 通用方法
  clearAllCache: () => {
    set({ 
      tags: null, 
      tagMap: new Map(),
      lastTagsFetchTime: 0,
      categories: null, 
      categoryMap: new Map(),
      lastCategoriesFetchTime: 0 
    });
  },
})); 