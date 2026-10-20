package itk.student.task.manager.aqa.hw.api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.hw.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.hw.dto.UpdateTaskStatusRequest;

import java.util.UUID;

import static io.restassured.RestAssured.*;

public class TaskApiClient {

    private final RequestSpecification requestSpecification;
    private static final String TASKS_PATH = ApiPaths.TASKS;
    private static final String TASK_BY_ID_PATH = ApiPaths.TASKS + "/{taskId}";

    public TaskApiClient(RequestSpecification requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    @Step("Получение задачи с id: {id}")
    public Response getTask(UUID id) {
        return given()
                .spec(requestSpecification)
                .pathParam("taskId", id)
                .when()
                .get(TASK_BY_ID_PATH);
    }

    @Step("Создание задачи {request.title}")
    public Response createTask(CreateTaskRequest request) {
        return given()
                .spec(requestSpecification)
                .body(request)
                .when()
                .post(TASKS_PATH);
    }

    @Step("Удаление задачи с id: {id}")
    public Response deleteTask(UUID id) {
        return given()
                .spec(requestSpecification)
                .pathParam("taskId", id)
                .when()
                .delete(TASK_BY_ID_PATH);
    }

    @Step("Обновление статуса задачи {taskId} на {request.status}")
    public Response updateTaskStatus(UUID taskId, UpdateTaskStatusRequest request) {
        return given()
                .spec(requestSpecification)
                .pathParam("taskId", taskId)
                .body(request)
                .when()
                .patch(TASK_BY_ID_PATH + "/status");
    }
}
