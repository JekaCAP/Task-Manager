package itk.student.task.manager.aqa.hw.playwright.support;

import com.microsoft.playwright.*;
import itk.student.task.manager.aqa.config.ApiTestConfig;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlaywrightExtension implements BeforeAllCallback, AfterAllCallback,
                                            AfterEachCallback, ParameterResolver
{

    static Playwright playwright;
    static Browser browser;
    static final ThreadLocal<BrowserContext> CONTEXT =
            new ThreadLocal<>();
    static final ThreadLocal<List<String>> PAGE_ERRORS =
            ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    static final ThreadLocal<APIRequestContext> API = new ThreadLocal<>();

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        browser = null;
        playwright = null;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        BrowserContext ctx = CONTEXT.get();
        if (ctx != null) {
            String name = context.getRequiredTestClass().getSimpleName() + "_"
                    + context.getRequiredTestMethod().getName();

            ctx.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get("traces", name + ".zip")));
            ctx.close();
            CONTEXT.remove();
        }

        List<String> errors = List.copyOf(PAGE_ERRORS.get());
        PAGE_ERRORS.remove();

        if (!errors.isEmpty()) {
            System.err.println("JS-ошибки на странице: " + errors);
        }

        APIRequestContext api = API.get();
        if (api != null) {
            api.dispose();
            API.remove();
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
            );
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return type == Page.class || type == APIRequestContext.class;
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == APIRequestContext.class) {
            APIRequestContext api = playwright.request().newContext(
                    new APIRequest.NewContextOptions()
                            .setBaseURL(ApiTestConfig.baseUrl()));
            API.set(api);
            return api;
        }


        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setBaseURL(ApiTestConfig.baseUrl())
        );

        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        CONTEXT.set(context);

        Page page = context.newPage();
        List<String> errors = PAGE_ERRORS.get();
        errors.clear();
        page.onPageError(errors::add);

        return page;
    }
}
