package itk.student.task.manager.repository;

import itk.student.task.manager.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByTaskId(UUID taskId, Pageable pageable);

    long countByTaskId(UUID taskId);
}
