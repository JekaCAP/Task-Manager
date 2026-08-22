package itk.student.task.manager.dto.response;

import java.util.Map;

public record DashboardStatsResponse(
        long totalProjects,
        long totalTasks,
        Map<String, Long> tasksByStatus,
        Map<String, Long> tasksByPriority,
        long overdueTasks
) {
}
