package io.github.createsequence.wheel.spring.lock;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;

/**
 * 加锁处理器
 *
 * @author huangchengxing
 */
public interface LockHandler {

    /**
     * 对指定的Key加锁
     *
     * @param key 用于加锁的Key
     * @param lockDefinition 加锁配置
     * @param lockOps 需要执行的任务
     * @return 返回值
     */
    Object handle(Object key, LockDefinition lockDefinition, LockOps lockOps);

    /**
     * 需要获取锁执行的任务
     */
    @FunctionalInterface
    interface LockOps {

        /**
         * 获取加锁结果
         *
         * @param execution 加锁结果
         * @return 返回值
         */
        Object accept(LockExecution execution);
    }

    /**
     * 加锁的指定结果
     *
     * @author huangchengxing
     */
    interface LockExecution {
    
        /**
         * 获取用于加锁的Key
         *
         * @return 用于加锁的Key
         */
        Object getKey();

        /**
         * 是否加锁成功
         *
         * @return 是否
         */
        boolean isLocked();

        /**
         * 加锁过程中捕获的异常
         *
         * @return 异常
         */
        @Nullable
        Throwable getException();
    }
}
