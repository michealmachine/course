'use client';

import { create } from 'zustand';
import { Course, Chapter, Section, CourseStatus } from '@/types/course';

interface CourseStore {
  // 课程列表状态
  courses: Course[];
  totalCourses: number;
  currentPage: number;
  pageSize: number;
  isLoading: boolean;
  error: string | null;
  
  // 当前课程
  currentCourse: Course | null;
  
  // 当前课程的章节
  chapters: Chapter[];
  
  // 当前选中的章节
  currentChapter: Chapter | null;
  
  // 当前章节的小节
  sections: Section[];
  
  // 当前选中的小节
  currentSection: Section | null;
  
  // 筛选条件
  filterTitle: string;
  filterStatus: CourseStatus | null;
  
  // 预览相关
  previewUrl: string | null;
  
  // 动作
  setCourses: (courses: Course[], total: number) => void;
  setCurrentCourse: (course: Course | null) => void;
  setChapters: (chapters: Chapter[]) => void;
  setCurrentChapter: (chapter: Chapter | null) => void;
  setSections: (sections: Section[]) => void;
  setCurrentSection: (section: Section | null) => void;
  setPage: (page: number, pageSize?: number) => void;
  setFilter: (filterData: { title?: string; status?: CourseStatus | null }) => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  setPreviewUrl: (url: string | null) => void;
  reset: () => void;
}

const useCourseStore = create<CourseStore>((set) => ({
  // 初始状态
  courses: [],
  totalCourses: 0,
  currentPage: 1,
  pageSize: 10,
  isLoading: false,
  error: null,
  
  currentCourse: null,
  chapters: [],
  currentChapter: null,
  sections: [],
  currentSection: null,
  
  filterTitle: '',
  filterStatus: null,
  
  previewUrl: null,
  
  // 动作
  setCourses: (courses, total) => set({
    courses,
    totalCourses: total
  }),
  
  setCurrentCourse: (course) => set({
    currentCourse: course
  }),
  
  setChapters: (chapters) => set({
    chapters
  }),
  
  setCurrentChapter: (chapter) => set({
    currentChapter: chapter
  }),
  
  setSections: (sections) => set({
    sections
  }),
  
  setCurrentSection: (section) => set({
    currentSection: section
  }),
  
  setPage: (page, pageSize = 10) => set({
    currentPage: page,
    pageSize
  }),
  
  setFilter: (filterData) => set((state) => ({
    ...state,
    filterTitle: filterData.title !== undefined ? filterData.title : state.filterTitle,
    filterStatus: filterData.status !== undefined ? filterData.status : state.filterStatus,
    currentPage: 1
  })),
  
  setLoading: (isLoading) => set({
    isLoading
  }),
  
  setError: (error) => set({
    error
  }),
  
  setPreviewUrl: (url) => set({
    previewUrl: url
  }),
  
  reset: () => set({
    currentCourse: null,
    chapters: [],
    currentChapter: null,
    sections: [],
    currentSection: null,
    error: null
  })
}));

export default useCourseStore; 