package hw.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateTaskRequest {

    private String projectId;

    private String title;

    private String description;

    private String priority;

    public CreateTaskRequest(String projectId, String title, String description, String priority) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

}
