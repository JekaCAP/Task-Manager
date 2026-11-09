package selenide;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class LoginPage {


    public void open() {
        Selenide.open("/login");
    }

    public void login(String email, String password) {

        $("[data-testid='login-email-input']").setValue(email);

        $("[data-testid='login-password-input']").setValue(password);

        $("[data-testid='login-submit-btn']").click();
    }

    public String getErrorMessage() {
        SelenideElement error = $("[data-testid='login-error']");
        if (error.exists()) {
            return error.getText();
        } else {
            return "";
        }
    }

    public boolean isOnLoginPage() {
        boolean urlIsCorrect = WebDriverRunner.url().contains("/login");

        boolean elementIsVisible = $("[data-testid='login-page']").is(visible);

        return urlIsCorrect && elementIsVisible;
    }

}