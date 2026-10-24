package itk.student.task.manager.aqa.hw.playwright;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import itk.student.task.manager.aqa.hw.playwright.pages.LoginPage;
import itk.student.task.manager.aqa.hw.playwright.support.PlaywrightExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PlaywrightExtension.class)
public class LoginPlaywrightTest {

    @Test
    void successLoginShowsBoard(Page page) {
        LoginPage loginPage = new LoginPage(page).open();

        Response response = page.waitForResponse(
                r -> r.url().contains("/api/v1/auth/login") && r.status() == 200,
                () -> loginPage.successLogin(ApiTestConfig.userEmail(), ApiTestConfig.userPassword()));

        assertThat(page).hasURL(ApiTestConfig.baseUrl() + "/");
        assertThat(page.getByTestId("user-name")).isVisible();
        assertTrue(response.text().contains("accessToken"));
    }

    @Test
    void failedLoginShouldStayAtLoginPage(Page page) {
        LoginPage loginPage = new LoginPage(page).open()
                .failedLogin(ApiTestConfig.userEmail(), "wrong123");

        assertThat(page).hasURL(Pattern.compile("./login$"));
        assertThat(loginPage.error()).isVisible();
        assertThat(loginPage.error()).hasText("Authentication failed");
    }
}
