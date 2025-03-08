package com.zhangziqi.online_course_mine.service;

import java.io.InputStream;
import java.util.List;

public interface MinioService {
    
    /**
     * 上传文件
     * 
     * @param objectName 对象名称
     * @param inputStream 文件输入流
     * @param contentType 文件类型
     * @return 文件访问URL
     */
    String uploadFile(String objectName, InputStream inputStream, String contentType);
    
    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    boolean deleteFile(String objectName);
    
    /**
     * 获取文件URL
     * 
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    String getFileUrl(String objectName);
    
    /**
     * 列出所有文件
     * 
     * @return 文件名列表
     */
    List<String> listAllFiles();
    
    /**
     * 检查存储桶是否存在，不存在则创建
     */
    void checkAndCreateBucket();
} 