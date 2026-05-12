# 微服务项目 - 多模块整合

## 项目简介
基于 **Spring Boot 3.2.0 + Spring Cloud Alibaba 2023.0.1.2** 构建的微服务快速开发平台。
已整合用户、库存、订单三大业务模块，集成 Nacos 注册/配置中心、Gateway 网关、Sentinel 流量治理、
Seata 分布式事务、Knife4j 文档聚合，支持 Feign 远程调用与 Redis 缓存。

**业务链路**：用户模块 → Feign 下单 → 订单模块创建订单 → Feign 扣减库存 → 库存模块响应

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
| 服务治理 | Nacos | 3.2.0 |
| 接口文档 | Knife4j | 4.5.0 |
| 缓存 | Redis | 5.0.14.1 |
| 流量控制 | Sentinel | 1.8.8 |
| 分布式事务 | Seata | 2.0.0 |
| 对象映射 | MapStruct | 1.5.5.Final |
| JSON 处理 | Jackson | 2.15.3 |
| 参数校验 | Jakarta Validation | 3.0.2 |
| 注解增强 | Lombok | 1.18.32 |
| 接口注解 | Swagger Annotations | 2.2.21 |

## 项目结构
```
micro-service-project
├── common-core    # 公共核心模块（统一返回、异常处理、工具类）
├── common-web     # 公共 Web 模块（全局异常拦截、序列化、分页配置）
├── gateway        # 网关模块（路由转发、鉴权、限流、文档聚合）
├── user-service   # 用户服务
├── stock-service  # 库存服务
├── order-service  # 订单服务
└── pom.xml        # 父工程 POM（统一依赖管理）
```

> 各业务模块采用统一分层：`config` / `controller` / `convert` / `dto` / `entity` / `mapper` / `service.impl` / `vo`。
> 
> 差异说明：
> - `user-service`、`order-service` 含 `feign` 包（远程调用其他服务）
> - `stock-service` 的 `service` 采用 DB + Cache 双实现
> - `order-service` 含 `common` 包（订单状态枚举等）

## 模块说明
| 模块 | 说明 | 端口 |
|------|------|------|
| `common-core` | 公共核心模块：Result 统一响应体、ResultCode 业务码枚举、BusinessException 业务异常、PageVO 分页工具、ExceptionUtil 异常解包 | - |
| `common-web` | 公共 Web 模块：GlobalExceptionHandler 全局异常拦截、JacksonConfig JSON 序列化配置、MyBatisPlusConfig 分页插件 | - |
| `gateway` | 网关模块：路由转发、全局鉴权（AuthGlobalFilter）、全局异常处理（GlobalErrorWebExceptionHandler）、Sentinel 网关流控（SentinelConfig、SentinelNacosGatewayConfig）、跨域、Knife4j 文档聚合 | 8088 |
| `user-service` | 用户服务：CRUD、分页查询，Feign 调用订单模块下单，接入 Sentinel 熔断降级 | 8081 |
| `stock-service` | 库存服务：CRUD、分页查询、扣减库存、回滚库存，Redis 缓存，接入 Sentinel 熔断降级 | 8082 |
| `order-service` | 订单服务：创建订单（生成订单号、扣减库存）、取消订单（回滚库存）、CRUD、分页查询，Seata 分布式事务，接入 Sentinel 熔断降级 | 8083 |

## 快速开始
### 1. 环境要求
- **JDK**：17+
- **Maven**：3.6+
- **数据库**：PostgreSQL 15+
- **中间件**：Nacos 3.2.0、Redis 5.0.14.1、Seata 2.0.0

### 2. 克隆项目
```bash
git clone https://gitee.com/city_xing/micro_service_project.git
cd micro-service-project
```

### 3. 配置环境
各服务配置文件按环境拆分（application.yml / dev / test / prod / bootstrap.yml），修改对应环境的配置即可。

- **数据库**：PostgreSQL 连接地址、用户名、密码
- **Nacos**：注册中心与配置中心地址（bootstrap.yml）
- **Redis**：连接地址与端口（stock-service）
- **Seata**：事务组与 TC Server 地址（order-service、stock-service）
- **Sentinel**：网关流控规则存储在 Nacos（Data ID: gateway-gateway-flow-rules, Group: SENTINEL_GROUP）
- **日志**：dev 环境 debug，test 环境 info，prod 环境 warn + 文件滚动存储

### 4. 编译项目
```bash
mvn clean install -DskipTests
```

### 5. 启动中间件
按顺序启动以下中间件，确保各服务能正常注册与通信：

| 中间件 | 版本 | 启动方式 | 访问地址 | 默认账号/密码 |
|--------|------|---------|---------|--------------|
| **Nacos** | 3.2.0 | `startup.cmd -m standalone` | `http://localhost:8080` | nacos / nacos |
| **Redis** | 5.0.14.1 | 双击 `redis-server.exe` | - | - |
| **Seata** | 2.0.0 | `seata-server.bat` | - | - |

### 6. 启动服务
按依赖顺序启动：中间件 → 业务服务 → 网关
```bash
# 启动用户服务
mvn spring-boot:run -pl user-service

# 启动库存服务
mvn spring-boot:run -pl stock-service

# 启动订单服务
mvn spring-boot:run -pl order-service

# 启动网关
mvn spring-boot:run -pl gateway
```
> 网关放最后，等子服务都注册到 Nacos 了再启动。

### 7. 访问接口文档
启动完成后，通过网关统一入口访问接口文档：
```
http://localhost:8088/doc.html
```

## 网关路由规则

| 路由前缀 | 目标服务 | 说明 |
|---------|---------|------|
| `/user-service/**` | user-service (8081) | 用户服务，StripPrefix=1 |
| `/order-service/**` | order-service (8083) | 订单服务，StripPrefix=1 |
| `/stock-service/**` | stock-service (8082) | 库存服务，StripPrefix=1 |

白名单路径（无需鉴权）：

- 登录注册：`/user/login`、`/user/register`
- Knife4j 文档：`/doc.html`、`/webjars/**`、`/**/v3/api-docs/**`、`/**/swagger-resources/**`、`/**/swagger-ui/**`

鉴权失败返回 HTTP 401 + 统一 JSON 响应体（GATEWAY_TOKEN_MISSING、GATEWAY_TOKEN_EXPIRED 等）。

## 服务间调用关系

```
用户浏览器
  └── 网关 (8088)
        ├── /user-service/** → user-service (8081)
        │     └── POST /user/order → Feign → order-service (8083)
        ├── /order-service/** → order-service (8083)
        │     ├── POST /order/create → Feign → stock-service (8082)
        │     └── PUT /order/cancel/{orderNo} → Feign → stock-service (8082)
        └── /stock-service/** → stock-service (8082)
```

## 流量控制与熔断降级

### 网关层
网关集成 Sentinel，支持从 Nacos 动态加载流控规则：

- **Data ID**：`gateway-gateway-flow-rules`
- **Group**：`SENTINEL_GROUP`
- **规则类型**：QPS 限流 → `ParamFlowException`，授权规则 → `AuthorityException`
- **限流响应**：HTTP 429 + 统一 JSON 格式（`GATEWAY_RATE_LIMIT`）

### 业务层
各子服务通过 `@SentinelResource` 接入熔断降级：

| 服务 | 资源名 | fallback | blockHandler |
|------|--------|----------|-------------|
| user-service | `userCreateOrder` | 系统异常 → `USER_DEGRADE` | 限流/熔断 → `USER_FLOWING` |
| order-service | `createOrder` | 系统异常 → `ORDER_DEGRADE` | 限流/熔断 → `ORDER_FLOWING` |
| order-service | `cancelOrder` | 系统异常 → `ORDER_DEGRADE` | 限流/熔断 → `ORDER_FLOWING` |
| stock-service | `deductStock` | 系统异常 → `STOCK_DEGRADE` | 限流/熔断 → `STOCK_FLOWING` |

**降级策略**：
- 业务异常（`BusinessException`）通过 `ExceptionUtil.unwind()` 解包后透传原错误码
- 系统异常返回对应模块的 `DEGRADE` 业务码
- 限流/熔断返回对应模块的 `FLOWING` 业务码

## 待办事项

- [x] 完善网关路由规则说明
- [x] 完善服务间调用关系说明
- [x] 接入 Sentinel 流量控制与熔断降级
- [ ] 接入 RocketMQ 异步消息
- [ ] 补充部署说明（Docker / Docker Compose）

## License
待补充
