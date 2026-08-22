package itk.student.task.manager.dto.response;

import java.util.List;
import java.util.UUID;

public record BulkUpdateTasksResponse(
        int updated,
        List<UUID> failed
) {
}
