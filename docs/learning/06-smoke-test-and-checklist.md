# 06 启动测试与第一篇验收

先用自动化测试验证装配与接口，再用真正启动的文件库应用完成验收。这两次运行检查同一个 Spring Boot 项目，数据源和端口的选取方式不同。

## 按断言读启动测试

打开 [LessonApplicationSmokeTest](../../src/test/java/local/lesson/springdemo/LessonApplicationSmokeTest.java)，从类上的注解读起。

1. `@ActiveProfiles("local")` 选中 H2 配置。`@SpringBootTest(webEnvironment = RANDOM_PORT)` 启动一个实际监听的 HTTP 服务，测试端口由系统分配。
2. 注解中的测试属性把 JDBC URL 改成内存库，也为应用信息端点配置测试口令。运行测试不会写入平时跟练的 `.local-db/lesson.mv.db`。
3. `Flyway.info().current()` 对照真实迁移历史，先断言当前版本为 `1`。
4. `TestRestTemplate` 实际发送 HTTP 请求。按顺序看 readiness、OpenAPI、学生查询和请求 ID，再看应用信息在匿名和认证状态下的两条断言。

从仓库根目录执行。

```sh
./mvnw -Dtest=LessonApplicationSmokeTest test
```

通过时，Maven 会报告 `Tests run: 1, Failures: 0, Errors: 0`。失败时打开 `target/surefire-reports/local.lesson.springdemo.LessonApplicationSmokeTest.txt`，先确定是应用启动时报错，还是某条 HTTP 断言收到不同的状态码或内容。一次测试包含多条断言，`Tests run: 1` 是测试方法数。

## 验收运行中的文件库

按 [02 启动 H2](02-startup-and-profiles.md#启动-h2-模式)，再执行 [05 的四条请求](05-probes-and-security.md)。从你当前的运行记录填写下表。如果同个文件库已经启动过，Flyway 显示与 V1 一致即可。

| 记录 | 通过条件 |
| --- | --- |
| 当前 JDK 和 Maven | `./mvnw -v` 显示 Java 21 和 Maven 3.10.0 |
| JAR 和运行参数 | 由项目根目录启动 JAR，日志显示 active profile 为 `local`，端口为 `8082` |
| 数据库和迁移 | JDBC URL 是 H2 文件库，日志显示 V1 已应用或 schema 当前为版本 1 |
| readiness 和 OpenAPI | 都是 `200`，readiness 为 `UP`，OpenAPI 包含学生路径 |
| 学生查询 | 初始 `/student/1` 返回 `200` 和 Tom。复制一次响应头中的 `X-Request-ID` |
| 受保护的应用信息 | 匿名请求返回 `401` |
| 启动测试 | `LessonApplicationSmokeTest` 通过 |

## 用失败信号缩小范围

| 最后观察到的现象 | 从哪里开始查 |
| --- | --- |
| Maven 未生成 Jar | 回到 [01 构建](01-build.md)，确认依赖解析、编译和测试走到哪一步 |
| Java 退出，始终没有 `Tomcat started` | 先找最内层异常，再按 profile、Bean、数据源或迁移排查。[02 启动与配置](02-startup-and-profiles.md) |
| readiness 为 `503` | 等应用完成启动后重试。若仍是 `503`，查看这个探针组依赖的就绪状态和数据库连接。[05 探针与认证](05-probes-and-security.md) |
| readiness 正常，单个接口失败 | 对照请求的方法、路径、参数和响应码，在对应的 Controller 或过滤链定位。[04 HTTP 请求](04-student-http.md) |

验收完成后，可以重新发一次 `GET /student/1`。试着不看文档描述，从 URL 说到 Controller、Repository、H2 和 JSON 响应，并指出请求 ID 在哪一层生成。

[上一篇：readiness、OpenAPI 与认证](05-probes-and-security.md) · [返回目录](index.md)
