package com.zozo.zozopicturebackend.common;

import com.zozo.zozopicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;




@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private String message;

    private T data;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.message = message;
        this.data = data;
    }



    /**
     * 打印错误
     * @param errorCode
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }



}
