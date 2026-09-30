package itk.student.task.manager.aqa.hw;

import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.aqa.support.BaseApiTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class UserControllerTest extends BaseApiTest {

    private static final String USERS = "/api/v1/users";

    @Test
    void listUsers() {
        authSpec
                .when()
                .get(USERS)
                .then()
                .statusCode(403)
                .body("code", equalTo("FORBIDDEN"));

        AuthSupport.authenticatedJsonSpec(
                        AuthSupport.obtainAccessToken("admin@demo.com", ApiTestConfig.userPassword()))
                .when()
                .get(USERS)
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("meta.page", equalTo(0));
    }

    @Test
    void getUser() {
        String userId = currentUserId();

        authSpec
                .when()
                .get(USERS + "/" + userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("email", equalTo(ApiTestConfig.userEmail()));
    }

    @Test
    void updateUser() {
        String userId = currentUserId();
        String originalFirstName = authSpec
                .when()
                .get(USERS + "/" + userId)
                .then()
                .statusCode(200)
                .extract()
                .path("firstName");

        try {
            authSpec
                    .body(Map.of("firstName", "RestAssured"))
                    .when()
                    .patch(USERS + "/" + userId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(userId))
                    .body("firstName", equalTo("RestAssured"));
        } finally {
            authSpec
                    .body(Map.of("firstName", originalFirstName == null ? "QA" : originalFirstName))
                    .when()
                    .patch(USERS + "/" + userId)
                    .then()
                    .statusCode(200);
        }
    }

    @Test
    void deleteUser() {
        authSpec
                .when()
                .delete(USERS + "/" + currentUserId())
                .then()
                .statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void getUserTasks() {
        authSpec
                .queryParam("status", "IN_PROGRESS")
                .when()
                .get(USERS + "/" + currentUserId() + "/tasks")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("meta", notNullValue());
    }

    @Test
    void myProfile() {
        authSpec
                .when()
                .get(USERS + "/me/profile")
                .then()
                .statusCode(200)
                .body("user.email", equalTo(ApiTestConfig.userEmail()))
                .body("assignedTasksCount", notNullValue())
                .body("ownedProjectsCount", notNullValue())
                .body("memberSince", notNullValue());
    }

    private String currentUserId() {
        return authSpec
                .when()
                .get(ApiPaths.AUTH_ME)
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }
}
