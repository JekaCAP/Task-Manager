package itk.student.task.manager.repository;

import itk.student.task.manager.entity.Task;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    Page<Task> findByProjectIdAndDeletedAtIsNull(UUID projectId, Pageable pageable);

    Page<Task> findByAssigneeIdAndDeletedAtIsNull(UUID assigneeId, Pageable pageable);

    Page<Task> findByAssigneeIdAndStatusAndDeletedAtIsNull(UUID assigneeId, TaskStatus status, Pageable pageable);

    long countByAssigneeIdAndDeletedAtIsNull(UUID assigneeId);

    long countByProjectIdAndDeletedAtIsNull(UUID projectId);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.deletedAt IS NULL
              AND (:projectId IS NULL OR t.projectId = :projectId)
            """)
    long countActive(@Param("projectId") UUID projectId);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.deletedAt IS NULL
              AND t.dueDate < :now
              AND t.status NOT IN :closedStatuses
              AND (:projectId IS NULL OR t.projectId = :projectId)
            """)
    long countOverdue(@Param("projectId") UUID projectId,
                      @Param("now") Instant now,
                      @Param("closedStatuses") java.util.Collection<TaskStatus> closedStatuses);

    @Query("""
            SELECT t.status, COUNT(t) FROM Task t
            WHERE t.deletedAt IS NULL
              AND (:projectId IS NULL OR t.projectId = :projectId)
            GROUP BY t.status
            """)
    java.util.List<Object[]> countGroupedByStatus(@Param("projectId") UUID projectId);

    @Query("""
            SELECT t.priority, COUNT(t) FROM Task t
            WHERE t.deletedAt IS NULL
              AND (:projectId IS NULL OR t.projectId = :projectId)
            GROUP BY t.priority
            """)
    java.util.List<Object[]> countGroupedByPriority(@Param("projectId") UUID projectId);
}
