package local.lesson.springdemo;

import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data 在启动时提供实现。Student 是实体类型，Long 是主键类型。
public interface StudentRepository extends JpaRepository<Student, Long> {
}
