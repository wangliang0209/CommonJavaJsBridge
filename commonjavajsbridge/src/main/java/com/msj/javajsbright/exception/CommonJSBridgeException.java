package com.msj.javajsbright.exception;

/**
 * @author Vincent.M
 * @date 16/11/28
 * @copyright ©2016 孟祥程 All Rights Reserved
 * @desc
*/
public class CommonJSBridgeException extends RuntimeException {

    public CommonJSBridgeException() {
    }

    public CommonJSBridgeException(String detailMessage) {
        super(detailMessage);
    }
}
