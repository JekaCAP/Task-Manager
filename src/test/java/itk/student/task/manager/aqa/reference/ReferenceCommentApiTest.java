package itk.student.task.manager.aqa.reference;

import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.reference.dto.CreateCommentRequest;
import itk.student.task.manager.aqa.reference.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.support.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class ReferenceCommentApiTest extends BaseApiTest {

    @Test
    @DisplayName("create comment on task via TasksApi")
    void createComment_onTask_returns201() {
        UUID demoProjectId = UUID.fromString(given()
                .spec(ApiSpecs.qaJsonSpec())
                .queryParam("size", 20)
                .when()
                .get(ApiPaths.PROJECTS)
                .then()
                .extract()
                .path("content.find { it.key == 'DEMO' }.id"));

        TasksApi tasksApi = new TasksApi(ApiSpecs.qaJsonSpec());
        var task = tasksApi.createTask(new CreateTaskRequest(
                demoProjectId,
                "HW-CMT-" + System.currentTimeMillis(),
                "comment test",
                "MEDIUM"));

        tasksApi.createComment(task.id(), new CreateCommentRequest("Reference comment"))
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("body", equalTo("Reference comment"))
                .body("id", notNullValue());

        tasksApi.deleteTask(task.id());
    }
}
