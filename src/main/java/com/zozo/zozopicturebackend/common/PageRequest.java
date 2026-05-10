package com.zozo.zozopicturebackend.common;


import lombok.Data;


/**
 * 通用分页请求
 */
@Data
public class PageRequest {



    /**
     * 当前页
     */
    private int current = 1;

    /**
     * 每页大小
     */
    private int PageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式
     */
    private String sortOrder = "descend";


}
