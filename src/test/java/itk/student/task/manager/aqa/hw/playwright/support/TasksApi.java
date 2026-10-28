package itk.student.task.manager.aqa.hw.playwright.support;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import io.restassured.path.json.JsonPath;
import itk.student.task.manager.aqa.config.ApiTestConfig;

import java.util.Map;

public class TasksApi {

    private final APIRequestContext api;
    private static final String TASKS_URL = "/api/v1/tasks";
    private static final String PROJECTS_URL = "/api/v1/projects";
    private static final String AUTH_URL = "/api/v1/auth/login";

    public TasksApi(APIRequestContext api) {
        this.api = api;
    }

    public String getAccessToken(String email, String password) {
        APIResponse response = api.post(AUTH_URL, RequestOptions.create()
                .setData(Map.of(
                        "email", email,
                        "password", password)
                ));
        return JsonPath.from(response.text()).getString("accessToken");
    }

    public String createTask(String title, String projectId, String priority) {
        APIResponse response = api.post(TASKS_URL, RequestOptions.create()
                .setHeader("Authorization",
                        "Bearer " + getAccessToken(
                                ApiTestConfig.userEmail(), ApiTestConfig.userPassword())
                ).setData(Map.of(
                        "projectId", projectId,
                        "title", title,
                        "priority", priority
                )));

        return JsonPath.from(response.text()).getString("id");
    }

    public String getProjectId(String key) {
        APIResponse response = api.get(PROJECTS_URL, RequestOptions.create()
                .setHeader("Authorization",
                        "Bearer " + getAccessToken(
                                ApiTestConfig.userEmail(), ApiTestConfig.userPassword())
                ));

        return JsonPath.from(response.text()).getString("content.find { it.key == '" + key + "' }.id");
    }

    public void deleteTask(String id) {
        api.delete(TASKS_URL + "/" + id, RequestOptions.create()
                .setHeader("Authorization", "Bearer " +
                        getAccessToken(ApiTestConfig.userEmail(), ApiTestConfig.userPassword())));
    }
}
