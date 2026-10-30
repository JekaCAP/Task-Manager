package itk.student.task.manager.aqa.hw.cucumber.steps;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import itk.student.task.manager.aqa.hw.api.ProjectApiClient;
import itk.student.task.manager.aqa.hw.api.TaskApiClient;
import itk.student.task.manager.aqa.hw.cucumber.world.ScenarioWorld;
import itk.student.task.manager.aqa.hw.dto.CreateTaskRequest;
import itk.student.task.manager.aqa.hw.dto.UpdateTaskStatusRequest;
import itk.student.task.manager.aqa.support.AuthSupport;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;

import static org.hamcrest.Matchers.*;

public class TaskSteps {

    private final ScenarioWorld world;

    public TaskSteps(ScenarioWorld world) {
        this.world = world;
    }

    private TaskApiClient taskApi() {
        return new TaskApiClient(AuthSupport.authenticatedJsonSpec(world.accessToken));
    }

    private ProjectApiClient projectApi() {
        return new ProjectApiClient(AuthSupport.authenticatedJsonSpec(world.accessToken));
    }


    @When("пользователь запрашивает список проектов")
    public void getProjects() {
        world.lastResponse = projectApi().getProjects(20);
    }

    @Then("найден проект {string}")
    public void findProject(String key) {
        world.demoProjectId = world.lastResponse.then()
                .body("content.key", hasItem(key))
                .extract()
                .jsonPath()
                .param("key", key)
                .getUUID("content.find { it.key == key }.id");
    }

    @When("пользователь создает задачу с уникальным заголовком в проекте")
    public void createTask() {
        String title = "HW-DA-" + System.currentTimeMillis();
        world.taskTitle = title;

        world.lastResponse = taskApi().createTask(new CreateTaskRequest(
                world.demoProjectId,
                title,
                "some description",
                TaskPriority.LOW
        ));
    }

    @Then("задача создана")
    public void taskCreated() {
        world.lastResponse.then().statusCode(201);
        world.taskId = world.lastResponse.jsonPath().getUUID("id");
        world.version = world.lastResponse.jsonPath().getInt("version");
    }

    @Then("статус задачи {string}")
    public void taskStatus(String status) {
        world.lastResponse.then()
                .body("status", equalTo(status));
    }

    @Then("версия задачи {int}")
    public void taskVersion(int version) {
        world.lastResponse.then()
                .body("version", equalTo(version));
    }

    @When("пользователь запрашивает задачу")
    public void getTask() {
        world.lastResponse = taskApi().getTask(world.taskId);

    }

    @Then("заголовок задачи совпадает с заданным")
    public void getTitle() {
        world.lastResponse.then()
                .body("task.title", equalTo(world.taskTitle));
        world.version = world.lastResponse.jsonPath().getInt("task.version");
    }

    @When("пользователь переводит задачу в статус {string}")
    public void changeStatus(String status) {
        world.lastResponse = taskApi().updateTaskStatus(world.taskId,
                new UpdateTaskStatusRequest(TaskStatus.valueOf(status), world.version));
    }

    @Then("версия задачи увеличилась")
    public void versionIncreased() {
        world.lastResponse.then()
                .body("version", greaterThan(world.version));
        world.version = world.lastResponse.jsonPath().getInt("version");
    }

    @When("пользователь удаляет задачу")
    public void deleteTask() {
        world.lastResponse = taskApi().deleteTask(world.taskId);
    }

    @Then("задача удалена")
    public void taskDeleted() {
        world.lastResponse.then().statusCode(204)
                .body(emptyOrNullString());
    }

    @Then("задача не найдена с ошибкой {string}")
    public void taskNotFound(String code) {
        world.lastResponse.then().statusCode(404)
                .body("code", equalTo(code));
    }
}
