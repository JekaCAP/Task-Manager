package itk.student.task.manager.service;

import itk.student.task.manager.dto.common.PagedResponse;
import itk.student.task.manager.dto.request.CreateCommentRequest;
import itk.student.task.manager.dto.request.UpdateCommentRequest;
import itk.student.task.manager.dto.response.CommentResponse;
import itk.student.task.manager.entity.Comment;
import itk.student.task.manager.entity.Task;
import itk.student.task.manager.entity.User;
import itk.student.task.manager.enums.ActivityAction;
import itk.student.task.manager.exception.ForbiddenException;
import itk.student.task.manager.exception.ResourceNotFoundException;
import itk.student.task.manager.mapper.CommentMapper;
import itk.student.task.manager.mapper.PageMapper;
import itk.student.task.manager.repository.CommentRepository;
import itk.student.task.manager.repository.TaskRepository;
import itk.student.task.manager.repository.UserRepository;
import itk.student.task.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final PageMapper pageMapper;
    private final ProjectAccessService projectAccessService;
    private final ActivityLogService activityLogService;

    @Transactional
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireWriteAccess(task.getProjectId());

        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setAuthorId(SecurityUtils.currentUserId());
        comment.setBody(request.body());
        commentRepository.save(comment);

        activityLogService.log(taskId, ActivityAction.COMMENT_ADDED, "Comment added");
        return toResponse(comment);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> listComments(UUID taskId, Pageable pageable) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireReadAccess(task.getProjectId());
        Page<Comment> page = commentRepository.findByTaskId(taskId, pageable);
        return pageMapper.toPagedResponse(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        Task task = findActiveTask(comment.getTaskId());
        projectAccessService.requireReadAccess(task.getProjectId());
        return toResponse(comment);
    }

    @Transactional
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getAuthorId().equals(SecurityUtils.currentUserId()) && !SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Only comment author can update it");
        }
        if (!comment.getVersion().equals(request.version())) {
            throw new itk.student.task.manager.exception.ConflictException("Resource was modified by another user");
        }
        comment.setBody(request.body());
        try {
            return toResponse(commentRepository.save(comment));
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new itk.student.task.manager.exception.ConflictException("Resource was modified by another user");
        }
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getAuthorId().equals(SecurityUtils.currentUserId()) && !SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Only comment author can delete it");
        }
        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        User author = userRepository.findById(comment.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        return commentMapper.toResponse(comment, author);
    }

    private Task findActiveTask(UUID taskId) {
        return taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }
}
