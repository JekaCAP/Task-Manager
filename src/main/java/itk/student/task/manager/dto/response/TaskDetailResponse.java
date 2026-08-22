package itk.student.task.manager.dto.response;

import java.util.List;

public record TaskDetailResponse(
        TaskResponse task,
        List<TagResponse> tags,
        long commentsCount,
        long attachmentsCount
) {
}
