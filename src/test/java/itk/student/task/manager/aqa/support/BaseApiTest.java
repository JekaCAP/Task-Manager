package itk.student.task.manager.aqa.support;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseApiTest {

    protected RequestSpecification authSpec;

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void obtainAuthSpec() {
        authSpec = AuthSupport.authenticatedJsonSpec();
    }
}
