package itk.student.task.manager.aqa.hw;

import static itk.student.task.manager.aqa.hw.consts.TasksApi.TASKS_COMMENTS;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.hw.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.support.BaseApiTest;
import itk.student.task.manager.dto.request.UpdateTaskStatusRequest;
import itk.student.task.manager.dto.response.CommentResponse;
import itk.student.task.manager.dto.response.TaskDetailResponse;
import itk.student.task.manager.dto.response.TaskResponse;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DilekeevaTaskLifecycleApiTest extends BaseApiTest {

  private static final String TASK_BY_TASK_ID = ApiPaths.TASKS + "/{taskId}";
  private static final String TASKS_STATUS = TASK_BY_TASK_ID + "/status";

  @DisplayName("Проверить сквозной сценарий жизненного цикла задачи в проекте с ключом DEMO")
  @Test
  public void checkTaskLifecycleForDEMO_happyPath() {
    // 1. Получить список проектов и найти DEMO
    String projectId = authSpec
        .queryParam("size", 20)
        .when()
        .get(ApiPaths.PROJECTS)
        .then()
        .log().ifValidationFails()
        .spec(commonResponseSpec())
        .body("content.key", Matchers.hasItem("DEMO"))
        .extract().path("content.find { it.key.equals(\"DEMO\") }.id");

    Assertions.assertThat(projectId)
        .as("ID project is blank")
        .isNotBlank();

    // 2. Создать задачу в проекте DEMO
    CreateTaskRequest taskCreateRequest = createCreateTaskRequest(projectId);

    TaskResponse taskCreateResponse = authSpec
        .when()
        .body(taskCreateRequest)
        .post(ApiPaths.TASKS)
        .then()
        .log().ifValidationFails()
        .statusCode(201)
        .extract().as(TaskResponse.class);
    UUID id = taskCreateResponse.id();
    Long version = taskCreateResponse.version();

    Assertions.assertThat(taskCreateResponse)
        .as("response is null")
        .isNotNull();

    var softAssertions = new SoftAssertions();
    softAssertions.assertThat(taskCreateResponse.status())
        .as("status")
        .isEqualTo(TaskStatus.TODO);
    softAssertions.assertThat(version)
        .as("version")
        .isEqualTo(0);
    softAssertions.assertThat(taskCreateResponse.title())
        .as("title")
        .isEqualTo(taskCreateRequest.getTitle());

    // 3. Получить созданную задачу по ID
    TaskDetailResponse taskDetailResponse = authSpec
        .when()
        .pathParam("taskId", id)
        .get(TASK_BY_TASK_ID)
        .then()
        .log().ifValidationFails()
        .spec(commonResponseSpec())
        .extract().as(TaskDetailResponse.class);

    Assertions.assertThat(taskDetailResponse)
        .as("response is null")
        .isNotNull();
    Assertions.assertThat(taskDetailResponse.task())
        .as("task is null")
        .isNotNull();
    TaskResponse taskGetResponse = taskDetailResponse.task();

    softAssertions.assertThat(taskGetResponse.id())
        .as("taskId")
        .isEqualTo(id);
    softAssertions.assertThat(taskGetResponse.title())
        .as("title")
        .isEqualTo(taskCreateRequest.getTitle());
    version = taskGetResponse.version();

    // 4. Обновить статус задачи (TODO → IN_PROGRESS)
    TaskResponse taskStatusResponse = authSpec
        .when()
        .pathParam("taskId", id)
        .body(new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS, version))
        .patch(TASKS_STATUS)
        .then()
        .log().ifValidationFails()
        .spec(commonResponseSpec())
        .body("status", CoreMatchers.equalTo(TaskStatus.IN_PROGRESS.name()))
        .extract().as(TaskResponse.class);

    softAssertions.assertThat(taskStatusResponse.version())
        .as("version")
        .isGreaterThan(version);

    // 5. Удалить задачу
    authSpec
        .when()
        .pathParam("taskId", id)
        .delete(TASK_BY_TASK_ID)
        .then()
        .log().ifValidationFails()
        .statusCode(204);

    // 6. Проверить, что задача удалена
    authSpec
        .when()
        .pathParam("taskId", id)
        .get(TASK_BY_TASK_ID)
        .then()
        .log().ifValidationFails()
        .statusCode(404);
  }

  @DisplayName("Создать комментарий к задаче")
  @Test
  public void createCommentOfTask_happyPath() {
    // Получить список проектов и найти DEMO
    String projectId = authSpec
        .queryParam("size", 20)
        .when()
        .get(ApiPaths.PROJECTS)
        .then()
        .log().ifValidationFails()
        .spec(commonResponseSpec())
        .body("content.key", Matchers.hasItem("DEMO"))
        .extract().path("content.find { it.key.equals(\"DEMO\") }.id");

    // Создать задачу в проекте DEMO
    CreateTaskRequest taskCreateRequest = createCreateTaskRequest(projectId);

    TaskResponse taskCreateResponse = authSpec
        .when()
        .body(taskCreateRequest)
        .post(ApiPaths.TASKS)
        .then()
        .log().ifValidationFails()
        .statusCode(201)
        .extract().as(TaskResponse.class);

    String expected = "Задача реализована";

    CommentResponse commentResponse = authSpec
        .when()
        .pathParam("taskId", taskCreateResponse.id())
        .body(Map.of("body", expected))
        .post(TASKS_COMMENTS)
        .then()
        .log().ifValidationFails()
        .statusCode(201)
        .extract().as(CommentResponse.class);

    Assertions.assertThat(commentResponse)
        .as("response is null")
        .isNotNull();

    Assertions.assertThat(commentResponse.body())
        .as("comment.body")
        .isEqualTo(expected);

    // Удалить задачу
    authSpec
        .when()
        .pathParam("taskId", taskCreateResponse.id())
        .delete(TASK_BY_TASK_ID)
        .then()
        .log().ifValidationFails()
        .statusCode(204);
  }

  private CreateTaskRequest createCreateTaskRequest(String projectId) {
    return new CreateTaskRequest(
        UUID.fromString(projectId),
        "HW-dil-" + System.currentTimeMillis(),
        "description", TaskPriority.LOW
    );
  }

  public static ResponseSpecification commonResponseSpec() {
    return new ResponseSpecBuilder()
        .expectStatusCode(200)
        .expectContentType("application/json")
        .build();
  }

}
