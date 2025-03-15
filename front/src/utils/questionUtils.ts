import { QuestionType, QuestionDifficulty } from '@/types/question';

/**
 * 获取题目类型的描述文本
 * @param type 题目类型
 * @returns 类型描述文本
 */
export function getQuestionTypeText(type: QuestionType): string {
  switch (type) {
    case QuestionType.SINGLE_CHOICE:
      return '单选题';
    case QuestionType.MULTIPLE_CHOICE:
      return '多选题';
    case QuestionType.TRUE_FALSE:
      return '判断题';
    case QuestionType.FILL_BLANK:
      return '填空题';
    case QuestionType.SHORT_ANSWER:
      return '简答题';
    default:
      return '未知类型';
  }
}

/**
 * 获取题目难度的描述文本
 * @param difficulty 难度级别
 * @returns 难度描述文本
 */
export function getQuestionDifficultyText(difficulty: QuestionDifficulty): string {
  switch (difficulty) {
    case QuestionDifficulty.EASY:
      return '简单';
    case QuestionDifficulty.MEDIUM:
      return '中等';
    case QuestionDifficulty.HARD:
      return '困难';
    default:
      return '未知难度';
  }
}

/**
 * 获取题目难度的颜色
 * @param difficulty 难度级别
 * @returns Badge 变体名称
 */
export function getQuestionDifficultyColor(difficulty: QuestionDifficulty): 'success' | 'default' | 'destructive' {
  switch (difficulty) {
    case QuestionDifficulty.EASY:
      return 'success';
    case QuestionDifficulty.MEDIUM:
      return 'default';
    case QuestionDifficulty.HARD:
      return 'destructive';
    default:
      return 'default';
  }
} 