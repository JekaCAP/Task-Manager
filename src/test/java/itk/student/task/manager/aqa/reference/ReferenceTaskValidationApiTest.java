package itk.student.task.manager.aqa.reference;

import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.reference.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.reference.dto.UpdateTaskStatusRequest;
import itk.student.task.manager.aqa.support.BaseApiTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ReferenceTaskValidationApiTest extends BaseApiTest {

    private static UUID demoProjectId;

    @BeforeAll
    static void resolveDemoProject() {
        String projectId = given()
                .spec(ApiSpecs.qaJsonSpec())
                .queryParam("size", 20)
                .when()
                .get(ApiPaths.PROJECTS)
                .then()
                .extract()
                .path("content.find { it.key == 'DEMO' }.id");
        demoProjectId = UUID.fromString(projectId);
    }

    @Test
    @DisplayName("empty title returns 400 VALIDATION_ERROR")
    void createTask_emptyTitle_returns400() {
        given()
                .spec(ApiSpecs.qaJsonSpec())
                .body(new CreateTaskRequest(demoProjectId, "", "bad", "MEDIUM"))
                .when()
                .post(ApiPaths.TASKS)
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("invalid status transition TODO to DONE returns 422")
    void updateStatus_invalidTransition_returns422() {
        String taskId = given()
                .spec(ApiSpecs.qaJsonSpec())
                .body(new CreateTaskRequest(
                        demoProjectId,
                        "HW-VAL-" + System.currentTimeMillis(),
                        "validation test",
                        "LOW"))
                .when()
                .post(ApiPaths.TASKS)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .spec(ApiSpecs.qaJsonSpec())
                .pathParam("taskId", taskId)
                .body(new UpdateTaskStatusRequest("DONE", 0))
                .when()
                .patch(ApiPaths.TASKS + "/{taskId}/status")
                .then()
                .log().ifValidationFails()
                .statusCode(422)
                .body("code", equalTo("UNPROCESSABLE_ENTITY"));

        given()
                .spec(ApiSpecs.qaJsonSpec())
                .pathParam("taskId", taskId)
                .when()
                .delete(ApiPaths.TASKS + "/{taskId}")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("stale version returns 409 CONFLICT")
    void updateStatus_staleVersion_returns409() {
        String taskId = given()
                .spec(ApiSpecs.qaJsonSpec())
                .body(new CreateTaskRequest(
                        demoProjectId,
                        "HW-VER-" + System.currentTimeMillis(),
                        "version test",
                        "LOW"))
                .when()
                .post(ApiPaths.TASKS)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .spec(ApiSpecs.qaJsonSpec())
                .pathParam("taskId", taskId)
                .body(new UpdateTaskStatusRequest("IN_PROGRESS", 0))
                .when()
                .patch(ApiPaths.TASKS + "/{taskId}/status")
                .then()
                .statusCode(200);

        given()
                .spec(ApiSpecs.qaJsonSpec())
                .pathParam("taskId", taskId)
                .body(new UpdateTaskStatusRequest("IN_PROGRESS", 0))
                .when()
                .patch(ApiPaths.TASKS + "/{taskId}/status")
                .then()
                .log().ifValidationFails()
                .statusCode(409)
                .body("code", equalTo("CONFLICT"));

        given()
                .spec(ApiSpecs.qaJsonSpec())
                .pathParam("taskId", taskId)
                .when()
                .delete(ApiPaths.TASKS + "/{taskId}")
                .then()
                .statusCode(204);
    }
}
