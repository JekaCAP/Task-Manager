package itk.student.task.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import itk.student.task.manager.dto.common.PagedResponse;
import itk.student.task.manager.dto.request.AddProjectMemberRequest;
import itk.student.task.manager.dto.request.CreateProjectRequest;
import itk.student.task.manager.dto.request.UpdateProjectRequest;
import itk.student.task.manager.dto.response.ProjectDetailResponse;
import itk.student.task.manager.dto.response.ProjectMemberResponse;
import itk.student.task.manager.dto.response.ProjectResponse;
import itk.student.task.manager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * CRUD проектов и управление участниками (RBAC на уровне проекта).
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Проекты и участники")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Список проектов", description = "Фильтр archived, pagination")
    public PagedResponse<ProjectResponse> listProjects(
            @RequestParam(required = false, defaultValue = "false") Boolean archived,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return projectService.listProjects(archived, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создание проекта", description = "Создатель становится OWNER")
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request) {
        return projectService.createProject(request);
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Детали проекта", description = "Проект, участники, количество задач")
    public ProjectDetailResponse getProject(@PathVariable UUID projectId) {
        return projectService.getProject(projectId);
    }

    @PatchMapping("/{projectId}")
    @Operation(summary = "Обновление проекта", description = "OWNER/ADMIN проекта")
    public ProjectResponse updateProject(@PathVariable UUID projectId,
                                         @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удаление проекта", description = "OWNER/ADMIN проекта")
    public void deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId);
    }

    @GetMapping("/{projectId}/members")
    @Operation(summary = "Участники проекта")
    public List<ProjectMemberResponse> listMembers(@PathVariable UUID projectId) {
        return projectService.listMembers(projectId);
    }

    @PostMapping("/{projectId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить участника", description = "OWNER/ADMIN. Роль MEMBER или ADMIN")
    public ProjectMemberResponse addMember(@PathVariable UUID projectId,
                                           @Valid @RequestBody AddProjectMemberRequest request) {
        return projectService.addMember(projectId, request);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить участника", description = "Нельзя удалить OWNER")
    public void removeMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        projectService.removeMember(projectId, userId);
    }
}
