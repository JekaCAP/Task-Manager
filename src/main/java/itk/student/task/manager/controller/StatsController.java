package itk.student.task.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import itk.student.task.manager.dto.response.DashboardStatsResponse;
import itk.student.task.manager.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Агрегированная статистика для dashboard UI и API-практики.
 */
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Stats", description = "Статистика")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard stats", description = "Сводка по задачам, опционально projectId")
    public DashboardStatsResponse dashboard(@RequestParam(required = false) UUID projectId) {
        return statsService.dashboard(projectId);
    }
}
