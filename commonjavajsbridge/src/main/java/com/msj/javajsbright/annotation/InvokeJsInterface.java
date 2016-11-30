package com.msj.javajsbright.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vincent.M
 * @date 16/11/28
 * @copyright ©2016 孟祥程 All Rights Reserved
 * @desc
 * * java会主动调用js提供的接口，该注解就是用来标注这些接口的,{@link #value()}js提供的接口的名字
 * <p>例子：</p>
 * <pre>
 *     @InvokeJSInterface("jtest")
 *     public void jtest():
 *
 *     该例子表明java会调用js提供的{@code jtest}这样的接口
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface InvokeJsInterface {
    String value();
}
