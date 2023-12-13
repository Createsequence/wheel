package io.github.createsequence.wheel.spring.lock;

/**
 * 锁类型
 *
 * @author huangchengxing
 */
public enum LockType {

    /**
     * 非公平锁
     */
    LOCK,

    /**
     * 公平锁
     */
    FAIR_LOCK,

    /**
     * 读锁
     */
    READ_LOCK,

    /**
     * 写锁
     */
    WRITE_LOCK;
}
