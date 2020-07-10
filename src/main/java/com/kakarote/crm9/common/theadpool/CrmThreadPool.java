package com.kakarote.crm9.common.theadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: honglei.wan
 * @Description: CRM线程池
 * @Date: Create in 2020/2/28 16:27
 */
public enum CrmThreadPool {

    /**
     * 唯一枚举对象
     */
    INSTANCE;

    private final ThreadPoolExecutor threadPool;

    CrmThreadPool(){
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = 100;
        int keepAliveTime = 60 * 1000;

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(5000);
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger integer = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "CrmTheadPool-Thread " + integer.getAndIncrement() + "-");
            }
        };

        threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, factory, handler);
    };

    public ThreadPoolExecutor getInstance() {
        return threadPool;
    }
}

