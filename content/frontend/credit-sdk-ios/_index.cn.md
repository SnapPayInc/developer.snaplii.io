---
title: "信用付iOS SDK接入文档"
date: 2023-04-13T15:13:38-04:00
draft: false
---
# **信用付iOS SDK接入文档**

**一.概述 Introduction**
---------------------

Snaplii信用付SDK

<a href="./demo.zip" target="_blank" style="color:blue">下载demo工程</a>

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

**四. OTP生成算法**
---------

One time passcode是采用标准的基于时间的TOTP算法，目前设置的有效时间跨度为1分钟。OTP的密钥为app secret + personal token拼接而成。密钥算法为"HmacSHA1"。Python算法可参照：

    secret_key= secret + pt
    key_str = base64.b32encode(secret_key.encode('utf-8')).decode("utf-8")
    pyotp.TOTP(key_str, digest="sha1", interval=60).now()
其他语言的实现可参照对应的实现文档


**五. 服务端准备工作: partner服务端Server准备**
---------

请参考服务端接入文档。

**六. 客户端接入流程准备工作 (可参考文档中的demo代码工程)**
---------

**步骤1:添加依赖库**
---------

通过 CocoaPods 导入
在 `podfile` 文件添加

```
pod 'SnapliiSDK', :git => 'git@github.com:SnapPayInc/cocoapods.git', :branch => 'master'

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
---------

为正常完成良好的支付流程体验以及支付风控需要，`info.plist` 中的开启以下的权限

|Privacy - Camera Usage Description|相机权限|App needs your consent to access the camera to scan the card information. If it is forbidden, it will not be able to obtain the card information
| :- | :- | :- |
|Privacy - Location When In Use Usage Description|定位权限|App needs your geographic location to show you local promotions

**步骤3：初始化SDK Initialize SDK**

---------
**建议在App冷启动后调用SDK初始化方法.**
```swift
SnapliiSDKManager.defaultService().initAppId("后台注册的App标识", language: "语言", personalToken: "用户号", customData:"自定义用户数据") { [weak self] responseCallback in
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

 
// MARK: 获取OTP
func getOtp(callback: @escaping (_ result: String?, _ error: String?) -> Void) {
    //网络请求获取OTP
    if(成功) {
        callback("otp")
    } else {
        callback("请求错误")
    }
}
```


**步骤4: 获取用户是否开通了Snaplii信用付**
---------
```swift
SnapliiSDKManager.defaultService().hasSnapliiCredit { [weak self] success, message in
    guard let strongSelf = self else { return }
    if (result == false) {
        print("没有信用付")
    } else {
        print("有信用付")
    }
}
```

**步骤5: 开通信用付 Initialize SnapliiCredit user**
---------
开通信用付
```swift
[[SnapliiSDKManager defaultService] initSnapliiCreditCallback:^(NSString * _Nonnull result) {
   //返回成功或失败的错误码
    NSLog(@"%@", result);
}];
```
**步骤6: 支付 Start a Payment**
---------
```swift
SnapliiSDKManager.defaultService().payment("签名", orderAmount: "订单金额" , outterOrderNo: "订单号", viewController: "当前控制器") { [weak self] success, message in
    guard let strongSelf = self else { return }
    //返回成功或失败的错误码
    dump("Payment result: \(message ?? "success")")
}
```

**步骤8: 退出**
---------
```swift
    SnapliiSDKManager.defaultService().logout()
```
**[可选配置接口] configurations**
---------
**1.配置语言:**

`SnapliiSDKManager.defaultService().setLanguage(lang)`

**2.获取sdk版本:**
---------

`let version = SnapliiSDKManager.defaultService().getVersion()`

**五.SDK接口说明**
---------

- 初始化SDK
  商户APP工程中引入依赖后，调用API前，需要先向注册您的AppId，代码如下：

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

- 查询信用付账号开通信息

`- (void)hasSnapliiCredit:(HasSnapliiCreditCallback)completion`

```Objective-C
typedef void (^Callback)(BOOL success, NSString * _Nullable message);
typedef Callback HasSnapliiCreditCallback;
```

- 注册开通信用付

`- (void)initSnapliiCredit:(UIViewController *)viewController
                 callback:(ApplyResultCallback)callback`

```Objective-C
typedef void (^Callback)(BOOL success, NSString * _Nullable message);
typedef Callback ApplyResultCallback;
```


| 参数        | 类型                | 说明            |
|-----------|-------------------|-------------------|
| _ currentController  | UIViewController | currentController |
| callback  | ApplyResultCallback | 申请结果的回调 |

- 设置语言
    
`- (void)setLanguage:(NSString *)language;`

|**参数**|**类型**|**说明**|
| :-: | :-: | :-: |
|value|NSString|zh/en|

- 获取Sdk版本号

`- (NSString *)getVersion;`

|**返回值**|**类型**|**说明**|
| :-: | :-: | :-: |
|result|String|获取Sdk版本号，例如0.0.1|

**六.测试和发布**
---------
在应用发布之前，需要仔细测试集成的支付 SDK 功能，确保支付过程流畅且无错误。一旦确信支付功能已经正常工作，就可以将你的应用发布到市场上供用户使用。
