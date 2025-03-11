import { useState } from 'react';
import { toast } from 'sonner';
import { MediaService, CompleteUploadDTO } from '@/services/media-service';

interface UploadOptions {
  file: File;
  title?: string;
  description?: string;
  chunkSize?: number;
  onProgress?: (progress: number) => void;
  onSuccess?: (mediaId: number) => void;
  onError?: (error: any) => void;
}

export function useMediaUpload(mediaService: MediaService) {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  const upload = async (options: UploadOptions) => {
    const { 
      file, 
      title = file.name, 
      description = '', 
      chunkSize = 10 * 1024 * 1024, 
      onProgress, 
      onSuccess, 
      onError 
    } = options;

    try {
      setUploading(true);
      setProgress(0);

      console.log('开始初始化上传', {
        fileName: file.name,
        fileSize: file.size,
        fileType: file.type,
        chunkSize
      });

      // 初始化上传
      const initResult = await mediaService.initiateUpload({
        title,
        description,
        filename: file.name,
        contentType: file.type,
        fileSize: file.size,
        chunkSize
      });

      if (!initResult.data) {
        throw new Error(initResult.message || '初始化上传失败');
      }

      const { mediaId, uploadId, totalParts, presignedUrls } = initResult.data;

      console.log('开始上传分片', {
        mediaId,
        uploadId,
        totalParts,
        fileSize: file.size,
        chunkSize,
      });

      // 记录已完成的分片
      const completedParts: CompleteUploadDTO['completedParts'] = [];

      // 上传所有分片
      const uploadPromises = presignedUrls.map(async ({ partNumber, url }) => {
        const start = (partNumber - 1) * chunkSize;
        const end = partNumber === totalParts ? file.size : partNumber * chunkSize;

        console.log(`准备上传分片 ${partNumber}/${totalParts} - 大小: ${end - start}, 范围: ${start}-${end}`);

        const chunk = file.slice(start, end);
        
        try {
          // 上传分片到S3
          console.log(`分片 ${partNumber} 开始上传到URL: ${url}`);
          const response = await fetch(url, {
            method: 'PUT',
            body: chunk,
            headers: {
              'Content-Type': 'application/octet-stream'
            }
          });

          if (!response.ok) {
            throw new Error(`分片 ${partNumber} 上传失败: ${response.statusText}`);
          }

          // 获取ETag并确保正确处理格式
          let eTag = response.headers.get('ETag') || '';
          console.log(`分片 ${partNumber} 上传到S3成功，原始ETag: ${eTag}`);
          
          // 打印所有响应头
          const headers = [...response.headers.entries()].reduce((obj, [key, value]) => {
            obj[key] = value;
            return obj;
          }, {} as any);
          console.log(`分片 ${partNumber} 响应头:`, headers);
          
          // 如果没有ETag，尝试从不同的header获取
          if (!eTag) {
            console.warn(`分片 ${partNumber} 没有找到ETag，尝试从其他header获取`);
            // 尝试从不同的header格式获取
            eTag = response.headers.get('etag') || 
                   headers['x-amz-etag'] || 
                   headers['ETag'] || 
                   headers['etag'] || 
                   '';
            
            if (eTag) {
              console.log(`分片 ${partNumber} 从其他header获取到ETag: ${eTag}`);
            } else {
              // 如果仍然没有找到，生成一个占位符（注意：这只是调试目的）
              console.error(`分片 ${partNumber} 无法获取ETag，这可能导致合并失败`);
            }
          }

          console.log(`分片 ${partNumber} 处理后的ETag: ${eTag}`);

          // 记录已完成的分片
          completedParts.push({
            partNumber: Number(partNumber),
            eTag: eTag  // 直接发送原始ETag，由后端处理格式问题
          });

          // 更新进度
          const currentProgress = (completedParts.length / totalParts) * 100;
          setProgress(currentProgress);
          onProgress?.(currentProgress);

        } catch (error) {
          console.error(`分片 ${partNumber} 上传失败:`, error);
          throw error;
        }
      });

      // 等待所有分片上传完成
      await Promise.all(uploadPromises);

      // 按分片编号排序
      completedParts.sort((a, b) => a.partNumber - b.partNumber);
      
      console.log('所有分片上传完成，准备合并分片 (JSON)：', JSON.stringify(completedParts, null, 2));
      console.log('所有分片上传完成，原始对象：', completedParts);

      // 构建请求对象
      const completeRequest = {
        uploadId,
        completedParts: completedParts.map(part => ({
          partNumber: part.partNumber,
          eTag: part.eTag
        }))
      };
      
      // 检查请求对象是否正确
      console.log('合并请求对象 (JSON)：', JSON.stringify(completeRequest, null, 2));
      console.log('合并请求原始对象：', completeRequest);
      console.log('是否所有分片都有eTag：', completeRequest.completedParts.every(part => part.eTag !== null && part.eTag !== undefined && part.eTag !== ''));
      
      // 详细检查每个分片的ETag
      completeRequest.completedParts.forEach(part => {
        console.log(`分片 ${part.partNumber} 的ETag: "${part.eTag}", 类型: ${typeof part.eTag}, 长度: ${part.eTag?.length || 0}`);
      });
      
      // 完成上传
      const completeResult = await mediaService.completeUpload(mediaId, completeRequest);

      if (!completeResult.data) {
        throw new Error(completeResult.message || '完成上传失败');
      }
      
      console.log('合并分片成功，返回结果：', completeResult.data);

      setProgress(100);
      onSuccess?.(mediaId);
      toast.success('上传成功', {
        description: "文件已成功上传到服务器"
      });

    } catch (error) {
      console.error('上传失败:', error);
      onError?.(error);
      toast.error('上传失败', {
        description: error instanceof Error ? error.message : String(error)
      });
    } finally {
      setUploading(false);
    }
  };

  return {
    upload,
    uploading,
    progress,
  };
} 