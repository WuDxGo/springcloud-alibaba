# SpringCloud Alibaba 接入 OAuth2-SSO 完整说明

> **创建时间**: 2026 年 3 月 26 日  
> **适用版本**: Spring Boot 3.5.11 + Spring Cloud 2025.0.1 + Spring Cloud Alibaba 2025.0.0.0

---

## 一、项目架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户浏览器                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    OAuth2-SSO 认证中心                            │
│                    (端口：8080)                                  │
│                  Spring Authorization Server                     │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ OAuth2 授权
                              │
┌─────────────────────────────────────────────────────────────────┐
│                     Gateway 网关                                 │
│                    (端口：8081)                                  │
│           OAuth2 Client + OAuth2 Resource Server                 │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
              ▼               ▼               ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Product Service │  │   Order Service  │  │   API Module     │
│    (端口：8082)   │  │    (端口：8083)   │  │   (Feign 客户端)   │
│ Resource Server  │  │ Resource Server  │  │                  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

## 二、各模块依赖添加说明

### 2.1 父 POM (pom.xml)

**位置**: `F:\Personal_Work\springcloud-alibaba\pom.xml`

父 POM 已统一管理依赖版本，**无需修改**。主要管理的 OAuth2 相关依赖：

```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot 依赖管理 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Spring Cloud 依赖管理 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

### 2.2 Gateway Server 模块

**位置**: `F:\Personal_Work\springcloud-alibaba\gateway-server`

#### 2.2.1 pom.xml 依赖

```xml
<dependencies>
    <!-- 网关核心 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>

    <!-- 服务发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- 负载均衡 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>

    <!-- 响应式 Redis（网关会话存储） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
    </dependency>

    <!-- ============ OAuth2 相关依赖 ============ -->
    
    <!-- OAuth2 客户端 - 用于 SSO 登录（核心） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>

    <!-- OAuth2 资源服务器 - 用于验证 JWT Token（核心） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Spring Security（核心） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

#### 2.2.2 application.yml 配置

```yaml
server:
  port: 8081

spring:
  application:
    name: gateway-server

  # ============ OAuth2 客户端配置（核心） ============
  security:
    oauth2:
      client:
        registration:
          gateway-client:
            client-id: gateway-client
            client-secret: gateway-secret
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8081/login/oauth2/code/gateway-client
            scope: profile,read,write
            client-name: 网关客户端
            provider: gateway-provider
        provider:
          gateway-provider:
            authorization-uri: http://localhost:8080/oauth2/authorize
            token-uri: http://localhost:8080/oauth2/token
            jwk-set-uri: http://localhost:8080/oauth2/jwks
            user-info-uri: http://localhost:8080/api/users/me
            user-name-attribute: username

      # ============ OAuth2 资源服务器配置（核心） ============
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/oauth2/jwks

  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: 6662f928-1f3c-453c-bb2d-083806cdc7d3
        group: gateway-server
        username: nacos
        password: nacos

    gateway:
      routes:
        - id: order-service
          uri: http://localhost:8083
          predicates:
            - Path=/order/**
          filters:
            - StripPrefix=0

        - id: product-service
          uri: http://localhost:8082
          predicates:
            - Path=/product/**
          filters:
            - StripPrefix=0
```

#### 2.2.3 SecurityConfig.java 配置类

**位置**: `gateway-server/src/main/java/com/xiao/gateway/config/SecurityConfig.java`

```java
package com.xiao.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Gateway 安全配置
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // 完全禁用 CSRF（WebFlux OAuth2 登录需要）
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // 配置授权规则
            .authorizeExchange(exchanges -> exchanges
                // 放行 OAuth2 相关端点
                .pathMatchers("/oauth2/**").permitAll()
                .pathMatchers("/login/**").permitAll()
                .pathMatchers("/logout").permitAll()
                // 放行错误页面
                .pathMatchers("/error").permitAll()
                // 放行静态资源
                .pathMatchers("/static/**", "/public/**", "/favicon.ico").permitAll()
                // 放行 Actuator 端点
                .pathMatchers("/actuator/**").permitAll()
                // 放行 API 文档
                .pathMatchers("/api/auth/**").permitAll()
                // 其他请求需要认证
                .anyExchange().authenticated()
            )
            // 启用 OAuth2 登录
            .oauth2Login(oauth2 -> oauth2
                .authorizedClientRepository(authorizedClientRepository())
            )
            // 启用 OAuth2 资源服务器（JWT 验证）
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter()))
                )
            )
            // 配置登出
            .logout(logout -> logout
                .logoutUrl("/logout")
            )
            // 配置异常处理 - 未认证时重定向到 OAuth2 登录
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((exchange, e) -> {
                    String redirectUrl = "/oauth2/authorization/gateway-client";
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().set("Location", redirectUrl);
                    return exchange.getResponse().setComplete();
                })
            );

        // 允许 iframe 嵌入（开发环境）
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions
                .mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN)
            )
        );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * 配置登录错误页面路由
     */
    @Bean
    public RouterFunction<ServerResponse> loginErrorRoute() {
        return RouterFunctions.route(GET("/login"), request -> {
            String error = request.queryParam("error").orElse(null);
            if (error != null) {
                return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .bodyValue("<html><body><h1>登录失败</h1><p>错误：" + error + "</p><a href='/oauth2/authorization/gateway-client'>重新登录</a></body></html>");
            }
            return ServerResponse.notFound().build();
        });
    }
}
```

---

### 2.3 Order Service 模块

**位置**: `F:\Personal_Work\springcloud-alibaba\order-service`

#### 2.3.1 pom.xml 依赖

```xml
<dependencies>
    <!-- API 模块（Feign 客户端） -->
    <dependency>
        <groupId>com.xiao</groupId>
        <artifactId>api</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- 公共组件模块 -->
    <dependency>
        <groupId>com.xiao</groupId>
        <artifactId>common</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- Web 服务 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 服务发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- ============ OAuth2 相关依赖 ============ -->
    
    <!-- OAuth2 资源服务器 - 用于验证 JWT Token（核心） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Spring Security（核心） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
</dependencies>
```

#### 2.3.2 application.yml 配置

```yaml
server:
  port: 8083

spring:
  application:
    name: order-service

  # ============ OAuth2 资源服务器配置（核心） ============
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/oauth2/jwks
          issuer-uri: http://localhost:8080

  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: 6662f928-1f3c-453c-bb2d-083806cdc7d3
        group: gateway-server
        username: nacos
        password: nacos

# 日志配置
logging:
  level:
    com.xiao.order: INFO
    org.springframework.security: INFO
```

#### 2.3.3 SecurityConfig.java 配置类

**位置**: `order-service/src/main/java/com/xiao/order/config/SecurityConfig.java`

```java
package com.xiao.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Order Service 安全配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（API 服务）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置会话管理为无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置授权规则
            .authorizeHttpRequests(authorize -> authorize
                // 放行 Actuator 端点
                .requestMatchers("/actuator/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 启用 OAuth2 资源服务器（JWT 验证）
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
```

---

### 2.4 Product Service 模块

**位置**: `F:\Personal_Work\springcloud-alibaba\product-service`

#### 2.4.1 pom.xml 依赖

```xml
<dependencies>
    <!-- Web 服务 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 服务发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- ============ OAuth2 相关依赖 ============ -->
    
    <!-- OAuth2 资源服务器 - 用于验证 JWT Token（核心） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Spring Security（核心） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>

    <!-- Druid 连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.2.20</version>
    </dependency>
</dependencies>
```

#### 2.4.2 application.yml 配置

```yaml
server:
  port: 8082

spring:
  application:
    name: product-service

  # ============ OAuth2 资源服务器配置（核心） ============
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/oauth2/jwks
          issuer-uri: http://localhost:8080

  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: 6662f928-1f3c-453c-bb2d-083806cdc7d3
        group: gateway-server
        username: nacos
        password: nacos

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3307/domestic?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
    type: com.alibaba.druid.pool.DruidDataSource

mybatis-plus:
  mapper-locations: classpath:/mapper/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true

# 日志配置
logging:
  level:
    org.springframework.security: INFO
```

#### 2.4.3 SecurityConfig.java 配置类

**位置**: `product-service/src/main/java/com/xiao/config/SecurityConfig.java`

```java
package com.xiao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Product Service 安全配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（API 服务）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置会话管理为无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置授权规则
            .authorizeHttpRequests(authorize -> authorize
                // 放行 Actuator 端点
                .requestMatchers("/actuator/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 启用 OAuth2 资源服务器（JWT 验证）
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
```

---

### 2.5 API 模块

**位置**: `F:\Personal_Work\springcloud-alibaba\api`

#### 2.5.1 pom.xml 依赖

API 模块主要定义 Feign 客户端接口，**不需要添加 OAuth2 依赖**。

```xml
<dependencies>
    <!-- OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
        <exclusions>
            <!-- 移除 commons-fileupload 依赖，存在漏洞 -->
            <exclusion>
                <groupId>commons-fileupload</groupId>
                <artifactId>commons-fileupload</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- 负载均衡 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

---

### 2.6 Common 模块

**位置**: `F:\Personal_Work\springcloud-alibaba\common`

#### 2.6.1 pom.xml 依赖

Common 模块包含 Feign 拦截器等公共组件，需要添加 OAuth2 依赖用于获取 JWT Token。

```xml
<dependencies>
    <!-- OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- Spring Security OAuth2 Resource Server（用于获取 JWT Token） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
</dependencies>
```

---

## 三、OAuth2-SSO 认证中心配置

### 3.1 启动认证中心

```bash
cd F:\Personal_Work\oauth2-sso

# 1. 初始化数据库（如果尚未初始化）
mysql -u root -p < init.sql

# 2. 启动 oauth-server
cd oauth-server
mvn spring-boot:run
```

**服务信息：**
- 端口：8080
- 登录页面：http://localhost:8080

### 3.2 客户端配置

确保 OAuth2-SSO 中已配置 `gateway-client` 客户端：

| 配置项 | 值 |
|--------|-----|
| client_id | gateway-client |
| client_secret | gateway-secret |
| authorization_grant_type | authorization_code |
| redirect_uri | http://localhost:8081/login/oauth2/code/gateway-client |
| scope | openid,profile,read,write |
| requireConsent | false |

---

## 四、启动顺序

### 步骤 1：启动 OAuth2-SSO 认证中心

```bash
cd F:\Personal_Work\oauth2-sso\oauth-server
mvn spring-boot:run
```

### 步骤 2：编译 springcloud-alibaba 项目

```bash
cd F:\Personal_Work\springcloud-alibaba
mvn clean install -DskipTests
```

### 步骤 3：启动各微服务

**顺序：**
1. Product Service (端口 8082)
2. Order Service (端口 8083)
3. Gateway Server (端口 8081)

```bash
# 启动 Product Service
cd F:\Personal_Work\springcloud-alibaba\product-service
mvn spring-boot:run

# 启动 Order Service
cd F:\Personal_Work\springcloud-alibaba\order-service
mvn spring-boot:run

# 启动 Gateway Server
cd F:\Personal_Work\springcloud-alibaba\gateway-server
mvn spring-boot:run
```

**注意：** 如果使用了 Nacos，请确保 Nacos 服务器已启动（端口 8848）

---

## 五、测试流程

### 5.1 方式一：通过 Gateway 访问（推荐）

#### 步骤 1：访问 Gateway 受保护接口

打开浏览器访问：
```
http://localhost:8081/order/callCf99
```

#### 步骤 2：自动重定向到 SSO 登录页

由于未登录，请求会自动重定向到 OAuth2-SSO 登录页：
```
http://localhost:8080/oauth2/authorize?client_id=gateway-client&...
```

#### 步骤 3：登录

使用以下账号登录：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | ADMIN, USER |
| user | 123456 | USER |

#### 步骤 4：授权同意（首次登录）

由于 `gateway-client` 配置了 `requireConsent=false`，登录成功后会自动回调，无需再次确认。

#### 步骤 5：访问成功

登录成功后，浏览器会重定向回原始请求：
```
http://localhost:8081/order/callCf99
```

应该能看到返回的九九乘法表数据。

---

### 5.2 方式二：直接访问微服务（测试 JWT 验证）

#### 步骤 1：获取 Token

使用 Postman 或 curl 获取 Token：

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Authorization: Basic Z2F0ZXdheS1jbGllbnQ6Z2F0ZXdheS1zZWNyZXQ=" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=admin&password=123456&scope=openid%20profile%20read%20write"
```

**Base64 解码说明：**
- `Z2F0ZXdheS1jbGllbnQ6Z2F0ZXdheS1zZWNyZXQ=` 是 `gateway-client:gateway-secret` 的 Base64 编码

#### 步骤 2：获取响应

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 7199,
  "scope": "openid profile read write"
}
```

#### 步骤 3：访问微服务接口

使用获取的 Token 访问：

```bash
# 访问商品服务
curl http://localhost:8082/product/cf99 \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# 访问订单服务
curl http://localhost:8083/order/callCf99 \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 六、测试接口列表

### 6.1 Gateway Server (端口 8081)

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/order/callCf99` | GET | 调用商品服务的九九乘法表 | 需要 |
| `/product/cf99` | GET | 直接访问商品服务（通过网关） | 需要 |
| `/api/auth/me` | GET | 获取当前登录用户信息 | 需要 |
| `/logout` | GET/POST | 登出 | 需要 |

### 6.2 Product Service (端口 8082)

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/product/cf99` | GET | 九九乘法表 | 需要 |
| `/product/helloWorld` | GET | Hello World 测试 | 需要 |
| `/product/getUserList` | GET | 获取用户列表 | 需要 |

### 6.3 Order Service (端口 8083)

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/order/callCf99` | GET | 调用商品服务 | 需要 |

---

## 七、完整登录流程图

```
┌─────────┐
│  用户   │
└────┬────┘
     │ 1. 访问 Gateway 受保护接口
     ▼
┌─────────────────────────────────┐
│         Gateway Server          │
│  检查会话中是否有有效登录        │
└────┬────────────────────────────┘
     │ 2. 未登录，重定向到 SSO
     ▼
┌─────────────────────────────────┐
│      OAuth2-SSO 认证中心         │
│  /oauth2/authorize               │
│  显示登录页面                    │
└────┬────────────────────────────┘
     │ 3. 输入用户名密码
     ▼
┌─────────────────────────────────┐
│      OAuth2-SSO 认证中心         │
│  验证用户身份                    │
│  生成授权码 (code)               │
└────┬────────────────────────────┘
     │ 4. 重定向回 Gateway（带 code）
     ▼
┌─────────────────────────────────┐
│         Gateway Server          │
│  用 code 换取 access_token       │
│  创建会话，存储用户信息           │
└────┬────────────────────────────┘
     │ 5. 重定向到原始请求
     ▼
┌─────────────────────────────────┐
│         Gateway Server          │
│  访问后端服务（携带 JWT Token）   │
└────┬────────────────────────────┘
     │ 6. 转发到微服务
     ▼
┌─────────────────────────────────┐
│      Product/Order Service      │
│  验证 JWT Token（使用 JWK）      │
│  返回业务数据                    │
└────┬────────────────────────────┘
     │ 7. 返回响应
     ▼
┌─────────┐
│  用户   │
│  看到结果│
└─────────┘
```

---

## 八、常见问题排查

### 8.1 无法重定向到 SSO 登录页

**检查项：**
- [ ] Gateway 服务是否正常启动（端口 8081）
- [ ] `application.yml` 中的 OAuth2 客户端配置是否正确
- [ ] `client_id` 和 `client_secret` 是否与 SSO 配置一致
- [ ] OAuth2-SSO 服务是否启动（端口 8080）

### 8.2 登录后回调失败

**检查项：**
- [ ] OAuth2-SSO 中配置的 `redirect_uri` 是否与 Gateway 一致
- [ ] 应该是：`http://localhost:8081/login/oauth2/code/gateway-client`
- [ ] Gateway 的 SecurityConfig 是否放行了 `/login/**` 端点

### 8.3 微服务返回 401 Unauthorized

**检查项：**
- [ ] JWT Token 是否有效（未过期）
- [ ] 微服务的 `jwk-set-uri` 配置是否正确
- [ ] 应该是：`http://localhost:8080/oauth2/jwks`
- [ ] OAuth2-SSO 的 JWK 端点是否正常：http://localhost:8080/oauth2/jwks

### 8.4 数据库连接失败

**检查项：**
- [ ] MySQL 是否启动
- [ ] `init.sql` 是否已执行
- [ ] 数据库用户名密码是否正确
- [ ] 数据库端口是否正确（默认 3306 或自定义 3307）

### 8.5 Nacos 连接失败

**检查项：**
- [ ] Nacos 服务器是否启动（端口 8848）
- [ ] `namespace` 是否正确
- [ ] `group` 是否正确
- [ ] Nacos 用户名密码是否正确

---

## 九、配置说明汇总

### 9.1 Gateway OAuth2 客户端配置

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          gateway-client:
            client-id: gateway-client          # 客户端 ID
            client-secret: gateway-secret       # 客户端密钥
            authorization-grant-type: authorization_code  # 授权码模式
            redirect-uri: http://localhost:8081/login/oauth2/code/gateway-client
            scope: profile,read,write           # 授权范围
            client-name: 网关客户端
            provider: gateway-provider          # 提供者配置
        provider:
          gateway-provider:
            authorization-uri: http://localhost:8080/oauth2/authorize
            token-uri: http://localhost:8080/oauth2/token
            jwk-set-uri: http://localhost:8080/oauth2/jwks
            user-info-uri: http://localhost:8080/api/users/me
            user-name-attribute: username
```

### 9.2 微服务 OAuth2 资源服务器配置

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/oauth2/jwks   # JWK 端点
          issuer-uri: http://localhost:8080                 # 颁发者 URI
```

---

## 十、登出测试

### 10.1 通过 Gateway 登出

访问：
```
http://localhost:8081/logout
```

登出后，再次访问受保护接口会重新跳转到 SSO 登录页。

### 10.2 登出流程

```
用户点击登出
     ↓
Gateway 清除本地会话
     ↓
可选：重定向到 SSO 登出端点
     ↓
SSO 清除全局会话
     ↓
重定向到登录页
```

---

## 十一、依赖添加清单

### 各模块依赖添加汇总

| 模块 | OAuth2 Client | OAuth2 Resource Server | Spring Security |
|------|---------------|------------------------|-----------------|
| **gateway-server** | ✅ 需要 | ✅ 需要 | ✅ 需要 |
| **order-service** | ❌ 不需要 | ✅ 需要 | ✅ 需要 |
| **product-service** | ❌ 不需要 | ✅ 需要 | ✅ 需要 |
| **api** | ❌ 不需要 | ❌ 不需要 | ❌ 不需要 |
| **common** | ❌ 不需要 | ✅ 需要（可选） | ❌ 不需要 |

### 快速复制依赖

#### Gateway Server 专属依赖（OAuth2 Client）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

#### 所有服务都需要（OAuth2 Resource Server）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

#### 所有服务都需要（Spring Security）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## 十二、快速测试命令

```bash
# 1. 启动 OAuth2-SSO
cd F:\Personal_Work\oauth2-sso\oauth-server
start mvn spring-boot:run

# 2. 编译 springcloud-alibaba
cd F:\Personal_Work\springcloud-alibaba
call mvn clean install -DskipTests

# 3. 启动 Product Service
cd F:\Personal_Work\springcloud-alibaba\product-service
start mvn spring-boot:run

# 4. 启动 Order Service
cd F:\Personal_Work\springcloud-alibaba\order-service
start mvn spring-boot:run

# 5. 启动 Gateway Server
cd F:\Personal_Work\springcloud-alibaba\gateway-server
start mvn spring-boot:run

# 6. 测试接口（等待服务启动后）
curl http://localhost:8081/order/callCf99
```

---

**文档版本**: v2.0  
**最后更新**: 2026 年 3 月 26 日
