package itk.student.task.manager.dto.response;

import java.time.Instant;

public record UserProfileResponse(
        UserResponse user,
        long assignedTasksCount,
        long ownedProjectsCount,
        Instant memberSince
) {
}
