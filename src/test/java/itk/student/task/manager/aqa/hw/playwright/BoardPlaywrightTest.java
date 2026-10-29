package itk.student.task.manager.aqa.hw.playwright;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Page;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.playwright.pages.BoardPage;
import itk.student.task.manager.aqa.hw.playwright.pages.LoginPage;
import itk.student.task.manager.aqa.hw.playwright.pages.TaskModal;
import itk.student.task.manager.aqa.hw.playwright.support.PlaywrightExtension;
import itk.student.task.manager.aqa.hw.playwright.support.TasksApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@ExtendWith(PlaywrightExtension.class)
public class BoardPlaywrightTest {

    private final List<String> createdTasks = new ArrayList<>();

    @AfterEach
    void cleanup(APIRequestContext api) {
        TasksApi tasksApi = new TasksApi(api);
        createdTasks.forEach(tasksApi::deleteTask);
    }

    @Test
    void createTaskShouldShowsItInToDoColumn(Page page) {
        String title = "Test task " + System.currentTimeMillis();

        BoardPage boardPage = new LoginPage(page)
                .open()
                .successLogin(ApiTestConfig.userEmail(), ApiTestConfig.userPassword())
                .waitForBoardLoaded()
                .createTask(
                        title,
                        "HIGH",
                        LocalDate.now().plusDays(1).toString()
                );

        createdTasks.add(boardPage.getTaskIdByTitle(title));
        assertThat(boardPage.getColumn("TODO")).containsText(title);
    }

    @Test
    void changeTaskStatusShouldMoveTaskInAnotherColumn(Page page, APIRequestContext api) {
        String title = "Test task " + System.currentTimeMillis();

        TasksApi tasksApi = new TasksApi(api);

        createdTasks.add(tasksApi.createTask(
                title,
                tasksApi.getProjectId(ApiTestConfig.demoProjectKey()),
                "HIGH"
        ));

        BoardPage boardPage = new LoginPage(page)
                .open()
                .successLogin(ApiTestConfig.userEmail(), ApiTestConfig.userPassword())
                .waitForBoardLoaded()
                .openTaskByTitle(title)
                .setStatus("IN PROGRESS")
                .save();

        assertThat(boardPage.getColumn("TODO")).not().containsText(title);
        assertThat(boardPage.getColumn("IN_PROGRESS")).containsText(title);
    }

    @Test
    void addCommentToTaskAndCheck(Page page) {
        BoardPage boardPage = new LoginPage(page)
                .open()
                .successLogin(ApiTestConfig.userEmail(), ApiTestConfig.userPassword())
                .waitForBoardLoaded();

        String title = boardPage.getSomeTaskTitleFromColumn("TODO").getByTestId("task-title").innerText();

        String commentText = "test comment " + System.currentTimeMillis();

        TaskModal modal = boardPage.openTaskByTitle(title)
                .addComment(commentText);

        assertThat(modal.getCommentByText(commentText)).containsText(commentText);
    }
}
