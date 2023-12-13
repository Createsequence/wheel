package io.github.createsequence.wheel.spring.page;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>{@link AutoPaging}的扩展注解，
 * 使用该注解标记的方法，只在上下文生成分页参数而不应用自动分页。<br />
 * 适用于需要通过{@link AutoPagingContext}获取分页参数后手动分页的场合。
 *
 * @author huangchengxing
 */
@AutoPaging(condition = AutoPaging.ALWAYS_NOT_APPLY_PAGING_CONDITION)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ManualPaging {

    /**
     * 默认分页数，当分页数为空时将默认使用该值
     *
     * @return 分页数
     */
    @AliasFor(annotation = AutoPaging.class)
    int defaultNum() default 1;

    /**
     * 默认分页大小，当分页数为空时将默认使用该值
     *
     * @return 分页大小
     */
    @AliasFor(annotation = AutoPaging.class)
    int defaultSize() default 10;

    /**
     * <p>指定分页参数在参数列表中的索引下标，不指定时默认取首个符合条件的参数。<br />
     * 若指定了{@link #size()}与{@link #num()}，
     * 则尝试从该下标对应的参数对象中获取对应的属性值作为分页参数。
     *
     * @return 索引下标
     */
    @AliasFor(annotation = AutoPaging.class)
    int argAt() default -1;

    /**
     * 用于设置分页大小的参数名称
     *
     * @return 参数名称
     */
    @AliasFor(annotation = AutoPaging.class)
    String size() default "";

    /**
     * 用于分页的参数名称
     *
     * @return 参数名称
     */
    @AliasFor(annotation = AutoPaging.class)
    String num() default "";

    /**
     * 默认的排序字段，格式为{@code 'prop1: asc, prop2: desc, prop3: asc'}
     *
     * @return 排序字段
     */
    @AliasFor(annotation = AutoPaging.class)
    String defaultSort() default "";

    /**
     * 用于排序的参数名称
     *
     * @return 参数名称
     */
    @AliasFor(annotation = AutoPaging.class)
    String sort() default "";

    /**
     * 支持排序的字段，如果传入的不在列表中的字段会报错
     *
     * @return 字段
     */
    @AliasFor(annotation = AutoPaging.class)
    String[] sortLimit() default {};
}
