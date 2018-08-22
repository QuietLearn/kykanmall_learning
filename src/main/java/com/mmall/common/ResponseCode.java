package com.mmall.common;

public enum ResponseCode {
    Success(0,"SUCCESS"),
    Error(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEAGAL_ARGUMENT(2,"ILLEAGAL_ARGUMENT");

    private int code;
    private String desc;

    private ResponseCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
