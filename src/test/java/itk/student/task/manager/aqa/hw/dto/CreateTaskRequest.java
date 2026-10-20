package itk.student.task.manager.aqa.hw.dto;

import itk.student.task.manager.enums.TaskPriority;

import java.util.UUID;

public record CreateTaskRequest(
        UUID projectId,
        String title,
        String description,
        TaskPriority priority
) {
}
