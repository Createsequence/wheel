package io.github.createsequence.wheel.spring.page;

import io.github.createsequence.wheel.common.util.ArrayUtils;
import io.github.createsequence.wheel.common.util.Asserts;
import io.github.createsequence.wheel.common.util.ReflectUtils;
import io.github.createsequence.wheel.common.util.StringUtils;
import io.github.createsequence.wheel.spring.expression.MethodBaseExpressionEvaluator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * <p>方法拦截器，用于根据方法上的{@link AutoPaging}注解生成分页参数。<br />
 * 拦截器默认并不会进行任何主动的分页操作，
 * 但是支持通过在子类重写{@link #starPage}和{@link #clearPage}方法实现主动分页逻辑。<br />
 * 在执行{@link #starPage}前，拦截器将会把分页参数存放到{@link AutoPagingContext}，
 * 该上下文中的分页参数将在完成方法调用后清除。
 *
 * @author huangchengxing
 * @see AutoPaging
 * @see ParameterNameDiscoverer
 * @see MethodBaseExpressionEvaluator
 */
@Slf4j
@RequiredArgsConstructor
public class ContextAutoPagingInterceptor implements MethodInterceptor {

    protected final Map<Method, PagingOps> resolvedMethods = new ConcurrentReferenceHashMap<>(16);
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final MethodBaseExpressionEvaluator methodBaseExpressionEvaluator;

    /**
     * 拦截器执行方法
     *
     * @return 调用结果
     */
    @SuppressWarnings("all")
    @Override
    public final Object invoke(MethodInvocation invocation) {
        PagingOps pagingOps = resolvePagingOps(invocation.getMethod());
        Pageable pageable = pagingOps.getPageable(invocation);

        boolean canApply = false;;
        // 如果能够获取到有效的分页参数，则尝试进行分页
        if (Objects.nonNull(pageable)) {
            AutoPagingContext.setCurrentPageable(pageable);
            // 根据条件表达式判断是否允许自动分页，否则只在上下文记录参数
            canApply = canApply(pagingOps.getAnnotation().condition(), invocation);
            if (canApply) {
                starPage(pageable, invocation);
            }
        }
        Object result = null;
        try {
            result = invocation.proceed();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            if (Objects.nonNull(pageable)) {
                if (canApply) {
                    clearPage(pageable, invocation, result);
                }
                AutoPagingContext.removeCurrentPageable();
            }
        }
        return result;
    }

    /**
     * 在执行方法前，触发分页操作
     *
     * @param pageable 分页参数
     * @param invocation 方法调用参数
     */
    protected void starPage(Pageable pageable, MethodInvocation invocation) {
        // do nothing
    }

    /**
     * 在执行方法后，清空分页参数
     *
     * @param pageable 分页参数
     * @param invocation 方法调用参数
     * @param result 返回值
     */
    protected void clearPage(Pageable pageable, MethodInvocation invocation, Object result) {
        // do nothing
    }

    /**
     * 从{@link #resolvedMethods}缓存中获取方法对应的分页方式，
     *
     * @param method 方法
     * @return 分页方式
     */
    @NonNull
    private PagingOps resolvePagingOps(Method method) {
        // 不确定Spring的ConcurrentMap是否解决了computeIfAbsent的问题（https://bugs.openjdk.org/browse/JDK-8161372）
        // 因此此处通过双重检查来保证线程安全
        PagingOps op = resolvedMethods.get(method);
        if (Objects.isNull(op)) {
            synchronized (resolvedMethods) {
                op = resolvedMethods.get(method);
                if (Objects.isNull(op)) {
                    AutoPaging annotation = AnnotatedElementUtils.findMergedAnnotation(method, AutoPaging.class);
                    op = Optional.ofNullable(annotation)
                        .map(a -> {
                            PagingOps ops = doResolvePagingOps(a, method);
                            log.info("Apply auto paging for method [{}]", method);
                            return ops;
                        })
                        .orElse(NoPaging.INSTANCE);
                    resolvedMethods.put(method, op);
                }
            }
        }
        return op;
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
     * @param method 方法
     * @return {@link PagingOps}对象
     */
    @Nullable
    protected PagingOps doResolvePagingOps(AutoPaging annotation, Method method) {
        // 如果方法没有参数，则默认总是按注解分页
        Class<?>[] parameterTypes = method.getParameterTypes();
        Asserts.isTrue(
            ArrayUtils.isNotEmpty(parameterTypes),
            "被@AutoPaging注解的方法[{}]没有任何参数，无法为其应用自动分页", method
        );

        // 若未指定参数下标，则尝试推断是否有实现了Pageable接口的参数可作为分页来源
        int paramIndex = annotation.argAt() > -1 ?
            annotation.argAt() : determinePageableParamIndex(parameterTypes);
        boolean hasAnySpecifiedParamName = StringUtils.isNotEmpty(annotation.size())
            || StringUtils.isNotEmpty(annotation.num());
        Asserts.isTrue(
            paramIndex > 0 || hasAnySpecifiedParamName,
            "方法[{}]没有任何分页参数，无法为其应用自动分页", method
        );

        return createPagingOps(annotation, method, parameterTypes, paramIndex, hasAnySpecifiedParamName);
    }

    @NonNull
    private PagingOps createPagingOps(
        AutoPaging annotation, Method method, Class<?>[] parameterTypes, int paramIndex, boolean hasAnySpecifiedParamName) {
        // 1、若指定了下标，但是未指定参数名称，则说明该参数直接实现了Pageable接口，应直接使用该参数作为分页
        PagingOps pagingOps;
        if (paramIndex > -1 && !hasAnySpecifiedParamName) {
            Class<?> paramType = parameterTypes[paramIndex];
            pagingOps = createPagingOpsByPageable(annotation, method, paramIndex, paramType);
        }
        // 2、若指定了下标，同时指定了参数名称，则将参数对象的指定属性值作为分页参数
        else if (paramIndex > -1) {
            Class<?> paramType = parameterTypes[paramIndex];
            // 如果入参直接是Map集合（比如JSONObject），则进行特殊处理
            pagingOps = Map.class.isAssignableFrom(paramType) ?
                new PagingByMapParam(annotation, paramIndex) :
                createPagingOpsByParamFields(annotation, method, paramType, paramIndex);
        }
        // 3、若未指定下标，但是指定了参数名称，则说明需要直接使用对应方法参数作为分页参数
        else {
            pagingOps = createPagingOpsByParams(annotation, method);
        }
        return pagingOps;
    }

    /**
     * 是否需要应用分页操作
     *
     * @param condition 条件表达式
     * @param invocation 调用参数
     * @return 是否
     */
    protected boolean canApply(String condition, MethodInvocation invocation) {
        if (Objects.equals(AutoPaging.ALWAYS_NOT_APPLY_PAGING_CONDITION, condition)) {
            return false;
        }
        return StringUtils.isEmpty(condition) || Boolean.TRUE.equals(methodBaseExpressionEvaluator.getValue(
            invocation.getMethod(), invocation.getArguments(), condition, Boolean.class
        ));
    }

    @NonNull
    private static PagingByPageable createPagingOpsByPageable(
        AutoPaging annotation, Method method, int paramIndex, Class<?> paramType) {
        Asserts.isTrue(
            Pageable.class.isAssignableFrom(paramType),
            "方法[{}]中指定下标的参数未实现[{}]接口，无法作为分页参数!", method, Pageable.class.getSimpleName()
        );
        return new PagingByPageable(annotation, paramIndex);
    }

    @NonNull
    private static PagingByParamFields createPagingOpsByParamFields(
        AutoPaging annotation, Method method, Class<?> paramType, int paramIndex) {
        Field pageNumField = ReflectUtils.getField(paramType, annotation.num());
        checkFieldPresent(pageNumField, method, paramIndex, annotation.num());
        Field pageSizeField = ReflectUtils.getField(paramType, annotation.size());
        checkFieldPresent(pageSizeField, method, paramIndex, annotation.size());
        Field sortField = ReflectUtils.getField(paramType, annotation.sort());
        checkFieldPresent(sortField, method, paramIndex, annotation.sort());
        return new PagingByParamFields(annotation, paramIndex, pageSizeField, pageNumField, sortField);
    }

    private static void checkFieldPresent(Field field, Method method, int paramIndex, String name) {
        Asserts.isNotNull(field, "方法[{}]的分页参数[{}]中没有属性[{}]", method, paramIndex, name);
    }

    @NonNull
    private PagingByParams createPagingOpsByParams(AutoPaging annotation, Method method) {
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
        Objects.requireNonNull(paramNames, () -> StringUtils.format("无法获取方法[{}]的参数名称", method));
        Map<String, Integer> paramNameWithIndex = IntStream.of(0, paramNames.length)
            .collect(HashMap::new, (map, idx) -> map.put(paramNames[idx], idx), HashMap::putAll);
        Integer pageSizeParamIndex = paramNameWithIndex.get(annotation.size());
        checkIndexPresent(pageSizeParamIndex, method, annotation.size());
        Integer pageNumParamIndex = paramNameWithIndex.get(annotation.num());
        checkIndexPresent(pageNumParamIndex, method, annotation.num());
        Integer sortIndex = paramNameWithIndex.get(annotation.sort());
        checkIndexPresent(sortIndex, method, annotation.sort());
        return new PagingByParams(annotation, pageSizeParamIndex, pageNumParamIndex, sortIndex);
    }

    private static void checkIndexPresent(Integer paramIndex, Method method, String annotation) {
        Asserts.isNotNull(paramIndex, "方法[{}]没有名为[{}]的参数", method, annotation);
    }

    private static int determinePageableParamIndex(Class<?>[] parameterTypes) {
        return IntStream.range(0, parameterTypes.length)
            .filter(idx -> Pageable.class.isAssignableFrom(parameterTypes[idx]))
            .findFirst()
            .orElse(-1);
    }

    /**
     * 分页操作
     */
    protected interface PagingOps {

        /**
         * 获取注解
         *
         * @return 注解
         */
        AutoPaging getAnnotation();

        /**
         * 获取分页参数
         *
         * @param methodInvocation 方法调用
         * @return 分页参数
         */
        @Nullable
        Pageable getPageable(MethodInvocation methodInvocation);
    }

    @RequiredArgsConstructor
    protected abstract static class PagingByIndexedParam implements PagingOps {
        @Getter
        protected final AutoPaging annotation;
        private final int argIndex;
        @Override
        public @Nullable Pageable getPageable(MethodInvocation methodInvocation) {
            Object arg = ArrayUtils.get(methodInvocation.getArguments(), argIndex);
            if (Objects.isNull(arg)) {
                return null;
            }
            int pageSize = Optional.ofNullable(resolvePageSize(arg))
                .orElse(annotation.defaultSize());
            int pageNum = Optional.ofNullable(resolvePageNum(arg))
                .orElse(annotation.defaultNum());
            String sort = Optional.ofNullable(resolveSort(arg))
                .filter(StringUtils::isNotBlank)
                .orElse(annotation.defaultSort());
            return new PagingParam(pageSize, pageNum, sort);
        }
        protected abstract Integer resolvePageNum(@NonNull Object arg);
        protected abstract Integer resolvePageSize(@NonNull Object arg);
        protected abstract String resolveSort(@NonNull Object arg);
    }

    /**
     * 根据{@link Pageable}类型的分页参数进行分页
     */
    protected static class PagingByPageable extends PagingByIndexedParam {
        public PagingByPageable(AutoPaging annotation, int argIndex) {
            super(annotation, argIndex);
        }
        @Override
        protected Integer resolvePageNum(@NonNull Object arg) {
            return ((Pageable)arg).getPageNum();
        }
        @Override
        protected Integer resolvePageSize(@NonNull Object arg) {
            return ((Pageable)arg).getPageSize();
        }
        @Override
        protected String resolveSort(@NonNull Object arg) {
            return ((Pageable)arg).getSort();
        }
    }

    /**
     * 根据参数对象中的指定属性进行分页
     */
    protected static class PagingByParamFields extends PagingByIndexedParam {
        private final Field pageSizeField;
        private final Field pageNumField;
        private final Field sortField;
        public PagingByParamFields(
            AutoPaging annotation, int argIndex, Field pageSizeField, Field pageNumField, Field sortField) {
            super(annotation, argIndex);
            this.pageSizeField = pageSizeField;
            this.pageNumField = pageNumField;
            this.sortField = sortField;
        }
        @Override
        protected Integer resolvePageNum(@NonNull Object arg) {
            return getVal(arg, pageNumField);
        }
        @Override
        protected Integer resolvePageSize(@NonNull Object arg) {
            return getVal(arg, pageSizeField);
        }
        @Override
        protected String resolveSort(@NonNull Object arg) {
            return getVal(arg, sortField);
        }
        @SuppressWarnings("unchecked")
        private <R> R getVal(Object arg, Field field) {
            // TODO 若有必要，使用Convert进行自动类型转换
            return (R)ReflectUtils.getFieldValue(arg, field);
        }
    }

    /**
     * 根据Map参数对象中的指定键值进行分页
     */
    protected static class PagingByMapParam extends PagingByIndexedParam {
        public PagingByMapParam(AutoPaging annotation, int argIndex) {
            super(annotation, argIndex);
        }
        @Override
        protected Integer resolvePageNum(@NonNull Object arg) {
            return getVal(arg, annotation.num());
        }
        @Override
        protected Integer resolvePageSize(@NonNull Object arg) {
            return getVal(arg, annotation.size());
        }
        @Override
        protected String resolveSort(@NonNull Object arg) {
            return getVal(arg, annotation.sort());
        }
        @SuppressWarnings("unchecked")
        private <R> R getVal(Object arg, String key) {
            // TODO 若有必要，使用Convert进行自动类型转换
            return (R)((Map<String, Object>)arg).get(key);
        }
    }

    /**
     * 根据方法参数进行分页
     */
    @RequiredArgsConstructor
    protected static class PagingByParams implements PagingOps {
        @Getter
        private final AutoPaging annotation;
        private final int pageSizeParamIndex;
        private final int pageNumParamIndex;
        private final int sortIndex;
        @Override
        public @Nullable Pageable getPageable(MethodInvocation methodInvocation) {
            int pageSize = Optional.ofNullable(ArrayUtils.get(methodInvocation.getArguments(), pageSizeParamIndex))
                .map(Integer.class::cast)
                .orElse(annotation.defaultSize());
            int pageNum = Optional.ofNullable(ArrayUtils.get(methodInvocation.getArguments(), pageNumParamIndex))
                .map(Integer.class::cast)
                .orElse(annotation.defaultSize());
            String sort = Optional.ofNullable(ArrayUtils.get(methodInvocation.getArguments(), sortIndex))
                .map(String.class::cast)
                .filter(StringUtils::isNotBlank)
                .orElse(annotation.defaultSort());
            return new PagingParam(pageSize, pageNum, sort);
        }
    }

    /**
     * 不分页
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    protected static class NoPaging implements PagingOps {
        protected static final NoPaging INSTANCE = new NoPaging();
        @Override
        public AutoPaging getAnnotation() {
            return null;
        }
        @Override
        public @Nullable Pageable getPageable(MethodInvocation methodInvocation) {
            return null;
        }
    }
}
