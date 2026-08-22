package itk.student.task.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import itk.student.task.manager.dto.response.TagResponse;
import itk.student.task.manager.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Теги проекта и привязка тегов к задачам.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Теги")
public class TagController {

    private final TagService tagService;

    @GetMapping("/api/v1/tags")
    @Operation(summary = "Список тегов", description = "Опциональный фильтр projectId")
    public List<TagResponse> listTags(@RequestParam(required = false) UUID projectId) {
        return tagService.listTags(projectId);
    }

    @PostMapping("/api/v1/tasks/{taskId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Привязать тег к задаче", description = "409 если тег уже привязан")
    public void attachTag(@PathVariable UUID taskId, @PathVariable UUID tagId) {
        tagService.attachTag(taskId, tagId);
    }
}
