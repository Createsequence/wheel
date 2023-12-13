package io.github.createsequence.wheel.spring.page;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * 分页上下文
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AutoPagingContext {

    private static final ThreadLocal<Pageable> CONTEXT = new ThreadLocal<>();

    /**
     * 获取当前的分页参数
     *
     * @return 分页参数
     */
    @NonNull
    public static Pageable getCurrentPageable() {
        return CONTEXT.get();
    }
    
    /**
     * 设置当前分页参数
     *
     * @param pageable 分页参数
     */
    public static void setCurrentPageable(Pageable pageable) {
        CONTEXT.set(pageable);
    }
    
    /**
     * 移除当前分页参数
     */
    public static void removeCurrentPageable() {
        CONTEXT.remove();
    }
}
