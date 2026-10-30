package itk.student.task.manager.aqa.hw.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.api.AuthApiClient;
import itk.student.task.manager.aqa.hw.cucumber.world.ScenarioWorld;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

public class AuthSteps {
    private final ScenarioWorld world;
    private final AuthApiClient apiClient;

    public AuthSteps(ScenarioWorld world, AuthApiClient apiClient) {
        this.world = world;
        this.apiClient = apiClient;
    }

    @Given("пользователь зарегистрирован")
    public void userExist() {
        assertFalse(ApiTestConfig.userEmail().isEmpty());
    }

    @When("пользователь входит в систему")
    public void login() {
        Response response = apiClient.login(ApiTestConfig.userEmail(), ApiTestConfig.userPassword());
        world.lastResponseCode = response.statusCode();
        world.lastResponse = response;
    }

    @Then("в ответе есть непустой токен")
    public void tokenNonEmpty() {
        world.lastResponse.then().body("accessToken", not(emptyOrNullString()));
    }

    @Given("пользователь авторизован")
    public void authorizedUser() {
        world.accessToken = apiClient.login(ApiTestConfig.userEmail(), ApiTestConfig.userPassword())
                .jsonPath().getString("accessToken");
    }

    @When("пользователь запрашивает свой профиль")
    public void requestProfile() {
        world.lastResponse = apiClient.me(world.accessToken);
    }

    @Then("в профиле указан email пользователя")
    public void emailInProfile() {
        world.lastResponse.then().body("email", equalTo(ApiTestConfig.userEmail()));
    }

    @Given("неавторизованный пользователь")
    public void unauthorizedUser() {
        world.accessToken = null;
    }

    @Then("доступ отклонен с ошибкой {string}")
    public void unauthorizedResponse(String code) {
        world.lastResponse.then().statusCode(401)
                .body("code", equalTo(code));
    }
}
