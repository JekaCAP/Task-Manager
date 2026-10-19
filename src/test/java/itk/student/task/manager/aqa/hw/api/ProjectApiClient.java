package itk.student.task.manager.aqa.hw.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import itk.student.task.manager.aqa.config.ApiPaths;

import static io.restassured.RestAssured.*;

public class ProjectApiClient {

    private final RequestSpecification spec;
    private final static String PROJECTS_PATH = ApiPaths.PROJECTS;

    public ProjectApiClient(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response getProjects(int size) {
        return given()
                .spec(spec)
                .queryParam("size", size)
                .when()
                .get(PROJECTS_PATH);
    }
}
