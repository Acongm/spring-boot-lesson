# 01 JDK、Maven Wrapper 与可执行 Jar

先把构建工具和应用进程分开理解。JDK 提供 `java` 和 `javac`。Maven Wrapper 运行指定版本的 Maven，Maven 再按 `pom.xml` 解析依赖、编译、测试和打包。最终运行 HTTP 服务的是 Java 进程。

## 按顺序看文件

1. 打开 [maven-wrapper.properties](../../.mvn/wrapper/maven-wrapper.properties)，看 `distributionUrl` 和 `distributionSha256Sum`。本项目固定 Maven 3.10.0，Wrapper 下载发行包后会校验它。macOS/Linux 从 `./mvnw` 进入，Windows PowerShell 使用 `.\mvnw.cmd`。
2. 在 [pom.xml](../../pom.xml) 先找 `parent`。这里继承 Spring Boot 3.5.16 的依赖和插件管理。然后找 `artifactId`、`version` 和 `java.version`，它们分别决定项目制品名、制品版本和 Java 编译目标。
3. 最后读 `dependencies` 和 `build`。这个仓库是单模块项目，直接从根目录执行 Maven，应用产物在 `target/`。

| 找到的依赖或插件 | 回到项目里找证据 |
| --- | --- |
| `spring-boot-starter-web` | 启动日志有内嵌 Tomcat，Controller 有 `@GetMapping` |
| `spring-boot-starter-data-jpa` | `StudentRepository` 继承 `JpaRepository`，启动时会扫描仓储接口 |
| `h2`、`mysql-connector-j`、`flyway-core` | 两套数据源配置选择不同 JDBC 驱动，Flyway 在 `local` 启动时迁移 |
| Actuator、Spring Security、springdoc | 分别对应探针、受保护的应用信息和 OpenAPI 文档 |
| `spring-boot-starter-test` | 启动测试位于 `src/test/java` |
| `spring-boot-maven-plugin` | `package` 日志中的 `repackage` 生成可用 `java -jar` 启动的制品 |

## 实际构建一次

```sh
java -version
./mvnw -v
./mvnw -Dtest=LessonApplicationSmokeTest package
```

对照前两条命令的 Java 版本，确认 Maven 进程也用 JDK 21。第三条的日志依次出现编译、测试、普通 Jar、Boot 重新打包，最后是 `BUILD SUCCESS`。产物是 `target/spring-boot-lesson-01-0.0.1-SNAPSHOT.jar`。

可以检查 Jar 里有什么。

```sh
jar tf target/spring-boot-lesson-01-0.0.1-SNAPSHOT.jar | sed -n '1,24p'
```

`BOOT-INF/classes/` 存应用类和资源，`BOOT-INF/lib/` 存运行时依赖。测试过程中也会临时启动 Tomcat，它使用随机端口，结束后进程会退出。单独运行 Jar 的命令在 [02 启动与配置](02-startup-and-profiles.md)。

## 构建失败时先看哪

- `./mvnw -v` 都失败，先检查 JDK、`JAVA_HOME` 和 Wrapper 下载。
- Maven 报 `Could not resolve dependencies`，记录失败的依赖坐标，再检查网络和 Maven 配置。
- Maven 已开始跑测试，查看 `target/surefire-reports/` 里失败的测试和异常。应用启动失败时读最内层 `Caused by`。

[返回目录](index.md) · [下一篇：启动类、Bean 与运行配置](02-startup-and-profiles.md)
