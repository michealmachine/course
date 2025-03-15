'use client';

import { create } from 'zustand';
import { Question, QuestionDifficulty, QuestionGroup, QuestionTag, QuestionType } from '@/types/question';

interface QuestionStore {
  // 问题列表状态
  questions: Question[];
  totalQuestions: number;
  currentPage: number;
  pageSize: number;
  
  // 筛选条件
  filterTitle: string;
  filterKeyword: string;
  filterType: QuestionType | null;
  filterDifficulty: QuestionDifficulty | null;
  filterTagIds: number[];
  filterGroupId: number | null;
  
  // 多选状态
  selectedQuestionIds: number[];
  isSelectAll: boolean;
  
  // 问题详情
  currentQuestion: Question | null;
  
  // 问题组状态
  questionGroups: QuestionGroup[];
  currentQuestionGroup: QuestionGroup | null;
  
  // 问题标签状态
  questionTags: QuestionTag[];
  
  // 导入相关状态
  isImporting: boolean;
  importProgress: number;
  
  // Action: 设置问题列表
  setQuestions: (questions: Question[], total: number) => void;
  
  // Action: 设置分页
  setPage: (page: number, pageSize: number) => void;
  
  // Action: 设置筛选条件
  setFilter: (filterData: {
    title?: string;
    keyword?: string;
    type?: QuestionType | null;
    difficulty?: QuestionDifficulty | null;
    tagIds?: number[];
    groupId?: number | null;
  }) => void;
  
  // Action: 选择问题
  selectQuestion: (id: number, selected: boolean) => void;
  
  // Action: 全选/取消全选
  selectAll: (selected: boolean) => void;
  
  // Action: 设置当前问题
  setCurrentQuestion: (question: Question | null) => void;
  
  // Action: 设置问题组列表
  setQuestionGroups: (groups: QuestionGroup[]) => void;
  
  // Action: 设置当前问题组
  setCurrentQuestionGroup: (group: QuestionGroup | null) => void;
  
  // Action: 设置问题标签列表
  setQuestionTags: (tags: QuestionTag[]) => void;
  
  // Action: 设置导入状态
  setImportState: (isImporting: boolean, progress: number) => void;
  
  // Action: 重置状态
  resetState: () => void;
  
  // Action: 设置筛选类型
  setFilterType: (type: QuestionType | null | any) => void;
  
  // Action: 设置当前页码
  setCurrentPage: (page: number) => void;
  
  // Action: 设置查询参数
  setQueryParams: (params: any) => void;
}

const useQuestionStore = create<QuestionStore>((set) => ({
  // 初始状态
  questions: [],
  totalQuestions: 0,
  currentPage: 1,
  pageSize: 10,
  
  filterTitle: '',
  filterKeyword: '',
  filterType: null,
  filterDifficulty: null,
  filterTagIds: [],
  filterGroupId: null,
  
  selectedQuestionIds: [],
  isSelectAll: false,
  
  currentQuestion: null,
  
  questionGroups: [],
  currentQuestionGroup: null,
  
  questionTags: [],
  
  isImporting: false,
  importProgress: 0,
  
  // Actions
  setQuestions: (questions, total) => set({
    questions,
    totalQuestions: total,
    selectedQuestionIds: [],
    isSelectAll: false
  }),
  
  setPage: (page, pageSize) => set({
    currentPage: page,
    pageSize
  }),
  
  setFilter: (filterData) => set((state) => ({
    ...state,
    filterTitle: filterData.title !== undefined ? filterData.title : state.filterTitle,
    filterKeyword: filterData.keyword !== undefined ? filterData.keyword : state.filterKeyword,
    filterType: filterData.type !== undefined ? filterData.type : state.filterType,
    filterDifficulty: filterData.difficulty !== undefined ? filterData.difficulty : state.filterDifficulty,
    filterTagIds: filterData.tagIds || state.filterTagIds,
    filterGroupId: filterData.groupId !== undefined ? filterData.groupId : state.filterGroupId,
    currentPage: 1
  })),
  
  selectQuestion: (id, selected) => set((state) => {
    if (selected && !state.selectedQuestionIds.includes(id)) {
      return {
        selectedQuestionIds: [...state.selectedQuestionIds, id],
        isSelectAll: state.questions.length === state.selectedQuestionIds.length + 1
      };
    } else if (!selected && state.selectedQuestionIds.includes(id)) {
      return {
        selectedQuestionIds: state.selectedQuestionIds.filter(qId => qId !== id),
        isSelectAll: false
      };
    }
    return state;
  }),
  
  selectAll: (selected) => set((state) => ({
    selectedQuestionIds: selected ? state.questions.map(q => q.id) : [],
    isSelectAll: selected
  })),
  
  setCurrentQuestion: (question) => set({
    currentQuestion: question
  }),
  
  setQuestionGroups: (groups) => set({
    questionGroups: groups
  }),
  
  setCurrentQuestionGroup: (group) => set({
    currentQuestionGroup: group
  }),
  
  setQuestionTags: (tags) => set({
    questionTags: tags
  }),
  
  setImportState: (isImporting, progress) => set({
    isImporting,
    importProgress: progress
  }),
  
  resetState: () => set({
    questions: [],
    totalQuestions: 0,
    currentPage: 1,
    pageSize: 10,
    
    filterTitle: '',
    filterKeyword: '',
    filterType: null,
    filterDifficulty: null,
    filterTagIds: [],
    filterGroupId: null,
    
    selectedQuestionIds: [],
    isSelectAll: false,
    
    currentQuestion: null,
    
    isImporting: false,
    importProgress: 0
  }),
  
  setFilterType: (type) => set((state) => ({
    ...state,
    filterType: type,
    currentPage: 1
  })),
  
  setCurrentPage: (page) => set((state) => ({
    ...state,
    currentPage: page
  })),
  
  setQueryParams: (params) => set((state) => ({
    ...state,
    ...params
  })),
}));

export default useQuestionStore; 