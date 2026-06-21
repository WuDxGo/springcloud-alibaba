# Spring Cloud Alibaba OAuth2 SSO 接入示例

本项目演示 Spring Cloud Alibaba 微服务体系接入 `oauth2-sso` 认证中心：Gateway 负责统一登录入口，业务服务作为资源服务器校验 JWT。

## 项目结构

```text
springcloud-alibaba/
├── gateway-server/     # 网关，OAuth2 Client + JWT Resource Server
├── order-service/      # 订单服务，资源服务器
├── product-service/    # 商品服务，资源服务器
├── common/             # 通用组件和 Feign Token 传递
├── common-security/    # 资源服务器自动配置
├── api/                # Feign API 定义
└── common-security.yml # Nacos 公共安全开关模板
```

## 调用链路

```text
浏览器 -> gateway-server -> order-service/product-service
          |
          +-- 未登录时跳转 oauth2-sso /oauth2/authorize
```

启用 SSO 时：

1. 用户访问 Gateway 受保护路径。
2. Gateway 未登录时跳转认证中心。
3. 认证中心登录成功后回调 Gateway。
4. Gateway 将 Token 传递给下游服务。
5. 业务服务校验 JWT，并从 `authorities` claim 读取权限。

## 一键 SSO 开关

三个服务统一导入 Nacos `nacos-common` 分组下的 `common-security.yml`：

```yaml
common:
  security:
    enabled: false
```

| 值 | 行为 |
|----|------|
| `true` | 启用 SSO，Gateway 跳转认证中心，业务服务校验 JWT |
| `false` | 关闭 SSO，Gateway 和业务服务放行请求 |

注意：

- 如果服务自己的 `application-nacos.yml` 也配置了 `common.security.enabled`，要删除或与公共配置保持一致。
- 修改 Nacos 配置后，建议重启相关服务，确保安全过滤链按新配置创建。
- `common-security.yml` 是本地模板，需要在 Nacos 中创建同名配置。

## Gateway 接入点

`gateway-server` 自定义安全配置：

- SSO 开启：启用 OAuth2 Login 和 JWT Resource Server。
- SSO 关闭：注册放行所有请求的 `SecurityWebFilterChain`。
- Token Relay 过滤器只在 SSO 开启时注册。

关键配置：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          gateway-client:
            client-id: gateway-client
            client-secret: gateway-secret
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8081/login/oauth2/code/gateway-client
            scope: openid,profile,read,write
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/oauth2/jwks
```

## 业务服务接入方式

### 需要认证的服务

引入 `common-security`：

```xml
<dependency>
    <groupId>com.xiao</groupId>
    <artifactId>common-security</artifactId>
    <version>${project.version}</version>
</dependency>
```

配置 JWK：

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/oauth2/jwks
```

权限解析配置保持：

```yaml
common:
  security:
    authority-prefix: ""
    authorities-claim-name: authorities
```

原因：认证中心签发的 JWT 中 `authorities` 已包含 `ROLE_ADMIN` 这类完整角色名，资源服务不要再追加 `ROLE_` 前缀。

### 不需要认证的服务

如果新增服务完全公开，最干净的方式是**不要引入安全依赖**：

- 不引入 `common-security`
- 不引入 `spring-boot-starter-security`
- 不引入 `spring-boot-starter-oauth2-resource-server`

这样服务自身不会创建 Spring Security 过滤链，也不需要配置 `common.security.enabled=false`。

但要注意：

- 如果请求经过 Gateway，Gateway 仍可能先拦截，需要在 Gateway 放行该服务路径，或关闭 Gateway SSO。
- 如果服务代码使用 `@PreAuthorize`、`SecurityContextHolder`、`JwtAuthenticationToken` 或当前用户解析能力，就仍然需要安全依赖。
- 如果服务不校验 JWT，应确保它不能被绕过 Gateway 直接访问，生产环境至少保留网关层限流、审计和来源控制。

## common-security 配置

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `common.security.enabled` | `true` | 是否启用资源服务器安全配置 |
| `common.security.permit-all-patterns` | `/actuator/**` | 放行路径 |
| `common.security.authority-prefix` | `ROLE_` | JWT 权限前缀 |
| `common.security.authorities-claim-name` | `authorities` | 权限 claim 名称 |

只放行部分接口示例：

```yaml
common:
  security:
    enabled: true
    permit-all-patterns:
      - /actuator/**
      - /public/**
      - /health
```

## 启动顺序

1. 启动 Nacos、MySQL、Redis。
2. 启动 `oauth2-sso/oauth-server`。
3. 启动 `gateway-server`。
4. 启动 `product-service`、`order-service`。

## 常见问题

### 关闭 SSO 后仍跳转认证中心

检查：

1. `common-security.yml` 是否在 Nacos `nacos-common` 分组。
2. 服务自己的 `application-nacos.yml` 是否覆盖了 `common.security.enabled=true`。
3. Gateway 是否已重启。
4. `gateway-server` 是否仍注册了 SSO 安全过滤链。

### 业务服务返回 403

检查：

1. JWT 是否包含 `authorities` claim。
2. 资源服务是否配置 `authority-prefix: ""`。
3. 方法权限是否与 JWT 中权限完全匹配。

### 不需要认证的服务是否必须导入 common-security

不必须。完全公开的服务可以不引入 security 相关依赖；如果需要读取用户身份或做权限控制，再引入 `common-security`。
