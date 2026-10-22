package itk.student.task.manager.aqa.hw.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class TaskModal {

    private final SelenideElement root = $("[data-testid='task-modal']");;
    private final SelenideElement titleInput = root.$("[data-testid='task-title-input']");
    private final SelenideElement descriptionInput = root.$("[data-testid='task-description-input']");
    private final SelenideElement prioritySelect = root.$("[data-testid='task-priority-select']");
    private final SelenideElement statusSelect = root.$("[data-testid='task-status-select']");
    private final SelenideElement dateInput = root.$("[data-testid='task-due-input']");
    private final SelenideElement saveBtn = root.$("[data-testid='task-save-btn']");
    private final SelenideElement cancelBtn = root.$("[data-testid='task-cancel-btn']");
    private final SelenideElement commentInput = root.$("[data-testid='comment-input']");
    private final SelenideElement sendCommentBtn = root.$("[data-testid='comment-submit-btn']");
    private final ElementsCollection commentsList = root.$$("li[data-testid^='comment-']");
    private final SelenideElement error = root.$("[data-testid='task-form-error']");

    public TaskModal setTitle(String title) {
        titleInput.setValue(title);
        return this;
    }

    public TaskModal setDescription(String description) {
        descriptionInput.setValue(description);
        return this;
    }

    public TaskModal setPriority(String priority) {
        prioritySelect.selectOption(priority);
        return this;
    }

    public TaskModal setStatus(String status) {
        statusSelect.selectOption(status);
        return this;
    }

    public TaskModal setDueDate(String date) {
        dateInput.setValue(date);
        return this;
    }

    public BoardPage save() {
        saveBtn.click();
        return new BoardPage();
    }

    public BoardPage cancel() {
        cancelBtn.click();
        return new BoardPage();
    }

    public TaskModal addComment(String commentText) {
        commentInput.setValue(commentText);
        sendCommentBtn.click();
        return this;
    }

    public SelenideElement getCommentByText(String text) {
        return commentsList.findBy(text(text));
    }

    public String getErrorMessage() {
        return error.getText();
    }
}
