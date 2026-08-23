package itk.student.task.manager.aqa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

public final class ApiTestConfig {

    private static final Properties PROPS = load();

    private ApiTestConfig() {
    }

    public static String baseUrl() {
        String fromEnv = System.getenv("TASK_MANAGER_BASE_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return stripTrailingSlash(fromEnv.trim());
        }
        String fromSys = System.getProperty("taskManager.baseUrl");
        if (fromSys != null && !fromSys.isBlank()) {
            return stripTrailingSlash(fromSys.trim());
        }
        return stripTrailingSlash(PROPS.getProperty("baseUrl"));
    }

    public static String userEmail() {
        return PROPS.getProperty("user.email");
    }

    public static String userPassword() {
        return PROPS.getProperty("user.password");
    }

    public static String viewerEmail() {
        return PROPS.getProperty("viewer.email", "viewer@demo.com");
    }

    public static String viewerPassword() {
        return PROPS.getProperty("viewer.password", "Demo123!");
    }

    public static String demoProjectKey() {
        return PROPS.getProperty("demo.project.key", "DEMO");
    }

    private static Properties load() {
        Properties props = new Properties();
        loadResource(props, "aqa.properties");
        loadResource(props, "aqa-local.properties");
        return props;
    }

    private static void loadResource(Properties props, String name) {
        try (InputStream in = ApiTestConfig.class.getClassLoader().getResourceAsStream(name)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + name, e);
        }
    }

    private static String stripTrailingSlash(String url) {
        Objects.requireNonNull(url, "baseUrl");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
