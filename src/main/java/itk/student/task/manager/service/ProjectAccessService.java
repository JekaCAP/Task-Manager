package itk.student.task.manager.service;

import itk.student.task.manager.entity.ProjectMember;
import itk.student.task.manager.enums.ProjectRole;
import itk.student.task.manager.exception.ForbiddenException;
import itk.student.task.manager.exception.ResourceNotFoundException;
import itk.student.task.manager.repository.ProjectMemberRepository;
import itk.student.task.manager.repository.ProjectRepository;
import itk.student.task.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private static final List<ProjectRole> WRITE_ROLES = List.of(
            ProjectRole.OWNER, ProjectRole.ADMIN, ProjectRole.MEMBER);
    private static final List<ProjectRole> ADMIN_ROLES = List.of(
            ProjectRole.OWNER, ProjectRole.ADMIN);

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public void requireProjectExists(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    public ProjectMember requireMembership(UUID projectId, UUID userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));
    }

    public void requireReadAccess(UUID projectId) {
        UUID userId = SecurityUtils.currentUserId();
        if (SecurityUtils.isAdmin()) {
            requireProjectExists(projectId);
            return;
        }
        requireMembership(projectId, userId);
    }

    public ProjectMember requireWriteAccess(UUID projectId) {
        UUID userId = SecurityUtils.currentUserId();
        if (SecurityUtils.isAdmin()) {
            requireProjectExists(projectId);
            return null;
        }
        ProjectMember member = requireMembership(projectId, userId);
        if (!WRITE_ROLES.contains(member.getRole())) {
            throw new ForbiddenException("Write access denied for this project");
        }
        return member;
    }

    public void requireAdminAccess(UUID projectId) {
        UUID userId = SecurityUtils.currentUserId();
        if (SecurityUtils.isAdmin()) {
            requireProjectExists(projectId);
            return;
        }
        ProjectMember member = requireMembership(projectId, userId);
        if (!ADMIN_ROLES.contains(member.getRole())) {
            throw new ForbiddenException("Admin access denied for this project");
        }
    }

    public boolean canWrite(ProjectRole role) {
        return WRITE_ROLES.contains(role);
    }

    public boolean isViewer(ProjectRole role) {
        return role == ProjectRole.VIEWER;
    }
}
