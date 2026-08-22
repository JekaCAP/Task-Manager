package itk.student.task.manager.dto.common;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorDetail> details,
        String timestamp,
        String path
) {
}
