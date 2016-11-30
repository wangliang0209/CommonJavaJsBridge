package com.msj.javajsbright;

/**
 * @author Vincent.M
 * @date 16/11/30
 * @copyright ©2016 孟祥程 All Rights Reserved
 * @desc java发送给js的响应状态 ,几个通用的status
 */
public enum ResponseStatus {
    OK(1,"ok"),
    FAILED(0,"failed"),
    FAILED_METHOD_NOT_DEFINED(-1,"method not defined"),
    FAILED_PARAM_ERROR(-2,"param error")
    , FAILED_TIME_OUT(-3,"time out"),
    FAILED_USER_CANCEL(-4,"user cancel");


    private int status;

    private String msg;

    ResponseStatus(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }


}
