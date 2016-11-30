package com.msj.jsbridge;

/**
 * @author Vincent.M
 * @date 16/11/30
 * @copyright ©2016 孟祥程 All Rights Reserved
 * @desc
 */

import com.msj.javajsbright.IJavaCallback2JS;
import com.msj.javajsbright.annotation.InvokeJsInterface;
import com.msj.javajsbright.annotation.Param;
import com.msj.javajsbright.annotation.ParamCallback;

public interface IInvokeJS{


public static class City{
    @Param("cityName")
    public String cityName;

    @Param("cityProvince")
    public String cityProvince;

    public int cityId;


}

    @InvokeJsInterface("exam")
    void exam(@Param("test") String testContent, @Param("id") int id,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJsInterface("exam1")
    void exam1(@Param City city, @ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJsInterface("exam2")
    void exam2(@Param City city, @Param("contry") String  contry,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJsInterface("exam3")
    void exam3(@Param(value = "city") City city, @Param("contry") String  contry,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJsInterface("exam4")
    void exam4(@ParamCallback IJavaCallback2JS iJavaCallback2JS);
}
