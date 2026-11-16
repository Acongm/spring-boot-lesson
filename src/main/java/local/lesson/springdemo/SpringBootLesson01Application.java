package local.lesson.springdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 从本包开始扫描组件，并根据 classpath 和配置启用所需的自动配置。
@SpringBootApplication
public class SpringBootLesson01Application {

	public static void main(String[] args) {
		// 创建 Spring 容器，装配 Bean，然后由内嵌 Tomcat 接收 HTTP 请求。
		SpringApplication.run(SpringBootLesson01Application.class, args);
	}

}
