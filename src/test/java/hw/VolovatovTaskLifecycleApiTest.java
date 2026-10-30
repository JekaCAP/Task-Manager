package hw;

import hw.dto.CreateTaskRequest;
import hw.dto.UpdateTaskStatusRequest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import itk.student.task.manager.aqa.support.AuthSupport;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VolovatovTaskLifecycleApiTest {

    private static RequestSpecification spec;
    private static String projectId;
    private static String taskId;
    private static int version;
    private static String title =
            "HW-VAA-" + System.currentTimeMillis();

    @BeforeAll
    public static void getToken() {
        String token = AuthSupport.obtainAccessToken();
        spec = AuthSupport.authenticatedJsonSpec(token);
    }

    @Test
    @Order(1)
    @DisplayName("Получить список проектов и найти DEMO")
    public void getListProjectFindDemo() {
        projectId = given()
                .spec(spec)
                .queryParam("size", 20)
                .when()
                .get("/api/v1/projects")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("content.key", hasItem("DEMO"))
                .extract()
                .path("content.find { it.key == 'DEMO' }.id");
    }

    @Test
    @Order(2)
    @DisplayName("Создать задачу в проекте DEMO")
    public void postTaskInTheDemoProject() {

        CreateTaskRequest request = new CreateTaskRequest(
                projectId,
                title,
                "Проверка создания задачи",
                "LOW"
        );

        taskId = given()
                .spec(spec)
                .body(request)
                .when()
                .post("/api/v1/tasks")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("status", equalTo("TODO"))
                .body("version", equalTo(0))
                .body("title", equalTo(request.getTitle()))
                .extract()
                .path("id")
        ;

        version = 0;
    }

    @Test
    @Order(3)
    @DisplayName("Получить созданную задачу по ID")
    public void getCreateTask() {

        version = given()
                .spec(spec)
                .pathParams("taskId", taskId)
                .when()
                .get("/api/v1/tasks/{taskId}")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("task.id", equalTo(taskId))
                .body("task.title", equalTo(title))
                .extract()
                .path("task.version");
    }

    @Test
    @Order(4)
    @DisplayName("Обновить статус задачи (TODO → IN_PROGRESS)")
    public void updateStatusTask() {

        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest("IN_PROGRESS", version);

        version = given()
                .spec(spec)
                .body(updateTaskStatusRequest)
                .pathParams("taskId", taskId)
                .when()
                .patch("/api/v1/tasks/{taskId}/status")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("version", greaterThan(version))
                .extract()
                .path("version");
    }

    @Test
    @Order(5)
    @DisplayName("Удалить задачу")
    public void deleteTask() {

        given()
                .spec(spec)
                .pathParams("taskId", taskId)
                .when()
                .delete("/api/v1/tasks/{taskId}")
                .then()
                .log().ifValidationFails()
                .statusCode(204);

    }

    @Test
    @Order(6)
    @DisplayName("Проверить, что задача удалена")
    public void checkTuskDelete() {

        given()
                .spec(spec)
                .pathParams("taskId", taskId)
                .when()
                .get("/api/v1/tasks/{taskId}")
                .then()
                .log().ifValidationFails()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"));


    }



}
