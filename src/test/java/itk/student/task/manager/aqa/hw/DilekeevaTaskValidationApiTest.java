package itk.student.task.manager.aqa.hw;

import static itk.student.task.manager.aqa.hw.consts.TasksApi.TASKS_STATUS;
import static itk.student.task.manager.aqa.hw.consts.TasksApi.TASK_BY_TASK_ID;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.aqa.support.BaseApiTest;
import itk.student.task.manager.dto.request.UpdateTaskStatusRequest;
import itk.student.task.manager.dto.response.TaskResponse;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DilekeevaTaskValidationApiTest extends BaseApiTest {

  private static final String ANOTHER_EMAIL = "viewer@demo.com";

  @Test
  @DisplayName("Создать задачу в проекте DEMO с пустым title")
  public void createTask_whenEmptyTitle_thenValidationError() {
    String projectId = getProjectIdForDemo();
    CreateTaskRequest taskCreateRequest = createCreateTaskRequest(projectId);
    taskCreateRequest.setTitle(null);

    authSpec
        .when()
        .body(taskCreateRequest)
        .post(ApiPaths.TASKS)
        .then()
        .log().ifValidationFails()
        .statusCode(400)
        .body("code", equalTo("VALIDATION_ERROR"));
  }

  @DisplayName("Обновить статус задачи с TODO → DONE (минуя IN_PROGRESS)")
  @Test
  public void updateTaskStatus_whenWithoutInProgress_thenError() {
    String projectId = getProjectIdForDemo();
    CreateTaskRequest taskCreateRequest = createCreateTaskRequest(projectId);
    TaskResponse taskCreateResponse = createTask(taskCreateRequest);

    authSpec
        .when()
        .pathParam("taskId", taskCreateResponse.id())
        .body(new UpdateTaskStatusRequest(TaskStatus.DONE, taskCreateResponse.version()))
        .patch(TASKS_STATUS)
        .then()
        .log().ifValidationFails()
        .statusCode(422);

    deleteTaskById(taskCreateResponse.id());
  }

  @DisplayName("Обновить статус задачи с устаревшей version")
  @Test
  public void updateTaskStatus_whenInvalidVersion_thenConflict() {
    String projectId = getProjectIdForDemo();
    CreateTaskRequest taskCreateRequest = createCreateTaskRequest(projectId);
    TaskResponse taskCreateResponse = createTask(taskCreateRequest);

    authSpec
        .when()
        .pathParam("taskId", taskCreateResponse.id())
        .body(new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS, 99L))
        .patch(TASKS_STATUS)
        .then()
        .log().ifValidationFails()
        .statusCode(409)
        .body("code", equalTo("CONFLICT"));

    deleteTaskById(taskCreateResponse.id());
  }

  @Test
  @DisplayName("Попытаться создать задачу с неверным токеном")
  public void createTask_whenWrongToken_thenForbidden() {
    String token = AuthSupport.obtainAccessToken(ANOTHER_EMAIL, ApiTestConfig.userPassword());
    RequestSpecification spec = AuthSupport.authenticatedJsonSpec(token);

    String projectId = getProjectIdForDemo();
    CreateTaskRequest taskCreateRequest = createCreateTaskRequest(projectId);

    spec
        .when()
        .body(taskCreateRequest)
        .post(ApiPaths.TASKS)
        .then()
        .log().ifValidationFails()
        .statusCode(403)
        .body("code", equalTo("FORBIDDEN"));
  }

  // TODO можно добавить в отельный класс для preconditions
  private String getProjectIdForDemo() {
    return authSpec
        .queryParam("size", 20)
        .when()
        .get(ApiPaths.PROJECTS)
        .then()
        .log().ifValidationFails()
        .spec(commonResponseSpec())
        .body("content.key", Matchers.hasItem("DEMO"))
        .extract().path("content.find { it.key.equals(\"DEMO\") }.id");
  }

  // TODO можно добавить в отдельный класс фабрику
  private CreateTaskRequest createCreateTaskRequest(String projectId) {
    return new CreateTaskRequest(
        UUID.fromString(projectId),
        "HW-dil-" + System.currentTimeMillis(),
        "description", TaskPriority.LOW
    );
  }

  // TODO можно добавить в отельный класс для preconditions
  private TaskResponse createTask(CreateTaskRequest body) {
    return authSpec
        .when()
        .body(body)
        .post(ApiPaths.TASKS)
        .then()
        .log().ifValidationFails()
        .statusCode(201)
        .extract().as(TaskResponse.class);
  }

  // TODO можно добавить в отельный класс для preconditions
  private void deleteTaskById(UUID taskId) {
    authSpec
        .when()
        .pathParam("taskId", taskId)
        .delete(TASK_BY_TASK_ID)
        .then()
        .log().ifValidationFails()
        .statusCode(204);
  }

  public static ResponseSpecification commonResponseSpec() {
    return new ResponseSpecBuilder()
        .expectStatusCode(200)
        .expectContentType("application/json")
        .build();
  }

}
