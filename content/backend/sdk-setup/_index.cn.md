---
title: "信用付SDK服务端接入文档"
date: 2023-04-13T15:24:12-04:00
draft: false
---
一、名词解释
------

appId ： partner申请APP接入后得到  
appSecret ： partner申请APP接入后得到  
SnapliiPubKey： Snaplii提供的验签用公钥

二、签名机制
------

1.  数据传递的CharSet统一设置为"UTF-8"。
    
2.  对称签名使用HmacSHA1算法，密钥为appSecret。  
    例如，appSecret为\[9d879a513337670d0fa4ab3ffcdb79fb\]，原始字符串为\[out\_trans\_no=20150320010101001&refund\_amount=50.4&refund\_reason=个人原因&trans\_no=2014112611001004680 073956707\]，则  
    对应签名为\[CNA8QPTGTIUHKI8SQ8AZUBWHTEO=\]
    
3.  非对称签名使用RSA2算法，公钥为SnapliiPubKey。  
    例如，SnapliiPubKey为\[MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkJ0b8xHTIZQufqpd183q0gObPv8cbA0/kqD/61/onvI08tI/CMW5J84sQmGWwiqM6xsTtcD00MZsqklt6yNG3+7ymh2ZIz6Elug5MxGrZIj1GyHJpbOx/ITiwWaVejf/yRp0AQhhlO+1fz3WBgMctZC2hlkHQv2/QOvS14Vg+94Nx24XF58/iC3+u6YuWXqfdm2oDRhDXfVQGC067QxR82U4dZJ4bL4VUFIMqArBznAsCjIjHo9MuBjjEiIFb+npbDYzOGTXHeIAUSlV5PUJ2nQH4doFEbZ3+qXUr1wuUgD/cdjbk2DtI7XXJvZYy7Cq6QKUAuVi8fjuBSDEMyxY8wIDAQAB\]，原始字符串为\[out\_trans\_no=20150320010101001&refund\_amount=50.4&refund\_reason=个人原因&trans\_no=2014112611001004680 073956707\]，则  
    对应签名为\[QpISa6r8/K5dYaIzRydINXBpRoKRygeZR5GGdeZqW98FUjWJwgSSy3ECmoy3TZxDUbnR8uKlbUVtLEvO3A0ovEF1lbZ2HPyiKk6zMFMUz/fsG00NZ2dOABrSoGMVF/oc0/3C3Y1cmdTbISNVMB56ALZK0tVJxJ4kTAKyco5jQVkfkUM9Whd0R6F0aDNn5ZCqF1F9qNyUZekUPr1iureuUZC0mo0v1V1ZKhvtwF+4300grHj9N22QObHvwmb+VgGI6CAuBc0Z5zmldERubYxDFDUFkpUTCTCtD78uFWd1xkVdRhbnfiRLe7OhzMQN/U9BNoEzdyE5NpgjlDh8Ds+m+A==\]
    
4.  加签原理  
    1). 获取biz\_content的所有内容，不包括字节类型参数，如文件、字节流，剔除值为空的参数；  
    2). 按照第一个字符的键值 ASCII 码递增排序（字母升序排序），如果遇到相同字符则按照第二个字符的键值 ASCII 码递增排序，以此类推；  
    3). 将排序后的参数与其对应值，组合成 参数=参数值 的格式，并且把这些参数用 & 字符连接起来，此时生成的字符串为待签名字符串。  
    例如，biz\_content内容为\[{"trans\_no":"2014112611001004680 073956707","out\_trans\_no":"20150320010101001","refund\_amount":"50.4","refund\_reason":"个人原因"}\]，则  
    生成的原文串为\[out\_trans\_no=20150320010101001&refund\_amount=50.4&refund\_reason=个人原因&trans\_no=2014112611001004680 073956707\]，
    

三、服务器调用机制
---------

Snaplii Server接收partner Server的请求request和Snaplii Server的响应response均使用签名机制来保证接口调用安全性。  
其中，request签名使用对称签名，secret为appSecret； response验签使用非对称签名，publicKey为SnapliiPubKey。

四、服务端调用接口
---------

{{< swagger src="partner-server.json" >}}

五、交易状态说明
--------

| 枚举名称           | 枚举说明                  |
|----------------|-----------------------|
| TRADE_CLOSED   | 未付款交易超时关闭，或支付完成后全额退款。 |
| TRADE_SUCCESS  | 交易支付成功。               |
| TRADE_FINISHED | 交易结束，不可退款。            |


六、错误码示例
-------

| 错误码  | 说明        |
|------|-----------|
| 1001 | 签名错误      |
| 1002 | 参数缺失      |
| 1003 | app未注册    |
| 1004 | 订单错误      |
| 1005 | 支付失败      |
| 1006 | 风控错误      |
| 1007 | 登录token过期 |


六、签名代码参考
--------

{{% attachments title="参见下方Java或PHP代码" sort="asc" /%}}