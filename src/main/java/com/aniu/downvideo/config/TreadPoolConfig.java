package com.aniu.downvideo.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @version: V1.0
 * @ClassName: TreadPoolConfig
 * @Description:
 * @author: hanxie
 * @create: 2020/11/17
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Configuration
public class TreadPoolConfig {

    /**
     * 消费队列线程
     * @return
     */
    @Bean(value = "consumerQueueThreadPool")
    public ExecutorService buildConsumerQueueThreadPool() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("cons-queue-td-%d").build();
        int processors = Runtime.getRuntime().availableProcessors();
        int coreSize = (int) (1.5 * processors);
        ExecutorService pool = new ThreadPoolExecutor(coreSize, coreSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        return pool;
    }


}
