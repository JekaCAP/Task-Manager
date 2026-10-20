package itk.student.task.manager.aqa.support;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Login и сборка {@link RequestSpecification} с Bearer-токеном.
 * Не логирует пароль — только JSON body без log().all() на login.
 */
public final class AuthSupport {

    private AuthSupport() {
    }

    public static String obtainAccessToken() {
        return obtainAccessToken(ApiTestConfig.userEmail(), ApiTestConfig.userPassword());
    }

    public static String obtainAccessToken(String email, String password) {
        return given()
                .baseUri(ApiTestConfig.baseUrl())
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post(ApiPaths.AUTH_LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    public static RequestSpecification authenticatedJsonSpec() {
        return authenticatedJsonSpec(obtainAccessToken());
    }

    public static RequestSpecification authenticatedJsonSpec(String accessToken) {
        return given()
                .baseUri(ApiTestConfig.baseUrl())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken);
    }

    public static RequestSpecification anonymousJsonSpec() {
        return given()
                .baseUri(ApiTestConfig.baseUrl())
                .accept(ContentType.JSON);
    }
}
