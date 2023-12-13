package io.github.createsequence.wheel.spring.page;

import lombok.ToString;

/**
 * 分页参数对象
 *
 * @author huangchengxing
 */
@ToString
public class PagingParam implements Pageable {

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 分页数
     */
    private Integer pageNum;

    /**
     * 排序字段
     */
    private String sort;

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public Integer getPageNum() {
        return pageNum;
    }

    @Override
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public void setSort(String sort) {
        this.sort = sort;
    }

    public PagingParam(Integer pageSize, Integer pageNum, String sort) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.sort = sort;
    }

    public PagingParam() {
    }
}
