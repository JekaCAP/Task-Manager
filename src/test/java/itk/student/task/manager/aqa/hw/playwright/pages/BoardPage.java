package itk.student.task.manager.aqa.hw.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class BoardPage {

    private final Page page;
    private final Locator username;
    private final Locator createTaskBtn;
    private final Locator kanbanBoard;
    private final Locator loadingScreen;

    public BoardPage(Page page) {
        this.page = page;
        username = page.getByTestId("user-name");
        createTaskBtn = page.getByTestId("task-create-btn");
        kanbanBoard = page.getByTestId("kanban-board");
        loadingScreen = page.getByTestId("loading-screen");
    }

    public Locator username() {
        return username;
    }

    public BoardPage waitForBoardLoaded() {
        loadingScreen.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));;
        return this;
    }

    public TaskModal openTaskByTitle(String title) {
        getTaskByTitle(title).first().click();
        return new TaskModal(page);
    }

    public Locator getSomeTaskTitleFromColumn(String status) {
        return getColumn(status).locator("[data-testid^='task-card-']").first();
    }


    public BoardPage createTask(String title, String priority, String dueDate) {
        createTaskBtn.click();

        return new TaskModal(page)
                .setTitle(title)
                .setPriority(priority)
                .setDueDate(dueDate)
                .save();
    }

    public Locator getTaskByTitle(String title) {
        return kanbanBoard.getByTestId("task-title")
                .filter(new Locator.FilterOptions().setHasText(title));
    }

    public String getTaskIdByTitle(String title) {
        String testId = kanbanBoard.locator("[data-testid^='task-card-']")
                .filter(new Locator.FilterOptions().setHasText(title))
                .first()
                .getAttribute("data-testid");
        return testId.substring("task-card-".length());
    }

    public Locator getColumn(String status) {
        return kanbanBoard.getByTestId("kanban-column-" + status);
    }
}
