package com.msj.javajsbright;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.msj.javajsbright.annotation.InvokeJavaInterface;
import com.msj.javajsbright.annotation.InvokeJsInterface;
import com.msj.javajsbright.annotation.JavaCallback4JS;
import com.msj.javajsbright.exception.CommonJSBridgeException;
import com.msj.javajsbright.utils.MethodHandler;

import org.json.JSONObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Vincent.M
 * @date 16/11/30
 * @copyright ©2016 孟祥程 All Rights Reserved
 * @desc
 * 该类是本库的核心类，看例子
 * <p>生成{@link CommonJavaJsBridge}的实例：</p>
 * <pre>
 *     CommonJavaJsBridge instance = new CommonJavaJsBridge.Builder().addJavaInterface4JS(javaInterfaces4JS)
.setWebView(mWebView)
.setJSMethodName4Java("_JSBridge._handleMessageFromNative")
.setProtocol("didiam://__QUEUE_MESSAGE__/?").create();
 *
 *      这样可以创建一个CommonJavaJsBridge的实例，当然还可以设置其他参数，但是上面的几个参数是必须设定的，
 *      发送给对方的数据，我给起了个名字叫request（请求数据），
 *      request的格式是：
 *              <pre>
 *                  {
 *                      "handlerName":"test",
 *                      "callbackId":"c_111111",
 *                      "params":{
 *                          ....
 *                      }
 *                  }
 *              </pre>
 *      request里面的这三个关键的key值的名字是可以重新定义的，比如"handlerName"可以使用 new CommonJavaJsBridge.Builder().setHandlerName("interfaceName")
 *      来进行自定义。
 *
 *      接收到对方的数据，我给起个名字叫response(响应数据),
 *      response的格式是:
 *              <pre>
 *                  {
 *                      "responseId":"iii",
 *                      "data":{
 *                          "status":"1",
 *                          "msg":"ok",
 *                          "values":{
 *                              ......
 *                          }
 *                      }
 *                  }
 *              </pre>
 *       同理response里面的"responseId","data","values"这三个关键的key值的名字也是可以调用CommonJavaJsBridge.Builder进行自定义的
 * </pre>
 *
 * <p>调用js接口的例子:</p>
 * <pre>
 *     //声明一个调用js的interface
 *     interface IInvokeJS{
 *           //声明一个调用js的 jtest 的接口，该接口不需要传递参数
 *           :@InvokeJSInterface("jtest")
 *           void jtest(@ParamCallback IJavaCallback2JS iJavaCallback2JS);
 *     }
 *
 *     //开始调用js的接口
 *     IInvokeJs invokeJs = mSimpleJavaJsBridge.createInvokJSCommand(IInvokeJs.class);
 *     invokeJS.jtest(new IJavaCallback2JS{
 *         :@JavaCallback4JS
 *         public void callback(@ParamResponseStatus("status") String status){
 *
 *         }
 *     });
 *
 *     以上就是一个调用js的jtest接口的例子，其实该过程是模仿了retrofit的。这样做的好处是上层使用者完全不需要关心从json中解析数据这
 *     一繁琐的重复的体力劳动了
 *
 * </pre>
 */
public class CommonJavaJsBridge {

    private static final String TAG = CommonJavaJsBridge.class.getSimpleName();

    private static final String JAVASCRIPT = "javascript:";
    /**
     * java调用js的功能时，java会为js提供回调函数，但是不可能把回调函数传递给js，
     * 所以为回调函数提供一个唯一的id，
     */
    private static int sUniqueCallbackId = 1;


    /**
     * 保证发送给js数据时在ui线程中执行
     */
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 缓存java为js提供的接口
     */
    private HashMap<String, MethodHandler> mJavaInterfaces4JSCache = new HashMap<>();

    /**
     * 缓存java为js提供的回调方法
     */
    private HashMap<String, MethodHandler> mJavaCallbackMethods4JSCache = new HashMap<>();


    /*js为java敞开的唯一的一个可调用的方法，该方法接收一个字符串，字符串是json格式*/
    private String mJSMethod4SendData2JS;
    /**
     * 协议的格式是:scheme+"://"+host+"?"
     */
    private String mProtocol;
    private WebView mWebView;
    private CommonJavaJSWebChromeClient mCommonJavaJSWebChromeClient;

    /**
     * 是否是debug模式，debug模式可以把交互信息打出来
     */
    private boolean mIsDebug;


    CommonJavaJsBridge(Builder builder) {
        if (builder == null) {
            return;
        }
        Params.init(this);
        mWebView = builder.mWebView;
        mWebView.getSettings().setJavaScriptEnabled(true);
        mCommonJavaJSWebChromeClient = new CommonJavaJSWebChromeClient(builder.mWebChromeClient, this);
        mWebView.setWebChromeClient(mCommonJavaJSWebChromeClient);
        saveJavaMethods4JS(builder.mJavaMethod4JS);
        RequestResponseBuilder.init(builder.mResponseIdName, builder.mResponseName, builder.mResponseValuesName, builder.mRequestInterfaceName, builder.mRequestCallbackIdName, builder.mRequestValuesName);
        mJSMethod4SendData2JS = builder.mJSMethodName4Java;
        mProtocol = builder.mProtocol;
    }

    /**
     * 生成SimpleJavaJsBridge的实例
     */
    public static class Builder {

        private String mResponseName;
        private String mResponseValuesName;
        private String mResponseIdName;


        private String mRequestInterfaceName;
        private String mRequestCallbackIdName;
        private String mRequestValuesName;

        /*js为java敞开的唯一的一个可调用的方法，该方法接收一个字符串，字符串是json格式*/
        private String mJSMethodName4Java;
        private String mProtocol;


        private WebChromeClient mWebChromeClient;
        private ArrayList mJavaMethod4JS;
        private WebView mWebView;

        /**
         * 是否是debug模式
         */
        private boolean mIsDebug = true;

        public Builder() {

        }

        /**
         * debug模式下，可以把交互信息打印出来
         * @param debug
         * @return
         */
        public Builder setDebug(boolean debug) {
            mIsDebug = debug;
            return this;
        }

        /**
         *
         * <pre>
         *  response格式：
         *  {
         *      "responseId":"iii",
         *      "data":{
         *          "status":"1",
         *          "msg":"ok",
         *          "values":{
         *              ......
         *          }
         *      }
         *  }
         *  responseId 代表request中的callbackId
         *  data       代表响应的数据
         *  status     代表响应状态
         *  msg        代表响应状态对应的消息
         *  values     代表响应数据包含的值
         *  </pre>
         *
         *  <pre>
         *      responseName的默认名字是"data"，可以对这个名字进行设置
         *  </pre>
         *
         * @param responseName
         * @return
         */
        public Builder setResponseName(String responseName) {
            mResponseName = responseName;
            return this;
        }

        /**
         *   responseValuesName的默认名字是"values"，可以对这个名字进行设置
         *
         * @param responseValuesName
         * @return
         * @see #setResponseName(String)
         */
        public Builder setResponseValuesName(String responseValuesName) {
            mResponseValuesName = responseValuesName;
            return this;
        }

        /**
         * response中responseIdName的默认名字是"responseId"，可以对起进行设置
         * @param responseIdName
         * @return
         * @see #setResponseName(String)
         */
        public Builder setResponseIdName(String responseIdName) {
            mResponseIdName = responseIdName;
            return this;
        }

        /**
         * <pre>
         *    {
         *      "handlerName":"test",
         *      "callbackId":"c_111111",
         *      "params":{
         *          ....
         *      }
         *    }
         *
         *    hanlerName 代表java与js之间给对方暴漏的接口的名称，
         *    callbackId 代表对方在发起请求时，会为回调方法生产一个唯一的id值，它就代表这个唯一的id值
         *    params     代表传递的数据
         *  </pre>
         *  <pre>
         *      requestInterfaceName的默认值是"handlerName",可以进行设置它
         *  </pre>
         *
         * @param requestInterfaceName
         * @return
         */
        public Builder setRequestInterfaceName(String requestInterfaceName) {
            mRequestInterfaceName = requestInterfaceName;
            return this;
        }

        /**
         *
         * 同理requestCallbackIdName的默认值是"callbackId",可以对它进行设置
         *
         * @param requestCallbackIdName
         * @return
         * @see #setRequestInterfaceName(String)
         */
        public Builder setRequestCallbackIdName(String requestCallbackIdName) {
            mRequestCallbackIdName = requestCallbackIdName;
            return this;
        }

        /**
         * 同理requestValuesName的默认值是"params",可以对它进行设置
         *
         * @param requestValuesName
         * @return
         * @see #setRequestInterfaceName(String)

         */
        public Builder setRequestValuesName(String requestValuesName) {
            mRequestValuesName = requestValuesName;
            return this;
        }

        /**
         * 设置js为java暴漏的方法的名字，只需要提供方法名字即可，具体的关于"()"和参数不需要提供，因为该方法接收的是一个json字符串
         *
         * @param JSMethodName 方法名字 比如：handleMsgFromJava
         * @return
         */
        public Builder setJSMethodName4Java(String JSMethodName) {
            mJSMethodName4Java = JSMethodName;
            if (!TextUtils.isEmpty(mJSMethodName4Java) && !mJSMethodName4Java.startsWith(JAVASCRIPT)) {
                mJSMethodName4Java = JAVASCRIPT + mJSMethodName4Java ;
                if(!mJSMethodName4Java.contains("%s")){
                    mJSMethodName4Java = mJSMethodName4Java + "(%s)";
                }
            }
            return this;
        }

        /**
         * 设置协议，协议格式：scheme://host?，协议是必须进行设置的，否则报错
         *
         * @param scheme 比如 file或http等
         * @param host
         * @return
         */
        public Builder setProtocol(String scheme,String host) {
            if(TextUtils.isEmpty(scheme) || TextUtils.isEmpty(host)){
                return this;
            }
            mProtocol = scheme+"://"+host+"?";
            return this;
        }

        public Builder setWebChromeClient(WebChromeClient webChromeClient) {
            mWebChromeClient = webChromeClient;
            return this;
        }

        /**
         * 添加java提供给js的接口
         * @param javaMethod4JS
         * @return
         */
        public Builder addJavaInterface4JS(Object javaMethod4JS) {
            if (javaMethod4JS == null) {
                return this;
            }
            if (mJavaMethod4JS == null) {
                mJavaMethod4JS = new ArrayList();
            }
            mJavaMethod4JS.add(javaMethod4JS);
            return this;
        }

        /**
         * 必须进行设置
         *
         * @param webView
         * @return
         */
        public Builder setWebView(WebView webView) {
            mWebView = webView;
            return this;
        }

        /**
         * 检测协议是否符合要求
         *
         * @return
         * @throws CommonJSBridgeException
         */
        private void checkProtocol()  {
            if (TextUtils.isEmpty(mProtocol)) {
                throw new CommonJSBridgeException("必须调用setProtocol(String)设置协议");
            }
            Uri uri = Uri.parse(mProtocol);
            if (TextUtils.isEmpty(uri.getScheme()) || TextUtils.isEmpty(uri.getHost()) || !mProtocol.endsWith("?")) {
                throw new IllegalArgumentException("协议的格式必须是 scheme://host? 这种格式");
            }
        }

        private void checkJSMethod()  {
            if (TextUtils.isEmpty(mJSMethodName4Java)) {
                throw new IllegalArgumentException("必须调用 setJSMethodName4Java(String) 方法对给js发送消息的方法进行设置");
            }

        }

        public CommonJavaJsBridge create()  {
            /*检查协议是否设置，并设置正确了*/
            checkProtocol();
            checkJSMethod();
            if (mWebView == null) {
                throw new IllegalArgumentException("必须调用 setWebView(WebView) 方法设置Webview");
            }
            return new CommonJavaJsBridge(this);
        }
    }


    /**
     * 生成调用js的命令，在调用js之前必须得调用该方法，该模式是模仿retrofit的
     * @param tClass 必须是一个interface
     * @param <T>
     * @return
     */
    public <T> T createInvokJSCommand(Class<T> tClass) {
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class<?>[]{tClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String jsMethodName = null;
                if (method.getAnnotation(InvokeJsInterface.class) != null) {
                    InvokeJsInterface invokeJsInterface = method.getAnnotation(InvokeJsInterface.class);
                    jsMethodName = invokeJsInterface.value();
                }

                RequestResponseBuilder requstBuild = new RequestResponseBuilder(true);
                requstBuild.setInterfaceName(jsMethodName);
                Params params = Params.createParams(method);
                params.convertParamValues2Json(requstBuild, args);

                sendData2JS(requstBuild);
                return new Object();
            }
        });
    }

    /**
     * 存储java为js提供的接口们
     *
     * @param javaMethods4JSes
     */
    private void saveJavaMethods4JS(ArrayList javaMethods4JSes) {
        if (javaMethods4JSes != null) {

            for (int i = 0; i < javaMethods4JSes.size(); i++) {
                Object instance = javaMethods4JSes.get(i);
                if (instance != null) {

                    //把java提供给js调用的接口放到json中
                    Class bridgeClass = instance.getClass();
                    Method[] allMethod = bridgeClass.getDeclaredMethods();
                    for (Method method : allMethod) {

                        //说明这是提供给js的接口
                        if (method.getAnnotation(InvokeJavaInterface.class) != null) {
                            /*既然是提供给js的接口就得用JavascriptInterfaceKey标注，否则报错*/
                            InvokeJavaInterface jsKey = method.getAnnotation(InvokeJavaInterface.class);
                            MethodHandler methodHandler = MethodHandler.createMethodHandler(instance, method);
                            mJavaInterfaces4JSCache.put(jsKey.value(), methodHandler);
                        }
                    }

                }
            }
        }
    }

    /**
     * 生成唯一的回调id
     *
     * @return
     */
    private static String generaUniqueCallbackId() {
        return ++sUniqueCallbackId + "_" + System.currentTimeMillis();
    }

    /**
     * 给js发送request
     * @param request
     */
    private void sendRequest2JS(RequestResponseBuilder request) {
        if (request != null) {
            String callbackId = generaUniqueCallbackId();
            request.setCallbackId(callbackId);

        /*处理提供给js的回调方法*/
            if (request.getCallback() != null) {
                Class bridgeClass = request.getCallback().getClass();
                Method[] allMethod = bridgeClass.getDeclaredMethods();
                JavaCallback4JS javaCallback4JS = null;
                for (Method method : allMethod) {
                    javaCallback4JS = method.getAnnotation(JavaCallback4JS.class);
                    if (javaCallback4JS != null) {
                        mJavaCallbackMethods4JSCache.put(callbackId, MethodHandler.createMethodHandler(request.getCallback(), method));
                        break;
                    }

                }
            }

            startSendData2JS(request.toString());

        }
    }

    /**
     * 发送response给js
     * @param response
     */
    private void sendResponse2JS(RequestResponseBuilder response) {
        if (response != null) {
            startSendData2JS(response.toString());
        }
    }

    /**
     * 发送数据给js
     */
    public void sendData2JS(final RequestResponseBuilder requestResponseBuilder) {
        if (requestResponseBuilder == null) {
            return;
        }

        if (requestResponseBuilder.isBuildRequest()) {
            sendRequest2JS(requestResponseBuilder);
        } else {
            sendResponse2JS(requestResponseBuilder);
        }
    }


    /**
     * 开始发送数据给js
     *
     * @param data
     */
    private void startSendData2JS(String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }

        data = String.format(mJSMethod4SendData2JS, data);

        final String finalData = data;
        if(mIsDebug){

            Log.i(TAG, "发送给js的数据:" + data );
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(finalData);
            }
        });

    }

    /**
     * 解析从js传递过来的json数据
     *
     * @param json
     * @return true 代表可以解析当前数据，否则不可以解析
     */
    boolean parseJsonFromJs(String json) {
        boolean result = false;
        if (!TextUtils.isEmpty(json)) {
            if (json.startsWith(mProtocol)) {
                result = true;
                json = json.substring(json.indexOf(mProtocol, 0) + mProtocol.length());
                if(mIsDebug){

                    Log.i(TAG, "收到js发送过来的数据:" + json );
                }
                try {
                    JSONObject data = new JSONObject(json);
                    if (data == null) {
                        return false;
                    }
                /*开始调用java的方法*/
                    invokeJavaMethod(RequestResponseBuilder.create(data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 开始调用java的方法，
     *
     * @param requestResponseBuilder
     */
    private void invokeJavaMethod(RequestResponseBuilder requestResponseBuilder) {
        if (requestResponseBuilder == null) {
            return;
        }
        /*说明这是响应数据*/
        if (!requestResponseBuilder.isBuildRequest()) {
            MethodHandler methodHandler = mJavaCallbackMethods4JSCache.remove(requestResponseBuilder.getResponseId());
            if (methodHandler == null) {
                Log.e(TAG, "回调方法不存在");
                return;
            }
            methodHandler.invoke(requestResponseBuilder);
        } else {
            /*说明是js请求java的请求数据*/
            MethodHandler methodHandler = mJavaInterfaces4JSCache.get(requestResponseBuilder.getInterfaceName());
            if (methodHandler != null) {
                methodHandler.invoke(requestResponseBuilder);
            } else {
                Log.e(TAG, "所调用的接口不存在");

                RequestResponseBuilder errorResponse = new RequestResponseBuilder(false);
                errorResponse.setResponseId(requestResponseBuilder.getCallbackId());
                errorResponse.putResponseStatus("errmsg","所调用的接口不存在");
                sendResponse2JS(errorResponse);
            }

        }

    }
}
