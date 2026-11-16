package local.lesson.springdemo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StudentController {
    private final StudentRepository students;

    // Spring 创建 Controller 时，通过构造器传入 Repository Bean。
    public StudentController(StudentRepository students) {
        this.students = students;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/student/{id}")
    public Student get(@PathVariable long id) {
        // 例如 /student/1 会把路径中的 1 绑定给 id。
        return findStudent(id);
    }

    @PostMapping("/student")
    @ResponseStatus(HttpStatus.CREATED)
    public Student create(@RequestBody StudentInput input) {
        return students.save(new Student(input.name(), input.phone(), input.age()));
    }

    @PutMapping("/student/{id}")
    public Student update(@PathVariable long id, @RequestBody StudentInput input) {
        Student student = findStudent(id);
        student.update(input.name(), input.phone(), input.age());
        return students.save(student);
    }

    @DeleteMapping("/student/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        students.delete(findStudent(id));
    }

    private Student findStudent(long id) {
        // findById 没有查到记录时，在 HTTP 层返回 404。
        return students.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // POST 和 PUT 从请求体读取可编辑字段，记录的 ID 由路径或数据库决定。
    public record StudentInput(String name, String phone, int age) {
    }
}
