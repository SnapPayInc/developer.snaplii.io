---
title: "信用付Android SDK接入文档"
date: 2023-04-13T15:13:38-04:00
draft: false
---
**一.概述 Introduction**
---------------------

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

OTP: One Time Password。基于时间的TOTP，用app secret转为base32的字符串后作为key。

orderStr: 订单order String，其格式类似为

    {
		"outterOrderNo":"a275702d001746caace8b40b25a09df6",  
		"orderAmount":"0.01",  
		"personalToken":"9077777766",  
		"sign":"+LtDS7AFES\/k3ttx8yd46TSMlQM="  
    }
其中“sign"为签名，签名方式请参照"信用付SDK服务端接入文档"

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

在 App Module 的 app/build.gradle 中，添加以下内容，将Snaplii SDK 作为项目依赖。

ndk {  
//选择要添加的对应 cpu 类型的 .so 库。  
abiFilters 'armeabi-v7a', 'arm64-v8a'  
}

dependencies {  
// 添加下方的内容  
api 'com.snaplii.sdk:snapliisdk-android:1.0.0'  
// 其它依赖项  
}
```
步骤3:权限配置
--------

配置 AndroidManifest.xml 文件中的运行权限

为正常完成良好的支付流程体验以及支付风控需要，Snaplii SDK 需要使用以下权限：  
```
android.permission.INTERNET  
android.permission.ACCESS\_NETWORK\_STATE  
android.permission.ACCESS\_COARSE\_LOCATION  
android.permission.ACCESS\_FINE\_LOCATION  
android.permission.CAMERA

android.permission.ACCESS\_WIFI\_STATE
```
**步骤4：初始化SDK Initialize SDK**
-----------------------------

**建议在App冷启动后调用SDK初始化方法.**

```java
SnapliiSDK.initSdk(application, appId, SnapliiSDK.LAN_ZH, new OTPCallback() {
    @Override    
    public Observable<String> getOtp() {
    
    }
});
```

**步骤5: 获取用户是否开通了Snaplii信用付**
----------------------------

```java
boolean ret = SnapliiSDK.hasSnapliiCredit(String pt);
```

**步骤6: 开通信用付 Initialize SnapliiCredit user**
--------------------------------------------

开通信用付

```java
SnapliiSDK.initSnapliiCredit(activity, pt, otp, new ICCallback() {
    @Override    
    public void onSuccess() {

    }

    @Override    
    public void onError(int errorCode, int errorMsg) {

    }
});
```

**步骤7: 支付 Start a Payment**
---------------------------

```java
SnapliiSDK.payment(activity, orderStr, new PayResultCallback() {
    @Override    
    public void onSuccess() {

    }

    @Override    
    public void onError(int errorCode, int errorMsg) {

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
    **void SnapliiSdk.initSdk(Application applicationContext, String appId, String lang, OTPCallback callback);**
    

```java
public interface OTPCallback() {
    @Override    
    public Observable<String> getOtp() {
    
    }
});
```

| 参数                 | 类型          | 说明                |
|--------------------|-------------|-------------------|
| applicationContext | Application | Application上下文    |
| appId              | String      | 后台注册的App标识        |
| lang               | String      | 语言设置zh/en         |
| callback           | OTPCallback | sdk请求app更新otp回调方法 |

| OTPCallback返回值 | 类型                       | 说明                            |
|----------------|--------------------------|-------------------------------|
| return         | Observable&lt;String&gt; | app返回 io.reactivex.Observable |

*   查询信用付账号开通信息  
    **boolean SnapliiSdk.hasSnapliiCredit(String pt);**
    

| 参数 | 类型     | 说明             |
|----|--------|----------------|
| pt | String | personal token |


| 返回值    | 类型      | 说明                  |
|--------|---------|---------------------|
| return | boolean | true: 已开通false: 未开通 |


*   注册开通信用付
    

**void SnapliiSdk.initSnapliiCredit(Activity activity, String pt, String otp, ICCallback callback);**

```java
public interface ICCallback {

    void onSuccess();

    void onError(int errorCode, int errorMsg);

}
```

| 参数        | 类型         | 说明                |
|-----------|------------|-------------------|
| activity  | Activity   | activity          |
| pt        | String     | personal token    |
| otp       | String     | one time password |
| callback  | ICCallback | 回调类               |
| errorCode | int        | 错误码               |
| errorMsg  | String     | 错误描述              |

*   支付  
    **void SnapliiSdk.payment(String orderStr, String pt, PayResultCallback callback);**
    

```java
public interface PayResultCallback {

    void onSuccess();

    void onError(int errorCode, int errorMsg);

}
```

| 参数        | 类型                | 说明                |
|-----------|-------------------|-------------------|
| ordStr    | String            | 信用付支付order string |
| pt        | String            | personal token    |
| callback  | PayResultCallback | 支付结果的回调           |
| errorCode | int               | 查询错误码             |
| errorMsg  | String            | 查询错误描述            |

*   支付 (当支付接口返回 “**session 已过期**” 错误时调用)  
    **void SnapliiSdk.payment(String orderStr, String pt, String otp, PayResultCallback callback);**
    

```java
public interface PayResultCallback {

    void onSuccess();

    void onError(int errorCode, int errorMsg);

}
```

| 参数        | 类型                | 说明                |
|-----------|-------------------|-------------------|
| ordStr    | String            | 信用付支付order string |
| pt        | String            | personal token    |
| otp       | String            | one time password |
| callback  | PayResultCallback | 支付结果的回调           |
| errorCode | int               | 查询错误码             |
| errorMsg  | String            | 查询错误描述            |

*   设置语言
    

**void SnapliiSdk.setLanguage(int value);**

| 参数    | 类型      | 说明                                   |
|-------|---------|--------------------------------------|
| value | boolean | SnapliiSDK.EN = 0;SnapliiSDK.CN = 1; |

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

六.错误码示例（最终以Snaplii后端定义为准）
-------------------------

| 错误码  | 说明          |
|------|-------------|
| 1001 | 签名错误        |
| 1002 | 参数错误        |
| 1003 | app未注册      |
| 1004 | 订单错误        |
| 1005 | 支付失败        |
| 1006 | 风控错误        |
| 1007 | pt无效        |
| 1008 | otp无效       |
| 1009 | 信用付账号不存在    |
| 1010 | session 已过期 |

七.测试和发布
-------

在应用发布之前，需要仔细测试集成的支付 SDK 功能，确保支付过程流畅且无错误。一旦确信支付功能已经正常工作，就可以将你的应用发布到市场上供用户使用。