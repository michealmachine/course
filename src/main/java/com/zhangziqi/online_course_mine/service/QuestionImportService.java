package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.vo.QuestionImportResultVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 试题导入服务接口
 */
public interface QuestionImportService {

    /**
     * 生成试题Excel导入模板
     *
     * @param response HTTP响应
     * @throws IOException 如果输出流写入失败
     */
    void generateExcelTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入试题Excel
     *
     * @param file 上传的Excel文件
     * @param institutionId 机构ID
     * @param userId 用户ID
     * @param batchSize 批处理大小
     * @return 导入结果
     */
    QuestionImportResultVO importQuestions(MultipartFile file, Long institutionId, Long userId, Integer batchSize) throws IOException;
} 