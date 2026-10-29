package itk.student.task.manager.aqa.hw.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.api.TaskApiClient;
import itk.student.task.manager.aqa.hw.ui.pages.BoardPage;
import itk.student.task.manager.aqa.hw.ui.pages.LoginPage;
import itk.student.task.manager.aqa.hw.ui.pages.TaskModal;
import itk.student.task.manager.aqa.support.AuthSupport;
import org.junit.jupiter.api.*;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.codeborne.selenide.Condition.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardUiTest {

    private final List<UUID> created = new ArrayList<>();

    @BeforeAll
    static void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--lang=en-US");
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "en-US"));
        Configuration.browserCapabilities = options;
    }

    @BeforeEach
    void auth() {
        new LoginPage().open().successLogin(ApiTestConfig.userEmail(), ApiTestConfig.userPassword());
    }

    @AfterEach
    void clearSession() {
        deleteCreatedTasks();
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
                );

        created.add(page.getTaskIdByTitle(title));

        page.getColumn("TODO").shouldHave(text(title));
    }

    @Test
    void changeTaskStatusShouldMoveTaskInAnotherColumn() {
        BoardPage page = new BoardPage().waitForBoardLoaded();
        String title = "Test Task Selenide " + System.currentTimeMillis();

        page.createTask(title,
                "HIGH",
                LocalDate.now().plusDays(1));

        created.add(page.getTaskIdByTitle(title));

        page.openTaskByTitle(title)
                .setStatus("IN PROGRESS")
                .save();

        page.getColumn("TODO").shouldNotHave(text(title));
        page.getColumn("IN_PROGRESS").shouldHave(text(title));
    }

    @Test
    void addCommentToTaskAndCheck() {
        BoardPage page = new BoardPage().waitForBoardLoaded();
        String title = "Test Task Selenide " + System.currentTimeMillis();

        page.createTask(title,
                "HIGH",
                LocalDate.now().plusDays(1));

        created.add(page.getTaskIdByTitle(title));

        String commentText = "Test comment " + LocalDateTime.now();

        TaskModal task = page.openTaskByTitle(title).addComment(commentText);

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

        String title = "Test Task Selenide " + System.currentTimeMillis();
        page.createTask(title,
                "HIGH",
                LocalDate.now().plusDays(1));

        created.add(page.getTaskIdByTitle(title));

        int after = Integer.parseInt(page.getStatValue("stat-total-tasks"));

        assertTrue(after > before);
    }

    private void deleteCreatedTasks() {
        if (created.isEmpty()) return;

        TaskApiClient client = new TaskApiClient(AuthSupport.authenticatedJsonSpec());

        created.forEach(client::deleteTask);
    }
}
