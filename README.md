# 设计原则
约定>配置，简单、简单、还是简单。关于领域微服务架构，参考：[浅谈微服务体系中的分层设计和领域划分](https://tbwork.github.io/2018/10/25/layed-dev-arch/)


# 支持的功能
1. Spring Boot 2.7.14 + Spring Cloud 2021.0.8
2. Anole 多环境配置：不同环境的相同配置可以放到同一个文件中
3. RPC调用：OpenFeign Client定义时只需指定127.0.0.1本地地址，方便本地调试，仅会在local环境下生效，非local环境走注册中心
4. 注册中心：支持Eureka/Nacos
5. 消息队列：阿里云RocketMQ 5.x/腾讯云RocketMQ 5.x
6. 对象存储：阿里云OSS/腾讯云COS
7. 缓存：Redis/本地缓存
8. 自带登录体系，通过@Public和@Private可以快速定义公开和登录型接口
9. 全局上下文：在代码的任何地方均可快速的获取上下文的信息，比如用户，租户等
10. 链路追踪：全链路用traceId，应用内用requestId。
11. 数据库ORM：com.baomidou.mybatis-plus
12. 日志系统：slf4j，建议使用logback/log4j2
13. 异常处理：请严格使用MagicException；错误定义包含“开发”和“用户”双视角。
14. 接口文档：Swagger 2.9.2，自动拆分docket，按照约定写controller，无需额外配置
15. 常用的工具类：日期、数学、加密、Http访问等。方法定义简单、直接、防呆。

# 所有权归属单位
上海魔法向量网络有限公司