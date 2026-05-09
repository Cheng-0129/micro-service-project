# 微服务项目 - 多模块整合

## 项目简介
基于 **Spring Boot 3.2.0 + Spring Cloud Alibaba 2023.0.1.2** 构建的微服务项目，目前已整合用户服务、库存服务、订单服务、网关模块，支持服务间 Feign 远程调用、Knife4j 接口文档网关聚合、Seata 分布式事务，后续持续扩展中。

**业务说明**：用户模块可查询/管理用户信息，并通过 Feign 调用订单模块完成下单；订单模块创建订单时调用库存模块扣减库存，支持取消订单时回滚库存。

## 技术栈
| 类别 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.2.0 |
| 微服务框架 | Spring Cloud | 2023.0.1 |
| 微服务框架 | Spring Cloud Alibaba | 2023.0.1.2 |
| 网关 | Spring Cloud Gateway | 4.1.0 |
| 负载均衡 | Spring Cloud LoadBalancer | 4.1.0 |
| ORM 框架 | MyBatis-Plus | 3.5.12 |
| 数据库 | PostgreSQL | 15+ |
| 服务治理 | Nacos | 2.3.x |
| 接口文档 | Knife4j | 4.5.0 |
| 缓存 | Redis | 7.x |
| 分布式事务 | Seata | 2.1.0 |
| 对象映射 | MapStruct | 1.5.5.Final |
| JSON 处理 | Jackson | 2.15.3 |
| 参数校验 | Jakarta Validation | 3.0.2 |
| 注解增强 | Lombok | 1.18.32 |
| 接口注解 | Swagger Annotations | 2.2.21 |

## 项目结构
```
micro-service-project
├── common-core # 公共核心模块（工具类、异常处理、通用配置）
├── common-web # 公共业务模块
├── gateway # 网关模块（路由转发、Knife4j 接口文档聚合）
├── user-service # 用户服务
├── stock-service # 库存服务
├── order-service # 订单服务
└── pom.xml # 父工程 POM（统一依赖管理）
```

## 模块说明
| 模块 | 说明 | 端口 |
|------|------|------|
| `common-core` | 公共核心模块，包含统一返回结果（Result）、全局异常处理（BusinessException）、分页工具（PageVO）、通用工具类（ExceptionUtil） | - |
| `common-web` | 公共 Web 模块，包含全局异常拦截（GlobalExceptionHandler）、Jackson 序列化配置（JacksonConfig）、MyBatis-Plus 分页配置（MyBatisPlusConfig） | - |
| `gateway` | 网关模块，路由转发、Knife4j 接口文档聚合 | 8088 |
| `user-service` | 用户服务，提供用户增删改查、分页查询，通过 Feign 调用订单模块下单 | 8081 |
| `stock-service` | 库存服务，提供库存增删改查、扣减库存、回滚库存 | 8082 |
| `order-service` | 订单服务，提供订单创建（含库存扣减）、取消（含库存回滚）、查询、删除 | 8083 |

## 快速开始
### 1. 环境要求
- **JDK**：17+
- **Maven**：3.6+
- **数据库**：PostgreSQL 15+
- **中间件**：Nacos、Redis、Seata

### 2. 克隆项目
```bash
git clone https://gitee.com/city_xing/micro_service_project.git
cd micro-service-project
```

### 3. 配置环境
修改各模块 `application.yml` 中的以下配置：

- 数据库地址、用户名和密码

- Nacos 连接信息

- Redis 连接信息

- Seata 连接信息

- 网关连接信息

### 4. 编译项目
```bash
mvn clean install -DskipTests
```

### 5. 启动服务
按依赖顺序启动：common-core → 网关 → 业务服务
```bash 
# 启动网关
mvn spring-boot:run -pl gateway

# 启动用户服务
mvn spring-boot:run -pl user-service

# 启动库存服务
mvn spring-boot:run -pl stock-service

# 启动订单服务
mvn spring-boot:run -pl order-service
```

### 6. 访问接口文档
启动完成后，通过网关统一入口访问接口文档：
```
http://localhost:8088/doc.html
```

## 待办事项

- [ ] 完善网关路由规则说明

- [ ] 完善服务间调用关系说明

- [ ] 补充部署说明（Docker / Docker Compose）

## License
待补充
