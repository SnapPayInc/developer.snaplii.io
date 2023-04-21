---
title: "信用付iOS SDK接入文档"
date: 2023-04-13T15:13:38-04:00
draft: false
---
# **信用付iOS SDK接入文档**
## **一.概述 Introduction**
Snaplii信用付SDK
## **二.接入前准备**
申请一个业务方标识appId，初始化SDK时传入。
## **三.名词解释 Glossary**
Partner: 第三方接入方

SDK: Snaplii信用付sdk 

用户: 使用接入方宿主app的用户

信用付: Snaplii提供的支付产品

PT: Personal Token

OTP: One Time Password

AppId: App在Snaplii注册的应用标识ID

orderStr: 订单order String
## **步骤1: partner后端Server准备**
请参考后端接入文档。
## **步骤2:添加依赖库**
通过 CocoaPods 导入

pod 'SnapliiSDK-iOS'
## **步骤3:权限配置**
为正常完成良好的支付流程体验以及支付风控需要,info.plist中的开启以下的权限

|Privacy - Camera Usage Description|相机权限|App needs your consent to access the camera to scan the card information. If it is forbidden, it will not be able to obtain the card information
| :- | :- | :- |
|Privacy - Location When In Use Usage Description|定位权限|App needs your geographic location to show you local promotions
## **步骤4：初始化SDK Initialize SDK**
**建议在App冷启动后调用SDK初始化方法.**
```objective-c
__weak typeof(self) weakSelf = self;

[[SnapliiSDK defaultService] initAppId:@"后台注册的App标识" language:@"语言" 
Callback:^(CompletionBlock  _Nonnull responseCallback) {

        responseCallback([weakSelf getUserOTP]);
 }];
 
 
 #pragma mark ---- 获取OTP
 -(NSString *)getUserOTP{
    //网络请求获取OTP
    
    return @"OTP";
}
```
## **步骤5: 获取用户是否开通了Snaplii信用付**
```objective-c
BOOL result = [[SnapliiSDK defaultService] hasSnapliiCredit:@"personal token"];
```
## **步骤6: 开通信用付 Initialize SnapliiCredit user**
开通信用付
```objective-c
[[SnapliiSDK defaultService] initSnapliiCredit:@"personal token" optStr:
@"one time password" callback:^(NSDictionary *resultDic) {
//返回成功或失败的错误码
    NSLog(@"reslut = %@",resultDic);

}];
```
## **步骤7: 支付 Start a Payment**
```objective-c
[[SnapliiSDK defaultService] payment:@"orderStr" callback:^(NSString *result) {
//返回成功或失败的错误码
    NSLog(@"reslut = %@",result);
}];
```
## **[可选配置接口] configurations**
**1.配置语言:**

`[[SnapliiSDK defaultService] setLanguage:@"languag"];`

**2.获取sdk版本:**

`NSString *version = [[SnapliiSDK defaultService] getVersion];`
## **五.SDK接口说明**
- 初始化SDK
  商户APP工程中引入依赖后，调用API前，需要先向注册您的AppId，代码如下：

`[[SnapliiSDK defaultService] initAppId:(NSString *)appId language:(NSString *)language OTPCallback:(GetOTPBlock)OTPCallback];`

|**参数**|**类型**|**说明**|
| :-: | :-: | :-: |
|appId|String|后台注册的App标识|
|lang|String|语言设置zh/en|
|callback|OTPCallback|sdk请求app更新otp回调方法|
- 查询信用付账号开通信息

`[[SnapliiSDK defaultService] hasSnapliiCredit:(NSString *)personalToken];`

|**参数**|**类型**|**说明**|
| :-: | :-: | :-: |
|pt|String|personal token|


|**返回值**|**类型**|**说明**|
| :-: | :-: | :-: |
|return|boolean|<p>true: 已开通</p><p>false: 未开通</p>|
- 注册开通信用付

`[[SnapliiSDK defaultService] initSnapliiCredit:(NSString *)personalToken optStr:(id)optStr callback:(void(^)(NSString *))callback];`

|**参数**|**类型**|**说明**|
| :-: | :-: | :-: |
|activity|Activity|activity|
|pt|String|personal token|
|otp|String|one time password|
|callback|ICCallback|回调类|
|errorCode|int|错误码|
|errorMsg|String|错误描述|
- 设置语言

`[[SnapliiSDK defaultService] setLanguage::(NSString *)languag];`

|**参数**|**类型**|**说明**|
| :-: | :-: | :-: |
|value|NSString|zh/en|
- 获取Sdk版本号

`[[SnapliiSDK defaultService] getVersion];`

|**返回值**|**类型**|**说明**|
| :-: | :-: | :-: |
|result|String|获取Sdk版本号，例如1.0.0|
## **六.错误码示例（最终以Snaplii后端定义为准）**

|**错误码**|**说明**|
| :-: | :-: |
|1001|签名错误|
|1002|参数错误|
|1003|app未注册|
|1004|订单错误|
|1005|支付失败|
|1006|风控错误|
|1007|pt无效|
|1008|otp无效|
|1009|信用付账号不存在|
|1010|session 已过期|
## **七.测试和发布**
在应用发布之前，需要仔细测试集成的支付 SDK 功能，确保支付过程流畅且无错误。一旦确信支付功能已经正常工作，就可以将你的应用发布到市场上供用户使用。
