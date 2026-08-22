package itk.student.task.manager.repository;

import itk.student.task.manager.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    long countByTaskId(UUID taskId);
}
