# Spring Boot 第一集本机练习

[课程视频](https://www.bilibili.com/video/BV1gm411m7i6/)使用 Spring Boot、Maven、MySQL 和 Spring Data JPA 完成学生接口。本项目使用 Java 21、Spring Boot 3.5.16 和 Maven Wrapper。启用 `local` profile 可以用 H2 练习第一篇的应用装配与启动流程，默认的 `mysql` profile 继续用于视频跟练。

## 按顺序读代码

1. **构建**。先看 [Maven Wrapper](.mvn/wrapper/maven-wrapper.properties) 固定的 Maven 版本，再看 [pom.xml](pom.xml) 里的 Java 版本、Web 和 JPA 依赖，以及可执行 Jar 的打包插件。
2. **启动与配置**。从 [SpringBootLesson01Application.main()](src/main/java/local/lesson/springdemo/SpringBootLesson01Application.java) 进入 Spring。对照 [基础配置](src/main/resources/application.properties)、[H2 配置](src/main/resources/application-local.properties) 和 [MySQL 配置](src/main/resources/application-mysql.properties)，找出两个 profile 的端口、数据源和建表方式。
3. **看表与实体**。先看 [Flyway V1](src/main/resources/db/migration/V1__create_student.sql) 如何建表并插入 Tom，再对照 [Student](src/main/java/local/lesson/springdemo/Student.java) 的字段。已经执行过的迁移由 Flyway 校验，后续改表时新增 V2。用 MySQL 跟练时，对照的是 [容器初始化脚本](mysql/init.sql)。
4. **跟一条查询**。在 [StudentController.get()](src/main/java/local/lesson/springdemo/StudentController.java) 打断点，按下文的 IDEA 参数以 Debug 模式启动，再访问 `GET /student/1`。观察路径里的 `1` 怎样变成 Java 参数，再跟到 [StudentRepository](src/main/java/local/lesson/springdemo/StudentRepository.java) 的 `findById`。响应头的 `X-Request-ID` 来自请求进入 Controller 前经过的 [RequestIdFilter](src/main/java/local/lesson/springdemo/RequestIdFilter.java)。
5. **对照公开请求和受保护请求**。在 [LessonSecurity](src/main/java/local/lesson/springdemo/LessonSecurity.java) 看 `@Bean` 注册的规则覆盖哪个端点，再比较公开的 `/student/1` 和匿名访问 `/actuator/info` 的状态码。
6. **把观察变成验收**。读 [LessonApplicationSmokeTest](src/test/java/local/lesson/springdemo/LessonApplicationSmokeTest.java) 怎样用内存 H2 启动整套应用并检查迁移与 HTTP，再按下文用文件库运行一次。

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

Maven 在启动前报 `Could not resolve dependencies` 时，先检查依赖下载。启动时遇到 `UnsatisfiedDependencyException`，沿异常的 `Caused by` 查看实际失败的 Bean。Flyway 报错时检查 H2 URL、迁移文件和最内层异常。

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
