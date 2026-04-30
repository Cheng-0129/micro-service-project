\# 微服务项目 - 多模块整合（目前已有用户/库存/订单）

\## 项目简介

基于 \*\*Spring Boot 3.x + Spring Cloud Alibaba\*\* 构建的微服务项目，包含用户服务、库存服务、订单服务模块。



\## 技术栈

\- 核心框架：Spring Boot 3.x、Spring Cloud Alibaba

\- ORM 框架：MyBatis-Plus

\- 数据库：PostgreSQL

\- 服务治理：Nacos（服务注册与配置中心）

\- 接口文档：Knife4j

\- 工具类库：Lombok、Fastjson、MapStruct

\- 缓存：Redis



\## 启动步骤

1\.  克隆项目

&#x20;   bash

&#x20;   git clone https://gitee.com/city\_xing/micro\_service\_project.git

&#x20;   cd micro-service-project



2\.  配置环境

&#x20;   # 环境配置

&#x20;   确保 JDK 版本为 17，Maven 版本为 3.6+

&#x20;

&#x20;   # yml配置

&#x20;   修改各模块 `application.yml` 中的数据库地址、用户名和密码，Nacos、Redis 连接信息



3\.  启动项目

&#x20;   # 父工程编译

&#x20;   mvn clean install -DskipTests



&#x20;   # 启动各模块（按依赖顺序启动：common-core → 基础服务 → 业务服务）

&#x20;   mvn spring-boot:run -pl user-service

&#x20;   mvn spring-boot:run -pl stock-service

&#x20;   mvn spring-boot:run -pl order-service 



\## 模块说明

\- `common-core`：公共模块（工具类、异常处理、通用配置）

\- `order-service`：订单模块

\- `stock-service`：库存模块

\- `user-service`：用户模块

