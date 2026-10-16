package itk.student.task.manager.aqa.hw;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import io.restassured.response.Response;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.api.ProjectApiClient;
import itk.student.task.manager.aqa.hw.api.TaskApiClient;
import itk.student.task.manager.aqa.hw.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.hw.dto.TaskDto;
import itk.student.task.manager.aqa.hw.dto.UpdateTaskStatusRequest;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.aqa.support.BaseApiTest;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@Epic("Task Manager")
@Feature("Жизненный цикл задачи")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MorozovTaskLifecycleApiTest extends BaseApiTest {

    private static TaskDto contextTask;
    private static TaskApiClient taskApiClient;

    @BeforeAll
    static void setAuthSpecToApiClient() {
        taskApiClient = new TaskApiClient(AuthSupport.authenticatedJsonSpec());
        contextTask = new TaskDto();
        contextTask.setTitle("HW-DA-" + System.currentTimeMillis());
    }

    @Test
    @Order(1)
    @TmsLink("TC-L1")
    @Story("Получение списка проектов")
    @DisplayName("L1 list of projects contains DEMO")
    void getProjects_returns200AndListOfProjectsContainsDEMO() {
        String demoProjectKey = ApiTestConfig.demoProjectKey();

        String projectId = new ProjectApiClient(authSpec).getProjects(20)
                .then()
                .statusCode(200)
                .body("meta.size", equalTo(20))
                .body("content.key", hasItem(demoProjectKey))
                .extract()
                .jsonPath()
                .param("projectKey", demoProjectKey)
                .getString("content.find { it.key == projectKey }.id");

        contextTask.setProjectId(UUID.fromString(projectId));
    }

    @Test
    @Order(2)
    @TmsLink("TC-L2")
    @Story("Создание задачи")
    @DisplayName("L2 created Task in DEMO contains default params")
    void createTaskInProjectDEMO_returns201AndCreatedTask() {

        CreateTaskRequest request = new CreateTaskRequest(
                contextTask.getProjectId(),
                contextTask.getTitle(),
                "New task for project DEMO",
                TaskPriority.LOW
        );

        Response createdTask = taskApiClient.createTask(request);

        contextTask = createdTask.then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/task-response-schema.json"))
                .body("status", equalTo(TaskStatus.TODO.toString()))
                .body("version", equalTo(0))
                .body("title", equalTo(contextTask.getTitle()))
                .extract()
                .as(TaskDto.class);
    }

    @Test
    @Order(3)
    @TmsLink("TC-L3")
    @Story("Получение созданной задачи")
    @DisplayName("L3 GET Task by ID returns Task with valid ID and title")
    void getTaskById_returns200AndTask() {
        Integer version = taskApiClient.getTask(contextTask.getId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/task-detail-response-schema.json"))
                .body("task.id", equalTo(contextTask.getId().toString()))
                .body("task.title", equalTo(contextTask.getTitle()))
                .extract()
                .path("task.version");

        contextTask.setVersion(version);
    }

    @Test
    @Order(4)
    @TmsLink("TC-L4")
    @Story("Изменение статуса задачи")
    @DisplayName("L4 PATCH /tasks/{taskId}/status returns Task with new status and version")
    void updateTaskStatus_returns200AndTaskWithNewStatusAndVersion() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(
                TaskStatus.IN_PROGRESS,
                contextTask.getVersion()
        );

        Integer version = taskApiClient.updateTaskStatus(contextTask.getId(), request)
                .then()
                .statusCode(200)
                .body("id", equalTo(contextTask.getId().toString()))
                .body("status", equalTo(TaskStatus.IN_PROGRESS.toString()))
                .body("version", greaterThan(contextTask.getVersion()))
                .extract()
                .path("version");

        contextTask.setVersion(version);
    }

    @Test
    @Order(5)
    @TmsLink("TC-L5")
    @Story("Удаление задачи")
    @DisplayName("L5 DELETE /tasks/{taskId} return 204 No Content and body is empty")
    void deleteTaskById_returns204WithEmptyBody() {
        taskApiClient.deleteTask(contextTask.getId())
                .then()
                .statusCode(204)
                .body(emptyOrNullString());
    }

    @Test
    @Order(6)
    @TmsLink("TC-L6")
    @Story("Получение удаленной задачи")
    @DisplayName("L6 GET deleted task returns 404 NOT FOUND")
    void getDeletedTaskById_returns404() {
        taskApiClient.getTask(contextTask.getId())
                .then()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }
}