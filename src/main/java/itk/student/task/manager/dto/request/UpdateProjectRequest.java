package itk.student.task.manager.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(max = 200) String name,
        @Size(max = 2000) String description,
        Boolean archived
) {
}
