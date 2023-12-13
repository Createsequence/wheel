package io.github.createsequence.wheel.spring.lock;

import java.util.concurrent.TimeUnit;

/**
 * 锁定义，用于描述超时时间、锁类型等配置
 *
 * @author huangchengxing
 */
public interface LockDefinition {

    /**
     * 获取锁类型
     *
     * @return 锁类型
     */
    LockType getLockType();

    /**
     * 获取等待时间
     *
     * @return 等待时间
     */
    long getWaitTime();

    /**
     * 锁超时时间，超过该时间后将会释放
     *
     * @return 锁超时时间
     */
    long getReleaseTime();

    /**
     * 超时时间单位
     *
     * @return 时间单位
     */
    TimeUnit getTimeUnit();

    /**
     * 当存在复数锁时的加锁顺序，越小越优先
     *
     * @return 顺序
     */
    int getOrder();
}
