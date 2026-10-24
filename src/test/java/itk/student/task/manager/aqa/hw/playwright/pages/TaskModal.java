package itk.student.task.manager.aqa.hw.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class TaskModal {

    private final Page page;
    private final Locator root;
    private final Locator titleInput;
    private final Locator descriptionInput;
    private final Locator prioritySelect;
    private final Locator statusSelect;
    private final Locator dateInput;
    private final Locator saveBtn;
    private final Locator cancelBtn;
    private final Locator commentInput;
    private final Locator sendCommentBtn;
    private final Locator commentsList;
    private final Locator error;

    public TaskModal(Page page) {
        this.page = page;
        root = page.getByTestId("task-modal");
        titleInput = root.getByTestId("task-title-input");
        descriptionInput = root.getByTestId("task-description-input");
        prioritySelect = root.getByTestId("task-priority-select");
        statusSelect = root.getByTestId("task-status-select");
        dateInput = root.getByTestId("task-due-input");
        saveBtn = root.getByTestId("task-save-btn");
        cancelBtn = root.getByTestId("task-cancel-btn");
        commentInput = root.getByTestId("comment-input");
        sendCommentBtn = root.getByTestId("comment-submit-btn");
        commentsList = root.locator("li[data-testid^='comment-']");
        error = root.getByTestId("task-form-error");
    }

    public TaskModal setTitle(String title) {
        titleInput.fill(title);
        return this;
    }

    public TaskModal setDescription(String description) {
        descriptionInput.fill(description);
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
        dateInput.fill(date);
        return this;
    }

    public BoardPage save() {
        saveBtn.click();
        return new BoardPage(page);
    }

    public BoardPage cancel() {
        cancelBtn.click();
        return new BoardPage(page);
    }

    public TaskModal addComment(String text) {
        commentInput.fill(text);
        sendCommentBtn.click();
        return this;
    }

    public Locator getCommentByText(String text) {
        return commentsList.filter(new Locator.FilterOptions().setHasText(text));
    }

    public Locator error() {
        return error;
    }
}
