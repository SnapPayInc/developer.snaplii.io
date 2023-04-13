---
title: "Demo Index"
date: 2023-04-12T14:37:39-04:00
draft: false
---
# aaa
## bbb

```mermaid
sequenceDiagram
Snaplii->>Trans.Order: Order Info and item
Trans.Order-->>Trans: getDiscount
Trans.Order-->>Trans.Payment: getPaymentOption
Trans.Order->>Snaplii: Order Quote
Snaplii->>Snaplii: User making decision on discount and payment
```

{{< swagger src="sample.json" >}}