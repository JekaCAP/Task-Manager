package itk.student.task.manager.aqa.hw.dto;

import itk.student.task.manager.enums.TaskStatus;

public record UpdateTaskStatusRequest(
        TaskStatus status,
        Integer version
) {
}
