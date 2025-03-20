"use client";

import { useState, useEffect, useRef } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Slider } from '@/components/ui/slider';
import { 
  Play, Pause, Volume2, VolumeX, 
  Maximize, SkipForward, SkipBack 
} from 'lucide-react';
import { formatTime } from '@/utils/format';
import { toast } from 'sonner';
import { MediaVO } from '@/types/learning';
import { learningService } from '@/services';

export interface CourseMediaPlayerProps {
  media: MediaVO;
  onComplete?: () => void;
  onError?: (error: Error) => void;
}

export function CourseMediaPlayer({
  media,
  onComplete,
  onError
}: CourseMediaPlayerProps) {
  const mediaRef = useRef<HTMLVideoElement | HTMLAudioElement | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(1);
  const [isMuted, setIsMuted] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [progress, setProgress] = useState(0);
  const [isVideoFormat, setIsVideoFormat] = useState(true);
  const playerContainerRef = useRef<HTMLDivElement>(null);

  // 确定媒体类型
  useEffect(() => {
    if (media && media.url) {
      const videoFormats = ['mp4', 'webm', 'ogg', 'mov'];
      const fileExt = media.url.split('.').pop()?.toLowerCase() || '';
      setIsVideoFormat(videoFormats.includes(fileExt));
    }
  }, [media]);

  // 加载媒体
  useEffect(() => {
    if (mediaRef.current && media) {
      const element = mediaRef.current;
      
      // 处理元数据加载
      const handleLoadedMetadata = () => {
        setDuration(element.duration);
        setIsLoading(false);
      };
      
      // 处理时间更新
      const handleTimeUpdate = () => {
        setCurrentTime(element.currentTime);
        setProgress((element.currentTime / element.duration) * 100);
      };
      
      // 处理播放结束
      const handleEnded = () => {
        setIsPlaying(false);
        setCurrentTime(0);
        element.currentTime = 0;
        
        // 记录完成学习
        if (onComplete) {
          onComplete();
        }
      };
      
      // 处理媒体错误
      const handleError = (e: Event) => {
        const error = new Error(`媒体加载失败: ${(e.target as HTMLMediaElement).error?.message || '未知错误'}`);
        toast.error("媒体加载失败，请稍后重试");
        if (onError) {
          onError(error);
        }
      };
      
      // 添加事件监听器
      element.addEventListener('loadedmetadata', handleLoadedMetadata);
      element.addEventListener('timeupdate', handleTimeUpdate);
      element.addEventListener('ended', handleEnded);
      element.addEventListener('error', handleError);
      
      // 清理函数
      return () => {
        element.removeEventListener('loadedmetadata', handleLoadedMetadata);
        element.removeEventListener('timeupdate', handleTimeUpdate);
        element.removeEventListener('ended', handleEnded);
        element.removeEventListener('error', handleError);
      };
    }
  }, [mediaRef, media, onComplete, onError]);

  // 播放/暂停控制
  const togglePlay = () => {
    if (mediaRef.current) {
      if (isPlaying) {
        mediaRef.current.pause();
      } else {
        mediaRef.current.play()
          .catch(error => {
            toast.error("播放失败: " + error.message);
            if (onError) {
              onError(error);
            }
          });
      }
      setIsPlaying(!isPlaying);
    }
  };

  // 调整进度
  const handleProgressChange = (value: number[]) => {
    if (mediaRef.current && duration) {
      const newTime = (value[0] / 100) * duration;
      mediaRef.current.currentTime = newTime;
      setCurrentTime(newTime);
    }
  };

  // 调整音量
  const handleVolumeChange = (value: number[]) => {
    if (mediaRef.current) {
      const newVolume = value[0] / 100;
      mediaRef.current.volume = newVolume;
      setVolume(newVolume);
      
      if (newVolume === 0) {
        setIsMuted(true);
      } else if (isMuted) {
        setIsMuted(false);
      }
    }
  };

  // 静音切换
  const toggleMute = () => {
    if (mediaRef.current) {
      mediaRef.current.muted = !isMuted;
      setIsMuted(!isMuted);
    }
  };

  // 全屏
  const toggleFullscreen = () => {
    if (playerContainerRef.current) {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        playerContainerRef.current.requestFullscreen();
      }
    }
  };

  // 前进10秒
  const skipForward = () => {
    if (mediaRef.current) {
      const newTime = Math.min(mediaRef.current.currentTime + 10, duration);
      mediaRef.current.currentTime = newTime;
    }
  };

  // 后退10秒
  const skipBackward = () => {
    if (mediaRef.current) {
      const newTime = Math.max(mediaRef.current.currentTime - 10, 0);
      mediaRef.current.currentTime = newTime;
    }
  };

  return (
    <Card className="w-full">
      <CardContent className="p-0">
        <div ref={playerContainerRef} className="relative w-full">
          {isVideoFormat ? (
            <video
              ref={mediaRef as React.RefObject<HTMLVideoElement>}
              src={media.url || media.accessUrl}
              className="w-full h-auto"
              preload="metadata"
            />
          ) : (
            <div className="relative w-full bg-slate-900 pt-[56.25%]">
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-white text-xl font-medium">{media.title || '音频播放'}</div>
                <audio
                  ref={mediaRef as React.RefObject<HTMLAudioElement>}
                  src={media.url || media.accessUrl}
                  preload="metadata"
                  className="hidden"
                />
              </div>
            </div>
          )}
          
          {/* 播放器控制区 */}
          <div className="absolute bottom-0 left-0 right-0 bg-black/60 p-2 text-white">
            <div className="flex items-center space-x-2 mb-1">
              <Slider
                value={[progress]}
                min={0}
                max={100}
                step={0.1}
                onValueChange={handleProgressChange}
                className="flex-1"
              />
            </div>
            
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="h-8 w-8 text-white hover:text-white/80"
                  onClick={togglePlay}
                >
                  {isPlaying ? <Pause className="h-5 w-5" /> : <Play className="h-5 w-5" />}
                </Button>
                
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="h-8 w-8 text-white hover:text-white/80"
                  onClick={skipBackward}
                >
                  <SkipBack className="h-5 w-5" />
                </Button>
                
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="h-8 w-8 text-white hover:text-white/80"
                  onClick={skipForward}
                >
                  <SkipForward className="h-5 w-5" />
                </Button>
                
                <span className="text-sm">
                  {formatTime(currentTime)} / {formatTime(duration)}
                </span>
              </div>
              
              <div className="flex items-center space-x-2">
                <div className="flex items-center space-x-1 w-24">
                  <Button 
                    variant="ghost" 
                    size="icon" 
                    className="h-8 w-8 text-white hover:text-white/80"
                    onClick={toggleMute}
                  >
                    {isMuted ? <VolumeX className="h-5 w-5" /> : <Volume2 className="h-5 w-5" />}
                  </Button>
                  
                  <Slider
                    value={[isMuted ? 0 : volume * 100]}
                    min={0}
                    max={100}
                    step={1}
                    onValueChange={handleVolumeChange}
                    className="w-16"
                  />
                </div>
                
                {isVideoFormat && (
                  <Button 
                    variant="ghost" 
                    size="icon" 
                    className="h-8 w-8 text-white hover:text-white/80"
                    onClick={toggleFullscreen}
                  >
                    <Maximize className="h-5 w-5" />
                  </Button>
                )}
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
} 