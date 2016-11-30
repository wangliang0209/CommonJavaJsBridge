# CommonJavaJsBridge

参考仿照大神写的，目的主要为了学习。学习注解和反射的使用

## 通讯的协议格式
### request
```
{      
          //js调java接口名称
          "interfaceName":"test",
          //java调js
          “handlerName”:"test",
          //回调id值
          "callbackId":"c_111111",
          //传递的参数
          "params":{
                 ....
          }
     }
```

### response

```
  {
            //回调id，同时这也是response的标志
           "responseId":"c_111111",
            //response数据
           "data":{
               //状态数据
              "status":"1",
              "msg":"ok",
              //response的处理结果数据
              "values":{
                     ......
              }
          }
     }
```

## 开发思路
思路来自于鼎鼎有名的Retrofit，Retrofit通过注解的方式解决了构建request和解析response的问题，因此注解也可以解决我现在遇到的问题。那我们就来认识下这些注解。 
InvokeJsInterface用来标注java给js发送消息的方法，它的value值代表js提供的功能的接口名字

JavaCallback4JS用来标注java提供给js的回调方法的

InvokeJavaInterface用来标注java提供给js的接口，它的value值代表功能的接口名字

Param用来标注参数或者类的实例属性，它的value值代表参数被存入json中的key值，它的needConvert代表当前的参数是否需要进行转换，因为通过JsonObject类往json中存放的数据是有要求的，JsonObject中只能存放基本数据和JsonObject和JsonArray这些数据类型，对于其他的类型就得进行转换了。因此只要是不能直接通过JsonObject存放的类型该值必须为true

ParamCallback用来标注回调类型的参数，比如发送request给js的方法中，需要有一个回调参数，那这个参数必须用它来标注

ParamResponseStatus用来标注响应状态类型的参数，比如：statusCode，StatusMsg这些参数，它的value值是json中的key值。

## 使用

```Java

  CommonJavaJsBridge instance = new CommonJavaJsBridge.Builder()
          .addJavaInterface4JS(javaInterfaces4JS)                                       
          .setWebView(webView)                                   
           .setJSMethodName4Java("_JSBridge._handleMessageFromNative")                                   
          .setProtocol("msj","receive_msg").create();
```

通过CommonJavaJsBridge.Builder来构建一个CommonJavaJsBridge对象， 
- addJavaInterface4JS用来添加java提供给js的接口 
- setWebView 设置WebView这是必须进行设置的 
- setJSMethodName4Java 设置js为java唯一暴漏的方法名字 
- setProtocol设置协议字段，这也是必须的，这个字段主要是为了ios而设置的

### js给java的有参接口发送消息
```Java
 /** * 给js发送响应消息的接口*/
      public interface   IResponseStatusCallback { 
           void callbackResponse(@ParamResponseStatus("status") int status, @ParamResponseStatus("msg") String msg);
      }

      /** * 必须有无参构造函数 ，只有被@Param注解的属性才会存入json中*/
      public static class Person {
           @Param("name") 
           String name;

           @Param("age") 
           public int age;  

           public Person() {    } 

           public Person(String name, int age) {  
              this.name = name;  
              this.age = age; 
           }
      }

      //java提供给js的“test1”接口，Person是无法直接往JsonObject中存放的，
      //所以needConvert必须为true，会自动把Person中用注解标注的属性放入json中
      @JavaInterface4JS("test1")
      public void test(@Param(needConvert = true) Person personInfo, @ParamCallback IResponseStatusCallback jsCallback) {
             对收到的数据进行处理....;
             jsCallback.callback(1, "ok");
      }

      //下面是js代码,js给java的"test1"接口发送消息
      _JSNativeBridge._doSendRequest("test1", {"name":"niu","age":10}, function(responseData){

      });
```

### java给js发送消息

```
 //给js发送消息的方法要定义在一个interface中，这个过程是模仿Retrofit的
    public interface IInvokeJS {

          //复杂类型，只有用@Param标注的属性才会放入json中
          public static class City{
                   @Param("cityName") 
                   public String cityName; 

                   @Param("cityProvince") 
                   public String cityProvince;

                   public int cityId;
          }

          //给js的“exam”接口发送数据，参数是需要传递的数据
          @InvokeJsInterface("exam")
          void exam(@Param("test") String testContent, @Param("id") int id,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

          //给js的“exam1”接口发送数据，参数同样也是需要传递的数据
          @InvokeJsInterface("exam1")
          void exam1(@Param(needConvert = true) City city, @ParamCallback IJavaCallback2JS iJavaCallback2JS);
    ｝


   //使用，使用方式和Retrofit一样，先使用CommonJavaJsBridge的
  //createInvokJSCommand实例方法生成一个IInvokeJS实例
  IInvokeJS invokeJs = commonJavaJsBridge.createInvokJSCommand(IInvokeJS.class);

  //给js的"exam"发送消息，发送的是基本数据类型
  invokeJs.exam("hello js",20, new IJavaCallback2JS{
            //接收js发送的响应数据的回调方法，该方法的名字可以任意，但必须用@JavaCallback4JS标注
            @JavaCallback4JS
            public void callback(@ParamResponseStatus("msg")String statusMsg,@Param("msg") String msg) {

            }
  });

 City city = new City();
 city.cityName = "长治";
 city.cityId = 11;
 city.cityProvince = "山西";
 //给js的“exam1”发送消息，city是一个复杂对象
 invokeJs.exam1(city, new IJavaCallback2JS{
            @JavaCallback4JS
            public void callback(@ParamResponseStatus("msg")String statusMsg,@Param("msg") String msg) {

            }
  });
  
```