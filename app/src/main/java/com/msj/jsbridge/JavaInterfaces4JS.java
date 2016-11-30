package com.msj.jsbridge;


import com.msj.javajsbright.ResponseStatus;
import com.msj.javajsbright.annotation.InvokeJavaInterface;
import com.msj.javajsbright.annotation.Param;
import com.msj.javajsbright.annotation.ParamCallback;
import com.msj.javajsbright.annotation.ParamResponseStatus;
import com.msj.jsbridge.fragment.WebViewFragment;


/**
 * @author Vincent.M
 * @date 16/11/30
 * @copyright ©2016 孟祥程 All Rights Reserved
 * @desc
 * java提供给js的接口
 */
public class JavaInterfaces4JS {


    private WebViewFragment mWebViewFragment;

    public JavaInterfaces4JS(WebViewFragment webViewFragment) {
        mWebViewFragment = webViewFragment;
    }


    /**
     * 必须有无参构造函数
     */
    public static class Person {
        @Param("name")
        String name;
        @Param("age")
        public int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }


    }

    /**
     * 发送响应状态的接口
     */
    public interface IResponseStatusCallback {
        void callbackResponse(@ParamResponseStatus ResponseStatus responseStatus);
    }

    public interface ITestJSCallback extends IResponseStatusCallback {
        void callback(@ParamResponseStatus ResponseStatus responseStatus, @Param("content") String content);
    }

    public interface ITest1JSCallback extends IResponseStatusCallback {
        void callback(@ParamResponseStatus ResponseStatus responseStatus, @Param Person person);
    }

    @InvokeJavaInterface("test")
    public void test(@Param("msg") String msg, @ParamCallback ITestJSCallback jsCallback) {
        mWebViewFragment.setResult("js传递数据: " + msg);
        jsCallback.callbackResponse(ResponseStatus.FAILED);
    }



    @InvokeJavaInterface("test1")
    public void test(@Param Person personInfo, @ParamCallback final ITest1JSCallback jsCallback) {

        if (personInfo != null) {
            mWebViewFragment.setResult("native的test1接口被调用，js传递数据: " + "name=" + personInfo.name + " age=" + personInfo.age);

        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    jsCallback.callback(ResponseStatus.OK, new Person("niuxiaowei", 30));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }


    @InvokeJavaInterface("test2")
    public void test2(@Param(value = "person") Person personInfo, @ParamCallback ITest1JSCallback jsCallback) {

        if (personInfo != null) {
            mWebViewFragment.setResult("native的test2接口被调用，js传递数据: " + "name=" + personInfo.name + " age=" + personInfo.age);

        }
        jsCallback.callback(ResponseStatus.OK, new Person("niuxiaowei", 30));
    }

    @InvokeJavaInterface("test3")
    public void test3(@Param("jiguan") String jiguan, @Param(value = "person") Person personInfo, @ParamCallback ITest1JSCallback jsCallback) {

        if (personInfo != null) {
            mWebViewFragment.setResult("native的test3接口被调用，js传递的数据: " + "jiguan=" + jiguan + " name=" + personInfo.name + " age=" + personInfo.age);

        }
        jsCallback.callback(ResponseStatus.OK, new Person("niuxiaowei", 30));
    }

    @InvokeJavaInterface("test4")
    public void test3(@ParamCallback IResponseStatusCallback jsCallback) {

        mWebViewFragment.setResult("native的test4无参接口被调用");

        jsCallback.callbackResponse(ResponseStatus.OK);
    }


}
