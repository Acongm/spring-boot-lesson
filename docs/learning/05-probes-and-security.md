# 05 readiness、OpenAPI 与认证

应用启动以后，比较四种不同用途的请求。用 [02 的 H2 启动方式](02-startup-and-profiles.md#启动-h2-模式) 准备好 `8082`，然后在另一个终端执行。

```sh
curl -i http://127.0.0.1:8082/actuator/health/readiness
curl -fsS http://127.0.0.1:8082/v3/api-docs
curl -i http://127.0.0.1:8082/student/1
curl -i http://127.0.0.1:8082/actuator/info
```

| 请求 | 初始库的结果 | 对应的代码或配置 |
| --- | --- | --- |
| readiness | `200`，响应为 `{"status":"UP"}` | [local 配置](../../src/main/resources/application-local.properties) 把 `readinessState` 和 `db` 放进同一个探针组 |
| OpenAPI | `200`，JSON 的 `paths` 含 `/student/{id}` | [POM](../../pom.xml) 引入 springdoc，local profile 启用文档端点 |
| 查询学生 | `200`，响应包含学生数据和 `X-Request-ID` 响应头 | [StudentController](../../src/main/java/local/lesson/springdemo/StudentController.java) 和 [RequestIdFilter](../../src/main/java/local/lesson/springdemo/RequestIdFilter.java) |
| 应用信息 | 匿名时 `401`，响应头含 `WWW-Authenticate: Basic` | [LessonSecurity](../../src/main/java/local/lesson/springdemo/LessonSecurity.java) 匹配 `InfoEndpoint` 并要求认证 |

readiness 回答的是这个实例和数据库当前是否可用。OpenAPI 列出路径和参数，具体查询仍要发送到 `/student/{id}`。应用信息端点被暴露后，访问规则还要看 Spring Security。探针配置详见 [Spring Boot Actuator 文档](https://docs.spring.io/spring-boot/3.5/reference/actuator/endpoints.html)。

## 在源码里追到 401

1. 在 [local 配置](../../src/main/resources/application-local.properties) 找 `management.endpoints.web.exposure.include=health,info`。它让这两类 Actuator 端点可以通过 HTTP 访问。
2. 在 `LessonSecurity.infoSecurity()` 找 `securityMatcher(EndpointRequest.to(InfoEndpoint.class))`。它决定这条过滤链处理应用信息请求。再看 `authenticated()` 和 `httpBasic(...)`，分辨匹配范围与认证要求。
3. 打开 [基础配置](../../src/main/resources/application.properties)，找到用户名 `lesson` 及口令来源。没有提供 `LESSON_INFO_PASSWORD` 时，Spring 为本次启动生成练习口令。下面用独立进程比较认证失败和成功。

## 用同一个端点做两次请求

用 `8084` 和独立内存库启动一份临时应用。以下口令只用于这次本机实验。

```sh
LESSON_INFO_PASSWORD=lesson-local-demo java -jar target/spring-boot-lesson-01-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local \
  --server.port=8084 \
  '--spring.datasource.url=jdbc:h2:mem:lesson-auth;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH'
```

在另一个终端先匿名请求，再带上认证。

```sh
curl -i http://127.0.0.1:8084/actuator/info
curl -i -u 'lesson:lesson-local-demo' http://127.0.0.1:8084/actuator/info
```

第一条应是 `401`。第二条应是 `200`，JSON 中的 `app.name` 为 `spring-boot-lesson-01`。观察这两条命令的 URL 相同，变化发生在 curl 发送的认证信息。实验完成后按 Ctrl+C 停止临时应用。

[上一篇：跟一条 HTTP 请求读完 CRUD](04-student-http.md) · [返回目录](index.md) · [下一篇：启动测试与第一篇验收](06-smoke-test-and-checklist.md)
