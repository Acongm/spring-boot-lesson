package local.lesson.springdemo;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:lesson-smoke;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.security.user.password=smoke-test-only"
})
class LessonApplicationSmokeTest {
    @Autowired
    private TestRestTemplate http;

    @Autowired
    private Flyway flyway;

    @Test
    void startsWithLocalProfileAndServesTheLessonEndpoints() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");

        var readiness = http.getForEntity("/actuator/health/readiness", JsonNode.class);
        assertThat(readiness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readiness.getBody().path("status").asText()).isEqualTo("UP");

        var openApi = http.getForEntity("/v3/api-docs", JsonNode.class);
        assertThat(openApi.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openApi.getBody().path("paths").has("/student/{id}")).isTrue();

        var student = http.getForEntity("/student/1", JsonNode.class);
        assertThat(student.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(student.getBody().path("name").asText()).isEqualTo("Tom");
        assertThat(student.getHeaders().getFirst("X-Request-ID")).isNotBlank();
        UUID.fromString(student.getHeaders().getFirst("X-Request-ID"));

        assertThat(http.getForEntity("/actuator/info", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        var authenticatedInfo = http.withBasicAuth("lesson", "smoke-test-only")
                .getForEntity("/actuator/info", JsonNode.class);
        assertThat(authenticatedInfo.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authenticatedInfo.getBody().path("app").path("name").asText())
                .isEqualTo("spring-boot-lesson-01");
    }
}
