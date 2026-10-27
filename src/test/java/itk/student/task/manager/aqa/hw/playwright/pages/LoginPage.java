package itk.student.task.manager.aqa.hw.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginPage {

    private final Page page;
    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator error;
    private final Locator loginPage;

    public LoginPage(Page page) {
        this.page = page;
        emailInput = page.getByTestId("login-email-input");
        passwordInput = page.getByTestId("login-password-input");
        loginButton = page.getByTestId("login-submit-btn");
        error = page.getByTestId("login-error");
        loginPage = page.getByTestId("login-page");
    }

    public LoginPage open() {
        page.navigate("/login");
        return this;
    }

    public BoardPage successLogin(String email, String password) {
        emailInput.fill(email);
        passwordInput.fill(password);
        loginButton.click();
        return new BoardPage(page);
    }

    public LoginPage failedLogin(String email, String password) {
        emailInput.fill(email);
        passwordInput.fill(password);
        loginButton.click();
        return this;
    }

    public Locator error() {
        return error;
    }

    public Locator loginPage() {
        return loginPage;
    }
}
