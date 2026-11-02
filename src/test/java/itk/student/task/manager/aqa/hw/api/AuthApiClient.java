package itk.student.task.manager.aqa.hw.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import itk.student.task.manager.aqa.config.ApiPaths;

import java.util.Map;

import static io.restassured.RestAssured.*;

public class AuthApiClient {

    public Response login(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post(ApiPaths.AUTH_LOGIN);
    }

    public Response me(String accessToken) {
        return given()
                .header("Authorization", "Bearer " + accessToken)
                .accept(ContentType.JSON)
                .when()
                .get(ApiPaths.AUTH_ME);
    }
}
