package com.zhangziqi.online_course_mine.service;

import com.alibaba.excel.EasyExcel;
import com.zhangziqi.online_course_mine.model.excel.QuestionExcelData;
import com.zhangziqi.online_course_mine.model.vo.QuestionImportResultVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionVO;
import com.zhangziqi.online_course_mine.service.impl.QuestionImportServiceImpl;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 试题导入服务测试类
 */
@ExtendWith(MockitoExtension.class)
public class QuestionImportServiceTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionTagService questionTagService;
    
    @Mock
    private Executor importTaskExecutor;

    @Spy
    @InjectMocks
    private QuestionImportServiceImpl questionImportService;

    private MockHttpServletResponse response;
    private MultipartFile excelFile;

    @BeforeEach
    void setUp() throws IOException {
        // 创建模拟的HttpServletResponse
        response = new MockHttpServletResponse();

        // 创建一个测试Excel文件
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<QuestionExcelData> testData = createTestData();
        EasyExcel.write(outputStream, QuestionExcelData.class).sheet("测试数据").doWrite(testData);
        byte[] excelContent = outputStream.toByteArray();

        // 创建MultipartFile模拟对象
        excelFile = new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelContent);
                
        // 设置默认配置
        ReflectionTestUtils.setField(questionImportService, "concurrentEnabled", false);
        ReflectionTestUtils.setField(questionImportService, "maxImportRows", 1000);
    }

    @Test
    void testGenerateExcelTemplate() throws IOException {
        // 测试生成Excel模板
        questionImportService.generateExcelTemplate(response);

        // 验证响应头
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8", response.getContentType());
        assertTrue(response.getHeader("Content-disposition").contains("attachment"));
        assertNotNull(response.getContentAsByteArray());
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    @Test
    void testImportQuestions_Sequential() throws IOException {
        // 设置顺序处理模式
        ReflectionTestUtils.setField(questionImportService, "concurrentEnabled", false);
        
        // 当调用createQuestion方法时返回模拟的QuestionVO
        QuestionVO mockQuestionVO = QuestionVO.builder().id(1L).build();
        when(questionService.createQuestion(any(), anyLong())).thenReturn(mockQuestionVO);
        
        // 当调用getAllTags方法时返回空列表
        when(questionTagService.getAllTags(anyLong())).thenReturn(new ArrayList<>());

        // 执行导入
        QuestionImportResultVO result = questionImportService.importQuestions(
                excelFile, 1L, 1L, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalCount()); // 我们创建了2条测试数据
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getDuration() > 0);

        // 验证questionService.createQuestion被调用的次数
        verify(questionService, times(2)).createQuestion(any(), eq(1L));
    }
    
    @Test
    void testImportQuestions_Concurrent() throws Exception {
        // 设置并发处理模式
        ReflectionTestUtils.setField(questionImportService, "concurrentEnabled", true);
        
        // 模拟异步处理返回结果
        CompletableFuture<QuestionImportResultVO> mockFuture = CompletableFuture.completedFuture(
                QuestionImportResultVO.builder()
                        .totalCount(2)
                        .successCount(2)
                        .failureCount(0)
                        .build());
        
        // 模拟processBatchAsync方法的调用
        doReturn(mockFuture).when(questionImportService).processBatchAsync(anyList(), anyLong(), anyLong(), anyInt());
        
        // 执行导入
        QuestionImportResultVO result = questionImportService.importQuestions(excelFile, 1L, 1L, 10);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getDuration() > 0);
        
        // 验证processBatchAsync被调用
        verify(questionImportService, times(1)).processBatchAsync(anyList(), eq(1L), eq(1L), eq(1));
    }
    
    @Test
    void testProcessBatchAsync() throws Exception {
        // 当调用createQuestion方法时返回模拟的QuestionVO
        QuestionVO mockQuestionVO = QuestionVO.builder().id(1L).build();
        when(questionService.createQuestion(any(), anyLong())).thenReturn(mockQuestionVO);
        
        // 执行批处理
        CompletableFuture<QuestionImportResultVO> future = questionImportService.processBatchAsync(
                createTestData(), 1L, 1L, 1);
        
        // 等待执行完成并获取结果
        QuestionImportResultVO result = future.get();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        
        // 验证questionService.createQuestion被调用的次数
        verify(questionService, times(2)).createQuestion(any(), eq(1L));
    }

    /**
     * 创建测试数据
     */
    private List<QuestionExcelData> createTestData() {
        List<QuestionExcelData> testData = new ArrayList<>();
        
        // 添加单选题测试数据
        testData.add(QuestionExcelData.builder()
                .title("测试单选题")
                .content("这是一道单选题内容")
                .type(1)
                .difficulty(1)
                .score(5)
                .analysis("这是解析")
                .optionA("选项A")
                .optionB("选项B")
                .optionC("选项C")
                .optionD("选项D")
                .correctAnswer("A")
                .tags("标签1,标签2")
                .build());
        
        // 添加多选题测试数据
        testData.add(QuestionExcelData.builder()
                .title("测试多选题")
                .content("这是一道多选题内容")
                .type(2)
                .difficulty(2)
                .score(10)
                .analysis("这是解析")
                .optionA("选项A")
                .optionB("选项B")
                .optionC("选项C")
                .optionD("选项D")
                .correctAnswer("ABC")
                .tags("标签3,标签4")
                .build());
        
        return testData;
    }
} 