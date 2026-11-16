package local.lesson.springdemo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private int age;

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
