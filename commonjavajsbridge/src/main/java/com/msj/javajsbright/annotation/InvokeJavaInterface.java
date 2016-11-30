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
 *  * 该注解是用来标注java为js提供的接口，{@link #value()}的值是代表java与js之间约定好的接口名字。
 * <p>例子1：string参数</p>
 * <pre>
 *
 *      @JavaInterface4JS("ntest")
 *      public void test(@Param("msg") String msg);
 * <p>例子2: 实体参数</p>
 * <pre>
 *
 *      public class User{
 *
 *          @Param("userName")
 *          private String userName;
 *
 *          @Param("userId")
 *          private String userId;
 *      }
 *
 *      @JavaInterface4JS("ntest")
 *      public void test(@Param("user") User user);
 *
 *
 *      上面的例子，表明java为js提供一个名字为{@code ntest}的接口
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InvokeJavaInterface {
    /**
     * 代表java与js之间约定好的接口名字
     * @return
     */
    String value();
}
