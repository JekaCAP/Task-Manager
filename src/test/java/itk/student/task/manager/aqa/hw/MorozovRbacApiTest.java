package itk.student.task.manager.aqa.hw;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.api.ProjectApiClient;
import itk.student.task.manager.aqa.hw.api.TaskApiClient;
import itk.student.task.manager.aqa.hw.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.aqa.support.BaseApiTest;
import itk.student.task.manager.enums.TaskPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.*;

@Epic("Task Manager")
@Feature("Auth")
public class MorozovRbacApiTest extends BaseApiTest {

    private static TaskApiClient taskApiClient;
    private static ProjectApiClient projectApiClient;

    @BeforeEach
    void setup() {
        String accessToken = AuthSupport.obtainAccessToken(
                ApiTestConfig.viewerEmail(),
                ApiTestConfig.userPassword());

        taskApiClient = new TaskApiClient(AuthSupport.authenticatedJsonSpec(accessToken));
        projectApiClient = new ProjectApiClient(AuthSupport.authenticatedJsonSpec(accessToken));
    }

    @Test
    @TmsLink("TC-R1")
    @Story("Создание задачи без прав")
    @DisplayName("R1 create Task with role 'VIEWER' returns 403")
    void createTask_withViewerRole_returns403() {
        String projectId = projectApiClient.getProjects(20)
                .then().statusCode(200).extract().path("content[0].id");

        CreateTaskRequest request = new CreateTaskRequest(
                UUID.fromString(projectId),
                "HW-DA-" + System.currentTimeMillis(),
                "some description",
                TaskPriority.LOW
        );

        taskApiClient.createTask(request)
                .then()
                .statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }
}
