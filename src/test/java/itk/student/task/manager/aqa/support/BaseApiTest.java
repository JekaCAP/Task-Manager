package itk.student.task.manager.aqa.support;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.security.MaskingPrintStream;
import itk.student.task.manager.aqa.hw.security.SecureAllureFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintStream;

/**
 * Базовый класс для API-тестов против sandbox (VPS или localhost).
 * Не поднимает Spring Context — только HTTP через Rest Assured.
 */
public abstract class BaseApiTest {

    protected RequestSpecification authSpec;

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.reset();
        RestAssured.baseURI = ApiTestConfig.baseUrl();
        PrintStream masked = new MaskingPrintStream(System.out);

        RestAssured.config = RestAssured.config().logConfig(
                LogConfig.logConfig()
                        .defaultStream(masked)
                        .blacklistHeader("Authorization")
                        .enableLoggingOfRequestAndResponseIfValidationFails());

        RestAssured.filters(new SecureAllureFilter());
    }

    @BeforeEach
    void obtainAuthSpec() {
        authSpec = AuthSupport.authenticatedJsonSpec();
    }
}