package itk.student.task.manager.aqa.hw.ui.pages;

import com.codeborne.selenide.SelenideElement;

import java.util.UUID;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class BoardPage {

    private final SelenideElement boardPage = $("[data-testid='board-page']");
    private final SelenideElement username = $("[data-testid='user-name']");
    private final SelenideElement createTaskButton = $("[data-testid='task-create-btn']");
    private final SelenideElement kanbanBoard = $("[data-testid='kanban-board']");
    private final SelenideElement statsBar = $("[data-testid='stats-bar']");
    private final SelenideElement logoutBtn = $("[data-testid='logout-btn']");

    public BoardPage waitForBoardLoaded() {
        boardPage.shouldBe(visible);
        return this;
    }

    public String getUserName() {
        return username.getText();
    }

    public BoardPage createTask(String title, String priority, String dueDate) {
        createTaskButton.click();

        return new TaskModal().setTitle(title)
                .setPriority(priority)
                .setDueDate(dueDate)
                .save();
    }

    public TaskModal openTaskByTitle(String title) {
        getTaskCard(title).click();
        return new TaskModal();
    }

    public SelenideElement getTaskCard(String title) {
        return kanbanBoard.$$("h4[data-testid='task-title']").findBy(text(title));
    }

    public SelenideElement getColumn(String status) {
        return kanbanBoard.$("[data-testid='kanban-column-" + status + "']");
    }

    public SelenideElement getTaskFromColumn(String status) {
        return getColumn(status).$("[data-testid^='task-card-']");
    }

    public String getStatValue(String statTestId) {
        return statsBar.$("[data-testid='" + statTestId + "']").$("strong").getText();
    }

    public SelenideElement getStat(String statTestId) {
        return statsBar.$("[data-testid='" + statTestId + "']").$("strong");
    }

    public UUID getTaskIdByTitle(String title) {
        String testId = $$("[data-testid^='task-card-']")
                .findBy(text(title))
                .getAttribute("data-testid");
        return UUID.fromString(testId.substring("task-card-".length()));
    }

    public LoginPage logout() {
        logoutBtn.click();
        return new LoginPage();
    }
}
