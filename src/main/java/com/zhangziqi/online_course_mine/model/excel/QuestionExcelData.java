package com.zhangziqi.online_course_mine.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试题Excel数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ContentRowHeight(20) // 内容行高
@HeadRowHeight(25)    // 标题行高
public class QuestionExcelData {

    /**
     * 题目标题/简称
     */
    @ExcelProperty(value = "题目标题", index = 0)
    @ColumnWidth(25)
    private String title;

    /**
     * 题目内容/题干
     */
    @ExcelProperty(value = "题目内容", index = 1)
    @ColumnWidth(40)
    private String content;

    /**
     * 题目类型 0-单选题，1-多选题，2-判断题，3-填空题，4-简答题
     */
    @ExcelProperty(value = "题目类型(0单选/1多选/2判断/3填空/4简答)", index = 2)
    @ColumnWidth(35)
    private Integer type;

    /**
     * 难度级别 (1-简单, 2-中等, 3-困难)
     */
    @ExcelProperty(value = "难度(1简单/2中等/3困难)", index = 3)
    @ColumnWidth(25)
    private Integer difficulty;

    /**
     * 分值
     */
    @ExcelProperty(value = "分值", index = 4)
    @ColumnWidth(10)
    private Integer score;

    /**
     * 解析
     */
    @ExcelProperty(value = "解析", index = 5)
    @ColumnWidth(30)
    private String analysis;

    /**
     * 选项A内容
     */
    @ExcelProperty(value = "选项A", index = 6)
    @ColumnWidth(30)
    private String optionA;

    /**
     * 选项B内容
     */
    @ExcelProperty(value = "选项B", index = 7)
    @ColumnWidth(30)
    private String optionB;

    /**
     * 选项C内容
     */
    @ExcelProperty(value = "选项C", index = 8)
    @ColumnWidth(30)
    private String optionC;

    /**
     * 选项D内容
     */
    @ExcelProperty(value = "选项D", index = 9)
    @ColumnWidth(30)
    private String optionD;

    /**
     * 选项E内容 (可选)
     */
    @ExcelProperty(value = "选项E(可选)", index = 10)
    @ColumnWidth(30)
    private String optionE;

    /**
     * 选项F内容 (可选)
     */
    @ExcelProperty(value = "选项F(可选)", index = 11)
    @ColumnWidth(30)
    private String optionF;

    /**
     * 正确答案，如：A、B、AB、BCD等
     */
    @ExcelProperty(value = "正确答案", index = 12)
    @ColumnWidth(15)
    private String correctAnswer;

    /**
     * 标签，多个标签用逗号分隔
     */
    @ExcelProperty(value = "标签(逗号分隔)", index = 13)
    @ColumnWidth(30)
    private String tags;
} 