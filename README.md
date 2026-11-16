# Spring Boot 第一集本机练习

[课程视频](https://www.bilibili.com/video/BV1gm411m7i6/)使用 Spring Boot、Maven、MySQL 和 Spring Data JPA 完成学生接口。本项目使用 Java 21、Spring Boot 3.5.16 和 Maven Wrapper。启用 `local` profile 可以用 H2 练习第一篇的应用装配与启动流程，默认的 `mysql` profile 继续用于视频跟练。

## 第一篇验收（H2）

从项目目录运行以下命令。Maven 用内存 H2 执行启动测试，再打包。Jar 启动时用单独的文件库 `.local-db/lesson.mv.db`，首次启动由 Flyway 执行 V1 创建学生表和示例数据。

```sh
./mvnw -v
./mvnw -Dtest=LessonApplicationSmokeTest package
java -jar target/spring-boot-lesson-01-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

在另一个终端执行以下命令。最后一条预期返回 `401`。

```sh
curl -fsS http://127.0.0.1:8082/actuator/health/readiness
curl -fsS http://127.0.0.1:8082/v3/api-docs
curl -i http://127.0.0.1:8082/student/1
curl -i http://127.0.0.1:8082/actuator/info
```

| 检查 | 预期 |
| --- | --- |
| readiness | `200`，状态为 `UP` |
| OpenAPI | `200`，包含学生接口 |
| 匿名读取学生 | `200`，初始记录为 Tom，`X-Request-ID` 响应头含请求 ID |
| 匿名读取应用信息 | `401`，由 Spring Security 要求 HTTP Basic 认证 |
| Flyway | 日志显示 V1 已执行或 schema 已处于版本 1 |

按 `pom.xml` → `SpringBootLesson01Application.main()` → `application-local.properties` → `db/migration/V1__create_student.sql` 的顺序读代码。Maven 在启动前报 `Could not resolve dependencies` 时，先检查依赖下载。启动时遇到 `UnsatisfiedDependencyException`，沿异常的 `Caused by` 查看实际失败的 Bean。Flyway 报错时检查 H2 URL、迁移文件和最内层异常。

在 IntelliJ IDEA 中以 Maven 项目打开本目录，项目 SDK 选 JDK 21，Maven 选择 Use Maven wrapper。运行 `SpringBootLesson01Application` 时将工作目录设为项目根目录，要启用 H2 则在程序参数添加 `--spring.profiles.active=local`。

## 视频跟练（MySQL）

在本目录下运行。如果 OrbStack 未运行，先打开 OrbStack。

```sh
docker --context orbstack compose up -d --wait
./mvnw spring-boot:run
```

用浏览器或 Postman 验证以下接口。

| 方法 | 地址 | 预期 |
| --- | --- | --- |
| GET | `http://127.0.0.1:8080/hello` | `Hello World` |
| GET | `http://127.0.0.1:8080/student/1` | 初始学生 Tom |
| POST | `http://127.0.0.1:8080/student` | 创建记录，返回 ID 和 201 |
| PUT | `http://127.0.0.1:8080/student/{id}` | 更新刚创建的记录 |
| DELETE | `http://127.0.0.1:8080/student/{id}` | 删除记录，返回 204 |

POST 或 PUT 在 Postman 里选 Body → raw → JSON，例如 `{"name":"Amy","phone":"1234567890","age":20}`。

### 打包和停用

用 `./mvnw clean package` 构建 Jar。停止已有的 Java 进程后，在项目根目录运行 `java -jar target/spring-boot-lesson-01-0.0.1-SNAPSHOT.jar` 可以启动打包后的应用。

数据库通过 `127.0.0.1:3307` 连接，库名和应用账号见 `.env`，密码仅留在本机。将项目复制到新机器时，复制 `.env.example` 为 `.env` 并填入新密码，再初始化容器。

练习结束后用 `docker --context orbstack compose stop` 停止数据库，数据卷会保留。首次创建数据卷时会初始化学生表和示例记录。
