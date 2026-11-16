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

    public StudentController(StudentRepository students) {
        this.students = students;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/student/{id}")
    public Student get(@PathVariable long id) {
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
        return students.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record StudentInput(String name, String phone, int age) {
    }
}
