package com.zhangziqi.online_course_mine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 导入任务线程池
     */
    @Bean(name = "importTaskExecutor")
    public Executor importTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        
        // 最大线程数：CPU核心数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        
        // 队列容量
        executor.setQueueCapacity(500);
        
        // 线程名前缀
        executor.setThreadNamePrefix("import-task-");
        
        // 拒绝策略：由调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        log.info("导入任务线程池初始化完成，核心线程数: {}, 最大线程数: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
} 