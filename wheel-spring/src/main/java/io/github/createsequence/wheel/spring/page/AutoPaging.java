package io.github.createsequence.wheel.spring.page;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>表明该方法执行后，需要根据注解配置进行自动分页。 <br />
 * 一个典型的用法如下：
 * <pre>{@code
 * @AutoPaging(
 *      condition = "#queryBean.id == null", // 仅当 queryBean 的 id 参数为空时才应用排序
 *      defaultNum = 1, defaultSize = 10, // 不传入分页参数时，默认每页 10 条数据，并且查询第 1 页
 *      defaultSort = "id:desc", // 默认按 ID 倒序
 *      sortLimit = {"id", "name"} // 只允许根据 id 和 name 排序
 * )
 * public PageInfo<Foo> pageFoo(
 *      FooQueryBean queryBean, PagingParam pagingQueryBean) {
 * }
 * }</pre>
 *
 * <p><h3>隐式指定分页参数</h3>
 * <p>当方法的参数列表中存在任意一个参数类型实现了{@link Pageable}接口时（比如{@link PagingParam}），
 * 拦截器将默认把首个符合条件的参数作为分页参数使用。<br />
 * 比如：
 * <pre>{@code
 *  @AutoPaging
 *  List<Foo> queryPage(QueryParam param, PagingParam pageParam);
 * }</pre>
 * 或者，也可以直接令你的查询参数对象实现{@link Pageable}接口：
 * <pre>{@code
 *  // 令参数对象Pageable接口
 *  public class QueryParam implementations Pageable {}
 *  // 将其直接作为分页对象
 *  @AutoPaging
 *  List<Foo> queryPage(QueryParam param);
 * }</pre>
 * 如果指定了{@link #argAt()}，则拦截器将强制使用该下标对应的参数作为分页参数。
 *
 * <p><h3>显式指定分页参数</h3>
 * <p>你可以直接通过{@link #size()}和{@link #num()}指定<i>方法的入参</i>获取作为分页参数，<br />
 * 比如：
 * <pre>{@code
 *  @AutoPaging(numName = "offset", sizeName = "count")
 *  List<Foo> queryPage(QueryParam param, Integer offset, Integer count);
 * }</pre>
 * 或者，结合{@link #argAt()}，指定使用<i>某个参数对象的属性</i>作为分页参数：
 * <pre>{@code
 *  public class QueryParam {
 *      private Integer offset;
 *      private Integer count;
 *  }
 *  @AutoPaging(argAt = 0, numName = "offset", sizeName = "count")
 *  List<Foo> queryPage(QueryParam param);
 * }</pre>
 *
 * <p><h3>指定排序参数</h3>
 * <p>当方法中存在实现了{@link Pageable}接口的参数时，
 * 你可以传入格式为{@code 'prop1: asc, prop2: desc, prop3: asc'}的字符串从而指定本次查询的排序规则。<br />
 * 或通过{@link #sort()}指定用于提供排序字段的<i>对象属性或方法参数的名称</i>。
 *
 * <p><h3>默认排序与限制字段</h3>
 * <p>当没有对应的排序条件时，你可以通过{@link #defaultSort()}来指定哪些排序<i>总是默认生效</i>的。<br />
 * 此外，如果需要避免用户对一些<i>不必要的字段</i>进行排序，
 * 你可以通过{@link #sortLimit()}来限制用户只最多允许按哪些字段进行排序（参见{@link SortLimit}）。<br />
 * 比如：
 * <pre>{@code
 * @AutoPaging(
 *      defaultSort = "id:desc", // 默认按 id 倒序
 *      sortLimit = {"id", "name"} // 只允许根据 id 和 name 排序，若传入 age 则会报错
 * )
 * public PageInfo<Foo> pageFoo(
 *      FooQueryBean queryBean, PagingParam pagingQueryBean)
 * }</pre>
 *
 * <p><h3>分页上下文</h3>
 * <p>对于每个被拦截的方法，无论{@link #condition()}的执行结果是否为{@code true}，
 * 都可以在方法调用中通过{@link AutoPagingContext}获得解析后的分页参数。<br />
 * 因此，在一些无法直接自动分页的场景，
 * 你也可以设置{@link #condition()}为{@link #ALWAYS_NOT_APPLY_PAGING_CONDITION},
 * 然后再在方法周通过{@link AutoPagingContext}获取解析出分页参数，
 * 然后再根据请求手动进行分页。（参见{@link ManualPaging}注解）
 *
 * @author huangchengxing
 * @see ContextAutoPagingInterceptor
 * @see AutoPagingContext
 */
@SortLimit
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoPaging {

    /**
     * 永远不应用自动分页
     */
    String ALWAYS_NOT_APPLY_PAGING_CONDITION = "false";

    /**
     * 总是  不应用自动分页
     */
    String ALWAYS_APPLY_PAGING_CONDITION = "";

    /**
     * <p>是否要应用该分页条件，为空时表示总是应用分页条件。<br />
     * 如果需要指定总是手动的进行分页，则可以设置为{@link #ALWAYS_NOT_APPLY_PAGING_CONDITION}。
     * 
     * <p>该表达式遵循SpEL规范，支持通过{@code '#参数名'}的方式在表达式中引用方法参数 <br />
     * 比如：
     * <pre>{@code
     * @AutoPaging(
     *      condition = "#queryBean != null && #pagingQueryBean.pageNum > 0"
     * )
     * public PageInfo<Foo> pageFoo(
     *      FooQueryBean queryBean, PagingParam pagingQueryBean) {
     * }
     * }</pre>
     *
     * @return SpEL表达式
     */
    String condition() default ALWAYS_APPLY_PAGING_CONDITION;

    /**
     * 默认分页数，当分页数为空时将默认使用该值
     *
     * @return 分页数
     */
    int defaultNum() default 1;

    /**
     * 默认分页大小，当分页数为空时将默认使用该值
     *
     * @return 分页大小
     */
    int defaultSize() default 10;

    /**
     * <p>指定分页参数在参数列表中的索引下标，不指定时默认取首个符合条件的参数。<br />
     * 若指定了{@link #size()}与{@link #num()}，
     * 则尝试从该下标对应的参数对象中获取对应的属性值作为分页参数。
     *
     * @return 索引下标
     */
    int argAt() default -1;

    /**
     * 用于设置分页大小的参数名称
     *
     * @return 参数名称
     */
    String size() default "";

    /**
     * 用于分页的参数名称
     *
     * @return 参数名称
     */
    String num() default "";

    /**
     * 默认的排序字段，格式为{@code 'prop1: asc, prop2: desc, prop3: asc'}
     *
     * @return 排序字段
     */
    String defaultSort() default "";

    /**
     * 用于排序的参数名称
     *
     * @return 参数名称
     */
    String sort() default "";

    /**
     * 支持排序的字段，如果传入的不在列表中的字段会报错
     *
     * @return 字段
     */
    @AliasFor(attribute = "value", annotation = SortLimit.class)
    String[] sortLimit() default {};
}
