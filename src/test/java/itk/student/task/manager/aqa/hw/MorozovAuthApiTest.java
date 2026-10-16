package itk.student.task.manager.aqa.hw;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import io.restassured.http.ContentType;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.aqa.support.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("Task Manager")
@Feature("Аутентификация")
public class MorozovAuthApiTest extends BaseApiTest {

    @Test
    @TmsLink("TC-A1")
    @Story("Успешный логин")
    @DisplayName("A1 login with valid credentials returns accessToken")
    void login_withValidCredentials_returnsToken() {
        String email = ApiTestConfig.userEmail();
        String password = ApiTestConfig.userPassword();

        given()
                .baseUri(ApiTestConfig.baseUrl())
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post(ApiPaths.AUTH_LOGIN)
                .then()
                .statusCode(200)
                .body("accessToken", not(emptyOrNullString()));
    }

    @Test
    @TmsLink("TC-A2")
    @Story("Получение авторизованного пользователя")
    @DisplayName("A2 GET /auth/me with token returns 200 and current user email")
    void getUser_withToken_returns200AndCurrentUserEmail() {
        given()
                .spec(authSpec)
                .when()
                .get(ApiPaths.AUTH_ME)
                .then()
                .statusCode(200)
                .body("email", equalTo(ApiTestConfig.userEmail()));
    }

    @Test
    @TmsLink("TC-A3")
    @Story("Доступ без токена")
    @DisplayName("A3 GET /auth/me without token returns 401 UNAUTHORIZED")
    void getUser_withoutToken_returns401() {
        given()
                .spec(AuthSupport.anonymousJsonSpec())
                .when()
                .get(ApiPaths.AUTH_ME)
                .then()
                .statusCode(401)
                .body("code", equalTo("UNAUTHORIZED"));
    }
}
