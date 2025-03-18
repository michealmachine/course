'use client';

import { useState, useEffect } from 'react';
import { SectionVO } from '@/types/course';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { reviewService } from '@/services';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { toast } from 'sonner';
import { 
  Video, 
  FileText, 
  Headphones, 
  AlertCircle, 
  Loader2,
  BrainCircuit,
  Clock,
  ExternalLink,
  Download
} from 'lucide-react';

interface ReviewContentPlayerProps {
  section: SectionVO;
}

export function ReviewContentPlayer({ section }: ReviewContentPlayerProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [content, setContent] = useState<any | null>(null);
  const [activeTab, setActiveTab] = useState<string>('content');
  
  // 加载小节资源
  useEffect(() => {
    async function loadSectionResources() {
      try {
        setLoading(true);
        setError(null);
        
        // 根据资源类型加载对应资源，使用特定的预览API
        if (section.resourceTypeDiscriminator === 'MEDIA' && section.mediaId) {
          const mediaData = await reviewService.getSectionMedia(String(section.id));
          setContent({
            type: 'MEDIA',
            ...mediaData
          });
        } else if (section.resourceTypeDiscriminator === 'QUESTION_GROUP' && section.questionGroupId) {
          const questionData = await reviewService.getSectionQuestionGroup(String(section.id));
          setContent({
            type: 'QUESTION_GROUP',
            ...questionData
          });
        } else {
          setContent(null);
        }
      } catch (err: any) {
        console.error('加载小节资源失败:', err);
        setError(err.message || '无法加载小节资源');
      } finally {
        setLoading(false);
      }
    }
    
    if (section && section.id) {
      loadSectionResources();
    }
  }, [section]);

  // 渲染媒体资源
  const renderMediaContent = () => {
    if (!content || !content.accessUrl) {
      return (
        <div className="flex flex-col items-center justify-center p-12 text-muted-foreground">
          <FileText className="h-12 w-12 mb-4" />
          <p>该媒体资源暂不可访问</p>
        </div>
      );
    }
    
    // 从文件URL或类型判断媒体类型
    const mediaType = content.type || 'unknown';
    const fileExt = content.accessUrl.split('.').pop()?.toLowerCase() || '';
    
    // 检查是否为视频格式
    const isVideo = 
      mediaType.includes('video') || 
      mediaType === 'VIDEO' ||
      ['mp4', 'mov', 'avi', 'wmv', 'flv', 'webm', 'mkv'].includes(fileExt);
    
    // 检查是否为音频格式
    const isAudio = 
      mediaType.includes('audio') || 
      mediaType === 'AUDIO' ||
      ['mp3', 'wav', 'ogg', 'aac', 'm4a'].includes(fileExt);
    
    // 检查是否为图片格式
    const isImage = 
      mediaType.includes('image') || 
      mediaType === 'IMAGE' ||
      ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(fileExt);
    
    // 检查是否为文档格式
    const isDocument = 
      mediaType.includes('pdf') || 
      mediaType.includes('document') || 
      mediaType.includes('msword') || 
      mediaType.includes('excel') || 
      mediaType.includes('powerpoint') ||
      mediaType === 'DOCUMENT' ||
      ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(fileExt);
    
    // 根据媒体类型渲染不同的播放器
    if (isVideo) {
      return (
        <div className="rounded-lg overflow-hidden border shadow-sm">
          <div className="bg-muted p-2 flex justify-between items-center">
            <span className="text-sm font-medium">{content.title || '视频资源'}</span>
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={() => window.open(content.accessUrl, '_blank')}
            >
              <ExternalLink className="h-4 w-4 mr-1" />
              新窗口打开
            </Button>
          </div>
          
          <div className="aspect-video bg-black">
            <video
              key={content.accessUrl}
              src={content.accessUrl}
              controls
              controlsList="nodownload"
              playsInline
              preload="metadata"
              className="w-full h-full"
              onError={(e) => {
                console.error('视频加载错误:', e);
                toast.error('视频加载失败，请重试');
              }}
            >
              您的浏览器不支持HTML5视频播放，请更新浏览器版本。
            </video>
          </div>
          
          {content.description && (
            <div className="p-3 bg-muted/50 border-t">
              <p className="text-sm text-muted-foreground">{content.description}</p>
            </div>
          )}
          
          <div className="p-2 bg-muted flex justify-between items-center text-xs text-muted-foreground">
            <div>
              提示：可使用空格键暂停/播放，左右方向键快退/快进
            </div>
            <div className="flex items-center gap-2">
              <Button 
                variant="ghost" 
                size="sm" 
                className="h-7 px-2"
                onClick={() => {
                  const videoElement = document.querySelector('video');
                  if (videoElement) {
                    if (videoElement.requestFullscreen) {
                      videoElement.requestFullscreen();
                    }
                  }
                }}
              >
                <span className="text-xs">全屏播放</span>
              </Button>
            </div>
          </div>
        </div>
      );
    } else if (isAudio) {
      return (
        <div className="p-6 bg-muted rounded-lg">
          <div className="flex flex-col items-center space-y-4">
            <div className="w-40 h-40 bg-primary/10 rounded-full flex items-center justify-center mb-2">
              <Headphones className="h-16 w-16 text-primary" />
            </div>
            
            <div className="text-center mb-2">
              <h3 className="text-lg font-medium">{content.title || '音频资源'}</h3>
              {content.description && (
                <p className="text-sm text-muted-foreground mt-1">{content.description}</p>
              )}
            </div>
            
            <div className="w-full max-w-md bg-card p-4 rounded-lg border shadow-sm">
              <audio
                key={content.accessUrl}
                src={content.accessUrl}
                controls
                className="w-full"
                controlsList="nodownload"
                preload="metadata"
                onError={(e) => {
                  console.error('音频加载错误:', e);
                  toast.error('音频加载失败，请重试');
                }}
              >
                您的浏览器不支持HTML5音频播放，请更新浏览器版本。
              </audio>
              
              <div className="flex justify-between items-center mt-3 text-xs text-muted-foreground">
                <div>
                  提示：可使用空格键暂停/播放
                </div>
                <Button 
                  variant="ghost" 
                  size="sm" 
                  onClick={() => window.open(content.accessUrl, '_blank')}
                >
                  <ExternalLink className="h-3 w-3 mr-1" />
                  新窗口打开
                </Button>
              </div>
            </div>
          </div>
        </div>
      );
    } else if (isDocument) {
      return (
        <div className="relative rounded-lg overflow-hidden border">
          <div className="flex justify-between items-center bg-muted p-2">
            <span className="text-sm font-medium">{content.title || '文档资源'}</span>
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={() => window.open(content.accessUrl, '_blank')}
            >
              <ExternalLink className="h-4 w-4 mr-1" />
              新窗口打开
            </Button>
          </div>
          <iframe
            src={content.accessUrl}
            className="w-full h-[500px] border-0"
            title={content.title || "文档预览"}
            sandbox="allow-scripts allow-same-origin allow-forms"
            referrerPolicy="no-referrer"
            loading="lazy"
            onError={(e) => {
              console.error('文档加载错误:', e);
              toast.error('文档加载失败，请重试');
            }}
          />
        </div>
      );
    } else if (isImage) {
      return (
        <div className="flex flex-col items-center p-4 bg-muted rounded-lg">
          <div className="mb-4 text-center">
            <h3 className="text-lg font-medium">{content.title || '图片资源'}</h3>
            {content.description && (
              <p className="text-sm text-muted-foreground mt-1">{content.description}</p>
            )}
          </div>
          
          <div className="relative rounded-lg overflow-hidden border bg-white">
            <img
              src={content.accessUrl}
              alt={content.title || '图片资源'}
              className="max-w-full max-h-[500px] object-contain"
            />
          </div>
          
          <Button 
            variant="ghost" 
            size="sm"
            className="mt-4"
            onClick={() => window.open(content.accessUrl, '_blank')}
          >
            <ExternalLink className="h-4 w-4 mr-2" />
            新窗口查看原图
          </Button>
        </div>
      );
    } else {
      return (
        <div className="p-6 bg-card rounded-lg border shadow-sm">
          <div className="flex flex-col items-center justify-center">
            <div className="w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center mb-4">
              <FileText className="h-10 w-10 text-primary" />
            </div>
            
            <h3 className="text-lg font-medium mb-1">{content.title || '文件资源'}</h3>
            
            {content.description && (
              <p className="text-sm text-muted-foreground text-center mb-4 max-w-md">{content.description}</p>
            )}
            
            <div className="text-center mb-6">
              <p className="text-sm text-muted-foreground">
                当前文件类型 ({mediaType || '未知类型'}) 不支持在线预览
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                文件扩展名: {fileExt || '未知'}, 检测类型: {JSON.stringify({isVideo, isAudio, isImage, isDocument})}
              </p>
            </div>
            
            <div className="flex gap-3">
              <Button 
                variant="outline" 
                onClick={() => window.open(content.accessUrl, '_blank')}
              >
                <ExternalLink className="h-4 w-4 mr-2" />
                在浏览器中打开
              </Button>
              
              <Button 
                onClick={() => {
                  // 创建一个临时链接元素来触发下载
                  const a = document.createElement('a');
                  a.href = content.accessUrl || '';
                  // 确保设置一个有效的文件名
                  const fileName = (content.title && content.title.trim()) ? content.title : '资源下载';
                  a.download = fileName;
                  document.body.appendChild(a);
                  a.click();
                  document.body.removeChild(a);
                }}
              >
                <Download className="h-4 w-4 mr-2" />
                下载文件
              </Button>
            </div>
          </div>
        </div>
      );
    }
  };

  // 渲染题组内容
  const renderQuestionGroupContent = () => {
    if (!content || !content.items || content.items.length === 0) {
      return (
        <div className="flex flex-col items-center justify-center p-12 text-muted-foreground">
          <BrainCircuit className="h-12 w-12 mb-4" />
          <p>该题组没有可显示的题目</p>
        </div>
      );
    }
    
    return (
      <div className="space-y-6 p-4">
        <div className="flex items-center space-x-2">
          <BrainCircuit className="h-6 w-6 text-primary" />
          <h3 className="text-lg font-medium">{content.name || '题目组'}</h3>
        </div>
        
        <Alert>
          <Clock className="h-4 w-4 text-muted-foreground" />
          <AlertDescription>
            共 {content.items.length} 个题目，预览模式
          </AlertDescription>
        </Alert>
        
        <div className="space-y-8">
          {content.items.map((item: any, index: number) => (
            <div key={item.id} className="border rounded-md p-4">
              <div className="font-medium">题目 {index + 1}</div>
              <div className="mt-2">{item.question?.content || '未找到题目内容'}</div>
              
              {item.question?.options && item.question.options.length > 0 && (
                <div className="mt-4 space-y-2">
                  {item.question.options.map((option: any) => (
                    <div key={option.id} className="flex items-start space-x-2">
                      <div className="border rounded w-6 h-6 flex items-center justify-center flex-shrink-0">
                        {option.label}
                      </div>
                      <div>{option.content}</div>
                    </div>
                  ))}
                </div>
              )}
              
              {item.question?.analysis && (
                <div className="mt-4 p-3 border-l-4 border-primary/50 bg-primary/5">
                  <div className="font-medium">题目解析</div>
                  <div className="mt-1 text-muted-foreground">
                    {item.question.analysis}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="space-y-4">
            <Skeleton className="w-2/3 h-8" />
            <Skeleton className="w-full h-[300px]" />
            <Skeleton className="w-full h-20" />
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardContent className="p-6">
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>加载失败</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        </CardContent>
      </Card>
    );
  }
  
  return (
    <Card>
      <CardHeader>
        <CardTitle>{section.title}</CardTitle>
        {section.description && (
          <CardDescription>{section.description}</CardDescription>
        )}
      </CardHeader>
      <CardContent>
        {!content ? (
          <div className="flex flex-col items-center justify-center p-12 text-muted-foreground">
            <FileText className="h-12 w-12 mb-4" />
            <p>此小节没有可预览的内容</p>
          </div>
        ) : content.type === 'MEDIA' || section.resourceTypeDiscriminator === 'MEDIA' ? (
          renderMediaContent()
        ) : content.type === 'QUESTION_GROUP' || section.resourceTypeDiscriminator === 'QUESTION_GROUP' ? (
          renderQuestionGroupContent()
        ) : (
          <div className="flex flex-col items-center justify-center p-12 text-muted-foreground">
            <FileText className="h-12 w-12 mb-4" />
            <p>不支持的内容类型</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
} 