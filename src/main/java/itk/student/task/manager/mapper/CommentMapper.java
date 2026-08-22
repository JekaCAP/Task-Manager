package itk.student.task.manager.mapper;

import itk.student.task.manager.dto.response.CommentResponse;
import itk.student.task.manager.entity.Comment;
import itk.student.task.manager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment, User author) {
        String authorName = author.getFirstName() + " " + author.getLastName();
        return new CommentResponse(
                comment.getId(),
                comment.getTaskId(),
                comment.getAuthorId(),
                authorName,
                comment.getBody(),
                comment.getVersion(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
