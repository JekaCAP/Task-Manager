package itk.student.task.manager.aqa.hw.ui;

import com.codeborne.selenide.Selenide;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.ui.pages.BoardPage;
import itk.student.task.manager.aqa.hw.ui.pages.LoginPage;
import itk.student.task.manager.aqa.hw.ui.pages.TaskModal;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardUiTest {

    @BeforeEach
    void auth() {
        new LoginPage().open().successLogin(ApiTestConfig.userEmail(), ApiTestConfig.userPassword());
    }

    @AfterEach
    void clearSession() {
        Selenide.clearBrowserLocalStorage();
    }

    @Test
    void createTaskAndCheckInKanban() {
        String title = "Test Task Selenide " + System.currentTimeMillis();
        BoardPage page = new BoardPage()
                .waitForBoardLoaded()
                .createTask(
                        title,
                        "HIGH",
                        LocalDate.now().plusDays(1)
                                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                );

        page.getColumn("TODO").shouldHave(text(title));
    }

    @Test
    void changeTaskStatusShouldMoveTaskInAnotherColumn() {
        BoardPage page = new BoardPage().waitForBoardLoaded();
        String taskTitle = page.getSomeTaskTitleFromColumn("TODO").getText();

        page.openTaskByTitle(taskTitle)
                .setStatus("IN PROGRESS")
                .save();

        page.getColumn("TODO").shouldNotHave(text(taskTitle));
        page.getColumn("IN_PROGRESS").shouldHave(text(taskTitle));
    }

    @Test
    void addCommentToTaskAndCheck() {
        BoardPage page = new BoardPage().waitForBoardLoaded();
        String taskTitle = page.getSomeTaskTitleFromColumn("TODO").getText();

        String commentText = "Test comment " + LocalDateTime.now();

        TaskModal task = page.openTaskByTitle(taskTitle).addComment(commentText);

        task.getCommentByText(commentText).shouldHave(text(commentText));
    }

    @Test
    void logoutButtonShouldRedirectToLoginPage() {
        LoginPage page = new BoardPage()
                .waitForBoardLoaded()
                .logout();

        assertTrue(page.isOnLoginPage());
    }

    @Test
    void addNewTaskShouldIncreaseTotalTasksStat() {
        BoardPage page = new BoardPage().waitForBoardLoaded();
        int before = Integer.parseInt(page.getStatValue("stat-total-tasks"));

        page.createTask("Test Task Selenide " + System.currentTimeMillis(),
                "HIGH",
                LocalDate.now().plusDays(1)
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        page.getStat("stat-total-tasks").shouldHave(exactText(String.valueOf(before + 1)));

    }
}
