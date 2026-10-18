package itk.student.task.manager.aqa.hw;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import itk.student.task.manager.aqa.hw.api.ProjectApiClient;
import itk.student.task.manager.aqa.hw.api.TaskApiClient;
import itk.student.task.manager.aqa.hw.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.hw.dto.TaskDto;
import itk.student.task.manager.aqa.hw.dto.UpdateTaskStatusRequest;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.aqa.support.BaseApiTest;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

@Epic("Task Manager")
@Feature("Tasks")
public class MorozovTaskValidationApiTest extends BaseApiTest {

    private static TaskApiClient taskApiClient;
    private static UUID projectId;
    private final List<UUID> createdTaskIds = new ArrayList<>();

    @BeforeAll
    static void setAuthSpecToApiClient() {
        taskApiClient = new TaskApiClient(AuthSupport.authenticatedJsonSpec());
        String id = new ProjectApiClient(AuthSupport.authenticatedJsonSpec()).getProjects(20)
                .then().extract().path("content[0].id");

        projectId = UUID.fromString(id);
    }

    @AfterEach
    void deleteCreatedTasks() {
        createdTaskIds.forEach(id -> taskApiClient.deleteTask(id));
        createdTaskIds.clear();
    }

    @Test
    @Story("Создание задачи без заголовка")
    @DisplayName("V1 create Task without title returns 400 VALIDATION_ERROR")
    void createTaskWithEmptyTitle_returns400() {

        CreateTaskRequest request = new CreateTaskRequest(
                projectId,
                "",
                "some description",
                TaskPriority.LOW
        );

        taskApiClient.createTask(request)
                .then()
                .statusCode(400)
                .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @Story("Изменение статуса задачи TODO -> DONE пропуская IN_PROGRESS")
    @DisplayName("V2 PATCH status TODO -> DONE skipping IN_PROGRESS returns 422")
    void updateSkippingStatus_returns422() {
        TaskDto task = createTask();

        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(
                TaskStatus.DONE,
                task.getVersion()
        );

        taskApiClient.updateTaskStatus(task.getId(), request)
                .then()
                .statusCode(422)
                .body("code", equalTo("UNPROCESSABLE_ENTITY"));

        taskApiClient.getTask(task.getId())
                .then()
                .statusCode(200)
                .body("task.status", equalTo(task.getStatus().toString()));
    }

    @Test
    @Story("Обновление статуса задачи с устаревшей версией")
    @DisplayName("V3 PATCH task with old version returns 409 Conflict")
    void updateTask_withOldVersion_returns409() {
        TaskDto task = createTask();
        int lastVersion = task.getVersion();

        taskApiClient.updateTaskStatus(task.getId(),
                        new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS, lastVersion))
                .then()
                .statusCode(200);

        taskApiClient.updateTaskStatus(task.getId(),
                        new UpdateTaskStatusRequest(TaskStatus.DONE, lastVersion))
                .then()
                .statusCode(409)
                .body("code", equalTo("CONFLICT"));
    }

    private TaskDto createTask() {
        CreateTaskRequest request = new CreateTaskRequest(
                projectId,
                "HW-DA-" + System.currentTimeMillis(),
                "some description",
                TaskPriority.LOW
        );

        TaskDto task = taskApiClient.createTask(request)
                .then()
                .statusCode(201)
                .extract()
                .as(TaskDto.class);

        createdTaskIds.add(task.getId());

        return task;
    }
}
