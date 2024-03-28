# 链接巧匠平台
## 介绍

链接巧匠平台：项目介绍: 短链接 (ShortLink) 是指将一个原始的长 URL (Uniform ResourceLocator) 通过特定的算法或服务转化为一个更短、易于记忆的 URL。短链接通常只包含几个字符，而原始的长 URL 可能会非常长。

## 在线体验地址
http://linkcraft.icu
体验账号：admin  密码：admin123456

## 功能

![image.png](https://cdn.nlark.com/yuque/0/2024/png/38717174/1707912674328-0b9b8a52-5586-4055-98d3-d338ef1a421f.png#averageHue=%23f6f5f4&clientId=u36d9381c-3ac5-4&from=paste&height=428&id=u4ab277ad&originHeight=856&originWidth=2346&originalType=binary&ratio=2&rotation=0&showTitle=false&size=272937&status=done&style=none&taskId=ueef822ee-579f-44e3-b817-e3b57cf8d6d&title=&width=1173)

- 生成唯一标识符：当用户输入或提交一个长 URL 时，短链接服务会生成一个唯一的标识符或者短码。
- 将标识符与长 URL 关联：短链接服务将这个唯一标识符与用户提供的长 URL 关联起来，并将其保存在数据库或者其他持久化存储中。
- 创建短链接：将生成的唯一标识符加上短链接服务的域名（例如：http://nurl.ink/）作为前缀，构成一个短链接。
- 重定向：当用户访问该短链接时，短链接服务接收到请求后会根据唯一标识符查找关联的长 URL，然后将用户重定向到这个长 URL。
- 跟踪统计：一些短链接服务还会提供访问统计和分析功能，记录访问量、来源、地理位置等信息。

## 技术架构

![image.png](https://cdn.nlark.com/yuque/0/2024/png/38717174/1707912693825-6e76a214-6205-4744-9f7a-bae7a37933aa.png#averageHue=%23f8f6f6&clientId=u36d9381c-3ac5-4&from=paste&height=637&id=u4f23287b&originHeight=1274&originWidth=2726&originalType=binary&ratio=2&rotation=0&showTitle=false&size=624965&status=done&style=none&taskId=u228b8b94-c72e-4f8b-b07e-dda76639631&title=&width=1363)

