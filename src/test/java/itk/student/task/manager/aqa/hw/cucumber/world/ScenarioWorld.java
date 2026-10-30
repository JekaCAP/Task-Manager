package itk.student.task.manager.aqa.hw.cucumber.world;

import io.restassured.response.Response;

import java.util.UUID;

public class ScenarioWorld {
    public String accessToken;
    public UUID demoProjectId;
    public UUID taskId;
    public String taskTitle;
    public int version;
    public int lastResponseCode;
    public Response lastResponse;
}
