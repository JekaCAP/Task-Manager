package itk.student.task.manager.dto.response;

import itk.student.task.manager.enums.GlobalRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        GlobalRole globalRole,
        String avatarUrl
) {
}
