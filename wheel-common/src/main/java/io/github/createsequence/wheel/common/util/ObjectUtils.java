package io.github.createsequence.wheel.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link Object}工具类
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectUtils {

    /**
     * <p>获取目标元素的类型：
     * <ul>
     *     <li>{@code null};</li>
     *     <li>单个对象：{@link Object#getClass()};</li>
     *     <li>数组：{@link Class#getComponentType()};</li>
     *     <li>集合或迭代器：首个非{@code null}元素的类型;</li>
     * </ul>
     *
     * @param target target
     * @return element type
     */
    @Nullable
    public static Class<?> getElementType(Object target) {
        if (Objects.isNull(target)) {
            return null;
        }
        Object firstNonNull = target;
        if (target instanceof Iterator) {
            firstNonNull = CollectionUtils.getFirstNotNull((Iterator<?>)target);
        }
        else if (target instanceof Iterable) {
            firstNonNull = CollectionUtils.getFirstNotNull((Iterable<?>)target);
        }
        else if (target.getClass().isArray()) {
            firstNonNull = ArrayUtils.getFirstNotNull((Object[])target);
        }
        return Objects.isNull(firstNonNull) ? null : firstNonNull.getClass();
    }

    /**
     * <p>若{@code target}则返回默认值
     *
     * @param target 目标值
     * @param defaultValue 默认值
     * @param <T> 元素类型
     * @return 若{@code target}则返回默认值，否则返回{@code target}
     */
    public static <T> T defaultIfNull(T target, T defaultValue) {
        return Objects.isNull(target) ? defaultValue : target;
    }

    /**
     * <p>获取指定的索引元素对象.
     *
     * @param target target
     * @param <T> element type
     * @return element
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T get(Object target, int index) {
        if (Objects.isNull(target)) {
            return null;
        }
        if (target instanceof List) {
            return CollectionUtils.get((List<T>)target, index);
        }
        if (target instanceof Iterator) {
            Iterator<T> iterator = (Iterator<T>)target;
            return CollectionUtils.get(iterator, index);
        }
        if (target instanceof Iterable) {
            return CollectionUtils.get((Iterable<T>)target, index);
        }
        if (target instanceof Map) {
            return get(((Map<?, T>)target).values(), index);
        }
        if (target.getClass().isArray()) {
            // if index is out of bounds, return null
            T[] array = (T[])target;
            return ArrayUtils.get(array, index);
        }
        return null;
    }

    /**
     * <p>判断元素是否为空
     *
     * @param target target
     * @return boolean
     */
    public static boolean isEmpty(Object target) {
        if (Objects.isNull(target)) {
            return true;
        }
        if (target instanceof Map) {
            return CollectionUtils.isEmpty((Map<?, ?>)target);
        }
        if (target instanceof Iterable) {
            return CollectionUtils.isEmpty((Iterable<?>)target);
        }
        if (target instanceof Iterator) {
            return CollectionUtils.isEmpty((Iterator<?>)target);
        }
        if (target.getClass().isArray()) {
            return ArrayUtils.isEmpty((Object[])target);
        }
        if (target instanceof CharSequence) {
            return StringUtils.isEmpty((CharSequence)target);
        }
        return false;
    }

    /**
     * 判断元素是否非空
     *
     * @param target target
     * @return boolean
     */
    public static boolean isNotEmpty(Object target) {
        return !isEmpty(target);
    }
}
