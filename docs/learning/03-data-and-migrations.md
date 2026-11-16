# 03 Flyway、实体与 Repository

从一条问题开始。访问 `/student/1` 得到 Tom，这条数据最早在哪里，谁又能读到它？

## 按「建表 → 映射 → 查询」阅读

| 读什么 | 在源码里找什么 |
| --- | --- |
| [H2 的 V1 迁移](../../src/main/resources/db/migration/V1__create_student.sql) | `CREATE TABLE student` 定义字段和主键，后面的 `INSERT` 放入初始数据 |
| [Student 实体](../../src/main/java/local/lesson/springdemo/Student.java) | `@Entity` 表示由 JPA 管理，`@Id` 对应主键，`IDENTITY` 让数据库在插入时生成 ID |
| [StudentRepository](../../src/main/java/local/lesson/springdemo/StudentRepository.java) | `JpaRepository<Student, Long>` 的第一个参数是实体，第二个参数是主键类型。接口继承 `findById`、`save` 和 `delete` |
| [基础配置](../../src/main/resources/application.properties) | `spring.jpa.hibernate.ddl-auto=validate` 让 Hibernate 检查数据库结构与实体映射是否兼容 |

对照 SQL 的 `name`、`phone`、`age` 和 Java 属性。读已有行时，JPA 可以用实体的无参构造器建立对象。创建新学生时，Controller 调用带业务字段的构造器，然后交给 Repository 保存。数据库返回主键，响应里才有新的 `id`。

在本项目的 `local` profile 中，Flyway 在 JPA 校验表结构前执行迁移。它把执行过的版本和校验和记录到 `flyway_schema_history`。看到 `now at version v1` 表示这个库刚执行了 V1，看到 `Schema ... is up to date` 表示当前库已应用对应版本。后续改表时新建迁移，例如 `V2__add_student_field.sql`。已执行脚本的变动会触发 Flyway 校验。对照 [Flyway validate 文档](https://documentation.red-gate.com/flyway/reference/commands/validate)。

MySQL 模式的建表入口是 [mysql/init.sql](../../mysql/init.sql)，由容器首次初始化空数据卷时运行。两个模式使用同一个 Java 实体和 Repository，但数据库文件、数据卷和迁移历史各自独立。

## 看一条实际执行的 SQL

先按 [01](01-build.md) 打包，再用一个临时内存库启动另一份应用。示例使用 `8083`，与默认 H2 文件库的端口分开。

```sh
java -jar target/spring-boot-lesson-01-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local \
  --server.port=8083 \
  '--spring.datasource.url=jdbc:h2:mem:lesson-sql;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH' \
  --logging.level.org.hibernate.SQL=DEBUG
```

在另一个终端请求一次。

```sh
curl -i http://127.0.0.1:8083/student/1
```

响应里能看到 Tom。应用日志里找 Hibernate 输出的 `select`，留意 `student` 表和 `id=?` 条件。问号是 SQL 参数的位置，Controller 调用 `findById(1L)` 后，Hibernate 负责把主键值传给数据库。日志里的 SQL 别名可能不同。

练习结束后按 Ctrl+C。重新启动这条命令会得到新的内存库。常规 `local` 命令启动的是文件库，退出后数据仍在 `.local-db/lesson.mv.db`。

## 对照故障阶段

- 找不到表时，先核对当前 JDBC URL 和 Flyway 启动日志，再看 JPA 的结构校验。
- V1 校验失败时，用 Git 对照已执行的迁移文件。要试不同的表结构，可以从新的练习库开始，再用新迁移表达变更。
- 应用正常启动但查询返回 `404`，沿 Controller 中的 `findStudent()` 检查请求 ID 和数据库记录。

[上一篇：启动类、Bean 与运行配置](02-startup-and-profiles.md) · [返回目录](index.md) · [下一篇：跟一条 HTTP 请求读完 CRUD](04-student-http.md)
