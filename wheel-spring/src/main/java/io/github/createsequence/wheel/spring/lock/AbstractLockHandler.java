package io.github.createsequence.wheel.spring.lock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author huangchengxing
 */
@Slf4j
public abstract class AbstractLockHandler implements LockHandler {

    /**
     * 对指定的Key加锁
     *
     * @param key 用于加锁的Key
     * @param lockDefinition 加锁配置
     * @param lockOps 需要执行的任务
     */
    @Override
    public Object handle(Object key, LockDefinition lockDefinition, LockOps lockOps) {
        Execution execution = getExecution(key, lockDefinition);
        try {
            // 尝试加锁
            boolean locked = doLock(key, lockDefinition);
            execution.setLocked(locked);
            log.info("success to lock [{}]", key);
        } catch (Throwable ex) {
            log.warn("fail to lock [{}] by definition [{}]", key, lockDefinition);
            execution.setException(ex);
            doReleaseOnFailLock(key, execution);
        }

        Object resultOfLockOps = null;
        try {
            // 仅当加锁成功时执行操作
            if (execution.isLocked()) {
                resultOfLockOps = lockOps.accept(execution);
            }
        } finally {
            try {
                // 无论任务是否成功都需要释放锁
                doReleaseOnSuccessLock(key, execution);
            } catch (Throwable ex) {
                log.warn("fail to release lock [{}]", key);
            }
        }
        return resultOfLockOps;
    }

    /**
     * 加锁
     *
     * @param key 用于加锁的Key
     * @param lockDefinition 加锁配置
     * @return 是否成功加锁
     * @throws Throwable 加锁过程中抛出的异常
     */
    protected abstract boolean doLock(Object key, LockDefinition lockDefinition) throws Throwable;

    /**
     * 释放锁，仅当{@link #doLock}返回{@code true}时调用
     *
     * @param key 用于加锁的Key
     * @param execution 执行结果
     */
    protected abstract void doReleaseOnSuccessLock(Object key, LockExecution execution);

    /**
     * 释放锁，仅当{@link #doLock}返回{@code false}时调用
     *
     * @param key 用于加锁的Key
     * @param execution 执行结果
     */
    protected void doReleaseOnFailLock(Object key, LockExecution execution) {
        throw new LockException("Fail to lock [{}]", key);
    }

    /**
     * 获取操作对象
     *
     * @param key 用于加锁的Key
     * @param lockDefinition 加锁配置
     * @return 操作对象
     */
    protected Execution getExecution(Object key, LockDefinition lockDefinition) {
        return new Execution(key);
    }
    
    @Getter
    @Setter
    @RequiredArgsConstructor
    protected static class Execution implements LockExecution {
        private final Object key;
        private boolean locked = false;
        private Throwable exception;
    }
}
