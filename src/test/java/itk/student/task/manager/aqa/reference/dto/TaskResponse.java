package itk.student.task.manager.aqa.reference.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        String status,
        String priority,
        UUID assigneeId,
        Long version
) {
}
