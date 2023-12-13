package io.github.createsequence.wheel.spring.page;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定支持排序的字段
 *
 * @author huangchengxing
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SortLimit {

    /**
     * <p>支持排序的字段，如果传入的不在列表中的字段会报错 <br />
     * 比如，限制参数为{@code {'prop1', 'prop2'}}，
     * 但是实际分页参数中的排序条件为{@code 'prop1: asc, prop2: desc, prop3: asc'},
     * 由于{@code 'prop3'}不在指定范围内，则直接报错。
     *
     * @return 字段
     */
    String[] value() default {};
}
