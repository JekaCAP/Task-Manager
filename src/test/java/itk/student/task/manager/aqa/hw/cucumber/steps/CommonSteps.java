package itk.student.task.manager.aqa.hw.cucumber.steps;

import io.cucumber.java.en.Then;
import itk.student.task.manager.aqa.hw.cucumber.world.ScenarioWorld;

public class CommonSteps {
    private final ScenarioWorld world;

    public CommonSteps(ScenarioWorld world) {
        this.world = world;
    }

    @Then("запрос выполнен успешно")
    public void successRequest() {
        world.lastResponse.then().statusCode(200);
    }

}
