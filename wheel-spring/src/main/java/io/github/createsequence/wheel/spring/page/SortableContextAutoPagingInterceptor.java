package io.github.createsequence.wheel.spring.page;

import io.github.createsequence.wheel.common.exception.WheelException;
import io.github.createsequence.wheel.common.util.ArrayUtils;
import io.github.createsequence.wheel.common.util.Asserts;
import io.github.createsequence.wheel.common.util.StringUtils;
import io.github.createsequence.wheel.spring.expression.MethodBaseExpressionEvaluator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.aopalliance.intercept.MethodInvocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>能够操作排序字段的分页拦截器，
 * 在{@link ContextAutoPagingInterceptor}的基础上，
 * 进一步增强了字段排序的能力，允许限制调用方仅能针对特定字段进行排序，
 * 并且通过{@link SortedPageable}获得更具体的排序参数。
 *
 * @author huangchengxing
 * @see SortLimit
 */
public class SortableContextAutoPagingInterceptor extends ContextAutoPagingInterceptor {

    /**
     * 创建一个自动分页切面
     *
     * @param parameterNameDiscoverer 方法参数名称查找器
     * @param methodBaseExpressionEvaluator 基于方法的表达式执行器
     */
    protected SortableContextAutoPagingInterceptor(
        ParameterNameDiscoverer parameterNameDiscoverer, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator) {
        super(parameterNameDiscoverer, methodBaseExpressionEvaluator);
    }

    /**
     * 根据方法上{@link AutoPaging}，解析得到方法的分页方式
     * <ul>
     *     <li>若不指定{@link AutoPaging#argAt()}，且不指定参数名称，则查找参数列表中实现了{@link Pageable}接口的参数；</li>
     *     <li>若指定了{@link AutoPaging#argAt()}，但不指定参数名称，则直接将指定参数视为实现了{@link Pageable}接口的分页参数；</li>
     *     <li>若不指定{@link AutoPaging#argAt()}，但是指定了参数名称，则查找参数列表中对应名称的方法参数；</li>
     *     <li>若指定了{@link AutoPaging#argAt()}，且指定了参数名称，则查找参数列表中对应参数类型中的指定字段值作为分页参数；</li>
     * </ul>
     *
     * @param annotation 注解
     * @param method     方法
     * @return {@link PagingOps}对象
     */
    @Override
    protected @Nullable PagingOps doResolvePagingOps(AutoPaging annotation, Method method) {
        PagingOps pagingOps = super.doResolvePagingOps(annotation, method);
        if (Objects.isNull(pagingOps)) {
            return null;
        }
        Set<String> support = Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(method, SortLimit.class))
            .map(SortLimit::value)
            .filter(ArrayUtils::isNotEmpty)
            .map(Arrays::asList)
            .<Set<String>>map(HashSet::new)
            .orElse(Collections.emptySet());
        return new SortablePagingOps(pagingOps, support);
    }

    /**
     * 检查排序参数是否合法
     *
     * @param sortablePagingOps 排序方式
     * @param sort 排序参数
     */
    protected void verifySortConditionLegality(SortablePagingOps sortablePagingOps, Sort sort) {
        Asserts.isTrue(
            sortablePagingOps.supportedProperties.isEmpty()
                || sortablePagingOps.supportedProperties.contains(sort.getProperty()),
            () -> new WheelException("非法的排序参数: {}", sort.getProperty())
        );
    }

    /**
     * 将格式为{@code 'prop1: asc, prop2: desc, prop3: asc'}的排序条件解析为排序字段
     *
     * @param sortConditions 排序条件
     * @return {@link Sort}
     */
    @NonNull
    protected List<Sort> resolveSortConditions(String sortConditions) {
        return Stream.of(sortConditions.split(","))
            .map(String::trim)
            .filter(StringUtils::isNotEmpty)
            .map(Sort::of)
            .collect(Collectors.toList());
    }

    /**
     * 支持解析排序条件的分页方式，通过其可以获得{@link SortedPageable}。
     */
    @RequiredArgsConstructor
    public class SortablePagingOps implements PagingOps {
        private final PagingOps delegate;
        private final Set<String> supportedProperties;
        @Override
        public AutoPaging getAnnotation() {
            return delegate.getAnnotation();
        }
        @Override
        public @Nullable Pageable getPageable(MethodInvocation methodInvocation) {
            Pageable pageable = delegate.getPageable(methodInvocation);
            if (Objects.isNull(pageable)) {
                return null;
            }
            List<Sort> sorts = resolveSortConditions(pageable.getSort());
            sorts.forEach(sort -> verifySortConditionLegality(this, sort));
            return sorts.isEmpty() ? pageable : new SortedPageable(sorts, pageable);
        }
    }

    /**
     * 携带排序条件的分页参数，参数条件可以修改
     */
    @Getter
    @AllArgsConstructor
    public class SortedPageable implements Pageable {
        private List<Sort> sorts;
        private final Pageable delegate;
        @Override
        public Integer getPageSize() {
            return delegate.getPageSize();
        }
        @Override
        public Integer getPageNum() {
            return delegate.getPageNum();
        }
        @Override
        public void setPageSize(Integer pageSize) {
            delegate.setPageSize(pageSize);
        }
        @Override
        public void setPageNum(Integer pageNum) {
            delegate.setPageNum(pageNum);
        }
        @Override
        public String getSort() {
            return sorts.stream()
                .map(Sort::toString)
                .collect(Collectors.joining(","));
        }
        @Override
        public void setSort(String sortProperties) {
            this.sorts = resolveSortConditions(sortProperties);
        }
    }

    /**
     * 排序条件
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Sort {
        private String property;
        private SortType type;
        public static Sort of(String condition) {
            String[] arr = condition.split(":");
            String property = arr[0].trim();
            String type = arr[1].trim();
            SortType st = null;
            if (SortType.ASC.name().equalsIgnoreCase(type)) {
                st = SortType.ASC;
            } else if (SortType.DESC.name().equalsIgnoreCase(type)) {
                st = SortType.DESC;
            } else {
                throw new IllegalArgumentException("非法的排序方式: " + type);
            }
            return new Sort(property, st);
        }
        @Override
        public String toString() {
            return property + ',' + type;
        }
    }

    /**
     * 排序方式
     */
    @Getter
    @RequiredArgsConstructor
    public enum SortType {
        /**
         * 正序
         */
        ASC("asc"),
        /**
         * 倒序
         */
        DESC("desc");
        private final String type;
    }
}
