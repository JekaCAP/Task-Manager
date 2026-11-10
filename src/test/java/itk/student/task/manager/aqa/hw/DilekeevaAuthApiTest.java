package itk.student.task.manager.aqa.hw;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.support.AuthSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("Task Manager")
@Feature("Auth")
public class DilekeevaAuthApiTest {

  @TmsLink("TC-111")
  @Severity(SeverityLevel.CRITICAL)
  @Story("Успешный логин")
  @DisplayName("Успешный логин")
  @Test
  void postLogin_withEmailAndPassword_returns200() {
    Allure.step("Авторизоваться под успешным логином", () -> {
      given()
          .baseUri(ApiTestConfig.baseUrl())
          .contentType(ContentType.JSON)
          .body(Map.of("email", ApiTestConfig.userEmail(), "password", ApiTestConfig.userPassword()))
          .when()
          .post(ApiPaths.AUTH_LOGIN)
          .then()
          .statusCode(200)
          .body("accessToken", notNullValue());
    });
  }

  @TmsLink("TC-112")
  @Story("Получение текущего пользователя по токену")
  @DisplayName("Получение текущего пользователя по токену")
  @Test
  void getMe_withToken_returns200() {
    Allure.step("Получить текущего пользователя", () -> {
      RestAssured
          .given(AuthSupport.authenticatedJsonSpec())
          .when()
          .get(ApiPaths.AUTH_ME)
          .then()
          .log().ifValidationFails()
          .statusCode(200)
          .body("email", equalTo(ApiTestConfig.userEmail()));
    });
  }

  @TmsLink("TC-113")
  @Story("Доступ без токена")
  @DisplayName("Доступ к защищённому ресурсу без токена")
  @Test
  void getMe_withoutToken_returns401() {
    Allure.step("Получение текущего пользователя без логина", () -> {
      given(AuthSupport.anonymousJsonSpec())
          .when()
          .get(ApiPaths.AUTH_ME)
          .then()
          .log().ifValidationFails()
          .statusCode(401)
          .body("code", equalTo("UNAUTHORIZED"));
    });
  }

}
