package itk.student.task.manager.aqa.hw;

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
    @DisplayName("L5 DELETE /tasks/{taskId} return 204 No Content and body is empty")
    void deleteTaskById_returns204WithEmptyBody() {
        taskApiClient.deleteTask(contextTask.getId())
                .then()
                .statusCode(204)
                .body(emptyOrNullString());
    }

    @Test
    @Order(6)
    @DisplayName("L6 GET deleted task returns 404 NOT FOUND")
    void getDeletedTaskById_returns404() {
        taskApiClient.getTask(contextTask.getId())
                .then()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }
}