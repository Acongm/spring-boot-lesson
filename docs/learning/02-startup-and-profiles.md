# 02 启动类、Bean 与运行配置

这一篇要认出应用入口、容器装配和运行参数，最后亲手请求一次 `/hello`。

## 从 main 找到 Spring 创建的对象

1. 打开 [SpringBootLesson01Application](../../src/main/java/local/lesson/springdemo/SpringBootLesson01Application.java)。Java 从 `main(String[] args)` 进入，`SpringApplication.run(..., args)` 读取启动参数，创建 Spring 容器。
2. 看 `@SpringBootApplication` 放在哪个包。它从 `local.lesson.springdemo` 开始组件扫描。这个包里的 [StudentController](../../src/main/java/local/lesson/springdemo/StudentController.java) 会成为 Spring 管理的对象，也就是 Bean。
3. 看 Controller 的构造器参数 `StudentRepository`。Spring Data 为 [接口](../../src/main/java/local/lesson/springdemo/StudentRepository.java) 提供实现，Spring 在构造 Controller 时把 Repository Bean 传进去。也可以对照 [LessonSecurity](../../src/main/java/local/lesson/springdemo/LessonSecurity.java) 的 `@Bean`，这里由 Java 方法明确提供一个过滤链 Bean。

## 配置如何选中数据库

[application.properties](../../src/main/resources/application.properties) 提供共享值，例如只监听 `127.0.0.1` 和启动时校验表结构。启动时没有指定 active profile 会用 `spring.profiles.default=mysql`。传入 `--spring.profiles.active=local` 时，本项目读取 H2 那套配置。对照 [Spring Boot profile 文档](https://docs.spring.io/spring-boot/3.5/reference/features/profiles.html)。

| 对照项 | [local](../../src/main/resources/application-local.properties) | [mysql](../../src/main/resources/application-mysql.properties) |
| --- | --- | --- |
| HTTP 端口 | `8082` | `8080` |
| 数据库 | H2 文件库 `.local-db/lesson.mv.db` | 本机 `3307` 转发到 MySQL 容器的 `3306` |
| 连接配置 | profile 文件中配置 JDBC URL | 从项目根目录的 `.env` 读取账号等参数 |
| 创建表 | 应用启动时由 Flyway 迁移 | 首次初始化 MySQL 数据卷时执行 SQL |

## 启动 H2 模式

先从 [01](01-build.md) 构建 Jar，再保持这个终端运行应用。

```sh
java -jar target/spring-boot-lesson-01-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

先找日志中的 active profile，再找 Flyway 的版本和 `Tomcat started on port 8082`。首次创建文件库会迁移到 `v1`，再次启动时通常显示库已是最新版本。在另一个终端执行以下请求。

```sh
curl -i http://127.0.0.1:8082/hello
```

收到 `200` 和 `Hello World` 后，回到 `StudentController.hello()` 找到这段响应。运行时的第一个终端按 Ctrl+C 可以停止前台 Java 进程。

### 在 IntelliJ IDEA 调试

1. 以 Maven 项目打开仓库根目录，Project SDK 选 JDK 21，Maven 选 Use Maven wrapper。
2. 创建或编辑 Java Application 运行配置，将 Main class 设为 `local.lesson.springdemo.SpringBootLesson01Application`，Working directory 设为项目根目录，Program arguments 填 `--spring.profiles.active=local`。
3. 如果现有 Jar 已占用端口或正使用同一个 H2 文件库，先结束那个进程，再点击 Debug。把 `server.port` 改到独立端口只会避开端口冲突，共用同一个 H2 文件仍可能冲突。

## 切换到视频使用的 MySQL

先从 [.env.example](../../.env.example) 复制一份 `.env`，填入本机的 `MYSQL_PASSWORD`。已有 `.env` 时继续使用现有文件。启用 OrbStack 的 Mac 可按以下命令启动。使用其他 Docker 环境时，将第一条换成该环境中的 `docker compose up -d --wait`。

```sh
docker --context orbstack compose up -d --wait
./mvnw spring-boot:run
```

使用 `http://127.0.0.1:8080/hello` 和 `/student/1` 验证。终止 Java 进程后执行 `docker --context orbstack compose stop` 可以停止 MySQL，数据卷仍会保留。修改 `.env` 不会替已有数据卷重新创建数据库用户。

## 启动排查

- 日志里出现 MySQL 或缺少 `MYSQL_PASSWORD`，先确认启动参数中的 profile 拼写和实际生效的 profile。
- `Port 8082 was already in use` 表示监听端口冲突。先确认本地是否已有旧的 Java 进程。
- `UnsatisfiedDependencyException` 表示某个 Bean 的依赖没装好。顺着 `Caused by` 找具体是数据源、Flyway 校验还是其他 Bean 出错。

[上一篇：JDK、Maven Wrapper 与可执行 Jar](01-build.md) · [返回目录](index.md) · [下一篇：Flyway、实体与 Repository](03-data-and-migrations.md)
