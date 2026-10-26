package itk.student.task.manager.aqa.hw.ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.ui.pages.BoardPage;
import itk.student.task.manager.aqa.hw.ui.pages.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginUiTest {

    @AfterEach
    void tearDown() {
        Selenide.clearBrowserLocalStorage();
    }

    @Test
    void successLoginShouldRedirectToBoardPage() {
        BoardPage boardPage = new LoginPage()
                .open()
                .successLogin(ApiTestConfig.userEmail(), ApiTestConfig.userPassword())
                .waitForBoardLoaded();

        assertEquals("QA Engineer", boardPage.getUserName());
        assertEquals(WebDriverRunner.url(), ApiTestConfig.baseUrl() + "/");
    }

    @Test
    void loginWithWrongPasswordShowsError() {
        LoginPage page = new LoginPage()
                .open()
                .failedLogin(ApiTestConfig.userEmail(), "wrong123");

        assertTrue(page.isOnLoginPage());
        assertEquals("Authentication failed", page.getErrorMessage());
    }
}
