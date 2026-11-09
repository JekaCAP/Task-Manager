package selenide;


import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;


public class TaskModal {

    private SelenideElement se;

    public TaskModal() {
        this.se = $("[data-testid='task-modal']");
    }

    public TaskModal(SelenideElement modalWindow) {
        this.se = modalWindow;
    }

    public TaskModal setTitle(String title) {
        se.$("[data-testid='task-title-input']").setValue(title);
        return this;
    }

    public TaskModal setDescription(String description) {
        se.$("[data-testid='task-description-input']").setValue(description);
        return this;
    }

    public TaskModal setPriority(String priority) {
        se.$("[data-testid='task-priority-select']").selectOptionByValue(priority);
        return this;
    }

    public TaskModal setStatus(String status) {
        se.$("[data-testid='task-status-select']").selectOptionByValue(status);
        return this;
    }

    public TaskModal setDueDate(String date) {
        se.$("[data-testid='task-due-input']").setValue(date);
        return this;
    }

    public BoardPage save() {
        se.$("[data-testid='task-save-btn']").click();
        return new BoardPage();
    }

    public BoardPage cancel() {
        se.$("[data-testid='task-cancel-btn']").click();
        return new BoardPage();
    }

    public BoardPage addComment(String commentText) {
        se.$("[data-testid='comment-input']").setValue(commentText);

        se.$("[data-testid='comment-submit-btn").click();
        return new BoardPage();
    }

    public SelenideElement getCommentByText(String text) {
        return se.$x(".//li[contains(@data-testid, 'comment-')][.//span[text()='" + text + "']]");
    }
}