'use client';

import { useState, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { AlertCircle, CheckCircle, Upload, X } from 'lucide-react';
import questionImportService from '@/services/question-import';
import { QuestionImportResultVO } from '@/types/question';
import useQuestionStore from '@/stores/question-store';

interface QuestionImportModalProps {
  institutionId: number;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function QuestionImportModal({
  institutionId,
  isOpen,
  onClose,
  onSuccess
}: QuestionImportModalProps) {
  // 引用文件输入
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  // 文件状态
  const [file, setFile] = useState<File | null>(null);
  const [fileName, setFileName] = useState<string>('');
  
  // 导入状态
  const [isImporting, setImporting] = useState<boolean>(false);
  const [progress, setProgress] = useState<number>(0);
  const [importResult, setImportResult] = useState<QuestionImportResultVO | null>(null);
  const [error, setError] = useState<string | null>(null);

  // 触发文件选择
  const handleSelectFile = () => {
    fileInputRef.current?.click();
  };

  // 文件选择变更处理
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      // 验证文件类型
      if (!selectedFile.name.endsWith('.xlsx') && !selectedFile.name.endsWith('.xls')) {
        setError('请选择Excel文件（.xlsx或.xls格式）');
        return;
      }
      
      // 清除之前的状态
      setFile(selectedFile);
      setFileName(selectedFile.name);
      setError(null);
      setImportResult(null);
    }
  };

  // 清除选择的文件
  const handleClearFile = () => {
    setFile(null);
    setFileName('');
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // 下载模板
  const handleDownloadTemplate = async () => {
    try {
      await questionImportService.downloadTemplate();
    } catch (error) {
      setError('下载模板失败，请重试');
      console.error('下载模板失败:', error);
    }
  };

  // 导入处理
  const handleImport = async () => {
    if (!file) {
      setError('请先选择要导入的Excel文件');
      return;
    }

    try {
      setImporting(true);
      setProgress(10); // 初始进度

      // 模拟上传进度
      const progressInterval = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return 90;
          }
          return prev + 10;
        });
      }, 500);

      // 执行导入
      const result = await questionImportService.importQuestions(file, institutionId);
      
      // 清除进度模拟
      clearInterval(progressInterval);
      setProgress(100);
      
      // 设置结果
      setImportResult(result);
      
      // 如果导入成功，通知父组件
      if (result.successCount > 0) {
        onSuccess();
      }
    } catch (error) {
      console.error('导入失败:', error);
      setError(typeof error === 'object' && error !== null && 'message' in error 
        ? (error as {message: string}).message
        : '导入失败，请检查文件格式或网络连接');
    } finally {
      setImporting(false);
    }
  };

  // 重置状态，准备新导入
  const handleReset = () => {
    setFile(null);
    setFileName('');
    setError(null);
    setImportResult(null);
    setProgress(0);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // 关闭模态框
  const handleClose = () => {
    if (!isImporting) {
      handleReset();
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>批量导入试题</DialogTitle>
        </DialogHeader>
        
        {/* 文件上传区域 */}
        {!importResult && (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <Button 
                type="button" 
                variant="outline" 
                onClick={handleDownloadTemplate}
                disabled={isImporting}
              >
                下载导入模板
              </Button>
            </div>
            
            <div 
              className={`border-2 border-dashed rounded-md p-6 text-center cursor-pointer hover:border-primary transition-colors
                ${file ? 'border-green-500' : 'border-gray-300'}`}
              onClick={handleSelectFile}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".xlsx,.xls"
                onChange={handleFileChange}
                className="hidden"
                disabled={isImporting}
              />
              
              {!file ? (
                <div className="space-y-2">
                  <Upload className="mx-auto h-12 w-12 text-gray-400" />
                  <div className="text-sm text-gray-500">
                    点击上传或拖拽Excel文件到此处
                  </div>
                  <div className="text-xs text-gray-400">
                    支持.xlsx和.xls格式
                  </div>
                </div>
              ) : (
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2 text-left">
                    <CheckCircle className="h-5 w-5 text-green-500" />
                    <div>
                      <div className="text-sm font-medium">{fileName}</div>
                      <div className="text-xs text-gray-500">
                        {(file.size / 1024).toFixed(2)} KB
                      </div>
                    </div>
                  </div>
                  
                  <Button 
                    type="button" 
                    variant="ghost" 
                    size="sm" 
                    onClick={(e) => {
                      e.stopPropagation();
                      handleClearFile();
                    }}
                    disabled={isImporting}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              )}
            </div>
            
            {/* 错误提示 */}
            {error && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}
            
            {/* 导入进度 */}
            {isImporting && (
              <div className="space-y-2">
                <Progress value={progress} className="h-2" />
                <div className="text-xs text-gray-500 text-center">
                  正在导入，请稍候...
                </div>
              </div>
            )}
          </div>
        )}
        
        {/* 导入结果 */}
        {importResult && (
          <div className="space-y-4">
            <Alert variant={importResult.failureCount > 0 ? "destructive" : "default"}>
              <div className="space-y-2">
                <div className="font-medium">导入完成</div>
                <div className="text-sm space-y-1">
                  <div>总条目数：{importResult.totalCount}</div>
                  <div>成功导入：{importResult.successCount}</div>
                  <div>导入失败：{importResult.failureCount}</div>
                  <div>用时：{(importResult.duration / 1000).toFixed(2)}秒</div>
                </div>
              </div>
            </Alert>
            
            {/* 失败项目列表 */}
            {importResult.failureItems.length > 0 && (
              <div className="space-y-2">
                <Label>失败记录：</Label>
                <div className="max-h-40 overflow-y-auto border rounded-md p-2">
                  {importResult.failureItems.map((item, index) => (
                    <div key={index} className="text-sm py-1 border-b last:border-0">
                      <div className="font-medium">第{item.rowIndex}行: {item.title}</div>
                      <div className="text-red-500">{item.errorMessage}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
        
        {/* 底部按钮 */}
        <DialogFooter>
          {!importResult ? (
            <>
              <Button 
                type="button" 
                variant="outline" 
                onClick={handleClose}
                disabled={isImporting}
              >
                取消
              </Button>
              <Button 
                type="button" 
                onClick={handleImport}
                disabled={!file || isImporting}
              >
                开始导入
              </Button>
            </>
          ) : (
            <>
              <Button 
                type="button" 
                variant="outline" 
                onClick={handleReset}
              >
                继续导入
              </Button>
              <Button 
                type="button" 
                onClick={handleClose}
              >
                完成
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
} 