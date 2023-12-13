package io.github.createsequence.wheel.spring.page;

/**
 * 分页参数的顶层接口
 *
 * @author huangchengxing
 * @see PagingParam
 */
public interface Pageable {

    /**
     * 获取分页大小
     *
     * @return int
     */
    Integer getPageSize();

    /**
     * 获取分页数
     *
     * @return int
     */
    Integer getPageNum();

    /**
     * 设置分页大小
     *
     * @param pageSize 分页大小
     */
    void setPageSize(Integer pageSize);

    /**
     * 设置分页数
     *
     * @param pageNum 分页数
     */
    void setPageNum(Integer pageNum);

    /**
     * 获取排序字段，格式为{@code 'prop1: asc, prop2: desc, prop3: asc'}
     *
     * @return 排序字段
     */
    String getSort();
    
    /**
     * 设置排序字段
     *
     * @param sortProperties 排序自读那
     */
    void setSort(String sortProperties);
}
