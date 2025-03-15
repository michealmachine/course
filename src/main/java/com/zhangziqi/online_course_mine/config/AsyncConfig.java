package com.zhangziqi.online_course_mine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

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
        ThreadPoolTaskExecutor delegate = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU核心数
        delegate.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        
        // 最大线程数：CPU核心数 * 2
        delegate.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        
        // 队列容量
        delegate.setQueueCapacity(500);
        
        // 线程名前缀
        delegate.setThreadNamePrefix("import-task-");
        
        // 拒绝策略：由调用者线程执行
        delegate.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        delegate.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        delegate.setAwaitTerminationSeconds(60);
        
        delegate.initialize();
        log.info("导入任务线程池初始化完成，核心线程数: {}, 最大线程数: {}", 
                delegate.getCorePoolSize(), delegate.getMaxPoolSize());
        
        // 使用DelegatingSecurityContextAsyncTaskExecutor包装原始执行器，确保安全上下文传播
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }
}