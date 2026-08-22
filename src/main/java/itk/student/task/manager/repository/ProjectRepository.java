package itk.student.task.manager.repository;

import itk.student.task.manager.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByProjectKeyIgnoreCase(String projectKey);

    Page<Project> findByArchived(boolean archived, Pageable pageable);

    Optional<Project> findByIdAndArchivedFalse(UUID id);

    long countByOwnerId(UUID ownerId);
}
