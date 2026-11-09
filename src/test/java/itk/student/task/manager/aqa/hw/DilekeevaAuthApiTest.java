package itk.student.task.manager.aqa.hw;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.support.AuthSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DilekeevaAuthApiTest {

  @DisplayName("Успешный логин")
  @Test
  void postLogin_withEmailAndPassword_returns200() {
    given()
        .baseUri(ApiTestConfig.baseUrl())
        .contentType(ContentType.JSON)
        .body(Map.of("email", ApiTestConfig.userEmail(), "password", ApiTestConfig.userPassword()))
        .when()
        .post(ApiPaths.AUTH_LOGIN)
        .then()
        .statusCode(200)
        .body("accessToken", notNullValue());
  }

  @DisplayName("Получение текущего пользователя по токену")
  @Test
  void getMe_withToken_returns200() {
    RestAssured
        .given(AuthSupport.authenticatedJsonSpec())
        .when()
        .get(ApiPaths.AUTH_ME)
        .then()
        .log().ifValidationFails()
        .statusCode(200)
        .body("email", equalTo(ApiTestConfig.userEmail()));
  }

  @DisplayName("Доступ к защищённому ресурсу без токена")
  @Test
  void getMe_withoutToken_returns401() {
    given(AuthSupport.anonymousJsonSpec())
        .when()
        .get(ApiPaths.AUTH_ME)
        .then()
        .log().ifValidationFails()
        .statusCode(401)
        .body("code", equalTo("UNAUTHORIZED"));
  }

}
