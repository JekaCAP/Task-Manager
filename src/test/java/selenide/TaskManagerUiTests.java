package selenide;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskManagerUiTests {

    private LoginPage loginPage;
    private BoardPage boardPage;
    private TaskModal taskModal;
    static String title = "TestTaskSelenide-" + System.currentTimeMillis();
    static String comment = "New comment" + System.currentTimeMillis();

    @BeforeEach
    public void setUp() {
        loginPage = new LoginPage();
        boardPage = new BoardPage();
        taskModal = new TaskModal();

        loginPage.open();
    }

    @AfterEach
    public void tearDown() {
        Selenide.closeWebDriver();
    }

    @Test
    @DisplayName("TC-501 Успешный логин")
    @Order(1)
    public void successLogin() {

        loginPage.login("qa@demo.com", "Demo123!");

        boardPage.waitForBoardLoaded();

        boardPage.getUserName();

        $("[data-testid='user-name']").shouldHave(text("QA Engineer"));

        String currentUrl = WebDriverRunner.url();
        assertThat(currentUrl).doesNotContain("/login");
    }

    @Test
    @DisplayName("TC-502 Не успешный логин")
    @Order(2)
    public void unsuccessLogin() {

        loginPage.login("qa@demo.com", "dssde!");

        $("[data-testid='login-error']").shouldBe(visible);

        String error = loginPage.getErrorMessage();

        assertThat(error).isEqualTo("Authentication failed");

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    @Test
    @DisplayName("TC-503 Создание задачи")
    @Order(3)
    public void createTask() {

        loginPage.login("qa@demo.com", "Demo123!");

        boardPage.waitForBoardLoaded();

        boardPage.createTask(title, "HIGH",
                LocalDate.now().plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        boardPage.getTaskCard(title).shouldBe(visible);
    }

    @Test
    @DisplayName("TC-504 Перемещение задачи по статусу (TODO → IN_PROGRESS)")
    @Order(4)
    public void statusChange() {

        loginPage.login("qa@demo.com", "Demo123!");

        boardPage.waitForBoardLoaded();

        boardPage.openTaskByTitle(title);

        taskModal.setStatus("IN_PROGRESS");

        taskModal.save();

        boardPage.getColumn("IN_PROGRESS").shouldHave(text(title));

        boardPage.getColumn("TODO").shouldNotHave(text(title));
    }

    @Test
    @DisplayName("TC-505 Добавление комментария к задаче")
    @Order(5)
    public void addComment() {

        loginPage.login("qa@demo.com", "Demo123!");

        boardPage.waitForBoardLoaded();

        boardPage.openTaskByTitle(title);

        taskModal.addComment(comment);

        taskModal.getCommentByText(comment).shouldBe(visible);
    }

    @Test
    @DisplayName("TC-506 Логаут")
    @Order(6)
    public void logout() {

        loginPage.login("qa@demo.com", "Demo123!");

        boardPage.waitForBoardLoaded();

        String currentUrl = WebDriverRunner.url();
        assertThat(currentUrl).doesNotContain("/login");

        boardPage.logout();

        currentUrl = WebDriverRunner.url();
        assertThat(currentUrl).contains("/login");
    }

}
