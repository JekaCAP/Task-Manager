package itk.student.task.manager.aqa.support;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;
import itk.student.task.manager.aqa.config.ApiTestConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Login и сборка {@link RequestSpecification} с Bearer-токеном.
 * Не логирует пароль — только JSON body без log().all() на login.
 * Токен основного пользователя кэшируется между вызовами.
 */
public final class AuthSupport {

    private AuthSupport() {
    }

    public static String obtainAccessToken() {
        return obtainAccessToken(ApiTestConfig.userEmail(), ApiTestConfig.userPassword());
    }

    private static volatile String cachedAccessToken;

    public static String obtainAccessToken(String email, String password) {
        if (cachedAccessToken != null
                && email.equals(ApiTestConfig.userEmail())
                && password.equals(ApiTestConfig.userPassword())) {
            return cachedAccessToken;
        }
        String token = given()
                .spec(anonymousJsonSpec())
                .body(Map.of("email", email, "password", password))
                .when()
                .post(ApiPaths.AUTH_LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
        if (email.equals(ApiTestConfig.userEmail()) && password.equals(ApiTestConfig.userPassword())) {
            cachedAccessToken = token;
        }
        return token;
    }

    public static RequestSpecification authenticatedJsonSpec() {
        return authenticatedJsonSpec(obtainAccessToken());
    }

    public static RequestSpecification authenticatedJsonSpec(String accessToken) {
        return new RequestSpecBuilder()
                .setBaseUri(ApiTestConfig.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    public static RequestSpecification authenticatedJsonSpec(String email, String password) {
        return authenticatedJsonSpec(obtainAccessToken(email, password));
    }

    public static RequestSpecification anonymousJsonSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ApiTestConfig.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
}
