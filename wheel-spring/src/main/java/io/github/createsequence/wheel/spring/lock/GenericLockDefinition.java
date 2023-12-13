package io.github.createsequence.wheel.spring.lock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * {@link LockDefinition}的通用实现
 *
 * @author huangchengxing
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenericLockDefinition implements LockDefinition {

    /**
     * 锁类型
     */
    protected LockType lockType;

    /**
     * 等待时间
     */
    protected long waitTime = 0;

    /**
     * 超时时间
     */
    protected long releaseTime = 0;

    /**
     * 等待和超时时间单位
     */
    protected TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    /**
     * 当存在复数锁时的加锁顺序，越小越优先
     */
    protected int order;
}
