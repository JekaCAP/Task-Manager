package itk.student.task.manager.aqa.support;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Базовый класс для API-тестов против sandbox (VPS или localhost).
 * Не поднимает Spring Context — только HTTP через Rest Assured.
 */
public abstract class BaseApiTest {

    protected RequestSpecification authSpec;

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = ApiTestConfig.baseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void obtainAuthSpec() {
        authSpec = AuthSupport.authenticatedJsonSpec();
    }
}
