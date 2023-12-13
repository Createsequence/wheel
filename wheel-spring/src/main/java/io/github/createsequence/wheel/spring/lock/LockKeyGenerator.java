package io.github.createsequence.wheel.spring.lock;

import java.lang.reflect.Method;

/**
 * 用于加锁的Key生成器
 *
 * @author huangchengxing
 */
public interface LockKeyGenerator {

    /**
     * 生成Key
     *
     * @param method 拦截的方法
     * @param arguments 方法的调用参数
     * @return 生成的Key
     */
    Object generateKey(Method method, Object[] arguments);
}
