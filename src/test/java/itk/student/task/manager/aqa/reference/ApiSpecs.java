package itk.student.task.manager.aqa.reference;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.support.AuthSupport;

import static io.restassured.RestAssured.given;

public final class ApiSpecs {

    private ApiSpecs() {
    }

    public static RequestSpecification qaJsonSpec() {
        return AuthSupport.authenticatedJsonSpec();
    }

    public static RequestSpecification qaJsonSpec(String accessToken) {
        return AuthSupport.authenticatedJsonSpec(accessToken);
    }

    public static RequestSpecification viewerJsonSpec() {
        return AuthSupport.authenticatedJsonSpec(
                ApiTestConfig.viewerEmail(),
                ApiTestConfig.viewerPassword());
    }

    public static RequestSpecification anonymousJsonSpec() {
        return AuthSupport.anonymousJsonSpec();
    }

    public static ResponseSpecification jsonSuccessSpec() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    public static ResponseSpecification createdJsonSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectContentType(ContentType.JSON)
                .build();
    }
}
