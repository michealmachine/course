'use client';

import { useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { CourseStudentStats } from './course-student-stats';

interface CourseLearningStatsProps {
  courseId: number;
}

export function CourseLearningStats({ courseId }: CourseLearningStatsProps) {
  const [activeTab, setActiveTab] = useState('students');

  return (
    <div className="space-y-6 mt-4">
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid grid-cols-1 mb-4">
          <TabsTrigger value="students">学生学习统计</TabsTrigger>
        </TabsList>

        <TabsContent value="students" className="mt-0">
          <CourseStudentStats courseId={courseId} />
        </TabsContent>
      </Tabs>
    </div>
  );
}
