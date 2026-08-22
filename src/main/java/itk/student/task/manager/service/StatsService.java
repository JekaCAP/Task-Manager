package itk.student.task.manager.service;

import itk.student.task.manager.dto.response.DashboardStatsResponse;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import itk.student.task.manager.repository.ProjectRepository;
import itk.student.task.manager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessService projectAccessService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse dashboard(UUID projectId) {
        if (projectId != null) {
            projectAccessService.requireReadAccess(projectId);
        }

        Map<String, Long> byStatus = Arrays.stream(TaskStatus.values())
                .collect(Collectors.toMap(Enum::name, status -> 0L, (a, b) -> a, LinkedHashMap::new));
        taskRepository.countGroupedByStatus(projectId).forEach(row -> {
            TaskStatus status = (TaskStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
        });

        Map<String, Long> byPriority = Arrays.stream(TaskPriority.values())
                .collect(Collectors.toMap(Enum::name, priority -> 0L, (a, b) -> a, LinkedHashMap::new));
        taskRepository.countGroupedByPriority(projectId).forEach(row -> {
            TaskPriority priority = (TaskPriority) row[0];
            Long count = (Long) row[1];
            byPriority.put(priority.name(), count);
        });

        return new DashboardStatsResponse(
                projectId == null ? projectRepository.count() : 1,
                taskRepository.countActive(projectId),
                byStatus,
                byPriority,
                taskRepository.countOverdue(projectId, Instant.now(), List.of(TaskStatus.DONE, TaskStatus.CANCELLED))
        );
    }
}
