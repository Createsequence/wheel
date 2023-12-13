package io.github.createsequence.wheel.spring.expression;

import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.StringValueResolver;

/**
 * {@link SpelMethodBaseExpressionEvaluator}的扩展实现，
 * 在前者的基础上，支持通过{@code ${xxx}}表达式引用配置文件参数。
 *
 * @author huangchengxing
 * @see StringValueResolver
 */
public class ValueResolveMethodBaseExpressionEvaluator
    extends SpelMethodBaseExpressionEvaluator implements EmbeddedValueResolverAware {

    @Setter
    private StringValueResolver embeddedValueResolver;


    /**
     * 创建一个表达式执行器
     *
     * @param parameterNameDiscoverer parameter name discoverer
     * @param beanFactory bean factory
     */
    public ValueResolveMethodBaseExpressionEvaluator(
        ParameterNameDiscoverer parameterNameDiscoverer, BeanFactory beanFactory) {
        super(parameterNameDiscoverer, beanFactory);
    }

    /**
     * 解析表达式
     *
     * @param exp 表达式
     * @param parser 表达式解析器
     * @return 表达式对象
     */
    @Override
    protected Expression parseExpression(String exp, ExpressionParser parser) {
        String resolvedExp = embeddedValueResolver.resolveStringValue(exp);
        return super.parseExpression(resolvedExp, parser);
    }
}
