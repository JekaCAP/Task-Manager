package itk.student.task.manager.aqa.hw.cucumber.hooks;

import io.cucumber.java.BeforeAll;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.security.MaskingPrintStream;
import itk.student.task.manager.aqa.hw.security.SecureAllureFilter;

import java.io.PrintStream;

public class ApiHooks {

    @BeforeAll()
    public static void configureRestAssured() {
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

}
