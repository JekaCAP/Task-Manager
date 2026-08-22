package itk.student.task.manager.dto.response;

import java.util.List;

public record ProjectDetailResponse(
        ProjectResponse project,
        List<ProjectMemberResponse> members,
        long tasksCount
) {
}
