package itk.student.task.manager.aqa.reference;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.reference.dto.CreateCommentRequest;
import itk.student.task.manager.aqa.reference.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.reference.dto.TaskResponse;
import itk.student.task.manager.aqa.reference.dto.UpdateTaskStatusRequest;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class TasksApi {

    private final RequestSpecification spec;

    public TasksApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        return given()
                .spec(spec)
                .body(request)
                .when()
                .post(ApiPaths.TASKS)
                .then()
                .log().ifValidationFails()
                .spec(ApiSpecs.createdJsonSpec())
                .extract()
                .as(TaskResponse.class);
    }

    public Response getTaskRaw(UUID taskId) {
        return given()
                .spec(spec)
                .pathParam("taskId", taskId)
                .when()
                .get(ApiPaths.TASKS + "/{taskId}");
    }

    public TaskResponse updateStatus(UUID taskId, UpdateTaskStatusRequest request) {
        return given()
                .spec(spec)
                .pathParam("taskId", taskId)
                .body(request)
                .when()
                .patch(ApiPaths.TASKS + "/{taskId}/status")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .as(TaskResponse.class);
    }

    public void deleteTask(UUID taskId) {
        given()
                .spec(spec)
                .pathParam("taskId", taskId)
                .when()
                .delete(ApiPaths.TASKS + "/{taskId}")
                .then()
                .log().ifValidationFails()
                .statusCode(204);
    }

    public Response createComment(UUID taskId, CreateCommentRequest request) {
        return given()
                .spec(spec)
                .pathParam("taskId", taskId)
                .body(request)
                .when()
                .post(ApiPaths.TASKS + "/{taskId}/comments");
    }
}
