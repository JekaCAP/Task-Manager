package hw;

import hw.dto.CreateTaskRequest;
import hw.dto.UpdateTaskStatusRequest;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import itk.student.task.manager.aqa.support.AuthSupport;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Task Manager")
@Feature("Tasks")
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
    @Story("Получаем список проектов и находим в них DEMO")
    public void getListProjectFindDemo() {

        Allure.step("Получаем список проектов и находим в них проект DEMO", () ->

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
                        .path("content.find { it.key == 'DEMO' }.id")
        );
    }

    @Test
    @Order(2)
    @DisplayName("Создать задачу в проекте DEMO")
    @Story("Создаем новую задачу")
    public void postTaskInTheDemoProject() {

        CreateTaskRequest request = new CreateTaskRequest(
                projectId,
                title,
                "Проверка создания задачи",
                "LOW"
        );

        Allure.step("Создаем задачу в проекте demo", () ->

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
        );

        version = 0;
    }

    @Test
    @Order(3)
    @DisplayName("Получить созданную задачу по ID")
    @Story("Получаем созданную задачу ")
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
    @Story("Обновляем статус задачи")
    public void updateStatusTask() {

        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest("IN_PROGRESS", version);

        Allure.step("Обновляем статус задачи на в процессе", () ->

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
                        .path("version")
        );
    }

    @Test
    @Order(5)
    @DisplayName("Удалить задачу")
    @Story("Удаляем задачу")
    public void deleteTask() {

        Allure.step("Удаляем задачу", () ->

                given()
                        .spec(spec)
                        .pathParams("taskId", taskId)
                        .when()
                        .delete("/api/v1/tasks/{taskId}")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(204)
        );

    }

    @Test
    @Order(6)
    @DisplayName("Проверить, что задача удалена")
    @Story("Проверяем что запись удалена")
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
