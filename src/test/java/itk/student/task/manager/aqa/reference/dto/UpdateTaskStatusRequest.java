package itk.student.task.manager.aqa.reference.dto;

public record UpdateTaskStatusRequest(
        String status,
        long version
) {
}
