package itk.student.task.manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private long maxFileSizeBytes;
    private List<String> allowedContentTypes;
}
