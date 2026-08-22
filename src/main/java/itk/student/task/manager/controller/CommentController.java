package itk.student.task.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import itk.student.task.manager.dto.request.UpdateCommentRequest;
import itk.student.task.manager.dto.response.CommentResponse;
import itk.student.task.manager.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Операции над отдельными комментариями (update/delete/get by id).
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Комментарии")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    @Operation(summary = "Комментарий по ID")
    public CommentResponse getComment(@PathVariable UUID commentId) {
        return commentService.getComment(commentId);
    }

    @PatchMapping("/{commentId}")
    @Operation(summary = "Обновление комментария", description = "Только автор или ADMIN")
    public CommentResponse updateComment(@PathVariable UUID commentId,
                                         @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удаление комментария", description = "Только автор или ADMIN")
    public void deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
    }
}
