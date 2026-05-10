package com.zozo.zozopicturebackend.common;

import com.zozo.zozopicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;




@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;



    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;

    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }



    /**
     * 打印错误
     * @param errorCode
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }



}
