package selenide;


import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;


public class BoardPage {

    public void waitForBoardLoaded() {
        $("[data-testid='board-page']").shouldBe(visible);
    }

    public String getUserName() {
        return $("[data-testid='user-name']").getText();
    }

    public void createTask(String title, String priority, String dueDate) {

        $("[data-testid='task-create-btn']").click();

        $("[data-testid='task-title-input']").setValue(title);

        $("[data-testid='task-priority-select']").selectOption(priority);

        $("[data-testid='task-due-input']").setValue(dueDate);

        $("[data-testid='task-save-btn']").click();
    }

    public void openTaskByTitle(String title) {
        $x("//button[contains(@data-testid, 'task-card')][.//h4[text()='" + title + "']]").click();
    }

    public SelenideElement getTaskCard(String title) {
        return $x("//button[contains(@data-testid, 'task-card')][.//h4[text()='" + title + "']]");
    }

    public SelenideElement getColumn(String status) {
        return $("[data-testid='kanban-column-" + status + "']");
    }

    public String getStatValue(String statTestId) {
        return $("[data-testid='" + statTestId + "'] strong").getText();
    }

    public void logout() {
        $("[data-testid = 'logout-btn']").click();
    }


}
