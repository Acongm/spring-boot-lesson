# Spring Boot 学习路线

用一个学生管理接口走完 Maven 构建、Spring 装配、HTTP 请求、数据库访问和测试。准备好 JDK 21，从仓库根目录执行命令，第一轮使用 `local` profile 和 H2。需要对照视频时，再切到 MySQL。

| 顺序 | 章节 | 学完能解释什么 |
| --- | --- | --- |
| 01 | [JDK、Maven Wrapper 与可执行 Jar](01-build.md) | 谁决定 Java 和 Maven 版本，为什么有 `BUILD SUCCESS` 还得单独启动应用 |
| 02 | [启动类、Bean 与运行配置](02-startup-and-profiles.md) | Spring 怎样拿到 Controller 的依赖，`local` 和 `mysql` 用哪套端口和数据库 |
| 03 | [Flyway、实体与 Repository](03-data-and-migrations.md) | 谁建了学生表，为什么没有手写 SQL 也能查询，以及在哪看迁移版本 |
| 04 | [跟一条 HTTP 请求读完 CRUD](04-student-http.md) | 路径参数和 JSON 请求体如何进入 Java，如何区分新增、更新与找不到记录 |
| 05 | [readiness、OpenAPI 与认证](05-probes-and-security.md) | 为什么学生查询是 `200`，应用信息在匿名访问时却是 `401` |
| 06 | [启动测试与第一篇验收](06-smoke-test-and-checklist.md) | 如何用内存库重复跑测试，以及如何记录文件库的实际验收结果 |

想先把服务跑起来，可以直接进入 [02 的本地启动步骤](02-startup-and-profiles.md#启动-h2-模式)。若应用已经在运行，可以从 [04 的查询断点](04-student-http.md#先跟一次-get) 开始。

后续主题按编号新增同级文件，再把入口加到这张表中。每篇保留自己的源码入口、可复现操作和验收结果。
