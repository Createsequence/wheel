package io.github.createsequence.wheel.spring.expression;

import io.github.createsequence.wheel.common.util.ArrayUtils;
import io.github.createsequence.wheel.common.util.CollectionUtils;
import io.github.createsequence.wheel.common.util.ObjectUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 基于方法调用的SpEL表达式执行器，可以在表达式中引用方法参数，获调用特定静态方法
 *
 * @author huangchengxing
 */
public class SpelMethodBaseExpressionEvaluator implements MethodBaseExpressionEvaluator {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final Map<String, Expression> expressionCache = new ConcurrentReferenceHashMap<>(16);

    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final BeanFactoryResolver beanFactoryResolver;
    private final Map<String, Method> registeredFunctions = new HashMap<>();

    /**
     * 创建一个表达式执行器
     *
     * @param parameterNameDiscoverer parameter name discoverer
     * @param beanFactory bean factory
     */
    public SpelMethodBaseExpressionEvaluator(
        ParameterNameDiscoverer parameterNameDiscoverer, BeanFactory beanFactory) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        this.beanFactoryResolver = new BeanFactoryResolver(beanFactory);
    }

    /**
     * 执行表达式，返回执行结果
     *
     * @param method 方法
     * @param arguments 调用参数
     * @param expression 表达式
     * @param resultType 返回值类型
     * @return 表达式执行结果
     */
    @Override
    public <T> T getValue(
        Method method, Object[] arguments, String expression, Class<T> resultType) {
        StandardEvaluationContext context = createEvaluationContext(method, arguments);
        Expression exp = parseExpression(expression, expressionParser);
        return exp.getValue(context, resultType);
    }

    /**
     * 创建一个{@link StandardEvaluationContext}，并向其中注册必要的变量和方法
     *
     * @param method 方法
     * @param args 调用参数
     * @return {@link StandardEvaluationContext}对象
     */
    protected StandardEvaluationContext createEvaluationContext(Method method, Object[] args) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(method);
        context.setBeanResolver(beanFactoryResolver);
        context.addPropertyAccessor(new MapAccessor());
        // 注册可在表达式中引用的变量
        if (ArrayUtils.isNotEmpty(args)) {
            registerVariables(method, args, context);
        }
        // 注册可在表达式中引用的静态方法
        registeredFunctions.forEach(context::registerFunction);
        return context;
    }

    /**
     * 向上下文注册可调用的方法
     *
     * @param methodName 方法名
     * @param function 方法
     */
    public final void registerFunction(String methodName, Method function) {
        Objects.requireNonNull(function);
        registeredFunctions.put(methodName, function);
    }

    /**
     * 向上下文注册参数
     *
     * @param method 方法
     * @param args 调用参数
     * @param context 上下文
     */
    protected void registerVariables(Method method, Object[] args, StandardEvaluationContext context) {
        List<Object> arguments = Arrays.asList(args);
        if (arguments.isEmpty()) {
            return;
        }
        // 根据参数名注册参数
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
        if (Objects.nonNull(paramNames)) {
            Map<String, Object> argsWithName = CollectionUtils.zip(Arrays.asList(paramNames), arguments);
            argsWithName.forEach(context::setVariable);
        }
        // 根据参数下标注册参数
        Map<String, Object> argsWithIdx = IntStream.range(0, arguments.size())
            .boxed()
            .collect(Collectors.toMap(String::valueOf, arguments::get));
        argsWithIdx.forEach(context::setVariable);
    }

    /**
     * 解析表达式
     *
     * @param exp 表达式
     * @param parser 表达式解析器
     * @return 表达式对象
     */
    protected Expression parseExpression(String exp, ExpressionParser parser) {
        return expressionCache.computeIfAbsent(exp, parser::parseExpression);
    }
}
