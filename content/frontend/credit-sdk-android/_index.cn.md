---
title: "信用付Android SDK接入文档"
date: 2023-04-13T15:13:38-04:00
draft: false
---
**一.概述 Introduction**
---------------------

<a href="./demo.zip" target="_blank" style="color:blue">下载demo工程</a>

Snaplii信用付SDK

**二.接入前准备**
-----------

申请一个业务方标识appId，初始化SDK时传入。

上传app的release/debug apk签名的md5.

开发环境: Android Studio.

**三.名词解释 Glossary**
-------------------

Partner: 第三方接入方

SDK: Snaplii信用付sdk 

用户: 使用接入方宿主app的用户

信用付: Snaplii提供的支付产品

PT: Personal Token。代表接入方的一个用户ID，推荐使用接入方用户ID的一个hash值，不同的用户要确保value不一样。

AppId: App在Snaplii注册的应用标识ID， 由Snaplii分配给第三方。

App Secret: 第三方接入方应用secret，由Snaplii分配。

OTP: One Time Password。基于时间的，用app secret转为base32的字符串后作为key。

orderStr: 订单order String，其格式类似为

    {
		"outterOrderNo":"a275702d001746caace8b40b25a09df6",  
		"orderAmount":"0.01",  
		"personalToken":"9077777766",  
		"sign":"+LtDS7AFES\/k3ttx8yd46TSMlQM="  
    }
其中“sign"为签名，签名方式请参照"信用付SDK服务端接入文档"

## 四. OTP生成算法
One time passcode是采用标准的基于时间的TOTP算法，目前设置的有效时间跨度为1分钟。OTP的密钥为app secret + personal token拼接而成。密钥算法为"HmacSHA1"。Python算法可参照：

    secret_key= secret + pt
    key_str = base64.b32encode(secret_key.encode('utf-8')).decode("utf-8")
    pyotp.TOTP(key_str, digest="sha1", interval=60).now()
其他语言的实现可参照对应的实现文档


步骤1: **partner后端Server准备**
--------------------------

请参考后端接入文档。

步骤2:添加依赖库
---------

在主项目的 build.gradle 中，添加以下内容：  
注意：只有"mavenCentral"的仓库可以同步到依赖。如果发现获取不到依赖库，请确认下获取的链接是否有问题。可以尝试将mavenCentral() 放到所有依赖库的第一个来保证优先从这个仓库获取依赖。  
```
allprojects {  
    repositories {  
         // 添加下方的内容  
         mavenCentral()  
         jcenter() 等其它仓库  
    }  
}

dependencyResolutionManagement {    
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
            maven {            
                    url 'https://maven.pkg.github.com/SnapPayInc/maven/'           
                    credentials {                
                        username = "snappay-jenkins" //snappayit
                        password = "*********************************" //请联系Snaplii获取password    
                    }        
            }        
            google()
            mavenCentral()
	    
	    maven { url 'https://raw.githubusercontent.com/iProov/android/master/maven/' }
            maven { url 'https://raw.githubusercontent.com/Acuant/AndroidSdkMaven/main/maven/' }
    }
}
```
在 App Module 的 app/build.gradle 中，添加以下内容，将Snaplii SDK 作为项目依赖。
```
defaultConfig {
    ... ...
    ndk {  
        //选择要添加的对应 cpu 类型的 .so 库。  
        abiFilters 'armeabi-v7a', 'arm64-v8a'  
    }

    dataBinding {
        enabled = true
    }
    ... ...
}

dependencies {  
    // 添加下方的内容  
    implementation 'com.snaplii.sdk:credit_sdk_sandbox:0.0.4'  //sandbox环境
    //implementation 'com.snaplii.sdk:credit_sdk_release:0.0.17' //生产环境


    //如果遇到依赖的sdk编译或者冲突问题，可以参考下面exclude该sdk，避免编译错误
    //implementation ("com.snaplii.sdk:credit_sdk_release:0.0.42") {
    //    exclude group: 'xxx.xxx.xxx'
    //}
    // 其它依赖项  
}
```
步骤3:权限配置
--------

配置 AndroidManifest.xml 文件中的运行权限

为正常完成良好的支付流程体验以及支付风控需要，Snaplii SDK 需要使用以下权限：  
```
android.permission.INTERNET  
android.permission.ACCESS_NETWORK_STATE  
android.permission.ACCESS_COARSE_LOCATION  
android.permission.ACCESS_FINE_LOCATION  
android.permission.CAMERA

android.permission.ACCESS_WIFI_STATE
```
**步骤4：初始化SDK Initialize SDK**
-----------------------------



```java
//如果是白名单用户
//JSONObject coustomData = new JSONObject();
//customData.put("white_list", "true");

SnapliiSdk.initSdk(this, appId, PT, language, customData, new OTPCallback() {      
    @Override
    public void getOtp(RetOTPCallback callback) {
        //mRetOTPCallback = callback;
         //new Thread(() -> reqOtp()).start();
    }
});
```

**步骤5: 获取用户是否开通了Snaplii信用付**
----------------------------

```java
SnapliiSdk.hasSnapliiCredit(new ICreditCallback() {
    @Override
    public void onSuccess(boolean hasCredit) {
    }

    @Override
    public void onError(String code, String msg) {
    }
});
```

**步骤6: 开通信用付 Initialize SnapliiCredit user**
--------------------------------------------

开通信用付

```java
SnapliiSdk.initSnapliiCredit(activity, new ICCallback() {
    @Override
    public void onSuccess() {
    }

    @Override
    public void onError(String errorCode, String errorMsg) {
    }
});
```

**步骤7: 支付 Start a Payment**
---------------------------

```java
SnapliiSdk.payment(activity, orderStr, new PayResultCallback() {
    @Override
    public void onSuccess() {
    }
    @Override
    public void onError(String errorCode, String errorMsg) {
    }
});
```

**\[可选配置接口\] configurations**
-----------------------------

**1.配置语言:**

**SnapliiSDK.setLanguage(SnapliiSDK.LAN\_EN);**

**2.输出logcat debug日志:**

**SnapliiSDK.setDebug(true);**

**3.获取sdk版本:**

**SnapliiSDK.getVersion();**

五.SDK接口说明
---------

*   初始化SDK  
    商户APP工程中引入依赖后，调用API前，需要先向注册您的AppId，代码如下：  
    **void SnapliiSdk.initSdk(Application applicationContext, String appId, String lang, JSONObject customData, OTPCallback callback);**
    

```java
public interface OTPCallback() {
    @Override    
    public void getOtp(RetOTPCallback callback) { 
    
    }
});
```

| 参数                 | 类型          | 说明                |
|--------------------|-------------|-------------------|
| applicationContext | Application | Application上下文    |
| appId              | String      | 后台注册的App标识        |
| lang               | String      | 语言设置zh/en         |
| coustomData        | JSONObject   | 业务自定义参数.        |
| callback           | OTPCallback | sdk请求app更新otp回调方法 |

| 回调 | 类型                       | 说明                            |
|----------------|--------------------------|-------------------------------|
| callback         | RetOTPCallback | app通过callback把otp设置给sdk |

*   查询信用付账号开通信息  
    **void SnapliiSdk.hasSnapliiCredit(ICreditCallback callback);**

```java
public interface ICreditCallback {

    void onSuccess();

    void onError(String errorCode, int errorMsg);

}
```


| 参数 | 类型     | 说明             |
|----|--------|----------------|
| callback | ICreditCallback | 回调 |
| errorCode | String        | 错误码               |
| errorMsg  | String     | 错误描述              |


*   注册开通信用付
    

**void SnapliiSdk.initSnapliiCredit(Activity activity, ICCallback callback);**

```java
public interface ICCallback {

    void onSuccess();

    void onError(String errorCode, int errorMsg);

}
```

| 参数        | 类型         | 说明                |
|-----------|------------|-------------------|
| activity  | Activity   | activity          |
| callback  | ICCallback | 回调类               |
| errorCode | String        | 错误码               |
| errorMsg  | String     | 错误描述              |

【注】已开通信用付的账号，如果换了手机设备，也需要先调用SnapliiSdk.hasSnapliiCredit()，判断是否开通信用付， 如果SnapliiSdk.hasSnapliiCredit()返回false，需要调用SnapliiSdk.initSnapliiCredit()方法进入手机号验证页面验证，才能使用SnapliiSdk.payment()接口进行支付

*   支付  
    **void SnapliiSdk.payment(Activity activity, String orderStr, PayResultCallback callback);**
    

```java
public interface PayResultCallback {

    void onSuccess();

    void onError(String errorCode, String errorMsg);

}
```

| 参数        | 类型                | 说明                |
|-----------|-------------------|-------------------|
| activity        | Activity            | activity    |
| ordStr    | String            | 信用付支付order string |
| callback  | PayResultCallback | 支付结果的回调           |
| errorCode | String            | 支付错误码             |
| errorMsg  | String            | 支付错误描述            |

*   退出登录
    

**void SnapliiSdk.logout();**  
  
*   设置语言
    

**void SnapliiSdk.setLanguage(int value);**

| 参数    | 类型      | 说明                                   |
|-------|---------|--------------------------------------|
| value | String | SnapliiSDK.LANGUAGE_EN = “en“;SnapliiSDK.LANGUAGE_ZH = “zh“; |

*   打开debug日志
    

**void SnapliiSdk.setDebug(boolean value);**

| 参数    | 类型      | 说明             |
|-------|---------|----------------|
| value | boolean | sdk是否输出debug日志 |

*   获取Sdk版本号
    

**String SnapliiSdk.getVersion();**

| 返回值 | 类型     | 说明               |
|-----|--------|------------------|
| ret | String | 获取Sdk版本号，例如1.0.0 |


六.测试和发布
-------

在应用发布之前，需要仔细测试集成的支付 SDK 功能，确保支付过程流畅且无错误。一旦确认Snaplii信用付支付功能已经正常工作，就可以将你的应用发布到市场上供用户使用。

七.运营广告跳转
-------

针对运营需求，在宿主App的运营Widget需要跳转到Snaplii的落地H5页面，该H5页面会跳转到信用付申请页，如果无法跳转，需要宿主App的WebView处理schema为"snapliisdk://"跳转，代码如下：
```java
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
    ... ...
        if (url.startsWith("snapliisdk://")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    ... ...
    }
```
