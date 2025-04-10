"use client";

import { useState, useEffect, useRef } from 'react';
import ReactPlayer from 'react-player';
import { throttle } from 'lodash';
import { 
  Play, Pause, Volume2, VolumeX, Maximize, 
  SkipForward, SkipBack, AlertCircle 
} from 'lucide-react';
import { formatTime } from '@/utils/format';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Slider } from '@/components/ui/slider';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { toast } from 'sonner';
import { MediaVO } from '@/types/learning';
import { learningService } from '@/services';

export interface CourseMediaPlayerProps {
  media: MediaVO;
  onComplete?: () => void;
  onError?: (error: Error) => void;
  courseId?: number;
  chapterId?: number;
  sectionId?: number;
}

export function CourseMediaPlayer({
  media,
  onComplete,
  onError,
  courseId,
  chapterId,
  sectionId
}: CourseMediaPlayerProps) {
  // 播放器引用
  const playerRef = useRef<ReactPlayer | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  
  // 基本状态
  const [playing, setPlaying] = useState(false);
  const [volume, setVolume] = useState(1);
  const [muted, setMuted] = useState(false);
  const [duration, setDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [progress, setProgress] = useState(0);
  const [loaded, setLoaded] = useState(0);
  const [loadError, setLoadError] = useState<Error | null>(null);
  const [isVideoFormat, setIsVideoFormat] = useState(true);
  
  // 学习记录状态
  const [recordId, setRecordId] = useState<number | null>(null);
  const [watchedDuration, setWatchedDuration] = useState(0);
  const watchedTimeRef = useRef<number>(0);
  const lastRecordTimeRef = useRef<number>(Date.now());
  const learningTimeRef = useRef<number>(0); // 累计学习时间
  const progressIntervalId = useRef<NodeJS.Timeout | null>(null);
  const learningTimerIntervalId = useRef<NodeJS.Timeout | null>(null);
  
  // 确定媒体类型
  useEffect(() => {
    if (media && media.url) {
      const videoFormats = ['mp4', 'webm', 'ogg', 'mov'];
      const fileExt = media.url.split('.').pop()?.toLowerCase() || '';
      setIsVideoFormat(videoFormats.includes(fileExt));
    }
  }, [media]);

  // 媒体源URL
  const mediaUrl = media?.accessUrl || media?.url || '';

  // 处理播放/暂停
  const handlePlayPause = () => {
    setPlaying(!playing);
  };

  // 处理播放进度更新
  const handleProgress = (state: { played: number; playedSeconds: number; loaded: number; loadedSeconds: number }) => {
    // 更新状态
    setCurrentTime(state.playedSeconds);
    setProgress(state.played * 100);
    setLoaded(state.loaded * 100);
    
    // 更新已观看时间引用
    watchedTimeRef.current = state.playedSeconds;
    
    // 自动标记完成
    if (state.played > 0.95 && onComplete) {
      onComplete();
    }
  };

  // 处理播放结束
  const handleEnded = () => {
    setPlaying(false);
    setCurrentTime(0);
    
    // 记录视频完成
    recordCompletedActivity(learningTimeRef.current, 'COMPLETED', 100);
    
    // 结束学习活动
    endLearningActivity();
    
    // 调用完成回调
    if (onComplete) {
      onComplete();
    }
  };

  // 处理持续时间变更
  const handleDuration = (duration: number) => {
    setDuration(duration);
  };

  // 处理播放错误
  const handleError = (error: any) => {
    console.error('媒体播放错误:', error);
    setLoadError(new Error(error?.message || '媒体加载失败'));
    toast.error('媒体加载失败，请稍后重试');
    
    if (onError) {
      onError(new Error(error?.message || '媒体加载失败'));
    }
  };

  // 处理播放准备就绪
  const handleReady = () => {
    console.log('媒体已准备好播放');
    setLoadError(null);
  };

  // 处理进度条点击 - 添加跟踪
  const handleProgressChange = (value: number[]) => {
    if (playerRef.current) {
      const newTime = (value[0] / 100) * duration;
      const oldTime = currentTime;
      console.log(`进度条跳转: 从${Math.round(oldTime)}秒到${Math.round(newTime)}秒`);
      
      // 记录跳转作为学习交互
      if (playing) {
        learningTimeRef.current += 1; // 最小计入1秒的交互时间
      }
      
      playerRef.current.seekTo(newTime / duration);
      setCurrentTime(newTime);
    }
  };

  // 处理音量变更
  const handleVolumeChange = (value: number[]) => {
    const newVolume = value[0] / 100;
    setVolume(newVolume);
    
    if (newVolume === 0 && !muted) {
      setMuted(true);
    } else if (newVolume > 0 && muted) {
      setMuted(false);
    }
  };

  // 切换静音
  const toggleMute = () => {
    setMuted(!muted);
  };

  // 全屏切换
  const toggleFullscreen = () => {
    if (containerRef.current) {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        containerRef.current.requestFullscreen();
      }
    }
  };

  // 前进10秒
  const skipForward = () => {
    if (playerRef.current) {
      const newTime = Math.min(currentTime + 10, duration);
      playerRef.current.seekTo(newTime / duration);
    }
  };

  // 后退10秒
  const skipBackward = () => {
    if (playerRef.current) {
      const newTime = Math.max(currentTime - 10, 0);
      playerRef.current.seekTo(newTime / duration);
    }
  };

  // 开始学习活动 - 使用节流避免频繁调用
  const startLearningActivity = async () => {
    if (!courseId || !sectionId || recordId) return;
    
    try {
      const activityType = isVideoFormat ? 'VIDEO_WATCH' : 'DOCUMENT_READ';
      const record = await learningService.startLearningActivity({
        courseId: Number(courseId),
        chapterId: chapterId ? Number(chapterId) : undefined,
        sectionId: Number(sectionId),
        activityType,
        contextData: JSON.stringify({
          mediaId: media.id,
          mediaTitle: media.title,
          mediaType: media.type
        })
      });
      
      setRecordId(record.id);
      lastRecordTimeRef.current = Date.now();
      console.log('学习活动开始记录成功:', record);
    } catch (error) {
      console.error('记录学习活动开始失败:', error);
    }
  };
  
  // 结束学习活动
  const endLearningActivity = async () => {
    if (!recordId) return;
    
    try {
      await learningService.endLearningActivity(recordId, {
        contextData: JSON.stringify({
          totalDuration: Math.round(learningTimeRef.current),
          progress: Math.round((currentTime / duration) * 100),
          currentTime: currentTime
        })
      });
      
      console.log('学习活动结束记录成功');
      setRecordId(null);
    } catch (error) {
      console.error('记录学习活动结束失败:', error);
    }
  };
  
  // 启动学习时间计时器
  const startLearningTimer = () => {
    if (learningTimerIntervalId.current) return;
    
    console.log('启动学习时间计时器');
    // 每秒更新学习时间
    learningTimerIntervalId.current = setInterval(() => {
      if (playing) {
        learningTimeRef.current += 1;
        if (learningTimeRef.current % 10 === 0) { // 每增加10秒打印一次日志
          console.log(`累计学习时间: ${learningTimeRef.current}秒`);
        }
      }
    }, 1000);
  };
  
  // 停止学习时间计时器
  const stopLearningTimer = () => {
    if (learningTimerIntervalId.current) {
      clearInterval(learningTimerIntervalId.current);
      learningTimerIntervalId.current = null;
      console.log(`停止学习时间计时器，累计学习时间: ${learningTimeRef.current}秒`);
    }
  };

  // 监听播放状态变化，管理学习时间计时器
  useEffect(() => {
    if (playing) {
      startLearningTimer();
    } else {
      stopLearningTimer();
    }
    
    return () => {
      stopLearningTimer();
    };
  }, [playing]);
  
  // 记录已完成活动 - 确保记录至少1秒
  const recordCompletedActivity = throttle(async (duration: number, status: string = 'PLAYING', progressValue: number = 0) => {
    if (!courseId || !sectionId) return;
    
    try {
      // 确保时长至少为1秒
      const actualDuration = Math.max(Math.round(duration), 1);
      
      console.log(`记录视频观看活动: 状态=${status}, 时长=${actualDuration}秒, 进度=${progressValue || Math.round((currentTime / duration) * 100)}%`);
      
      await learningService.recordCompletedActivity({
        courseId: Number(courseId),
        chapterId: chapterId ? Number(chapterId) : undefined,
        sectionId: Number(sectionId),
        activityType: isVideoFormat ? 'VIDEO_WATCH' : 'DOCUMENT_READ',
        durationSeconds: actualDuration,
        contextData: JSON.stringify({
          mediaId: media.id,
          mediaTitle: media.title,
          mediaType: media.type,
          status: status,
          currentTime: currentTime,
          duration: duration,
          progress: progressValue || Math.round((currentTime / duration) * 100)
        })
      });
      
      console.log(`记录活动完成: ${status}, 时长: ${actualDuration}秒`);
      // 记录成功后重置计时器
      if (status !== 'PLAYING') {
        learningTimeRef.current = 0;
      }
    } catch (error) {
      console.error('记录完成活动失败:', error);
    }
  }, 5000); // 从15秒减少为5秒，更频繁地记录
  
  // 定期记录学习进度
  useEffect(() => {
    if (playing) {
      // 播放开始后立即记录学习活动开始
      const startTimer = setTimeout(() => {
        startLearningActivity();
        console.log('开始记录视频观看活动');
        lastRecordTimeRef.current = Date.now();
      }, 500);
      
      // 每15秒记录一次学习时长（从30秒改为15秒）
      progressIntervalId.current = setInterval(() => {
        // 获取当前累计的学习时间
        const currentLearningTime = learningTimeRef.current;
        
        // 只要有播放就记录（无论时长多少）
        if (playing && currentLearningTime > 0) {
          console.log(`定期记录视频观看进度: 累计学习时间=${currentLearningTime}秒`);
          recordCompletedActivity(currentLearningTime);
          
          // 记录后不重置学习时间，保持累计
          // 只更新记录时间点
          lastRecordTimeRef.current = Date.now();
        }
      }, 15000); // 从30秒减少为15秒
      
      return () => {
        clearTimeout(startTimer);
        if (progressIntervalId.current) {
          clearInterval(progressIntervalId.current);
          progressIntervalId.current = null;
        }
      };
    } else if (!playing && progressIntervalId.current) {
      clearInterval(progressIntervalId.current);
      progressIntervalId.current = null;
      
      // 暂停时记录当前学习时间
      const currentLearningTime = learningTimeRef.current;
      if (currentLearningTime > 0) {
        console.log(`播放暂停，记录学习时间: ${currentLearningTime}秒`);
        recordCompletedActivity(currentLearningTime, 'PAUSED');
        // 记录后重置学习时间
        learningTimeRef.current = 0;
      }
    }
  }, [playing]);
  
  // 添加监听handlePlayPause的useEffect来确保播放状态变化触发计时器
  useEffect(() => {
    console.log(`播放状态变化: ${playing ? '开始播放' : '暂停播放'}`);
    
    // 在播放开始时，如果计时器不存在就启动它
    if (playing && !learningTimerIntervalId.current) {
      startLearningTimer();
    }
  }, [playing]);
  
  // 组件卸载时清理
  useEffect(() => {
    return () => {
      // 组件卸载时记录最后的学习时间
      const finalLearningTime = learningTimeRef.current;
      if (finalLearningTime > 0) {
        console.log(`组件卸载，记录最后学习时间: ${finalLearningTime}秒`);
        recordCompletedActivity(finalLearningTime, 'UNMOUNTED');
      }
      
      // 组件卸载时记录结束
      if (recordId) {
        endLearningActivity();
      }
      
      // 清理定时器
      if (progressIntervalId.current) {
        clearInterval(progressIntervalId.current);
      }
      
      if (learningTimerIntervalId.current) {
        clearInterval(learningTimerIntervalId.current);
      }
    };
  }, [recordId]);

  // 加载失败时显示错误
  if (loadError) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>媒体加载失败</AlertTitle>
        <AlertDescription>
          {loadError.message || '无法加载媒体，请稍后重试'}
          <Button 
            variant="outline" 
            size="sm" 
            className="mt-2" 
            onClick={() => window.location.reload()}>
            重新加载
          </Button>
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <Card className="w-full">
      <CardContent className="p-0">
        <div ref={containerRef} className="relative w-full">
          {/* 使用ReactPlayer播放媒体 */}
          <div className={isVideoFormat ? "w-full" : "relative w-full bg-slate-900 pt-[56.25%]"}>
            {!isVideoFormat && (
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-white text-xl font-medium">{media.title || '音频播放'}</div>
              </div>
            )}
            
            <ReactPlayer
              ref={playerRef}
              url={mediaUrl}
              className={isVideoFormat ? "w-full h-auto" : "hidden"}
              width="100%"
              height={isVideoFormat ? "auto" : "0"}
              playing={playing}
              volume={volume}
              muted={muted}
              onReady={handleReady}
              onPlay={() => setPlaying(true)}
              onPause={() => setPlaying(false)}
              onProgress={handleProgress}
              onDuration={handleDuration}
              onEnded={handleEnded}
              onError={handleError}
              config={{
                file: {
                  forceVideo: isVideoFormat,
                  forceAudio: !isVideoFormat,
                  attributes: {
                    controlsList: 'nodownload',
                    disablePictureInPicture: true
                  }
                }
              }}
              progressInterval={1000} // 更新进度的间隔
            />
          </div>
          
          {/* 播放控制区 */}
          <div className="absolute bottom-0 left-0 right-0 bg-black/60 p-2 text-white">
            {/* 进度条 */}
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
            
            {/* 控制按钮 */}
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="h-8 w-8 text-white hover:text-white/80"
                  onClick={handlePlayPause}
                >
                  {playing ? <Pause className="h-5 w-5" /> : <Play className="h-5 w-5" />}
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
                    {muted ? <VolumeX className="h-5 w-5" /> : <Volume2 className="h-5 w-5" />}
                  </Button>
                  
                  <Slider
                    value={[muted ? 0 : volume * 100]}
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