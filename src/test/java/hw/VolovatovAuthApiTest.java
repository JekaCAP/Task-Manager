package hw;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.support.AuthSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Task Manager")
@Feature("Auth")
public class VolovatovAuthApiTest {

    @Test
    @DisplayName("Успешный логин")
    @Story("Успешный логин")
    public void successLogin() {

        Allure.step("Выполнение запроса на авторизацию пользователя", () ->

                given()
                        .spec(AuthSupport.anonymousJsonSpec())
                        .contentType(ContentType.JSON)
                        .body(Map.of("email", ApiTestConfig.userEmail(), "password", ApiTestConfig.userPassword()))
                        .when()
                        .post("/api/v1/auth/login")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(200)
                        .body("accessToken", not(emptyOrNullString()))
        );
    }

    @Test
    @DisplayName("Получение текущего пользователя по токену")
    @Story("Получение текущего пользователя по токену")
    public void getUserByToken() {
        String token = AuthSupport.obtainAccessToken();

        given()
                .spec(AuthSupport.authenticatedJsonSpec(token))
                .when()
                .get("/api/v1/auth/me")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("email", equalTo(ApiTestConfig.userEmail()));
    }

    @Test
    @DisplayName("Доступ к защищённому ресурсу без токена")
    @Story("Доступ без токена")
    public void getUserWithoutToken() {
        given()
                .spec(AuthSupport.anonymousJsonSpec())
                .when()
                .get("/api/v1/auth/me")
                .then()
                .log().ifValidationFails()
                .statusCode(401)
                .body("code", equalTo("UNAUTHORIZED"));
    }

}
