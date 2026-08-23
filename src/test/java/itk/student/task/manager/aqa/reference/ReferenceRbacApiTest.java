package itk.student.task.manager.aqa.reference;

import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.reference.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.support.BaseApiTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ReferenceRbacApiTest extends BaseApiTest {

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
    @DisplayName("viewer cannot create task in DEMO project")
    void createTask_asViewer_returns403() {
        given()
                .spec(ApiSpecs.viewerJsonSpec())
                .body(new CreateTaskRequest(
                        demoProjectId,
                        "HW-VWR-" + System.currentTimeMillis(),
                        "rbac test",
                        "MEDIUM"))
                .when()
                .post(ApiPaths.TASKS)
                .then()
                .log().ifValidationFails()
                .statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }
}
