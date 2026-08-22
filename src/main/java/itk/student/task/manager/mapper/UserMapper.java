package itk.student.task.manager.mapper;

import itk.student.task.manager.dto.response.UserResponse;
import itk.student.task.manager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getGlobalRole(),
                user.getAvatarUrl()
        );
    }
}
