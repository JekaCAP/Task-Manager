package itk.student.task.manager.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
