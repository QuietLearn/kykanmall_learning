package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 复用度非常高的服务端响应对象
 * @param <T>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status){
        this.status = status;
    }

    private ServerResponse(int status ,String msg){
        this.status= status;
        this.msg = msg;
    }

    private ServerResponse(int status,String msg,T data){
        this.status= status;
        this.msg = msg;
        this.data = data;
    }

    private ServerResponse(int status,T data){
        this.status = status;
        this.data = data;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return this.status == ResponseCode.Success.getCode();
    }

    public T getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public static <T>  ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.Success.getCode());
    }

    public static <T>  ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse<T>(ResponseCode.Success.getCode(),msg);
    }

    public static <T>  ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.Success.getCode(),data);
    }

    public static <T>  ServerResponse<T> createBySuccess(String msg,T data){
        return new ServerResponse<T>(ResponseCode.Success.getCode(),msg,data);
    }

    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseCode.Error.getCode());
    }

    public static <T>  ServerResponse<T> createByErrorMessage(String errorMsg){
        return new ServerResponse<T>(ResponseCode.Error.getCode(),errorMsg);
    }

    public static <T>  ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMsg){
        return new ServerResponse<T>(errorCode,errorMsg);
    }


}
