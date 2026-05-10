package com.zozo.zozopicturebackend.common;


import lombok.Data;

import java.io.Serializable;


/**
 * 通用删除请求
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;


    private static final long serialVersionUID = 1L;


}
