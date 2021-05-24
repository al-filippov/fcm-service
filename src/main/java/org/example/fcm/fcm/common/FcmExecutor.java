package org.example.fcm.fcm.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FcmExecutor implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(FcmExecutor.class);

    private final int threadsCount;
    private final ReentrantLock takeUidLock;
    private final Condition takeUidNotFull;
    private final ExecutorService executorService;

    public FcmExecutor(int threadsCount) {
        this.takeUidLock = new ReentrantLock();
        this.takeUidNotFull = takeUidLock.newCondition();
        this.threadsCount = threadsCount;
        this.executorService = Executors.newFixedThreadPool(threadsCount);
    }

    public void signalUnlock() {
        takeUidLock.lock();
        try {
            takeUidNotFull.signal();
        } finally {
            takeUidLock.unlock();
        }
    }

    public void destroy() {
        log.info("Try to destroy executor");
        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Tasks interrupted");
        }
    }

    public void executeTask(Runnable task) {
        executorService.execute(task);
    }

    public <V> Future<V> executeCallableTask(Callable<V> task) {
        return executorService.submit(task);
    }

    public int getQueueSize() {
        return ((ThreadPoolExecutor) executorService).getQueue().size();
    }

    public int getActiveThreadsCount() {
        return ((ThreadPoolExecutor) executorService).getActiveCount();
    }

    public void lock() throws InterruptedException {
        takeUidLock.lockInterruptibly();
    }

    public void unlock() {
        takeUidLock.unlock();
    }

    public void await() throws InterruptedException {
        takeUidNotFull.await();
    }

    public void signal() {
        takeUidNotFull.signal();
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    @Override
    public void close() {
        destroy();
    }
}
