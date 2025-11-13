package io.renren.modules.app.param;

import lombok.Data;

/**
 * 分页参数基类
 */
@Data
public class PageParam {
    
    /**
     * 当前页码
     */
    private Integer page = 1;
    
    /**
     * 每页条数
     */
    private Integer limit = 20;
    
    /**
     * 排序字段
     */
    private String sidx;
    
    /**
     * 排序方式：asc/desc
     */
    private String order;
}

