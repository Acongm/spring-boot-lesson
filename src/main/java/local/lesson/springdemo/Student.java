package local.lesson.springdemo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// JPA 将 Student 映射到 student 表。
@Entity
public class Student {
    // ID 是主键，INSERT 时由数据库生成。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private int age;

    // JPA 读取数据库行时需要无参构造方法。
    protected Student() {
    }

    Student(String name, String phone, int age) {
        update(name, phone, age);
    }

    void update(String name, String phone, int age) {
        this.name = name;
        this.phone = phone;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public int getAge() {
        return age;
    }
}
