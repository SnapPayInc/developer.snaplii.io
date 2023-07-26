---
title: "信用付纯支付 iOS SDK 接入文档"
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

开发环境: Xcode.

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


## 五. 服务端准备工作: partner服务端Server准备

请参考服务端接入文档。

## 六. 客户端接入流程准备工作 (可参考文档中的demo代码工程)

**步骤1:添加依赖库**
---------

通过 CocoaPods 导入
在podfile文件添加
```
pod 'AFNetworking', '~> 4.0.1'
pod 'Masonry', '~> 1.1.0'
pod 'MJExtension', '~> 3.4.1'
pod 'MBProgressHUD', '~> 1.2.0'
pod 'YYCache', '~> 1.0.4'
pod 'AcuantiOSSDKV11/AcuantCamera', '~> 11.5.8'
pod 'AcuantiOSSDKV11/AcuantFaceCapture', '~> 11.5.8'
pod 'AcuantiOSSDKV11/AcuantHGLiveness', '~> 11.5.8'

post_install do |installer|
  installer.pods_project.targets.each do |target|
    if ['AcuantiOSSDKV11', 'Socket.IO-Client-Swift', 'Starscream'].include? target.name
      target.build_configurations.each do |config|
        config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
      end
    end
  end
end
```

**步骤2:权限配置**
--------

为正常完成良好的支付流程体验以及支付风控需要,info.plist中的开启以下的权限

|Privacy - Camera Usage Description|相机权限|App needs your consent to access the camera to scan the card information. If it is forbidden, it will not be able to obtain the card information
| :- | :- | :- |
|Privacy - Location When In Use Usage Description|定位权限|App needs your geographic location to show you local promotions

**步骤3：初始化SDK Initialize SDK**
-----------------------------


```swift
SnapliiSDKManager.defaultService().initAppId(APP_ID, language: lang, personalToken: pt, customerData: "") { [weak self] responseCallback in
    guard let strongSelf = self else {
        return
    }
    strongSelf.getOtp { result, error in
        if let opt = result {
            responseCallback(opt)
        } else if let error = error {
            dump(error)
        }
    }
}
```

**步骤4：选择信用付支付时，判断是否登录，未登录调用登录接口**
-----------------------------
```swift
let hasLogin = SnapliiSDKManager.defaultService().hasLogin()
if (!hasLogin) {
    SnapliiSDKManager.defaultService().login(self) { success, message in

    }
}
```

**步骤5: 支付 Start a Payment**
---------------------------

```swift
SnapliiSDKManager.defaultService().payment(sign, orderAmount: amount, outterOrderNo: outterOrderNo, viewController: self) { [weak self] success, message in
    guard let strongSelf = self else { return }
    dump("Payment result: \(message ?? "success")")
}
```




**\[可选配置接口\] configurations**
-----------------------------

**1.登出现有信用付账号，切换手机号:**
```Objective-C
SnapliiSDKManager.defaultService().logout()
```

**2.配置语言:**
```Objective-C
SnapliiSDKManager.defaultService().setLanguage(lang)
```


五.SDK接口说明
---------

*   初始化SDK  
    商户APP工程中引入依赖后，调用API前，需要先向注册您的AppId，代码如下：  

`- (void)initAppId:(NSString *)appId
         language:(NSString *)language
    personalToken:(NSString *)personalToken
     customerData:(NSString *)customerData
         callback:(OTPCallback)callback;`

```Objective-C
typedef void(^GetOTPBlock)(NSString *otp);
typedef void(^OTPCallback)(GetOTPBlock getOTPBlock);
```


|**参数**|**类型**|**说明**|
| :-: | :-: | :-: |
|appId|NSString|后台注册的App标识|
|lang|NSString|语言设置zh/en|
|personalToken|NSString|Personal Token|
|coustomData|NSString|业务自定义参数|
|callback|OTPCallback|app通过callback把otp设置给sdk|


*   登录
    

`- (void)login:(UIViewController *)currentController
     callback:(LoginCallback)callback;`

```Objective-C
typedef void (^LoginCallback)(BOOL success, NSString * _Nullable message);
```


| 参数        | 类型                | 说明            |
|-----------|-------------------|-------------------|
| _ currentController  | UIViewController | currentController |
| callback  | LoginCallback | 登录结果的回调 |


*   支付  
`- (void)payment:(NSString *)paymentSign
    orderAmount:(NSString *)orderAmount
  outterOrderNo:(NSString *)outterOrderNo
 viewController:(UIViewController *)viewController
       callback:(PayResultCallback)callback;`
    

```Objective-C
typedef void (^Callback)(BOOL success, NSString * _Nullable message);
```

| 参数        | 类型                | 说明                |
|-----------|-------------------|-------------------|
| _paymentSign  | NSString           | paymentSign    |
| orderAmount    | NSString            | 信用付支付order string |
| outterOrderNo  | NSString | 支付结果的回调           |
| viewController | UIViewController   | currentController            |
| callback  | PayResultCallback            | 支付回调           |


*   检查是否登录
    

`- (BOOL)hasLogin;`


*   退出登录
    
`- (void)logout;`
  
*   设置语言
    
`- (void)setLanguage:(NSString *)language;`

|**参数**|**类型**|**说明**|
| :-: | :-: | :-: |
|value|NSString|zh/en|


## **六.测试和发布**
在应用发布之前，需要仔细测试集成的支付 SDK 功能，确保支付过程流畅且无错误。一旦确信支付功能已经正常工作，就可以将你的应用发布到市场上供用户使用。


