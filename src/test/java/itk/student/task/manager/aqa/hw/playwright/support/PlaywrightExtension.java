package itk.student.task.manager.aqa.hw.playwright.support;

import com.microsoft.playwright.*;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;

public class PlaywrightExtension implements BeforeAllCallback, AfterAllCallback,
                                            AfterEachCallback, ParameterResolver
{

    static Playwright playwright;
    static Browser browser;
    static final ThreadLocal<BrowserContext> CONTEXT =
            new ThreadLocal<>();

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        BrowserContext ctx = CONTEXT.get();
        if (ctx != null) {
            ctx.close();
            CONTEXT.remove();
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setChannel("chrome")
                            .setHeadless(false)
            );
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == Page.class;
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setBaseURL("https://demo.itklabs.online")
        );
        CONTEXT.set(context);
        return context.newPage();
    }
}
