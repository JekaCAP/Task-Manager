package itk.student.task.manager.aqa.reference;

import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.aqa.support.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

class ReferenceAuthApiTest extends BaseApiTest {

    @Test
    @DisplayName("A1: login with valid credentials returns accessToken")
    void login_withValidCredentials_returnsToken() {
        given()
                .spec(ApiSpecs.anonymousJsonSpec())
                .body(Map.of(
                        "email", ApiTestConfig.userEmail(),
                        "password", ApiTestConfig.userPassword()))
                .when()
                .post(ApiPaths.AUTH_LOGIN)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("accessToken", not(blankOrNullString()))
                .body("refreshToken", not(blankOrNullString()));
    }

    @Test
    @DisplayName("A2: GET /auth/me with Bearer returns current user email")
    void getMe_withValidToken_returnsCurrentUser() {
        String token = AuthSupport.obtainAccessToken();

        given()
                .spec(ApiSpecs.qaJsonSpec(token))
                .when()
                .get(ApiPaths.AUTH_ME)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("email", equalTo(ApiTestConfig.userEmail()))
                .body("id", not(blankOrNullString()));
    }

    @Test
    @DisplayName("A3: GET /auth/me without token returns 401 UNAUTHORIZED")
    void getMe_withoutToken_returns401() {
        given()
                .spec(ApiSpecs.anonymousJsonSpec())
                .when()
                .get(ApiPaths.AUTH_ME)
                .then()
                .log().ifValidationFails()
                .statusCode(401)
                .body("code", equalTo("UNAUTHORIZED"));
    }
}
