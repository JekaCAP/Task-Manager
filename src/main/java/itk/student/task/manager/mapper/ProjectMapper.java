package itk.student.task.manager.mapper;

import itk.student.task.manager.dto.response.ProjectMemberResponse;
import itk.student.task.manager.dto.response.ProjectResponse;
import itk.student.task.manager.entity.Project;
import itk.student.task.manager.entity.ProjectMember;
import itk.student.task.manager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectKey(),
                project.isArchived(),
                project.getOwnerId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public ProjectMemberResponse toMemberResponse(ProjectMember member, User user) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
