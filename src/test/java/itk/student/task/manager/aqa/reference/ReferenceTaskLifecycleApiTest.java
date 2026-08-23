package itk.student.task.manager.aqa.reference;

import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.reference.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.reference.dto.UpdateTaskStatusRequest;
import itk.student.task.manager.aqa.support.BaseApiTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReferenceTaskLifecycleApiTest extends BaseApiTest {

    private static final String TITLE = "HW-REF-" + System.currentTimeMillis();

    private RequestSpecification qaSpec;
    private UUID demoProjectId;
    private UUID taskId;
    private long taskVersion;

    @BeforeAll
    void setUpSpec() {
        qaSpec = ApiSpecs.qaJsonSpec();
    }

    @Test
    @Order(1)
    @DisplayName("L1: list projects contains DEMO")
    void listProjects_containsDemo() {
        String projectId = given()
                .spec(qaSpec)
                .queryParam("size", 20)
                .when()
                .get(ApiPaths.PROJECTS)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("content", notNullValue())
                .body("content.find { it.key == '" + ApiTestConfig.demoProjectKey() + "' }.id", notNullValue())
                .extract()
                .path("content.find { it.key == '" + ApiTestConfig.demoProjectKey() + "' }.id");

        demoProjectId = UUID.fromString(projectId);
    }

    @Test
    @Order(2)
    @DisplayName("L2: create task in DEMO returns 201 TODO version 0")
    void createTask_inDemo_returns201() {
        String id = given()
                .spec(qaSpec)
                .body(new CreateTaskRequest(
                        demoProjectId,
                        TITLE,
                        "Rest Assured reference",
                        "MEDIUM"))
                .when()
                .post(ApiPaths.TASKS)
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/task-response.json"))
                .body("status", equalTo("TODO"))
                .body("version", equalTo(0))
                .body("title", equalTo(TITLE))
                .extract()
                .path("id");

        taskId = UUID.fromString(id);
        taskVersion = 0L;
    }

    @Test
    @Order(3)
    @DisplayName("L3: GET task by id returns task wrapper")
    void getTaskById_returnsCreatedTask() {
        taskVersion = ((Number) given()
                .spec(qaSpec)
                .pathParam("taskId", taskId)
                .when()
                .get(ApiPaths.TASKS + "/{taskId}")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/task-detail-response.json"))
                .body("task.id", equalTo(taskId.toString()))
                .body("task.title", equalTo(TITLE))
                .body("task.status", equalTo("TODO"))
                .extract()
                .path("task.version")).longValue();
    }

    @Test
    @Order(4)
    @DisplayName("L4: PATCH status TODO to IN_PROGRESS")
    void updateStatus_todoToInProgress() {
        long previousVersion = taskVersion;

        taskVersion = ((Number) given()
                .spec(qaSpec)
                .pathParam("taskId", taskId)
                .body(new UpdateTaskStatusRequest("IN_PROGRESS", taskVersion))
                .when()
                .patch(ApiPaths.TASKS + "/{taskId}/status")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("version", greaterThan((int) previousVersion))
                .extract()
                .path("version")).longValue();
    }

    @Test
    @Order(5)
    @DisplayName("L5: DELETE task returns 204")
    void deleteTask_returns204() {
        given()
                .spec(qaSpec)
                .pathParam("taskId", taskId)
                .when()
                .delete(ApiPaths.TASKS + "/{taskId}")
                .then()
                .log().ifValidationFails()
                .statusCode(204);
    }

    @Test
    @Order(6)
    @DisplayName("L6: GET deleted task returns 404 NOT_FOUND")
    void getDeletedTask_returns404() {
        given()
                .spec(qaSpec)
                .pathParam("taskId", taskId)
                .when()
                .get(ApiPaths.TASKS + "/{taskId}")
                .then()
                .log().ifValidationFails()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"))
                .body("path", notNullValue());
    }
}
