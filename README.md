## 介绍
链接巧匠平台：项目介绍: 短链接 (ShortLink) 是指将一个原始的长 URL (Uniform ResourceLocator) 通过特定的算法或服务转化为一个更短、易于记忆的 URL。短链接通常只包含几个字符，而原始的长 URL 可能会非常长。
## 在线体验地址
http://117.72.17.190/
体验账号：admin  密码：admin123456
## 功能
[https://images-machen.oss-cn-beijing.aliyuncs.com/image-20231115133642504.png](https://images-machen.oss-cn-beijing.aliyuncs.com/image-20231115133642504.png)

- 生成唯一标识符：当用户输入或提交一个长 URL 时，短链接服务会生成一个唯一的标识符或者短码。
- 将标识符与长 URL 关联：短链接服务将这个唯一标识符
与用户提供的长 URL 关联起来，并将其保存在数据库或者其他持久化存储中。
- 创建短链接：将生成的唯一标识符加上短链接服务的域名作为前缀，构成一个短链接。
- 重定向：当用户访问该短链接时，短链接服务接收到请求后会根据唯一标识符查找关联的长 URL，然后将用户重定向到这个长 URL。
- 跟踪统计：一些短链接服务还会提供访问统计和分析功能，记录访问量、来源、地理位置等信息。
## 技术架构
https://images-machen.oss-cn-beijing.aliyuncs.com/image-20231115133642504.png![image](https://github.com/axdmdYJ/shortlink-all/assets/121956515/b7238448-bc05-4482-a385-f5cc656bdc90)



