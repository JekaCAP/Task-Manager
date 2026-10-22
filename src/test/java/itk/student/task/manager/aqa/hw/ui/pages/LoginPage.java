package itk.student.task.manager.aqa.hw.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.*;

public class LoginPage {

    private final SelenideElement loginPage = $("[data-testid='login-page']");
    private final SelenideElement loginInput = $("[data-testid='login-email-input']");
    private final SelenideElement passwordInput = $("[data-testid='login-password-input']");
    private final SelenideElement loginButton = $("[data-testid='login-submit-btn']");
    private final SelenideElement error = $("[data-testid='login-error']");

    public LoginPage open() {
        Selenide.open("/login");
        return this;
    }

    public BoardPage login(String email, String password) {
        loginInput.sendKeys(email);
        passwordInput.sendKeys(password);
        loginButton.click();
        return new BoardPage();
    }

    public String getErrorMessage() {
        return error.getText();
    }

    public boolean isOnLoginPage() {
        return loginPage.isDisplayed();
    }
}
