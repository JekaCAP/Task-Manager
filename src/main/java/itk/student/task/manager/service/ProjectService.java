package itk.student.task.manager.service;

import itk.student.task.manager.dto.common.PagedResponse;
import itk.student.task.manager.dto.request.AddProjectMemberRequest;
import itk.student.task.manager.dto.request.CreateProjectRequest;
import itk.student.task.manager.dto.request.UpdateProjectRequest;
import itk.student.task.manager.dto.response.ProjectDetailResponse;
import itk.student.task.manager.dto.response.ProjectMemberResponse;
import itk.student.task.manager.dto.response.ProjectResponse;
import itk.student.task.manager.entity.Project;
import itk.student.task.manager.entity.ProjectMember;
import itk.student.task.manager.entity.User;
import itk.student.task.manager.enums.ProjectRole;
import itk.student.task.manager.exception.ConflictException;
import itk.student.task.manager.exception.ForbiddenException;
import itk.student.task.manager.exception.ResourceNotFoundException;
import itk.student.task.manager.mapper.PageMapper;
import itk.student.task.manager.mapper.ProjectMapper;
import itk.student.task.manager.repository.ProjectMemberRepository;
import itk.student.task.manager.repository.ProjectRepository;
import itk.student.task.manager.repository.TaskRepository;
import itk.student.task.manager.repository.UserRepository;
import itk.student.task.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;
    private final PageMapper pageMapper;
    private final ProjectAccessService projectAccessService;

    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> listProjects(Boolean archived, Pageable pageable) {
        boolean archivedFilter = archived != null && archived;
        Page<Project> page = projectRepository.findByArchived(archivedFilter, pageable);
        return pageMapper.toPagedResponse(page, projectMapper::toResponse);
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        if (projectRepository.existsByProjectKeyIgnoreCase(request.key())) {
            throw new ConflictException("Project key already exists");
        }
        UUID ownerId = SecurityUtils.currentUserId();
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setProjectKey(request.key().toUpperCase());
        project.setArchived(false);
        project.setOwnerId(ownerId);
        projectRepository.save(project);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProjectId(project.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(ProjectRole.OWNER);
        projectMemberRepository.save(ownerMember);

        return projectMapper.toResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse getProject(UUID projectId) {
        projectAccessService.requireReadAccess(projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        List<ProjectMemberResponse> members = projectMemberRepository.findByProjectId(projectId).stream()
                .map(this::toMemberResponse)
                .toList();
        return new ProjectDetailResponse(
                projectMapper.toResponse(project),
                members,
                taskRepository.countByProjectIdAndDeletedAtIsNull(projectId)
        );
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        projectAccessService.requireAdminAccess(projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (request.name() != null) {
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.archived() != null) {
            project.setArchived(request.archived());
        }
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        projectAccessService.requireAdminAccess(projectId);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        projectRepository.deleteById(projectId);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(UUID projectId) {
        projectAccessService.requireReadAccess(projectId);
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID projectId, AddProjectMemberRequest request) {
        projectAccessService.requireAdminAccess(projectId);
        if (request.role() == ProjectRole.OWNER) {
            throw new ForbiddenException("Cannot assign OWNER role via API");
        }
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new ConflictException("User is already a project member");
        }
        User user = userRepository.findByIdAndDeletedAtIsNull(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.userId());
        member.setRole(request.role());
        projectMemberRepository.save(member);
        return projectMapper.toMemberResponse(member, user);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        projectAccessService.requireAdminAccess(projectId);
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found"));
        if (member.getRole() == ProjectRole.OWNER) {
            throw new ForbiddenException("Cannot remove project owner");
        }
        projectMemberRepository.delete(member);
    }

    private ProjectMemberResponse toMemberResponse(ProjectMember member) {
        User user = userRepository.findByIdAndDeletedAtIsNull(member.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return projectMapper.toMemberResponse(member, user);
    }
}
